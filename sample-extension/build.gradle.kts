plugins {
    id("java")
}

group = "dev.marfien"
version = libs.versions.main

repositories {
    mavenCentral()
}

dependencies {
  implementation(project(":core"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
