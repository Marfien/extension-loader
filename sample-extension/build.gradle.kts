plugins {
    id("java")
}

var core = project(":")

dependencies {
  implementation(core)
  annotationProcessor(core)
  implementation(libs.guava)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
