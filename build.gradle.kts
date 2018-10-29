import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by project
val springBootVersion: String by project

buildscript {
  repositories {
    mavenCentral()
  }
}

plugins {
  kotlin("jvm") version "1.2.71"
  id("org.jetbrains.kotlin.plugin.spring") version "1.2.71"
  id("org.springframework.boot") version "2.0.6.RELEASE"
  id("io.spring.dependency-management") version "1.0.6.RELEASE"
}

tasks {
  getByName<Jar>("jar") {
    baseName = "virtual-keypad"
  }

  withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
  }

  withType<AbstractCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
  }

  register<Wrapper>("wrapper") {
    gradleVersion = "4.10.2"
  }
}


repositories {
  mavenCentral()
  maven("https://jitpack.io")
  maven("https://repo.spring.io/snapshot")
  maven("https://repo.spring.io/milestone")
}

dependencies {
  runtimeOnly("org.springframework.boot:spring-boot-devtools")
  implementation("org.springframework.boot:spring-boot-starter-websocket")
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))
  implementation( "com.fasterxml.jackson.module:jackson-module-kotlin:2.9.+")
  implementation("com.github.v3rm0n:dsc-it100-java:0.5.3")
  implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.0")
  implementation("org.rxtx:rxtx:2.1.7")

  implementation("org.webjars.npm:milligram:1.1.0")
  implementation("org.webjars.npm:angular:1.5.0")
  implementation("org.webjars.npm:angular-websocket:1.0.14") {
    isTransitive = false
  }

  testImplementation("org.springframework.boot:spring-boot-starter-test")
}
