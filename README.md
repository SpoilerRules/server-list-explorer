# <p align="center">Server List Explorer</p>

<p align="center">
  <a href="https://github.com/SpoilerRules/server-list-explorer/releases/latest">
    <img alt="Release" src="https://img.shields.io/github/v/release/SpoilerRules/server-list-explorer?label=release&style=flat-square"/>
  </a>
  <a href="https://github.com/SpoilerRules/server-list-explorer/releases">
    <img alt="Downloads" src="https://img.shields.io/github/downloads/SpoilerRules/server-list-explorer/total?style=flat-square"/>
  </a>
  <a href="https://github.com/SpoilerRules/server-list-explorer/stargazers">
    <img alt="Stars" src="https://img.shields.io/github/stars/SpoilerRules/server-list-explorer?style=flat-square&logo=github"/>
  </a>
  <a href="https://www.codefactor.io/repository/github/SpoilerRules/server-list-explorer">
    <img alt="CodeFactor Grade" src="https://img.shields.io/codefactor/grade/github/SpoilerRules/server-list-explorer?style=flat-square"/>
  </a>
  <a href="https://github.com/SpoilerRules/server-list-explorer/blob/main/LICENSE">
    <img alt="License" src="https://img.shields.io/github/license/SpoilerRules/server-list-explorer?style=flat-square"/>
  </a>
</p>

<p align="center">
  <a href="https://kotlinlang.org">
    <img alt="Kotlin/JVM" src="https://img.shields.io/badge/Kotlin-JVM-007396?style=flat-square&colorA=7F52FF&colorB=007396&logo=kotlin&logoColor=white"/>
  </a>
  <a href="https://jb.gg/cmp">
    <img alt="Compose Desktop" src="https://img.shields.io/badge/Desktop-4CAF50.svg?style=flat&logo=jetpackcompose&logoColor=FFFFFF&labelColor=4CAF50&label=Compose&colorA=4CAF50&colorB=6A1B9A"/>
  </a>
  <a href="https://discord.gg/fVA5Wr6Nns">
    <img alt="Discord" src="https://dcbadge.limes.pink/api/server/https://discord.gg/fVA5Wr6Nns?style=flat-square"/>
  </a>
</p>

<p align="center">
  <a href="https://github.com/SpoilerRules/server-list-explorer/releases/latest">
    <img alt="Download latest" src="https://img.shields.io/badge/Download-Latest%20Release-blue?style=for-the-badge"/>
  </a>
</p>

<p align="center">
  <b>Server List Explorer</b> is a tool for managing
  your <a href="https://www.minecraft.net/">Minecraft: Java Edition</a> server list and, in the future, your single-player world list.
  A complete overview of the features can be found on the
  <a href="https://github.com/SpoilerRules/server-list-explorer/wiki/Feature-List">Feature List wiki page</a>.
</p>

## User Interface Preview

| [![Screenshot 1](https://i.imgur.com/sQzIVyL.png)](https://i.imgur.com/sQzIVyL.png) | [![Screenshot 2](https://i.imgur.com/s3yGMjq.png)](https://i.imgur.com/s3yGMjq.png)  |
|-------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------|
| [![Screenshot 3](https://i.imgur.com/nXdLGW1.png)](https://i.imgur.com/nXdLGW1.png) | [![Screenshot 4](https://i.imgur.com/eMH8Hq6.png)](https://i.imgur.com/eMH8Hq6.png)  |
| [![Screenshot 5](https://i.imgur.com/q3eO9L4.png)](https://i.imgur.com/q3eO9L4.png) | [![Screenshot 6](https://i.imgur.com/AGBC5Js.png)](https://i.imgur.com/AGBC5Js.png)  |
| [![Screenshot 7](https://i.imgur.com/zHec8SU.png)](https://i.imgur.com/zHec8SU.png) | [![Screenshot 8](https://i.imgur.com/KnhzN9P.png)](https://i.imgur.com/KnhzN9P.png)  |
| [![Screenshot 9](https://i.imgur.com/lPcWLLU.png)](https://i.imgur.com/lPcWLLU.png) | [![Screenshot 10](https://i.imgur.com/encWz5c.png)](https://i.imgur.com/encWz5c.png) |

## Table of Contents

- [User Interface Preview](#user-interface-preview)
- [How to Build the Project](#how-to-build-the-project)
- [Wiki](#wiki)

## How to Build the Project

This project is built using Gradle and requires **Java 21 or higher** to run. Follow the steps below to build and execute the application:

### 1. Build the Project

From the root of the project, run the following Gradle task to generate the **shadow JAR**:

```
./gradlew :app:shadowJar
```

This task will produce a **fat JAR** (a JAR containing all dependencies) that can be run independently.

### 2. Locate the JAR

Once the build completes, the generated JAR files can be found in the `app/build/libs/` directory:

- **Shadow JAR** (includes all dependencies):
  `ServerListExplorer-all.jar`
  This is the recommended JAR to run for ease of use.

- **Normal JAR** (without bundled dependencies):
  `ServerListExplorer.jar`
  Use this only if you plan to manage dependencies manually.

### 3. Run the Application

Run the shadow JAR using Java 21 or higher:

```
java -jar app/build/libs/ServerListExplorer-all.jar
```

If you prefer the normal JAR, make sure to provide all required dependencies on the classpath.

## Wiki

For detailed usage instructions and troubleshooting tips, please refer to
the [GitHub Wiki](https://github.com/SpoilerRules/server-list-explorer/wiki).
