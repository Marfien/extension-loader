package dev.marfien.extensionloader.properties.dependency.failure;

import dev.marfien.extensionloader.properties.dependency.Dependency;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public non-sealed class ReportHandling implements FailureHandling {

  private final Dependency dependency;
  private final Logger logger;

  ReportHandling(@NotNull Dependency dependency, @NotNull Logger logger) {
    this.dependency = dependency;
    this.logger = logger;
  }

  @Override
  public void handleFailure(@NotNull Throwable error) {
    this.logger.warn("Cannot load dependency %s".formatted(this.dependency.toString()), error);
  }
}
