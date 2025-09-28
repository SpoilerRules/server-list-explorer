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

import java.io.FileOutputStream
import java.time.Year
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

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

    implementation(compose.runtime)
    listOf(
        compose.desktop.windows_x64,
        compose.desktop.windows_arm64,
        compose.desktop.linux_x64,
        compose.desktop.linux_arm64,
        compose.desktop.macos_x64,
        compose.desktop.macos_arm64,
    ).forEach { runtimeOnly(it) }
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
        attributes["Multi-Release"] = "true"
    }
}

tasks.shadowJar {
    archiveFileName.set("ServerListExplorer-all.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = mainFunction
        attributes["Multi-Release"] = "true"
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

        // include contents of that jar
        from({ project.zipTree(jarProvider.flatMap { it.archiveFile }) })
    }
}

val rawVersion = project.version.toString()
val (major, minor, patch, qualifier) =
    Regex("""(\d+)\.(\d+)(?:\.(\d+))?(?:-([\w.\-]+))?""")
        .matchEntire(rawVersion)
        ?.destructured
        ?: error("Version \"$rawVersion\" is not valid SemVer")

val build = patch.ifEmpty { "0" }
val numericWindowsVersion = listOf(major, minor, build).joinToString(".")
val windowsVersion =
    if (qualifier.isNotEmpty()) "$numericWindowsVersion-$qualifier" else numericWindowsVersion

require(major.toInt() in 0..255) { "Windows MAJOR must be <=255" }
require(minor.toInt() in 0..255) { "Windows MINOR must be <=255" }
require(build.toInt() in 0..65535) { "Windows BUILD must be <=65535" }

val startYear = 2025
val copyrightYears =
    startYear.let {
        val now = Year.now().value
        if (now > it) "$it–$now" else "$it"
    }

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

    nativeDistributions {
        packageName = "ServerListExplorer"

        windows {
            packageVersion = numericWindowsVersion
            msiPackageVersion = numericWindowsVersion
            exePackageVersion = windowsVersion
            console = false
        }

        description = "Minecraft server list explorer"
        copyright = "Copyright (C) $copyrightYears SpoilerRules"
        vendor = "SpoilerRules"
        licenseFile.set(rootProject.file("LICENSE"))

        targetFormats(
            org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
        )
    }

    buildTypes.release.proguard {
        optimize.set(false)
        obfuscate.set(false)
        configurationFiles.from(
            rootProject.file("proguard/base.pro"),
            rootProject.file("proguard/compose.pro"),
            rootProject.file("proguard/jna.pro"),
            rootProject.file("proguard/kotlinlogging.pro"),
            rootProject.file("proguard/kotlinx-serialization.pro"),
            rootProject.file("proguard/log4j.pro"),
            rootProject.file("proguard/HackedSelectionContainer.kt.pro"),
            rootProject.file("proguard/oshi.pro"),
            rootProject.file("proguard/slf4j.pro"),
        )
    }
}

@UntrackedTask(because = "Patches jars in place after Compose packaging.")
abstract class PatchComposeJarsTask : DefaultTask() {
    @get:InputDirectory
    abstract val jarsDir: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val mrSources: ConfigurableFileCollection

    @TaskAction
    fun run() {
        val dir = jarsDir.get().asFile
        val targets = dir.listFiles { f -> f.isFile && f.extension == "jar" }?.toList().orEmpty()
        if (targets.isEmpty()) {
            logger.lifecycle("No jars found under ${dir.absolutePath}")
            return
        }

        val sourceJars = mrSources.files.filter { it.isFile && it.extension == "jar" }

        fun copyEntries(
            src: File,
            out: JarOutputStream,
            seen: MutableSet<String>,
            filter: (String) -> Boolean,
        ) {
            JarFile(src).use { jf ->
                val it = jf.entries()
                while (it.hasMoreElements()) {
                    val e = it.nextElement()
                    val name = e.name
                    if (name == "META-INF/MANIFEST.MF") continue
                    if (!filter(name)) continue
                    if (!seen.add(name)) continue
                    val newEntry = JarEntry(name).also { je -> je.time = e.time }
                    try {
                        out.putNextEntry(newEntry)
                        if (!name.endsWith("/")) jf.getInputStream(e).use { inp -> inp.copyTo(out) }
                        out.closeEntry()
                    } catch (_: java.util.zip.ZipException) {
                        // ignore duplication
                    }
                }
            }
        }

        targets.forEach { jar ->
            val mf = JarFile(jar).use { it.manifest }
            val merged =
                Manifest().apply {
                    if (mf != null) mainAttributes.putAll(mf.mainAttributes)
                    mainAttributes.putValue("Multi-Release", "true")
                }

            val tmp = File(jar.parentFile, jar.nameWithoutExtension + "-mr.jar")
            val seen = HashSet<String>(INITIAL_SEEN_CAPACITY)

            JarOutputStream(FileOutputStream(tmp), merged).use { jos ->
                copyEntries(jar, jos, seen) { true }
                sourceJars.forEach { src ->
                    copyEntries(src, jos, seen) { it.startsWith("META-INF/versions/") }
                }
            }

            if (!jar.delete()) throw GradleException("Failed to delete ${jar.absolutePath}")
            if (!tmp.renameTo(
                    jar,
                )
            ) {
                throw GradleException("Failed to replace ${jar.name} with MR-patched jar")
            }
        }
    }

    companion object {
        private const val INITIAL_SEEN_CAPACITY = 8192
    }
}

val composeJarsDir = layout.buildDirectory.dir("compose/jars")
val runtimeCfg = configurations.getByName("runtimeClasspath")
val appJarProvider = tasks.named<Jar>("jar").flatMap { it.archiveFile }
val patchComposeJars =
    tasks.register<PatchComposeJarsTask>("patchComposeJars") {
        jarsDir.set(composeJarsDir)
        mrSources.setFrom(runtimeCfg, appJarProvider)
    }

tasks.configureEach {
    if (name.endsWith("UberJarForCurrentOS")) {
        finalizedBy(patchComposeJars)
    }
}
