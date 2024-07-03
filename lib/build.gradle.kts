plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
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

    val commons_csv_version = "1.10.0"
    val jackson_version = "2.17.0"
    val junit_version = "4.13.2"
    val kotlin_version = "2.0.0"
    val openjdk_version = "1.37"

//    api("com.fasterxml.jackson.core:jackson-databind:$jackson_version")

    api("org.apache.commons:commons-csv:$commons_csv_version")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")


    testImplementation("junit:junit:$junit_version")
    testImplementation("org.openjdk.jmh:jmh-core:$openjdk_version")
    testImplementation("org.openjdk.jmh:jmh-generator-annprocess:$openjdk_version")

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
