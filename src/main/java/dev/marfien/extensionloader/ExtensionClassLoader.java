package dev.marfien.extensionloader;

import dev.marfien.extensionloader.description.ExtensionDescription;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.net.URLClassLoader;

public class ExtensionClassLoader extends URLClassLoader {

  ExtensionClassLoader(final @NotNull ExtensionDescription description, final @NotNull ClassLoader parent, final @NotNull URL[] urls) {
    super("ext-%s".formatted(description.id()), urls, parent);
  }
}
