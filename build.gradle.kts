import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
}

group = "me.cjgj"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

val exposedVersion: String by project
dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation(kotlin("test"))
    implementation("dev.kord:kord-core:0.8.0-M9")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("com.willowtreeapps:fuzzywuzzy-kotlin:0.9.0")
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "9"
}

application {
    mainClass.set("MainKt")
}