package dev.marfien.extensionloader.properties.dependency.failure;

import org.jetbrains.annotations.NotNull;

public non-sealed class NoOpHandling implements FailureHandling {

  static NoOpHandling INSTANCE = new NoOpHandling();

  private NoOpHandling() {}

  @Override
  public void handleFailure(@NotNull Throwable error) {
    // Do nothing
  }
}
