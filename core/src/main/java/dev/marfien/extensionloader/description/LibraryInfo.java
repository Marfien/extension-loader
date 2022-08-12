package dev.marfien.extensionloader.description;

import net.minestom.dependencies.maven.MavenRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

public record LibraryInfo(
  @NotNull @Unmodifiable Set<Repository> repositories,
  @NotNull @Unmodifiable Set<Artifact> artifacts
) {

  public record Repository(@NotNull String name, @NotNull String url) {

    public @NotNull MavenRepository toMaven() {
      return new MavenRepository(this.name, this.url);
    }

  }

  public record Artifact(
    @NotNull String group,
    @NotNull String name,
    @NotNull String version
  ) {

    private static final String PATTERN = "%s:%s:%s";

    @Override
    public String toString() {
      return PATTERN.formatted(this.group, this.name, this.version);
    }

  }

}
