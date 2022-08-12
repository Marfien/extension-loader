package dev.marfien.extensionloader.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RepositoryContainer.class)
public @interface Repository {

  String name();
  String url();

}
