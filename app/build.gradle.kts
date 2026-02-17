/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2025-2026 SpoilerRules
 *
 * Server List Explorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Server List Explorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Server List Explorer.  If not, see <https://www.gnu.org/licenses/>.
*/

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.time.Year

plugins {
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":nbt"))
    implementation(project(":settings"))
    implementation(project(":ui"))

    compileOnly(libs.log4j.core)
    runtimeOnly(libs.log4j.slf4j2.impl)

    implementation(libs.jna)
    implementation(libs.jna.platform)

    implementation(libs.compose.runtime)
    implementation(libs.composeNativeTray)

    val onlyWindowsX64: Boolean by rootProject.extra
    val onlyWindowsArm64: Boolean by rootProject.extra
    val onlyLinuxX64: Boolean by rootProject.extra
    val onlyLinuxArm64: Boolean by rootProject.extra
    val targetRuntimes =
        when {
            onlyWindowsX64 -> listOf(libs.compose.desktop.jvm.windows.x64)
            onlyWindowsArm64 -> listOf(libs.compose.desktop.jvm.windows.arm64)
            onlyLinuxX64 -> listOf(libs.compose.desktop.jvm.linux.x64)
            onlyLinuxArm64 -> listOf(libs.compose.desktop.jvm.linux.arm64)
            else ->
                listOf(
                    libs.compose.desktop.jvm.windows.x64,
                    libs.compose.desktop.jvm.windows.arm64,
                    libs.compose.desktop.jvm.linux.x64,
                    libs.compose.desktop.jvm.linux.arm64,
                    libs.compose.desktop.jvm.macos.x64,
                    libs.compose.desktop.jvm.macos.arm64,
                )
        }

    targetRuntimes.forEach { runtimeOnly(it) }
}

val mainFunction = "com.spoiligaming.explorer.MainKt"

tasks.jar {
    /*
    The default JAR is named with an "-app" suffix to make it clear that this
    artifact contains only the classes from the app module itself.
     */
    archiveFileName.set("ServerListExplorer-app.jar")
    duplicatesStrategy = DuplicatesStrategy.FAIL
    manifest {
        attributes["Main-Class"] = mainFunction
    }
}

tasks.shadowJar {
    archiveFileName.set("ServerListExplorer-all.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = mainFunction
    }

    doLast {
        println("Shadow JAR generated at: ${archiveFile.get().asFile.absolutePath}")
    }
}

val buildWithProjectModules by tasks.registering(Jar::class) {
    archiveFileName.set("ServerListExplorer.jar")
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
    duplicatesStrategy = DuplicatesStrategy.FAIL

    // include current project output
    from(sourceSets["main"].output)

    // find all project (module) dependencies declared on common configurations
    val projectPaths: List<String> =
        configurations
            .filter { it.name in listOf("implementation", "api", "compileOnly", "runtimeOnly") }
            .flatMap { conf ->
                conf.dependencies.withType(ProjectDependency::class.java).map { dep -> dep.path }
            }.distinct()

    projectPaths.forEach { projectPath ->
        val depProject = project.project(projectPath)
        val jarProvider = depProject.tasks.named<Jar>("jar")

        // ensure the subproject jar is built before this task
        dependsOn(jarProvider)

        // include contents of that jar, but drop the generated BuildConfig to avoid conflicts
        from({ project.zipTree(jarProvider.flatMap { it.archiveFile }) }) {
            exclude("com/spoiligaming/explorer/build/BuildConfig.class")
            exclude("com/spoiligaming/explorer/build/PlatformDirs.class")
        }
    }
}

val appVersion: String by rootProject.extra
version = appVersion

val rawVersion = project.version.toString()

val versionMatch =
    Regex("""(\d+)\.(\d+)(?:\.(\d+))?(?:-([\w.\-]+))?""")
        .matchEntire(rawVersion)
        ?: error("Version \"$rawVersion\" is not valid SemVer")

val (majorStr, minorStr, patchStr, qualifier) = versionMatch.destructured

val major = majorStr.toInt()
val minor = minorStr.toInt()
val patch = patchStr.ifEmpty { "0" }.toInt()

require(major in 0..255) { "Windows MAJOR must be <=255 (got $major)" }
require(minor in 0..255) { "Windows MINOR must be <=255 (got $minor)" }
require(patch in 0..654) {
    "Windows PATCH must be between 0 and 654 for version mapping (got $patch)"
}

val preReleaseMatch =
    if (qualifier.isNotEmpty()) {
        Regex("""(dev|alpha|beta|rc)(?:\.(\d+))?""").matchEntire(qualifier)
            ?: error(
                "Unsupported pre-release identifier \"$qualifier\". " +
                    "Expected dev[.N], alpha.N, beta.N, or rc.N.",
            )
    } else {
        null
    }

val windowsBuild =
    if (preReleaseMatch == null) {
        patch * 100 + 99
    } else {
        val (stage, numStr) = preReleaseMatch.destructured
        val num = numStr.ifEmpty { "0" }.toInt()

        require(num in 0..98) {
            "Pre-release number must be between 0 and 98 (got $num)"
        }

        val stageCode =
            when (stage) {
                "dev" -> 0
                "alpha" -> 1
                "beta" -> 2
                "rc" -> 3
                else -> error("Unexpected stage \"$stage\"")
            }

        patch * 100 + stageCode * 10 + num
    }

require(windowsBuild in 0..65535) {
    "Windows BUILD must be <=65535 (got $windowsBuild)"
}

val numericWindowsVersion = "$major.$minor.$windowsBuild"
val debPackageVer =
    if (qualifier.isNotEmpty()) {
        "$major.$minor.$patch~$qualifier"
    } else {
        "$major.$minor.$patch"
    }

val isDebDebugBuild: Boolean =
    providers
        .gradleProperty("debDebug")
        .map { it.equals("true", ignoreCase = true) }
        .orElse(false)
        .get()

val startYear = 2025
val copyrightYears =
    startYear.let {
        val now = Year.now().value
        if (now > it) "$it–$now" else "$it"
    }

val optimizedJvmArgs =
    listOf(
        "-Xms128m",
        "-Xmx2g",
        "-XX:+UseG1GC",
        "-XX:MaxGCPauseMillis=50",
        "-XX:InitiatingHeapOccupancyPercent=30",
        "-XX:G1ReservePercent=15",
        "-XX:+ParallelRefProcEnabled",
        "-XX:+UseStringDeduplication",
    )

compose.desktop.application {
    mainClass = mainFunction
    javaHome = sequenceOf(
        System.getenv("JDK_21"),
        System.getenv("JAVA_HOME"),
        findProperty("org.gradle.java.home") as? String,
        runCatching {
            extensions
                .getByType(JavaToolchainService::class.java)
                .launcherFor { languageVersion.set(JavaLanguageVersion.of(21)) }
                .get()
                .metadata.installationPath.asFile.absolutePath
        }.getOrNull(),
        System.getProperty("java.home"),
    ).filterNotNull().map { it.trim() }.firstOrNull { it.isNotEmpty() }
        ?: error(
            """
            Could not locate a Java 21 installation.
            Please do one of the following and retry:
              - Set the JDK_21 environment variable
              - Set JAVA_HOME to a Java 21 installation
              - Set org.gradle.java.home in gradle.properties
              - Configure a Java 21 Gradle toolchain
            """.trimIndent(),
        )

    jvmArgs += optimizedJvmArgs
    if (isDebDebugBuild) {
        jvmArgs +=
            listOf(
                "-Denv=dev",
            )
    }

    nativeDistributions {
        windows {
            packageName = "ServerListExplorer"
            packageVersion = numericWindowsVersion
            msiPackageVersion = numericWindowsVersion
            exePackageVersion = numericWindowsVersion

            console = false

            shortcut = true
            menu = true
            perUserInstall = true
            dirChooser = true
            upgradeUuid = "4bd4c2a2-2567-4c63-9abf-aa5adab76c4c"
        }

        linux {
            packageName = "server-list-explorer"
            debPackageVersion = debPackageVer
            debMaintainer = "SpoilerRules"

            shortcut = true

            menuGroup = "Utility"
            appCategory = "Utility"

            modules("jdk.security.auth")
        }

        description = "Server List Explorer for Minecraft"
        copyright = "Copyright (C) $copyrightYears SpoilerRules"
        vendor = "SpoilerRules"
        licenseFile.set(rootProject.file("LICENSE"))

        targetFormats(
            TargetFormat.Msi,
            TargetFormat.Exe,
            TargetFormat.Deb,
        )

        modules("java.management", "jdk.unsupported", "jdk.accessibility")
    }

    buildTypes.release.proguard {
        version = libs.versions.proguard

        optimize.set(false)
        obfuscate.set(false)
        configurationFiles.from(
            rootProject.file("proguard/base.pro"),
            rootProject.file("proguard/ComposeNativeTray.pro"),
            rootProject.file("proguard/compose.pro"),
            rootProject.file("proguard/jna.pro"),
            rootProject.file("proguard/ktor.pro"),
            rootProject.file("proguard/kotlinlogging.pro"),
            rootProject.file("proguard/kotlinx-serialization.pro"),
            rootProject.file("proguard/log4j.pro"),
            rootProject.file("proguard/HackedSelectionContainer.kt.pro"),
            rootProject.file("proguard/oshi.pro"),
            rootProject.file("proguard/slf4j.pro"),
        )
    }
}
