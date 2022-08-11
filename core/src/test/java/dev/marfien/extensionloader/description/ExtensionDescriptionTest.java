package dev.marfien.extensionloader.description;

import dev.marfien.extensionloader.Extension;
import dev.marfien.extensionloader.annotation.Dependency;
import dev.marfien.extensionloader.annotation.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionDescriptionTest {

  private static final String ID = "test-extension";
  private static final String VERSION = "v0.0.1";

  private static final String TEST_DEPENDENCY_1_ID = "test-id-1";
  private static final boolean TEST_DEPENDENCY_1_REQUIRED = false;
  private static final String TEST_DEPENDENCY_2_ID = "test-id-1";
  private static final String AUTHOR = "TestAuthor";

  private static final String REPO_NAME = "test-repo";
  private static final String REPO_URL = "https://repo.test.com/maven";

  private static final String LIBRARY_GROUP = "com.test";
  private static final String LIBRARY_NAME = "test-lib";
  private static final String LIBRARY_VERSION = "0.0.0";

  @Test
  void testCreateFromClass() {
    final var description = ExtensionDescription.createFromClass(TestExtension.class);

    // meta data
    assertEquals(ID, description.id(), "id");
    assertEquals(VERSION, description.version(), "version");
    assertEquals(AUTHOR, description.author(), "author");
    assertNull(description.description(), "description");
    assertNull(description.url(), "website");

    // dependencies
    final var dependencies = description.dependencies();
    assertEquals(2, dependencies.size(), "Dependencies size");

    assertTrue(dependencies.contains(new dev.marfien.extensionloader.description.Dependency(TEST_DEPENDENCY_1_ID, TEST_DEPENDENCY_1_REQUIRED)), "Dependency 1");
    assertTrue(dependencies.contains(new dev.marfien.extensionloader.description.Dependency(TEST_DEPENDENCY_2_ID, true)), "Dependency 2");

    // libraries
    final var libInfo = description.libraries();

    final var repos = libInfo.repositories();
    assertEquals(1, repos.size(), "repo size");
    assertTrue(repos.contains(new LibraryInfo.Repository(REPO_NAME, REPO_URL)), "repo");

    final var artifacts = libInfo.artifacts();
    assertEquals(1, artifacts.size(), "artifacts size");
    assertTrue(artifacts.contains(new LibraryInfo.Artifact(LIBRARY_GROUP, LIBRARY_NAME, LIBRARY_VERSION)), "artifact");

    assertEquals(TestExtension.class, description.entrypoint(), "entrypoint");
  }

  @ExtensionMeta(
    id = ID,
    version = VERSION
  )
  @Author(AUTHOR)
  @Dependency(id = TEST_DEPENDENCY_1_ID, required = TEST_DEPENDENCY_1_REQUIRED)
  @Dependency(id = TEST_DEPENDENCY_2_ID)
  @Repository(name = REPO_NAME, url = REPO_URL)
  @Library(group = LIBRARY_GROUP, name = LIBRARY_NAME, version = LIBRARY_VERSION)
  final class TestExtension extends Extension {

    @Override
    protected void initialize() {

    }
  }

}
