plugins {
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

group = "net.azisaba"
version = "2.0.0"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.acrylicstyle.xyz/repository/maven-public/") }
    maven { url = uri("https://nexus.velocitypowered.com/repository/maven-public/") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
    implementation("xyz.acrylicstyle.util:yaml:0.16.6")
    compileOnly("com.velocitypowered:velocity-api:3.0.1")
}

tasks {
    shadowJar {
        relocate("kotlin", "net.azisaba.goToAfk.libs.kotlin")
        relocate("util", "net.azisaba.goToAfk.libs.util")

        minimize()
        archiveFileName.set("GoToAFK-${project.version}.jar")
    }
}
