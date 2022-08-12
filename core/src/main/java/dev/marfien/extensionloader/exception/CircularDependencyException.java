package dev.marfien.extensionloader.exception;

public class CircularDependencyException extends IllegalStateException {

  public CircularDependencyException(final String message) {
    super("Found circular dependencies: %s".formatted(message));
  }
}
