package dev.marfien.extensionloader;

import com.google.common.collect.Lists;
import dev.marfien.extensionloader.description.ExtensionDescription;
import dev.marfien.extensionloader.description.LibraryResolver;
import net.minestom.dependencies.ResolvedDependency;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipFile;

public class ExtensionLoader {

  private final Map<String, DiscoveredExtension> extensions = new HashMap<>();

  private final List<String> descriptionFileNames;
  private final LibraryClassLoader parentClassLoader;

  public ExtensionLoader(final @NotNull ClassLoader parentClassLoader, final @NotNull List<String> descriptionFileNames) {
    this.descriptionFileNames = descriptionFileNames;
    this.parentClassLoader = new LibraryClassLoader(parentClassLoader);
  }

  public ExtensionLoader() {
    this(ExtensionLoader.class.getClassLoader());
  }

  public ExtensionLoader(final @NotNull ClassLoader parent) {
    this(parent, List.of("extension.json", "extension.yaml"));
  }

  public ExtensionLoader(final @NotNull List<String> descriptionFileNames) {
    this(ExtensionLoader.class.getClassLoader(), descriptionFileNames);
  }

  public void loadExtensions(final @NotNull Path path) throws IOException {
    final var discoveredExt = this.discoverExtensions(path);

  }

  private void loadExtension(final @NotNull DiscoveredExtension extension, final @NotNull Path path) {
    this.loadLibraries(extension.getDescription(), path.resolve(".libs"));
  }

  private void loadLibraries(final @NotNull ExtensionDescription description, final @NotNull Path libsPath) {
    final var resolver = new LibraryResolver();
    resolver.addRepositories(description.libraries().repositories());

    for (final var artifact : description.libraries().artifacts()) {
      final var dependencies = resolver.resolve(artifact, libsPath);
      dependencies.stream().map(ResolvedDependency::getContentsLocation).forEach(this.parentClassLoader::addURL);
    }
  }

  public Collection<DiscoveredExtension> discoverExtensions(final @NotNull Path path) throws IOException {
    try (final var stream = Files.newDirectoryStream(path, "*.{jar,zip}")) {
      final List<DiscoveredExtension> list = Lists.newArrayList();

      for (final var extensionFile : stream) {
        list.add(this.discoverExtension(path));
      }

      return list;
    }
  }

  public DiscoveredExtension discoverExtension(final @NotNull Path path) throws IOException {
    if (!Files.isRegularFile(path)) throw new IOException("Not a regular file: %s".formatted(path));

    try (final var file = new ZipFile(path.toFile())) {
      final var description = this.readDescription(file);
      if (this.extensions.containsKey(description.id())) throw new IllegalStateException("An extension with id '%s' already exists.".formatted(description.id()));
      final var classLoader = new ExtensionClassLoader(description, this.parentClassLoader, new URL[]{ path.toUri().toURL() });
      final var extension = this.initExtension(description, classLoader);
      final var ext = new DiscoveredExtension(
        classLoader,
        extension,
        description,
        path,
        path.resolve(description.id())
      );
      extension.setParent(ext);

      return ext;
    }
  }

  private Extension initExtension(final ExtensionDescription description, final ClassLoader classLoader) throws IOException {
    try {
      @SuppressWarnings("unchecked")
      final Class<? extends Extension> entrypoint = (Class<? extends Extension>) classLoader.loadClass(description.entrypoint());
      return entrypoint.getConstructor().newInstance();
    } catch (final Exception e) {
      throw new IOException("Error during instantiating of %s: %s".formatted(description.id(), e.getMessage()), e);
    }
  }

  private ExtensionDescription readDescription(final @NotNull ZipFile file) throws IOException {
    final var descriptionNode = this.loadDescription(file).orElseThrow(() -> new IOException("No extension description file found!"));

    return descriptionNode.get(ExtensionDescription.class);
  }

  private Optional<ConfigurationNode> loadDescription(final ZipFile file) throws IOException {
    AbstractConfigurationLoader<? extends ScopedConfigurationNode<?>> loader = null;
    for (int i = 0; i < this.descriptionFileNames.size(); i++) {
      final var fileName = this.descriptionFileNames.get(i);
      final var entry = file.getEntry(fileName);

      if (entry == null) continue;
      if (entry.isDirectory()) continue;

      if (fileName.endsWith(".json")) {
        loader = GsonConfigurationLoader.builder()
          .source(() -> new BufferedReader(new InputStreamReader(file.getInputStream(entry))))
          .build();
        break;
      }

      if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
        loader = YamlConfigurationLoader.builder()
          .source(() -> new BufferedReader(new InputStreamReader(file.getInputStream(entry))))
          .build();
        break;
      }

      throw new IOException("Unsupported file ending: %s".formatted(fileName.substring(fileName.lastIndexOf('.'))));
    }

    return loader == null ? Optional.empty() : Optional.of(loader.load());
  }

}
