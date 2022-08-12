package dev.marfien.extensionloader;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionLoaderTest {

  private static final Path EXTENSIONS_PATH = Path.of("src", "test", "resources", "extensions");
  private static final Path LIBS_PATH = EXTENSIONS_PATH.resolve(".libs");

  private final ExtensionLoader loader = new ExtensionLoader();

  @Test
  void testLoadExtensions() throws IOException {
    loader.loadExtensions(EXTENSIONS_PATH, LIBS_PATH);

  }
}
