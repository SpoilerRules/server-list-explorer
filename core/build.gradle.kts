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
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":settings"))
    implementation(project(":nbt"))

    implementation(libs.kotlinx.coroutines.core.jvm)
    implementation(libs.kotlinx.serialization.json)

    runtimeOnly(libs.bungeecord.api) // required for mcServerPing
    implementation(libs.mcServerPing) {
        exclude(group = "net.md-5", module = "bungeecord-api")
    }

    implementation(libs.ktor.client.core.jvm)
    implementation(libs.ktor.client.cio.jvm)
    testImplementation(libs.ktor.client.core.jvm)
    testImplementation(libs.ktor.client.cio.jvm)
    testImplementation("io.ktor:ktor-client-mock:3.2.3")

    implementation(libs.oshi.core)
}
