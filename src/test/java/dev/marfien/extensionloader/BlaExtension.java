package dev.marfien.extensionloader;

import dev.marfien.extensionloader.annotation.*;
import org.slf4j.Logger;

@Autosupply
public class BlaExtension {

  private String test; // automatically supplied by autosuplly
  private String hello = "Hello World"; // not supplied because it's already initialized
  @Exclude private int foo; // not initialized cause of exclude
  @Supplied private boolean foo2; // used when Autosupply is not given
  @Supplied(by = "random-int") private int someInt; // supplied by specific supplier
  private final double PI = 3.1415D; // not supplied due to final modifier


  @PreInit
  static void discovered() {

  }

  public BlaExtension(Logger logger, @Supplied(by = "platform") String platform, Extension extension) { // extensionload with suppliers for classes and aliases

  }

  @Run.While(isOn = Level.LOAD) // default of 'of' is current class
  void load() {

  }

  @Run.Before(extension = "com.some.alias.XYZ", isOn = Level.ENABLE)
  void prepareXYZ() {

  }

  @Run.After(extension = "...", isOn = Level.DISABLE)
  void cleanUpXXX() {

  }

}
