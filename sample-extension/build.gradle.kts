plugins {
    id("java")
}

group = "dev.marfien"
version = "1.0.0"

var core = project(":core")

dependencies {
  implementation(core)
  annotationProcessor(core)
  implementation(libs.guava)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
