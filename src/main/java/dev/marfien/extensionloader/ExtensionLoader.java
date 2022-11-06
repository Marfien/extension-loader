package dev.marfien.extensionloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.jar.JarFile;

public class ExtensionLoader {

  private void loadFiles(Path path) throws IOException {
    if (!Files.isDirectory(path)) throw new NotDirectoryException(path.toString());

    try (var stream = Files.newDirectoryStream(path, entry -> Files.isRegularFile(entry) && entry.endsWith(".jar"))) {
      for (var file : stream) {

      }
    }
  }

  private void loadFile(Path file) throws IOException {
    if (!Files.isReadable(file)) throw new IOException("Cannot read '%s'. Please check permissions.".formatted(file));

    try (var jarFile = new JarFile(file.toFile())) {

    }
  }

  private void loadJar(final JarFile file) {
    try (var entries = file.stream().filter(entry -> entry.getName().endsWith(".ext.yml"))) {
      entries.forEach(entry -> {

      });
    }
  }

}
