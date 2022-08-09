package dev.marfien.extensionloader;

import dev.marfien.extensionloader.description.Dependency;
import dev.marfien.extensionloader.description.ExtensionDescription;
import dev.marfien.extensionloader.description.LibraryInfo;
import dev.marfien.extensionloader.exception.CircularDependencyException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TopologicalSortingTest {

  @Test
  void testSorting() {
    final var list = Arrays.asList(
      create("ext1", "ext2", "ext3"),
      create("ext2", "ext3"),
      create("ext3", "ext4"),
      create("ext4")
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
      create("ext1", "ext2"),
      create("ext2", "ext3"),
      create("ext3", "ext1")
    );

    assertThrows(CircularDependencyException.class, () -> TopologicalSorting.sort(list));
  }

  private DiscoveredExtension create(final String name, final String... dependencies) {
    return new DiscoveredExtension(
      null,
      null,
      new ExtensionDescription(
        name,
        "ignored",
        "ignored",
        null,
        null,
        null,
        Arrays.stream(dependencies).map(dep -> new Dependency(dep, true)).collect(Collectors.toSet()),
        new LibraryInfo(Set.of(), Set.of())
      ),
      null,
      null
    );
  }

}
