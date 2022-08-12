package dev.marfien.extensionloader;

import dev.marfien.extensionloader.description.ExtensionDescription;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

class DiscoveredExtension {

  private final ExtensionClassLoader classLoader;
  private Extension extension;

  private final ExtensionDescription description;
  private final Path file;
  private final Path dataDirectory;

  private State state = State.DISCOVERED;

  DiscoveredExtension(final @NotNull ExtensionClassLoader classLoader,
                      final @NotNull ExtensionDescription description,
                      final @NotNull Path file,
                      final @NotNull Path dataDirectory) {
    this.description = description;
    this.file = file;
    this.dataDirectory = dataDirectory;
    this.classLoader = classLoader;
  }

  public ExtensionClassLoader getClassLoader() {
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

  public State getState() {
    return this.state;
  }

  void setExtension(final @NotNull Extension extension) {
    this.extension = extension;
  }

  void setState(final @NotNull State state) {
    this.state = state;
  }

  enum State {

    DISCOVERED,
    PREPARED,
    INSTANCED,
    INITIALIZED,
    TERMINATED

  }

}
