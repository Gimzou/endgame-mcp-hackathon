import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mcpVersion = "0.3.0"
val slf4jVersion = "2.0.9"
val ktorVersion = "3.1.1"

plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("AppKt")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.modelcontextprotocol:kotlin-sdk:$mcpVersion")
    implementation("org.slf4j:slf4j-nop:$slf4jVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
