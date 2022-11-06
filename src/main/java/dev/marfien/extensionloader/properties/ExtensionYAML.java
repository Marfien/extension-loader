package dev.marfien.extensionloader.properties;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.marfien.extensionloader.properties.dependency.Dependency;

import java.util.Map;

@JsonSerialize
public record ExtensionYAML(
  String entrypoint,
  String version,
  String name,
  String[] authors,
  String description,
  Map<String, Object> variables,
  RawDependency[] classpath
) {



}
