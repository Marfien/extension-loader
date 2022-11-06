package dev.marfien.extensionloader;

import dev.marfien.extensionloader.properties.ExtensionProperties;
import org.slf4j.Logger;

public class Extension {

  private final String id;
  private final Logger logger;
  private final ExtensionProperties properties;

  private Level level = null;
  private Object instance;

}
