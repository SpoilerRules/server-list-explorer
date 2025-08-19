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

package com.spoiligaming.explorer.util

import oshi.SystemInfo
import java.util.Locale

object OSUtils {
    private val systemInfo by lazy { SystemInfo() }
    private val os by lazy { systemInfo.operatingSystem }
    private val versionInfo
        get() = os.versionInfo

    sealed class OSType {
        data class Windows(
            val buildNumber: Int,
            val majorVersion: Int,
            val minorVersion: Int,
        ) : OSType()

        data class MacOS(val version: MacOSVersion) : OSType()

        data class Linux(val distro: String, val version: String, val codename: String) : OSType()

        object Other : OSType()
    }

    data class MacOSVersion(
        val major: Int,
        val minor: Int,
        val patch: Int = 0,
    ) : Comparable<MacOSVersion> {
        override fun compareTo(other: MacOSVersion) =
            when {
                this.major != other.major -> this.major - other.major
                this.minor != other.minor -> this.minor - other.minor
                else -> this.patch - other.patch
            }

        override fun toString() = "$major.$minor.$patch"
    }

    val currentOSType: OSType by lazy {
        val family = os.family.lowercase(Locale.getDefault())
        when {
            "windows" in family -> {
                val build = versionInfo.buildNumber.toIntOrNull() ?: -1
                val parts = versionInfo.version.split(".", limit = 2)
                val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
                OSType.Windows(buildNumber = build, majorVersion = major, minorVersion = minor)
            }

            "mac os x" in family || "macos" in family -> {
                val versionParts =
                    versionInfo.version
                        .split(".", limit = 3)
                        .mapNotNull { it.toIntOrNull() }
                val (maj, min, pat) =
                    when (versionParts.size) {
                        0 -> listOf(0, 0, 0)
                        1 -> listOf(versionParts[0], 0, 0)
                        2 -> listOf(versionParts[0], versionParts[1], 0)
                        else -> listOf(versionParts[0], versionParts[1], versionParts[2])
                    }
                OSType.MacOS(MacOSVersion(maj, min, pat))
            }

            "linux" in family || (
                os.family.isNotEmpty() &&
                    !("windows" in family || "mac" in family)
            ) -> {
                val distroName = os.family
                val distroVersion = versionInfo.version
                val codename = versionInfo.codeName.ifBlank { "unknown" }

                OSType.Linux(
                    distro = distroName,
                    version = distroVersion,
                    codename = codename,
                )
            }

            else -> OSType.Other
        }
    }

    val isWindows
        get() = currentOSType is OSType.Windows

    val isMacOS
        get() = currentOSType is OSType.MacOS

    val isLinux
        get() = currentOSType is OSType.Linux

    val windowsBuild
        get() = (currentOSType as? OSType.Windows)?.buildNumber ?: -1

    val windowsMajorVersion
        get() = (currentOSType as? OSType.Windows)?.majorVersion ?: -1

    fun isWindowsBuildOrGreater(minBuild: Int) = isWindows && windowsBuild >= minBuild

    /**
     * Windows 11 22H2+ (build 22621+) supports backdrop types
     */
    val supportsDwmBackdropTypes
        get() = isWindowsBuildOrGreater(22621)

    /**
     * Windows 11 (build 22000) or later for Window Corner Preference (rounded corners).
     */
    val supportsDwmCornerPreference
        get() = isWindowsBuildOrGreater(22000)

    val macOSVersion
        get() = (currentOSType as? OSType.MacOS)?.version ?: MacOSVersion(0, 0, 0)

    fun isMacOSVersionOrGreater(target: MacOSVersion) = isMacOS && macOSVersion >= target

    val linuxDistro
        get() = (currentOSType as? OSType.Linux)?.distro ?: ""

    val linuxVersion
        get() = (currentOSType as? OSType.Linux)?.version ?: ""

    val linuxCodename
        get() = (currentOSType as? OSType.Linux)?.codename ?: ""

    fun isLinuxDistroIn(supportedDistros: Collection<String>): Boolean {
        if (!isLinux) return false
        val current = linuxDistro.lowercase(Locale.getDefault())

        return supportedDistros.any {
            it.lowercase(Locale.getDefault()) == current
        }
    }

    val osSummary
        get() =
            when (val o = currentOSType) {
                is OSType.Windows ->
                    "Windows ${o.majorVersion}." +
                        "${o.minorVersion} " +
                        "(Build ${o.buildNumber})"

                is OSType.MacOS -> "macOS ${o.version}"
                is OSType.Linux -> "Linux ${o.distro} ${o.version} (${o.codename})"
                OSType.Other -> os.family.ifBlank { "Unknown OS" }
            }
}
