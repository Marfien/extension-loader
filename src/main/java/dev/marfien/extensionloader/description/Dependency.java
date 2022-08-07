package dev.marfien.extensionloader.description;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record Dependency(@NotNull String id, boolean required) {

}
