package dev.marfien.extensionloader.properties.dependency;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class DependencyDeserializer implements StdDeserializer<Dependency> {

  @Override
  public Dependency deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
    return null;
  }
}
