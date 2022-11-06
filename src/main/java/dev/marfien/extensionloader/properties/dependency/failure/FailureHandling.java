package dev.marfien.extensionloader.properties.dependency.failure;

import dev.marfien.extensionloader.RunnableMethod;
import dev.marfien.extensionloader.properties.dependency.Dependency;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public sealed interface FailureHandling permits RunMethodHandling, NoOpHandling, ReportHandling, BreakUpHandling {

  void handleFailure(@NotNull Throwable error) throws Exception;

  static FailureHandling noOperationHandling() {
    return NoOpHandling.INSTANCE;
  }

  static FailureHandling reportHandling(@NotNull Dependency dependency, @NotNull Logger logger) {
    return new ReportHandling(dependency, logger);
  }

  static FailureHandling breakUpHandling(@NotNull Dependency dependency) {
    return new BreakUpHandling(dependency);
  }

  // TODO implement
  static FailureHandling customHandling(@NotNull RunnableMethod method, @NotNull Dependency dependency, Object[] arguments) {
    return new RunMethodHandling();
  }

}
