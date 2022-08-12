package dev.marfien.extensionloader;

import dev.marfien.extensionloader.description.Dependency;
import dev.marfien.extensionloader.description.ExtensionDescription;
import dev.marfien.extensionloader.description.LibraryInfo;
import dev.marfien.extensionloader.exception.CircularDependencyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TopologicalSortingTest {

  private static final String EXTENSION_1 = "ext1";
  private static final String EXTENSION_2 = "ext2";
  private static final String EXTENSION_3 = "ext3";
  private static final String EXTENSION_4 = "ext4";

  @Test
  void testSorting() {
    final var list = Arrays.asList(
      create(EXTENSION_1, EXTENSION_2, EXTENSION_3),
      create(EXTENSION_2, EXTENSION_3),
      create(EXTENSION_3, EXTENSION_4),
      create(EXTENSION_4)
    );

    final var sorted = TopologicalSorting.sort(list);

    assertEquals(list.get(3), sorted.get(0));
    assertEquals(list.get(2), sorted.get(1));
    assertEquals(list.get(1), sorted.get(2));
    assertEquals(list.get(0), sorted.get(3));
  }


  @Test
  void testCircularDependencies() {
    final var list = Arrays.asList(
      create(EXTENSION_1, EXTENSION_2),
      create(EXTENSION_2, EXTENSION_3),
      create(EXTENSION_3, EXTENSION_1)
    );

    assertThrows(CircularDependencyException.class, () -> TopologicalSorting.sort(list));
  }

  private DiscoveredExtension create(final String name, final String... dependencies) {
    return new DiscoveredExtension(
      null,
      new ExtensionDescription(
        name,
        null,
        null,
        null,
        null,
        null,
        Arrays.stream(dependencies).map(dep -> new Dependency(dep, true)).collect(Collectors.toSet()),
        new LibraryInfo(Set.of(), Set.of())
      ),
      Path.of("test"),
      Path.of("test", "data")
    );
  }

}
