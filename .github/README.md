Extension Loader
===
This library is used to load extensions as jar into current classloader and initialize them. \
Extensions can declare dependencies loaded before the extension, using the topological sorting algorithm. \
If the extension needs external maven libraries, it downloads them automatically. If they are not in Central the repo needs to be added my itself.

## Getting started
At first, you have to add this library to your project.
The version is can be found on JitPack.

[![](https://jitpack.io/v/Marfien/extension-loader.svg)](https://jitpack.io/#Marfien/extension-loader) \
Notable version tags:
- `-SNAPSHOT` uses the last commit
- `dev-SNAPSHOT` uses the last dev version
- `main-SNAPSHOT` uses the last stable version

### Maven:
```xml
<project>
  <repositroies>
    <!-- Add JitPack repo -->
    <repository>
      <name>jitpack</name>
      <url>https://jitpack.io/</url>
    </repository>
  </repositroies>

  <dependencies>
    <dependency>
      <groupId>com.github.Marfien</groupId>
      <artifactId>extension-loader</artifactId>
      <!-- put in your target version -->
      <!-- -SNAPSHOT uses always the newest commit available -->
      <version>-SNAPSHOT</version>
    </dependency>
  </dependencies>
</project>
```

### Gradle

#### Kotlin DSL˚
```kotlin
repositories {
  maven(url = "https://jitpack.io/")
}

val extensionloader = "com.github.Marfien:extension-loader:-SNAPSHOT"

dependencies {
  // replace -SNAPSHOT with your target version
  // -SNAPSHOT uses always the newest commit available
  implementation(extensionloader)
  // this is used to automatically create the entrypoint.extension
  annotationProcessor(extensionloader)
}
```

#### Groovy DSL
```groovy
repository {
  maven url: 'https://jitpack.io/'
}

dependencies {
  // replace -SNAPSHOT with your target version
  // -SNAPSHOT uses always the newest commit available
  implementation 'com.github.Marfien:extension-loader:-SNAPSHOT'
  // this is used to automatically create the entrypoint.extension
  annotationProcessor 'extensionloader'
}
```

## Usage
Currently, you can only load extension inside a directory and terminate all.

### Loading/terminating extensions

```java
import dev.marfien.extensionloader.ExtensionLoader;
import java.nio.file.Path;

public class Main {

  public static void main(String[] args) {
    // Creating a new instance of ExtensionLoader
    // The constructor accepts a ClassLoader used as parent for the ExtensionClassLoader
    // If no one is given the ClassLoader of the ExtensionLoader class is used
    // You can also set a list of filenames for the entrypoint file
    final ExtensionLoader loader = new ExtensionLoader();
    final Path extensionsFolder = Path.of("extensions");
    loader.load(extensionsFolder, extensionsFolder.resolve(".lib"));

    // Terminates the extension
    loader.terminate();
  }

}
```

### Create an extension
To create a valid extension you need to extend `dev.marfien.extensionloader.Extension`.
Moreover you need to annotate it with `@ExtensionMeta(String,String)`
An example can be found ![here](../sample-extension).
```java
import dev.marfien.extensionloader.Extension;
import dev.marfien.extensionloader.annotation.*;

@ExtensionMeta(
    id = "your.extension",
    version = "v1.0.0"
)
@EntryPointFileName("entrypoint.yourloader")
@Author("You")
@Website("https://www.your-domain.com/")
@Dependency(id = "another.dependency")
@Dependency(id = "another.dependency")
@Repository(name = "JitPack", url = "https://jitpack.io/")
@Library(group = "com.github.jitpack", name = "maven-simple", version = "1.1")
public class YourExtension extends Extension {

    protected void initialize() {
        // your code goes here
    }

}
```
