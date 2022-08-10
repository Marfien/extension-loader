package dev.marfien.sampleextension;

import dev.marfien.extensionloader.Extension;
import dev.marfien.extensionloader.annotation.*;

@ExtensionMeta(
  id = "sample-extension",
  version = "v1.0.0"
)
@Author("Marfien")
@Website("https://www.google.com")
@Description("This is a sample extension.")
// @Dependency(id = "unknown-dependency", required = false)
@Repository(name = "JitPack", url = "https://jitpack.io/")
public class SampleExtension extends Extension {

  @Override
  protected void initialize() {
    // TODO: annotation processor project
  }

}
