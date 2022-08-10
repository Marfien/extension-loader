package dev.marfien.extensionloader;

import com.google.common.collect.Lists;
import dev.marfien.extensionloader.description.ExtensionDescription;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

public class ExtensionClassLoader extends URLClassLoader {

  private final Collection<ClassLoader> children = Lists.newLinkedList();

  ExtensionClassLoader(final @NotNull ExtensionDescription description, final @NotNull ClassLoader parent, final @NotNull URL[] urls) {
    super("ext-%s".formatted(description.id()), urls, parent);
  }

  public void addChildClassLoader(final @NotNull ClassLoader child) {
    this.children.add(child);
  }

  @Override
  public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
    try {
      return super.loadClass(name, resolve);
    } catch (final ClassNotFoundException e) {
      for (final var child : this.children) {
        try {
          return child.loadClass(name);
        } catch (final ClassNotFoundException ignored) {}
      }

      throw e;
    }
  }
}
