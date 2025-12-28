<p align="center">
  <img src="https://github.com/Shopkeepers/Shopkeepers-Wiki/wiki/images/logos/shopkeepers_logo_small_with_text.png?raw=true" alt="Shopkeepers logo"/>
</p>

Shopkeepers [![Build Status](https://github.com/Shopkeepers/Shopkeepers/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/Shopkeepers/Shopkeepers/actions/workflows/build.yml)
===========

Shopkeepers is a Paper/Spigot plugin that allows you to set up custom villager shopkeepers that sell exactly what you want them to sell and for what price. 
You can set up admin shops, which have infinite supply, and you can also set up player shops, which pull supply from a container.

**Requirements:**
- Paper/Spigot 1.21+ (Paper recommended)
- Java 21 or later

## Documentation

Comprehensive documentation is available in the `docs/` folder:

* **[Features Guide](docs/features.md)**: Detailed documentation of all Shopkeepers features
* **[Configuration Guide](docs/configuration.md)**: Complete configuration reference
* **[Usage Guide](docs/usage.md)**: User guide for commands, permissions, and common tasks

**BukkitDev**: https://dev.bukkit.org/projects/shopkeepers  
**Spigot**: https://www.spigotmc.org/resources/shopkeepers.80756/  
**Wiki**: https://github.com/Shopkeepers/Shopkeepers-Wiki/wiki  
**Language Files**: https://github.com/Shopkeepers/Language-Files/  
**Issues**: https://github.com/Shopkeepers/Shopkeepers/issues  
**Discord**: https://discord.gg/d9NKd5z  
**Source code**: https://github.com/Shopkeepers/Shopkeepers/  

Shopkeepers API
----------------

If you want to write an add-on plugin for Shopkeepers, or integrate some Shopkeepers aspect into your plugin, you can add `ShopkeepersAPI` as a dependency to your plugin.

Maven repository:

```
<repositories>
  <repository>
    <id>shopkeepers-repo</id>
    <url>https://raw.githubusercontent.com/Shopkeepers/Repository/main/releases/</url>
  </repository>
</repositories>
```

Snapshot repository (for testing development builds): `https://raw.githubusercontent.com/Shopkeepers/Repository/main/snapshots/`  

Old repository (mirrors the above repository; might stop working in the future): `https://repo.projectshard.dev/repository/releases/`  
Old snapshot repository: `https://repo.projectshard.dev/repository/snapshots/`  

If the above Maven repositories are currently not available for some reason, you can also try to use Jitpack: https://jitpack.io/#Shopkeepers/Shopkeepers/  
Jitpack snapshots: https://jitpack.io/#Shopkeepers/Shopkeepers/master-SNAPSHOT  
However, this is not guaranteed to always work.

Maven dependency:
```
<dependency>
  <groupId>com.nisovin.shopkeepers</groupId>
  <artifactId>ShopkeepersAPI</artifactId>
  <!-- Replace this with the latest Shopkeepers version -->
  <version>2.15.1</version>
  <scope>provided</scope>
</dependency>
```

There is no documentation yet on how to use the API. But the various API classes and interfaces have some code documentation. You can find those here: https://github.com/Shopkeepers/Shopkeepers/tree/master/modules/api/src/main/java/com/nisovin/shopkeepers/api  
As an entry point to other components, you can use the class [`ShopkeepersAPI`](https://github.com/Shopkeepers/Shopkeepers/blob/master/modules/api/src/main/java/com/nisovin/shopkeepers/api/ShopkeepersAPI.java). For example, `ShopkeepersAPI.getShopkeeperRegistry()` returns you the `ShopkeeperRegistry`, with which you can query the loaded shopkeepers.

The API may still be quite unstable: On every Minecraft release, as well as whenever some API or data breaking change is made, the `Major` component of the Shopkeepers version (the `15` in `v2.15.1`) is incremented (which occurs quite regularly). Most of the time, only some aspects of the API change, so your plugin might still work fine without changes. But nevertheless, be prepared to check for breaking API changes at least as frequently as Minecraft updates are released.

The API is still quite limited. For example, it is not yet possible to implement custom shopkeeper types via the API. And many aspects, for example related to how trades are processed, cannot be altered via the API.

If you only want to check if a given entity is a shopkeeper, there is no need to hook into the Shopkeepers API: Every shopkeeper entity is tagged with the `'shopkeeper'` metadata, so you can check for that via `entity.hasMetadata("shopkeeper")`.

## Installation

### For Server Owners

1. Download Shopkeepers from [Spigot](https://www.spigotmc.org/resources/shopkeepers.80756/) or [GitHub Releases](https://github.com/Shopkeepers/Shopkeepers/releases)
2. Place the Shopkeepers jar file in your server's `plugins` folder
3. Restart your server
4. Configure Shopkeepers via `plugins/Shopkeepers/config.yml`
5. See the [Configuration Guide](docs/configuration.md) for detailed setup instructions

### Building from Source

This section assumes that you have [Git](https://git-scm.com/) installed.

We use Gradle to compile and build the plugin. This repository comes with Bash scripts to automatically install the required versions of Gradle and the Java SDK, build the required Paper/Spigot dependencies, and then use Gradle to build Shopkeepers.

**Requirements:**
- Java 21 or later
- Git

To build Shopkeepers, execute the following commands from within a Bash console. If you are on Windows, you can install [Git-for-Windows](https://gitforwindows.org/) and then execute these commands from within the "Git Bash".

```bash
git clone https://github.com/Shopkeepers/Shopkeepers.git
cd Shopkeepers
./build.sh
```

Or using Gradle directly:

```bash
git clone https://github.com/Shopkeepers/Shopkeepers.git
cd Shopkeepers
./gradlew build
```

If everything went well, the `build` folder will contain:
- **Plugin jar**: The main plugin file to install on your server
- **API jar**: For plugin developers (more stable public API)
- **Main jar**: Internal plugin code for developers (less stable)

Unless you are a developer, you only need the plugin jar.

## Recent Improvements

Shopkeepers has been modernized with the following improvements:

* **Paper API**: Migrated from deprecated Bukkit/Spigot APIs to modern Paper API
* **Java 21+**: Updated to require Java 21 for better performance and modern language features
* **ServiceIO Integration**: Enhanced support for ServiceIO (modern Vault replacement)
* **Improved Architecture**: Better code organization and maintainability
* **Enhanced Compatibility**: Better integration with protection plugins (WorldGuard, Towny, etc.)

Pull Requests & Contributing
----------

To import the project into your favorite Java IDE, refer to your IDE's respective documentation on how to import Gradle projects. For example, in Eclipse you can find this under **Import > Gradle > Existing Gradle Project**. Follow the instructions to select the root Shopkeepers folder and import all the Gradle projects found by Eclipse.

The root project contains several module projects. The most important ones are:
* `main`: This contains the core plugin code.
* `api`: This contains all API code.
* And several modules for the NMS / CraftBukkit version specific code of the supported server versions.

Shopkeepers requires several Spigot and CraftBukkit dependencies. The easiest way to automatically build and install these dependencies into your local Maven repository is to run the included `./scripts/installSpigotDependencies.sh` script.

To build the project from within your IDE, refer to your IDE's respective documentation on how to build Gradle projects. For Eclipse, right-click the root project, select **Run As > Run configurations...**, and then set up a 'Gradle Task' run configuration that executes the intended Gradle build tasks.  
Some shortcuts have been defined for commonly used combinations of build tasks. For example, 'cleanBuild' will trigger a clean build and runs all tests. 'cleanInstall' will additionally install the built jars into your local Maven repository.  

**Note:** We require Java 21 or later to build.

For more information on creating pull requests and contributing code to the project see [Contributing](CONTRIBUTING.md).
