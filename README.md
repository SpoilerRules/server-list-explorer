# Server List Explorer

[![Compose Desktop](https://img.shields.io/badge/Desktop-4CAF50.svg?style=flat&logo=jetpackcompose&logoColor=FFFFFF&labelColor=4CAF50&label=Compose&colorA=4CAF50&colorB=6A1B9A)](https://jb.gg/cmp)

[![](https://dcbadge.limes.pink/api/server/https://discord.gg/fVA5Wr6Nns?style=flat)](https://discord.gg/fVA5Wr6Nns)

**Server List Explorer** is the ultimate tool for effortlessly managing
your [Minecraft: Java Edition](https://www.minecraft.net/) server list and, soon, your single-player world list. It has
a sleek, utilitarian design that follows [Material 3 Design](https://m3.material.io/) principles. Discover all the
features on our [Feature List on the wiki](https://github.com/SpoilerRules/server-list-explorer/wiki/Feature-List), and
check out the [User Interface Preview](#user-interface-preview) to see the project up close.

## User Interface Preview

<div>
  <img src="https://i.imgur.com/sQzIVyL.png" alt="Screenshot 1">
  <img src="https://i.imgur.com/s3yGMjq.png" alt="Screenshot 2">
  <img src="https://i.imgur.com/nXdLGW1.png" alt="Screenshot 3">
  <img src="https://i.imgur.com/eMH8Hq6.png" alt="Screenshot 4">
  <img src="https://i.imgur.com/SGZOFtL.png" alt="Screenshot 5">
  <img src="https://i.imgur.com/zHec8SU.png" alt="Screenshot 6">
  <img src="https://i.imgur.com/bsewslp.png" alt="Screenshot 7">
  <img src="https://i.imgur.com/lPcWLLU.png" alt="Screenshot 8">
  <img src="https://i.imgur.com/encWz5c.png" alt="Screenshot 9">
  <img src="https://i.imgur.com/g9WLE7A.gif" alt="GIF 1">
</div>

## Table of Contents

- [User Interface Preview](#user-interface-preview)
- [How to Run the Project](#how-to-run-the-project)
- [Wiki](#wiki)

## How to Run the Project

This project is built using Gradle and requires **Java 17 or higher** to run. Follow the steps below to build and execute the application:

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

Run the shadow JAR using Java 17 or higher:

```
java -jar app/build/libs/ServerListExplorer-all.jar
```

If you prefer the normal JAR, make sure to provide all required dependencies on the classpath.

## Wiki

For detailed usage instructions and troubleshooting tips, please refer to
the [GitHub Wiki](https://github.com/SpoilerRules/server-list-explorer/wiki).
