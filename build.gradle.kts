plugins {
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "cc.tweaked"
version = "1.0-SNAPSHOT"
val modVersion = "1.111.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()

    exclusiveContent {
        forRepository {
            maven("https://squiddev.cc/maven")
        }
        filter {
            includeGroup("cc.tweaked")
        }
    }
}

dependencies {
    implementation("cc.tweaked:cc-tweaked-1.20.1-core:$modVersion")

    implementation("org.slf4j:slf4j-api:2.0.0")
    implementation("com.google.guava:guava:31.1-jre")

    // Instrumentation
    val otVersion = "1.29.0"
    implementation(platform("io.opentelemetry:opentelemetry-bom:$otVersion"))
    implementation(platform("io.opentelemetry:opentelemetry-bom-alpha:$otVersion-alpha"))

    implementation("io.opentelemetry:opentelemetry-api")
    implementation("io.opentelemetry:opentelemetry-api")
    implementation("io.opentelemetry:opentelemetry-sdk")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("io.opentelemetry:opentelemetry-extension-trace-propagators")
    implementation("io.opentelemetry:opentelemetry-semconv")

    runtimeOnly("io.opentelemetry.instrumentation:opentelemetry-logback-mdc-1.0:$otVersion-alpha")

    // Logging
    runtimeOnly("ch.qos.logback:logback-core:1.4.11")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.11")
}

application {
    mainClass.set("cc.tweaked.eval.Main")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to "eval.tweaked.cc",
                "Implementation-Version" to modVersion,
                "Implementation-Vendor" to "SquidDev",
            )
        )
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    mergeServiceFiles()
    minimize() {
        exclude(dependency("ch.qos.logback:.*:.*"))
        exclude(dependency("io.opentelemetry.*:.*:.*"))
    }
}

tasks.withType(AbstractArchiveTask::class.java).configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    dirMode = Integer.valueOf("755", 8)
    fileMode = Integer.valueOf("664", 8)
}
