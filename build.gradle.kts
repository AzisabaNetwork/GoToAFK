plugins {
    kotlin("jvm") version "1.5.30"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

group = "net.azisaba"
version = "1.0.2"

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://repo2.acrylicstyle.xyz") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.30")
    implementation("xyz.acrylicstyle:java-util-kotlin:0.15.4")
    compileOnly("io.github.waterfallmc:waterfall-proxy:1.17-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.1.67.Final")
}

tasks {
    shadowJar {
        relocate("kotlin", "net.azisaba.goToAfk.libs.kotlin")
        relocate("util", "net.azisaba.goToAfk.libs.util")
        relocate("net.blueberrymc", "net.azisaba.goToAfk.libs.net.blueberrymc")
        relocate("org", "net.azisaba.goToAfk.libs.org")
        relocate("com", "net.azisaba.goToAfk.libs.com")

        minimize()
        archiveFileName.set("GoToAFK-${project.version}.jar")
    }
}
