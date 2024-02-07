plugins {
    id("org.jetbrains.kotlin.jvm") version libs.versions.kotlin
    id("java-library")
    id("maven-publish")
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
    gradlePluginPortal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)


    api(libs.com.github.davidmoten.guava.mini)
    api(libs.org.apache.commons.commons.csv)
    api(libs.com.fasterxml.jackson.core.jackson.databind)


    testImplementation(libs.junit.junit)
    testImplementation(libs.com.github.davidmoten.junit.extras)
    testImplementation(libs.org.openjdk.jmh.jmh.core)
    testImplementation(libs.org.openjdk.jmh.jmh.generator.annprocess)
    testImplementation(libs.com.github.davidmoten.hilbert.curve)
}

group = "com.jonolds"
version = "0.1.26"
description = "big-sorter-kotlin"

java {
    version = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    withSourcesJar()
//    withJavadocJar()
}

kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("big-sorter-kotlin") {
            groupId = "com.jonolds"
            artifactId = "big-sorter-kotlin"
            version = "0.1.26"
            from(components["java"])
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

//tasks.withType<Javadoc> {
//    options.encoding = "UTF-8"
//}
