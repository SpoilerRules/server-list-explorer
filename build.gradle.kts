plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.ktlint)
}

val mainFunction = "com.spoiligaming.explorer.MainKt"

repositories {
    mavenCentral()
    google()
    maven("https://jitpack.io/")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.serialization.json) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
    }

    implementation(compose.desktop.windows_x64)
    implementation(compose.desktop.linux_x64) // UNCONFIRMED
    implementation(compose.desktop.linux_arm64) // UNCONFIRMED
    implementation(compose.desktop.macos_x64) // UNCONFIRMED
    implementation(compose.desktop.macos_arm64) // UNCONFIRMED
    implementation(compose.material)
    implementation(compose.material3)

    // Compose libraries
    implementation(libs.color.picker)
    implementation(libs.file.kit)

    // Other libraries
    implementation(files("libraries/mcserverping-1.0.7.jar"))
    implementation(libs.nbt)
    implementation(libs.s4lfj.noop)

    testImplementation(compose.desktop.uiTestJUnit4)
    testImplementation(libs.kotlin.test)
}

tasks {
    shadowJar {
        mergeServiceFiles()
        duplicatesStrategy = DuplicatesStrategy.FAIL
        archiveFileName.set("ServerListExplorer.jar")
        manifest {
            attributes["Main-Class"] = mainFunction
        }
        from("LICENSE") {
            into("")
        }
    }

    jar {
        from("LICENSE") {
            into("")
        }
    }
}

compose.desktop {
    application {
        mainClass = mainFunction

        javaHome = System.getenv("JDK_17")

        buildTypes.release.proguard {
            joinOutputJars.set(true)
            obfuscate.set(true)
            optimize.set(true)
            configurationFiles.from(project.file("proguard-rules.pro"))
        }

        nativeDistributions {
            licenseFile.set(project.file("LICENSE"))
        }
    }
}

ktlint {
    android = true

    debug.set(false)
    verbose.set(false)
    android.set(false)
    ignoreFailures.set(true)
    enableExperimentalRules.set(true)
    baseline.set(file("ktlint-baseline.xml"))

    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }

    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

kotlin {
    jvmToolchain(11)
}
