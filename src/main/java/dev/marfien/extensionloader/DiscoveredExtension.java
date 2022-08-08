package dev.marfien.extensionloader;

import dev.marfien.extensionloader.description.ExtensionDescription;

import java.nio.file.Path;

class DiscoveredExtension {

  private final ClassLoader classLoader;
  private final Extension extension;

  private final ExtensionDescription description;
  private final Path file;
  private final Path dataDirectory;

  DiscoveredExtension(ClassLoader classLoader, Extension extension, ExtensionDescription description, Path file, Path dataDirectory) {
    this.classLoader = classLoader;
    this.extension = extension;
    this.description = description;
    this.file = file;
    this.dataDirectory = dataDirectory;
  }

  public ClassLoader getClassLoader() {
    return this.classLoader;
  }

  public Extension getExtension() {
    return this.extension;
  }

  public Path getFile() {
    return this.file;
  }

  public Path getDataDirectory() {
    return this.dataDirectory;
  }

  public ExtensionDescription getDescription() {
    return this.description;
  }
}
