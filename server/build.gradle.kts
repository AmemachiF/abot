plugins {
    application
    kotlin("jvm")
}

version = "1.0-SNAPSHOT"

dependencies {
    api("net.mamoe", "mirai-core", "2.4.0")

    implementation(kotlin("stdlib"))
    implementation(project(":stub"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    implementation("org.apache.commons:commons-lang3:3.11")
    implementation("org.yaml:snakeyaml:1.27")

    runtimeOnly("io.grpc:grpc-netty:${rootProject.ext["grpcVersion"]}")
}

kotlin {
    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.Experimental")
    }
}

val createVersionProperties = task("createVersionProperties") {
    doLast {
        file("$buildDir/resources/main").apply {
            if (!exists()) mkdirs()
        }
        logger.info("$buildDir")
        file("$buildDir/resources/main/version.properties").writer().let {
            val p = org.jetbrains.kotlin.konan.properties.Properties()
            p["version"] = project.version.toString()
            p.store(it, null)
        }
    }
}.dependsOn("processResources")

tasks.named("classes") {
    dependsOn(createVersionProperties)
}

tasks.register<JavaExec>("Server") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    main = "com.amemachif.abot.server.MainKt"
}

val serverStartScripts = tasks.register<CreateStartScripts>("serverStartScripts") {
    mainClassName = "com.amemachif.abot.server.MainKt"
    applicationName = "abot-server"
    outputDir = tasks.named<CreateStartScripts>("startScripts").get().outputDir
    classpath = tasks.named<CreateStartScripts>("startScripts").get().classpath
}

tasks.named("startScripts") {
    dependsOn(serverStartScripts)
}
