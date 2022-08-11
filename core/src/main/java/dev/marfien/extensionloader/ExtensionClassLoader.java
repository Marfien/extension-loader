package dev.marfien.extensionloader;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.UUID;

public class ExtensionClassLoader extends URLClassLoader {

  private final Collection<ClassLoader> children = Lists.newLinkedList();
  private @NotNull State state = State.CREATED;

  ExtensionClassLoader(final @NotNull ClassLoader parent, final @NotNull URL[] urls) {
    super("ext-%s".formatted(UUID.randomUUID()), urls, parent);
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

  @Override
  public String getName() {
    return super.getName();
  }

  public @NotNull State getState() {
    return this.state;
  }

  void setState(final @NotNull State state) {
    this.state = state;
  }

  void addLibrary(final @NotNull URL url) {
    super.addURL(url);
  }

  enum State {

    CREATED,
    POPULATING,
    POPULATED

  }

}
