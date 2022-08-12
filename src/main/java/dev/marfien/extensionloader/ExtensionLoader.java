package dev.marfien.extensionloader;

import com.google.common.collect.Lists;
import dev.marfien.extensionloader.description.ExtensionDescription;
import dev.marfien.extensionloader.description.LibraryResolver;
import net.minestom.dependencies.ResolvedDependency;
import org.jetbrains.annotations.NotNull;

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
    this(parent, List.of("entrypoint.extension"));
  }

  public ExtensionLoader(final @NotNull List<String> descriptionFileNames) {
    this(ExtensionLoader.class.getClassLoader(), descriptionFileNames);
  }

  public @NotNull Optional<DiscoveredExtension> getDiscoveredExtension(final @NotNull String id) {
    return Optional.ofNullable(this.extensions.get(id));
  }

  public @NotNull List<String> getDescriptionFileNames() {
    return List.copyOf(this.descriptionFileNames);
  }

  // termination

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

  // loading

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

  private void prepareExtensions(final @NotNull Collection<DiscoveredExtension> extensions, final @NotNull Path path, final @NotNull Path libsPath) {
    for (final var extension : extensions) {
      this.prepareClassLoader(extension, path, libsPath);
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

  // <editor-folder desc="Prepare ClassLoader" defaultstate="collapsed">

  private synchronized void prepareClassLoader(final @NotNull DiscoveredExtension extension, final @NotNull Path path, final @NotNull Path libsPath) {
    final var classLoader = extension.getClassLoader();
    if (classLoader.getState() == ExtensionClassLoader.State.POPULATED) return;
    if (classLoader.getState() == ExtensionClassLoader.State.POPULATING) throw new AssertionError();

    classLoader.setState(ExtensionClassLoader.State.POPULATING);

    // add libraries
    // TODO optimization
    this.loadLibraries(extension.getDescription(), libsPath).forEach(classLoader::addLibrary);

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

    classLoader.setState(ExtensionClassLoader.State.POPULATED);
  }

  private @NotNull Collection<URL> loadLibraries(final @NotNull ExtensionDescription description, final @NotNull Path libsPath) {
    final var resolver = new LibraryResolver();
    resolver.addRepositories(description.libraries().repositories());

    return description.libraries().artifacts().stream().flatMap(artifact -> resolver.resolve(artifact, libsPath).stream()).map(ResolvedDependency::getContentsLocation).toList();
  }

  // </editor-folder>
  // <editor-folder desc="Create Extension Object" defaultstate="collapsed">

  private void createExtension(final @NotNull DiscoveredExtension discoveredExtension) throws IOException {
    final var extension = this.initExtension(discoveredExtension.getDescription(), discoveredExtension.getClassLoader());
    discoveredExtension.setExtension(extension);
    extension.setParent(discoveredExtension);
    discoveredExtension.setState(DiscoveredExtension.State.INSTANCED);
  }

  private Extension initExtension(final ExtensionDescription description, final ClassLoader classLoader) throws IOException {
    try {
      final Class<? extends Extension> entrypoint = description.entrypoint();
      return entrypoint.getConstructor().newInstance();
    } catch (final Exception e) {
      throw new IOException("An error occurred during instantiation of %s: %s".formatted(description.id(), e.getMessage()), e);
    }
  }

  // </editor-folder>
  // <editor-folder desc="Discover new Extensions" defaultstate="collapsed">

  private Collection<DiscoveredExtension> discoverExtensions(final @NotNull Path path) throws IOException {
    try (final var stream = Files.newDirectoryStream(path, "*.{jar,zip}")) {
      final List<DiscoveredExtension> list = Lists.newArrayList();

      for (final var extensionFile : stream) {
        list.add(this.discoverExtension(extensionFile));
      }

      return list;
    }
  }

  private synchronized DiscoveredExtension discoverExtension(final @NotNull Path path) throws IOException {
    if (!Files.isRegularFile(path)) throw new IOException("Not a regular file: %s".formatted(path));

    final var classLoader = new ExtensionClassLoader(this.parentClassLoader, new URL[] { path.toUri().toURL() });
    final var entryPoint = this.readEntryPoint(path).orElseThrow(() -> new IOException("Cannot read entrypoint of %s".formatted(path)));

    try {
      @SuppressWarnings("unchecked") final var extensionClass = (Class<? extends Extension>) classLoader.loadClass(entryPoint);
      final var description = ExtensionDescription.createFromClass(extensionClass);

      // if an extension is already presented it should not be loaded
      if (this.extensions.containsKey(description.id()))
        throw new IllegalStateException("An extension with id '%s' already exists.".formatted(description.id()));

      final var ext = new DiscoveredExtension(classLoader, description, path, path.getParent().resolve(description.id()));

      this.extensions.put(description.id(), ext);
      return ext;
    } catch (final ClassNotFoundException e) {
      throw new IOException("Cannot find entrypoint of %s".formatted(path), e);
    }
  }

  // </editor-folder>
  // <editor-folder desc="Load EntryPoint" defaultstate="collapsed">

  private Optional<String> readEntryPoint(final @NotNull Path path) throws IOException {
    if (this.descriptionFileNames.isEmpty()) return Optional.empty();

    try (final var zipFile = new ZipFile(path.toFile())) {
      for (final var descriptionFileName : this.descriptionFileNames) {
        final var zipEntry = zipFile.getEntry(descriptionFileName);
        if (zipEntry == null) continue;

        try(final var reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)))) {
          return Optional.ofNullable(reader.readLine());
        }
      }
    }

    return Optional.empty();
  }

  // </editor-folder>

  // Utility methods

  private static @NotNull Path checkDirectory(final @NotNull Path path) throws IOException {
    if (Files.isDirectory(path)) return path;
    return Files.createDirectories(path);
  }

}
