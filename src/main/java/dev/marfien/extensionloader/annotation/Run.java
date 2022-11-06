package dev.marfien.extensionloader.annotation;

import dev.marfien.extensionloader.Level;

public final class Run {

  public static final String SELF = "[self]";

  public @interface While {

    String extension() default SELF;

    Level isOn();

  }

  public @interface After {

    String extension() default SELF;

    Level isOn();

  }

  public @interface Before {

    String extension() default SELF;

    Level isOn();

  }
}
