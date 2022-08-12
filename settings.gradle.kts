rootProject.name = "extension-loader"

dependencyResolutionManagement {
  repositories {
    maven("https://jitpack.io")
    mavenCentral()
  }
}

include("sample-extension")
