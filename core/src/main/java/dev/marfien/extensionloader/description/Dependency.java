package dev.marfien.extensionloader.description;

import org.jetbrains.annotations.NotNull;

public record Dependency(@NotNull String id, boolean required) {

}
