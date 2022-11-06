package dev.marfien.extensionloader.properties.dependency;

import dev.marfien.extensionloader.properties.dependency.failure.FailureHandling;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

public sealed abstract class Dependency permits MavenDependency, ExtensionDependency, UrlDependency {

  protected abstract URL[] resolve0() throws Throwable;
  private final FailureHandling handling;

  public Dependency(@NotNull FailureHandling handling) {
    this.handling = handling;
  }

  public final URL[] resolve() throws Exception {
    try {
      return this.resolve0();
    } catch (Throwable throwable) {
      this.handling.handleFailure(throwable);
      return new URL[0];
    }
  }

}
