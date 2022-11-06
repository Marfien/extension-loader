package dev.marfien.extensionloader;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public record RunnableMethod(
  Object handle,
  @NotNull Method method
) {

  public RunnableMethod(Object handle, @NotNull Method method) {
    this.handle = handle;
    this.method = method;

    this.setAccessible();
  }

  private void setAccessible() {
    var modifiers = this.method.getModifiers();

    if (Modifier.isPublic(modifiers)) return;
    this.method.setAccessible(true);
  }

  public RunnableMethod(@NotNull Method method) {
    this(null, method);
  }

  public void run(Object... arguments) throws InvocationTargetException {
    try {
      this.method.invoke(this.handle, arguments);
    } catch (IllegalAccessException e) {
      // should already be checked above
      throw new IllegalStateException(e);
    }
  }

}
