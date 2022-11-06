package dev.marfien.extensionloader.annotation;

public @interface Supplied {

  String UNASSIGNED = "[unassigned]";

  String by() default UNASSIGNED;

}
