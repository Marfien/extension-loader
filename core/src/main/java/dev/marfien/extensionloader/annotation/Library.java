package dev.marfien.extensionloader.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(LibraryContainer.class)
public @interface Library {

  String group();
  String name();
  String version();

}
