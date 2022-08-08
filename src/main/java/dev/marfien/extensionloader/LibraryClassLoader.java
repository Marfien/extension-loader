package dev.marfien.extensionloader;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.net.URLClassLoader;

class LibraryClassLoader extends URLClassLoader {

  public LibraryClassLoader(final @NotNull ClassLoader parent) {
    super("LibraryLoader", new URL[0], parent);
  }

  @Override
  protected void addURL(final @NotNull URL url) {
    super.addURL(url);
  }
}
