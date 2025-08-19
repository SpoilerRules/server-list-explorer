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

plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":settings"))
    implementation(project(":nbt"))

    // Compose Desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    // Navigation Compose
    implementation(libs.navigation.compose)

    // UI Components
    implementation(libs.materialKolor)
    implementation(libs.color.picker)
    implementation(libs.file.kit)
    implementation(libs.composeShimmer)
    implementation(libs.reorderable.jvm)

    // JNA for DWM
    implementation(libs.jna)
    implementation(libs.jna.platform)

    // System theme detection
    implementation(libs.theme.detector) {
        exclude(group = "net.java.dev.jna", module = "jna")
        exclude(group = "net.java.dev.jna", module = "jna-platform")
    }

    // Resources
    implementation(compose.components.resources)
}

compose {
    resources {
        generateResClass = always

        customDirectory(
            sourceSetName = "main",
            directoryProvider =
                provider {
                    layout.projectDirectory.dir("src/main/composeResources")
                },
        )
    }
}
