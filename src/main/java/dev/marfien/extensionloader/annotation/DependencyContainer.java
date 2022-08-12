package dev.marfien.extensionloader.annotation;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ApiStatus.Internal
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DependencyContainer {

  Dependency[] value();

}
