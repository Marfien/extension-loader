package dev.marfien.extensionloader.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DependencyContainer.class)
public @interface Dependency {

  String id();
  boolean required() default true;

}
