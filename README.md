# Server List Explorer

[![Compose Desktop](https://img.shields.io/badge/Desktop-4CAF50.svg?style=flat&logo=jetpackcompose&logoColor=FFFFFF&labelColor=4CAF50&label=Compose&colorA=4CAF50&colorB=6A1B9A)](https://jb.gg/cmp)

**Server List Explorer** is your ultimate tool for effortlessly managing your [Minecraft: Java Edition](https://www.minecraft.net/) server list. With a sleek, modern user interface inspired by [Maple](https://maple.software/) and built using [Compose Multiplatform](https://jb.gg/cmp), it combines both functionality and visual appeal. Discover the full range of features in our [Feature List on the wiki](https://github.com/SpoilerRules/server-list-explorer/wiki/Feature-List), and check out the [User Interface Preview](#user-interface-preview) section to see our innovative design up close.

## User Interface Preview

<details>
  <summary>View Screenshots</summary>
  <p align="center">
    <img src="https://i.imgur.com/6XNfIMI.png" width="500" alt="Screenshot 1">
    <img src="https://i.imgur.com/vxgseoU.png" width="500" alt="Screenshot 2">
  </p>
</details>

## Table of Contents

- [User Interface Preview](#user-interface-preview)
- [Getting Started](#getting-started)
    - [Available Versions](#available-versions)
    - [Steps to Run](#steps-to-run)
- [Important Note for Users](#important-note-for-users)
- [Wiki](#wiki)
- [Contribution](#contribution)


## Getting Started

Getting started with Server List Explorer is straightforward. Follow these steps to get up and running:

1. **Install Java**: Ensure you have Java 11 runtime (or a later version) installed. If not, download and install it from the [official Java website](https://www.oracle.com/java/technologies/downloads/#java21).

2. **Download Server List Explorer**: Head to our [releases page](https://github.com/SpoilerRules/server-list-explorer/releases/latest) and download the version that best fits your needs.

### Available Versions

| Version Name                      | Description                                                                           | Supported Platforms                                   | Recommended |
|-----------------------------------|---------------------------------------------------------------------------------------|-------------------------------------------------------|-------------|
| `ServerListExplorer.jar`          | A ready-to-run executable. Not minified or obfuscated.                                | Windows (x64), Linux (x64, arm64), macOS (x64, arm64) | No          |
| `ServerListExplorer-minified.jar` | A ready-to-run executable. Obfuscated and minified using ProGuard for a smaller size. | Windows (x64), Linux (x64, arm64), macOS (x64, arm64) | Yes         |

### Steps to Run

1. **Locate the Downloaded File**: After downloading the appropriate version, find the `.jar` file in your downloads folder.

2. **Run the Application**:
    - **On Windows**: Double-click the `.jar` file to launch it.
    - **On Linux/Mac**: Open a terminal, navigate to the directory where the `.jar` file is located, and run:
      ```sh
      java -jar ServerListExplorer-minified.jar -XX:+UseStringDeduplication
      ```

And you’re all set! You can now start exploring and managing your Minecraft server list with ease.

For a more detailed and technical guide, including advanced instructions, visit our [Installation Wiki Page](https://github.com/SpoilerRules/server-list-explorer/wiki/Installation).

For any issues or further assistance, feel free to join [our Discord community](https://discord.gg/fVA5Wr6Nns) or visit the [GitHub issues page](https://github.com/SpoilerRules/server-list-explorer/issues).

## Important Note for Users

Base64 icon values copied from https://nbt.mcph.to/ or https://irath96.github.io/webNBT/ are **not valid**. Please ensure you use valid icon values to ensure proper functionality.

## Wiki

For detailed usage instructions and troubleshooting tips, please refer to the [GitHub Wiki](https://github.com/SpoilerRules/server-list-explorer/wiki).

## Contribution

We welcome contributions to the project and encourage adherence to idiomatic and concise code practices.

If your contribution involves user interface design changes, please consult with the lead developer on our [Discord server](https://discord.gg/fVA5Wr6Nns) to ensure your design aligns with the [Maple](https://maple.software/) design principles. However, if you're [Mikhail Cherepanov](https://github.com/mrflashstudio), the founder of [Maple](https://maple.software/), feel free to submit pull requests at your discretion.
