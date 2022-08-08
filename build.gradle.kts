plugins {
  `java-library`
  id("java")
}

group = "dev.marfien"
version = "1.0-SNAPSHOT"

dependencies {
  implementation(libs.bundles.configurate)
  implementation(libs.dependencyGetter)
  implementation(libs.slf4j)
  implementation(libs.guava)

  compileOnly(libs.annotations)
}

tasks.getByName<Test>("test") {
  useJUnitPlatform()
}
