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

import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.jk1.license.render.JsonReportRenderer
import com.github.jk1.license.render.ReportRenderer
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply true
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.buildConfig) apply true
    alias(libs.plugins.ktlint) apply true
    alias(libs.plugins.dependencyLicenseReport) apply true
}

val appVersionProvider: Provider<String> =
    providers
        .environmentVariable("APP_VERSION")
        .orElse(providers.gradleProperty("appVersion"))
        .orElse("0.0.0-dev")

val requestedTasks = gradle.startParameter.taskNames.flatMap { it.split(":") }

fun isWindowsHost() = System.getProperty("os.name").contains("win", ignoreCase = true)

fun isLinuxHost() = System.getProperty("os.name").contains("linux", ignoreCase = true)

fun isArm64Arch(): Boolean {
    val arch = System.getProperty("os.arch").lowercase()
    return arch.contains("aarch64") || arch.contains("arm64")
}

fun portableBuildTargetLabel() =
    when {
        isWindowsHost() && isArm64Arch() -> "Windows ARM64"
        isWindowsHost() -> "Windows x64"
        isLinuxHost() && isArm64Arch() -> "Linux ARM64"
        isLinuxHost() -> "Linux x64"
        else -> "Current OS"
    }

fun portableDistributionLabel(minified: Boolean) =
    if (minified) {
        "Portable (${portableBuildTargetLabel()}, Minified)"
    } else {
        "Portable (${portableBuildTargetLabel()})"
    }

val packagingTasks =
    setOf(
        "packageMsi",
        "packageExe",
        "packageDeb",
        "createDistributable",
        "packageReleaseMsi",
        "packageReleaseExe",
        "packageReleaseDeb",
        "createReleaseDistributable",
    )

fun isPackagingBuild() = requestedTasks.any { it in packagingTasks }

val appDistributionProvider: Provider<String> =
    providers
        .environmentVariable("APP_DISTRIBUTION")
        .orElse(providers.gradleProperty("appDistribution"))
        .orElse(
            provider {
                when {
                    "packageReleaseMsi" in requestedTasks -> "MSI Installer (Minified)"
                    "packageReleaseExe" in requestedTasks -> "EXE Installer (Minified)"
                    "packageReleaseDeb" in requestedTasks -> "DEB Package (Minified)"
                    "createReleaseDistributable" in requestedTasks -> portableDistributionLabel(minified = true)
                    "packageReleaseUberJarForCurrentOS" in requestedTasks -> "Minified Fat JAR"
                    "packageMsi" in requestedTasks -> "MSI Installer"
                    "packageExe" in requestedTasks -> "EXE Installer"
                    "packageDeb" in requestedTasks -> "DEB Package"
                    "createDistributable" in requestedTasks -> portableDistributionLabel(minified = false)
                    "shadowJar" in requestedTasks -> "Fat JAR"
                    "buildWithProjectModules" in requestedTasks -> "JAR"
                    else -> "unspecified"
                }
            },
        )

val onlyWindowsArm64Provider: Provider<Boolean> =
    providers
        .environmentVariable("ONLY_WINDOWS_ARM64")
        .map { it.toBoolean() }
        .orElse(providers.gradleProperty("onlyWindowsArm64").map { it.toBoolean() })
        .orElse(
            provider {
                isWindowsHost() && isPackagingBuild() && isArm64Arch()
            },
        )

val onlyWindowsX64Provider: Provider<Boolean> =
    providers
        .environmentVariable("ONLY_WINDOWS_X64")
        .map { it.toBoolean() }
        .orElse(providers.gradleProperty("onlyWindowsX64").map { it.toBoolean() })
        .orElse(
            provider {
                isWindowsHost() && isPackagingBuild() && !isArm64Arch()
            },
        )

val onlyLinuxArm64Provider: Provider<Boolean> =
    providers
        .environmentVariable("ONLY_LINUX_ARM64")
        .map { it.toBoolean() }
        .orElse(providers.gradleProperty("onlyLinuxArm64").map { it.toBoolean() })
        .orElse(
            provider {
                isLinuxHost() && isPackagingBuild() && isArm64Arch()
            },
        )

val onlyLinuxX64Provider: Provider<Boolean> =
    providers
        .environmentVariable("ONLY_LINUX_X64")
        .map { it.toBoolean() }
        .orElse(providers.gradleProperty("onlyLinuxX64").map { it.toBoolean() })
        .orElse(
            provider {
                isLinuxHost() && isPackagingBuild() && !isArm64Arch()
            },
        )

val onlyWindowsArm64: Boolean = onlyWindowsArm64Provider.get()
val onlyWindowsX64: Boolean = onlyWindowsX64Provider.get() && !onlyWindowsArm64
val onlyLinuxArm64: Boolean = onlyLinuxArm64Provider.get() && !onlyWindowsArm64 && !onlyWindowsX64
val onlyLinuxX64: Boolean = onlyLinuxX64Provider.get() && !onlyWindowsArm64 && !onlyWindowsX64 && !onlyLinuxArm64
val appVersion: String = appVersionProvider.get()
val appDistribution: String = appDistributionProvider.get()

extra["appVersion"] = appVersion
extra["appDistribution"] = appDistribution
extra["onlyWindowsX64"] = onlyWindowsX64
extra["onlyWindowsArm64"] = onlyWindowsArm64
extra["onlyLinuxX64"] = onlyLinuxX64
extra["onlyLinuxArm64"] = onlyLinuxArm64
version = appVersion

licenseReport {
    outputDir = "ui/src/main/resources"
    projects = arrayOf(project, *project.subprojects.toTypedArray())
    renderers = arrayOf<ReportRenderer>(JsonReportRenderer("open_source_licenses.json", true))
}

gradle.taskGraph.whenReady {
    if (appDistribution != "unspecified") {
        logger.lifecycle("Building distribution: $appDistribution")
    }
    if (onlyWindowsX64) {
        logger.lifecycle("Building for Windows x64 only (optimization enabled)")
    }
    if (onlyWindowsArm64) {
        logger.lifecycle("Building for Windows ARM64 only (optimization enabled)")
    }
    if (onlyLinuxX64) {
        logger.lifecycle("Building for Linux x64 only (optimization enabled)")
    }
    if (onlyLinuxArm64) {
        logger.lifecycle("Building for Linux ARM64 only (optimization enabled)")
    }
}

allprojects {
    plugins.withId("org.jetbrains.kotlin.jvm") {
        apply(plugin = "com.github.gmazzo.buildconfig")

        extensions.getByType<BuildConfigExtension>().apply {
            useKotlinOutput {
                internalVisibility = false
            }
            packageName("com.spoiligaming.explorer.build")

            buildConfigField("VERSION", appVersion)
            buildConfigField("DISTRIBUTION", appDistribution)

            forClass("PlatformDirs") {
                buildConfigField("WINDOWS_MACOS_APP_DIR_NAME", "Server List Explorer")
                buildConfigField("LINUX_APP_DIR_NAME", "server-list-explorer")
            }
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    dependencies {
        val libs = gradle.rootProject.libs

        implementation(libs.kotlin.logging)

        testImplementation(project(":app")) // we get log4j2-test.xml from the app module
        testImplementation(libs.kotlin.test)
        testRuntimeOnly(libs.log4j.slf4j2.impl)
    }

    plugins.withType<JavaBasePlugin> {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xwhen-expressions=indy")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        // inject app/src/test/resources/log4j2-test.xml into test runtime classpath
        if (project.name != "app") {
            classpath += project(":app").sourceSets["test"].output
        }
    }

    configure<KtlintExtension> {
        debug.set(false)
        verbose.set(false)
        android.set(false)
        ignoreFailures.set(false)
        enableExperimentalRules.set(true)
        baseline.set(rootProject.file("ktlint-baseline.xml"))
    }
}
