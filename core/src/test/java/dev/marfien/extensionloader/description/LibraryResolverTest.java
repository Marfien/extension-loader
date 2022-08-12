package dev.marfien.extensionloader.description;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

class LibraryResolverTest {

  private static final Path PATH = Path.of("src", "test", "resources", "resolver");

  @Test
  void testResolve() {
    final var resolver = new LibraryResolver();

    final var resolved = resolver.resolve(new LibraryInfo.Artifact("org.slf4j", "slf4j-simple", "1.7.36"), PATH);
    Assertions.assertEquals(2, resolved.size());
  }

  @AfterEach
  void cleanUp() throws IOException {
    try (final var stream = Files.walk(PATH)) {
      stream.sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::deleteOnExit);
    }
  }


}
