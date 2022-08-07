package dev.marfien.extensionloader.description;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Set;

@ConfigSerializable
public record LibraryInfo(
  @NotNull @Unmodifiable Set<Repository> repositories,
  @NotNull @Unmodifiable Set<Artifact> artifacts
) {

  @ConfigSerializable
  public record Repository(@NotNull String name, @NotNull String url) {
  }

  @ConfigSerializable
  public record Artifact(
    @NotNull String groupId,
    @NotNull String artifactId,
    @NotNull String version
  ) {

    private static final String PATTERN = "%s:%s:%s";

    @Override
    public String toString() {
      return PATTERN.formatted(this.groupId, this.artifactId, this.version);
    }

  }

}
