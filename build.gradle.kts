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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply true
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.ktlint) apply true
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://repo.azisaba.net/repository/maven-public/")
        maven("https://jitpack.io/")
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

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        debug.set(false)
        verbose.set(false)
        android.set(false)
        ignoreFailures.set(false)
        enableExperimentalRules.set(true)
        baseline.set(rootProject.file("ktlint-baseline.xml"))
    }
}
