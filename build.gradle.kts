plugins {
  `java-library`
  id("java")
}

allprojects {
  group = "com.github.Marfien"
  version = "1.0.0"
}

dependencies {
  implementation(libs.dependencyGetter)
  implementation(libs.slf4j)
  implementation(libs.guava)
  implementation(libs.bundles.kotlin)

  testImplementation(libs.junit.api)
  testRuntimeOnly(libs.junit.engine)

  compileOnly(libs.annotations)
}

tasks.getByName<Test>("test") {
  useJUnitPlatform()
}
