package dev.marfien.extensionloader.exception;

import org.jetbrains.annotations.NotNull;

public class DependencyNotFoundException extends Exception {

  public DependencyNotFoundException(final @NotNull String extensionId, final @NotNull String dependencyId) {
    super("%s is required by %s".formatted(dependencyId, extensionId));
  }
}
