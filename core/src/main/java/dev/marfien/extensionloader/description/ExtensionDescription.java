package dev.marfien.extensionloader.description;

import dev.marfien.extensionloader.Extension;
import dev.marfien.extensionloader.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public record ExtensionDescription(
  @NotNull String id,
  @NotNull String version,
  @NotNull String entrypoint,
  @Nullable String author,
  @Nullable String description,
  @Nullable String url,
  @NotNull Set<Dependency> dependencies,
  @NotNull LibraryInfo libraries
) {

  public static @NotNull ExtensionDescription createFromClass(final @NotNull Class<? extends Extension> extensionClass) {
    final var meta = getOpt(extensionClass, ExtensionMeta.class)
      .orElseThrow(() -> new IllegalStateException("Extension class needs to have a @ExtensionMeta annotation"));

    final var author = getOpt(extensionClass, Author.class).map(Author::value).orElse(null);
    final var description = getOpt(extensionClass, Description.class).map(Description::value).orElse(null);
    final var url = getOpt(extensionClass, Website.class).map(Website::value).orElse(null);

    final var dependencies = map(extensionClass.getAnnotationsByType(dev.marfien.extensionloader.annotation.Dependency.class), dependency -> new Dependency(dependency.id(), dependency.required()));
    final var repos = map(extensionClass.getAnnotationsByType(Repository.class), repository -> new LibraryInfo.Repository(repository.name(), repository.url()));
    final var artifacts = map(extensionClass.getAnnotationsByType(Library.class), library -> new LibraryInfo.Artifact(library.group(), library.name(), library.version()));

    return new ExtensionDescription(
      meta.id(),
      meta.version(),
      extensionClass.getName(),
      author,
      description,
      url,
      dependencies,
      new LibraryInfo(
        repos,
        artifacts
      )
    );
  }

  private static <A extends Annotation> Optional<A> getOpt(final Class<?> clazz, final Class<A> annotationClass) {
    return Optional.ofNullable(clazz.getAnnotation(annotationClass));
  }

  private static <A, R> Set<R> map(final @NotNull A[] dependencies, final @NotNull Function<A, R> mapper) {
    return dependencies.length == 0
      ? Set.of()
      : Arrays.stream(dependencies)
        .map(mapper)
        .collect(Collectors.toSet());
  }

}
