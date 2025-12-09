/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2025 SpoilerRules
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
}

val appVersionProvider: Provider<String> =
    providers
        .environmentVariable("APP_VERSION")
        .orElse(providers.gradleProperty("appVersion"))
        .orElse("0.0.0-dev")

val appDistributionProvider: Provider<String> =
    providers
        .environmentVariable("APP_DISTRIBUTION")
        .orElse(providers.gradleProperty("appDistribution"))
        .orElse(
            provider {
                val requestedTasks = gradle.startParameter.taskNames.flatMap { it.split(":") }

                when {
                    "packageReleaseMsi" in requestedTasks -> "MSI Installer (Minified Fat JAR)"
                    "packageReleaseExe" in requestedTasks -> "EXE Installer (Minified Fat JAR)"
                    "createReleaseDistributable" in requestedTasks -> "Portable (Minified Fat JAR)"
                    "packageReleaseUberJarForCurrentOS" in requestedTasks -> "Minified Fat JAR"
                    "packageMsi" in requestedTasks -> "MSI Installer (Fat JAR)"
                    "packageExe" in requestedTasks -> "EXE Installer (Fat JAR)"
                    "createDistributable" in requestedTasks -> "Portable (Fat JAR)"
                    "shadowJar" in requestedTasks -> "Fat JAR"
                    "buildWithProjectModules" in requestedTasks -> "JAR"
                    else -> "unspecified"
                }
            },
        )

val onlyWindowsX64Provider: Provider<Boolean> =
    providers
        .environmentVariable("ONLY_WINDOWS_X64")
        .map { it.toBoolean() }
        .orElse(providers.gradleProperty("onlyWindowsX64").map { it.toBoolean() })
        .orElse(
            provider {
                val requestedTasks = gradle.startParameter.taskNames.flatMap { it.split(":") }
                requestedTasks.any { task ->
                    task in
                        listOf(
                            "packageMsi",
                            "packageExe",
                            "createDistributable",
                            "packageReleaseMsi",
                            "packageReleaseExe",
                            "createReleaseDistributable",
                        )
                }
            },
        )

val appVersion = appVersionProvider.get()
val appDistribution = appDistributionProvider.get()
val onlyWindowsX64 = onlyWindowsX64Provider.get()

extra["appVersion"] = appVersion
extra["appDistribution"] = appDistribution
extra["onlyWindowsX64"] = onlyWindowsX64
version = appVersion

gradle.taskGraph.whenReady {
    if (appDistribution != "unspecified") {
        logger.lifecycle("Building distribution: $appDistribution")
    }
    if (onlyWindowsX64) {
        logger.lifecycle("Building for Windows x64 only (optimization enabled)")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://repo.azisaba.net/repository/maven-public/")
        maven("https://jitpack.io/")
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        apply(plugin = "com.github.gmazzo.buildconfig")

        extensions.getByType<BuildConfigExtension>().apply {
            useKotlinOutput {
                internalVisibility = false
            }
            packageName("com.spoiligaming.explorer.build")

            buildConfigField("VERSION", appVersion)
            buildConfigField("DISTRIBUTION", appDistribution)
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
