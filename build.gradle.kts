plugins {
    id("com.google.protobuf") version "0.8.13" apply false
    kotlin("jvm") version "1.4.30" apply false
}

ext {
    set("grpcVersion", "1.32.1")
    set("grpcKotlinVersion", "1.0.0") // CURRENT_GRPC_KOTLIN_VERSION
    set("protobufVersion", "3.13.0")
}

group = "com.amemachif.abot"
version = "1.0-SNAPSHOT"

allprojects {
    group = "com.amemachif.abot"

    repositories {
        mavenLocal()

        maven(uri("https://maven.aliyun.com/repository/central"))
        maven(uri("https://maven.aliyun.com/repository/public"))
        maven(uri("https://maven.aliyun.com/repository/google"))

        mavenCentral()
        jcenter()
        google()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks.create("assemble").dependsOn(":server:installDist")

tasks.create("buildProto").dependsOn(":stub:build")
