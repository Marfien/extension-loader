package dev.marfien.extensionloader.properties.dependency.failure;

import dev.marfien.extensionloader.properties.dependency.Dependency;
import org.jetbrains.annotations.NotNull;

public non-sealed class BreakUpHandling implements FailureHandling {

  private final Dependency dependency;

  public BreakUpHandling(@NotNull Dependency dependency) {
    this.dependency = dependency;
  }

  @Override
  public void handleFailure(@NotNull Throwable error) throws Exception {
    throw new BreakUpException(error);
  }

  public class BreakUpException extends Exception {

    private BreakUpException(Throwable cause) {
      super(cause);
    }

    public Dependency dependency() {
      return BreakUpHandling.this.dependency;
    }

  }

}
