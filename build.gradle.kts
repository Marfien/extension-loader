plugins {
  `java-library`
  id("java")
  id("maven-publish")
}

allprojects {
  group = "com.github.Marfien"
  version = "2.0.0"
}

dependencies {
  implementation(libs.slf4j)
  implementation(libs.guava)
  implementation(libs.configurate.core)

  testImplementation(libs.junit.api)
  testRuntimeOnly(libs.slf4j.simple)
  testRuntimeOnly(libs.junit.engine)

  compileOnly(libs.annotations)
}

tasks.getByName<Test>("test") {
  useJUnitPlatform()
}
