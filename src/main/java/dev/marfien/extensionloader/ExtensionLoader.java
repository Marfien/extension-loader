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
  private final ClassLoader parentClassLoader;

  public ExtensionLoader(final @NotNull ClassLoader parentClassLoader, final @NotNull List<String> descriptionFileNames) {
    this.descriptionFileNames = descriptionFileNames;
    this.parentClassLoader = parentClassLoader;
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

  public @NotNull Optional<DiscoveredExtension> getDiscoveredExtension(final @NotNull String id) {
    return Optional.ofNullable(this.extensions.get(id));
  }

  public void loadExtensions(@NotNull Path path, @NotNull Path libsPath) throws IOException {
    path = checkDirectory(path);
    libsPath = checkDirectory(libsPath);

    final var discoveredExtensions = this.discoverExtensions(path);

    if (discoveredExtensions.isEmpty()) return;

    final var sorted = TopologicalSorting.sort(discoveredExtensions);

    this.prepareExtensions(sorted, path, libsPath);
    this.createExtensions(sorted);
    sorted.forEach(extension -> extension.getExtension().preInitialize());
    this.initializeExtensions(sorted);
    sorted.forEach(extension -> extension.getExtension().postInitialized());
  }

  public void terminate() {
    final var extensions = this.extensions.values();

    extensions.forEach(extension -> extension.getExtension().preTerminate());
    extensions.forEach(this::terminateExtension);
    extensions.forEach(extension -> extension.getExtension().postTerminate());
  }

  private void terminateExtension(final @NotNull DiscoveredExtension extension) {
    extension.getExtension().terminate();
    extension.setState(DiscoveredExtension.State.TERMINATED);
  }

  private static @NotNull Path checkDirectory(final @NotNull Path path) throws IOException {
    if (Files.isDirectory(path)) return path;
    return Files.createDirectories(path);
  }

  private void prepareExtensions(final @NotNull Collection<DiscoveredExtension> extensions, final @NotNull Path path, final @NotNull Path libsPath) {
    for (final var extension : extensions) {
      this.prepareClassLoader(extension, path, libsPath);
      extension.setState(DiscoveredExtension.State.PREPARED);
    }
  }

  private void createExtensions(final @NotNull Collection<DiscoveredExtension> extensions) throws IOException {
    for (final var extension : extensions) {
      this.createExtension(extension);
    }
  }

  private void initializeExtensions(final @NotNull Collection<DiscoveredExtension> extensions) {
    for (final var extension : extensions) {
      extension.getExtension().initialize();
      extension.setState(DiscoveredExtension.State.INITIALIZED);
    }
  }

  private void createExtension(final @NotNull DiscoveredExtension discoveredExtension) throws IOException {
    final var extension = this.initExtension(discoveredExtension.getDescription(), discoveredExtension.getClassLoader());
    discoveredExtension.setExtension(extension);
    extension.setParent(discoveredExtension);
    discoveredExtension.setState(DiscoveredExtension.State.INSTANCED);
  }

  private synchronized void prepareClassLoader(final @NotNull DiscoveredExtension extension, final @NotNull Path path, final @NotNull Path libsPath) {
    if (extension.getClassLoader() != null) return;

    final var libs = this.loadLibraries(extension.getDescription(), libsPath);
    extension.createClassLoader(this.parentClassLoader, libs);
    final var classLoader = extension.getClassLoader();

    final var description = extension.getDescription();
    for (final var dependency : description.dependencies()) {
      final var optionalDependencyExtension = this.getDiscoveredExtension(dependency.id());

      if (optionalDependencyExtension.isEmpty()) {
        if (dependency.required()) throw new AssertionError("Dependency %s is required by %s, but not found".formatted(dependency.id(), description.id()));
        continue;
      }

      final var dependencyExtension = optionalDependencyExtension.get();
      this.prepareClassLoader(dependencyExtension, path, libsPath);
      final var childClassLoader = dependencyExtension.getClassLoader();
      assert childClassLoader != null;

      classLoader.addChildClassLoader(childClassLoader);
    }
  }

  private @NotNull Collection<URL> loadLibraries(final @NotNull ExtensionDescription description, final @NotNull Path libsPath) {
    final var resolver = new LibraryResolver();
    resolver.addRepositories(description.libraries().repositories());

    return description.libraries().artifacts().stream().flatMap(artifact -> resolver.resolve(artifact, libsPath).stream()).map(ResolvedDependency::getContentsLocation).toList();
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

  public synchronized DiscoveredExtension discoverExtension(final @NotNull Path path) throws IOException {
    if (!Files.isRegularFile(path)) throw new IOException("Not a regular file: %s".formatted(path));

    try (final var file = new ZipFile(path.toFile())) {
      final var description = this.readDescription(file);
      // if an extension is already presented it should not be loaded
      if (this.extensions.containsKey(description.id())) throw new IllegalStateException("An extension with id '%s' already exists.".formatted(description.id()));

      final var ext = new DiscoveredExtension(description, path, path.getParent().resolve(description.id()));

      this.extensions.put(description.id(), ext);
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
