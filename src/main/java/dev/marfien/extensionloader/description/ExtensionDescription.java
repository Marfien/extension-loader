package dev.marfien.extensionloader.description;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Set;

@ConfigSerializable
public record ExtensionDescription(
  @NotNull String id,
  @NotNull String version,
  @NotNull String entrypoint,
  @Nullable String author,
  @Nullable String description,
  @Nullable String url,
  @NotNull Set<Dependency> dependencies,
  @NotNull LibraryInfo libraries
) {}
