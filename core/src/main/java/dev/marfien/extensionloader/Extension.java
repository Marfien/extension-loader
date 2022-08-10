package dev.marfien.extensionloader;

import dev.marfien.extensionloader.description.ExtensionDescription;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Extension {

  private DiscoveredExtension parent;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected Extension() {}

  protected void preInitialize() {}
  protected abstract void initialize();
  protected void postInitialized() {}

  protected void preTerminate() {}
  protected void terminate() {}
  protected void postTerminate() {}


  public Logger getLogger() {
    return this.logger;
  }

  public ExtensionDescription getDescription() {
    return this.parent.getDescription();
  }

  void setParent(final @NotNull DiscoveredExtension parent) {
    this.parent = parent;
  }
}
