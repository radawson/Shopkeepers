# Shopkeepers Project Structure

This document provides a comprehensive overview of the Shopkeepers project structure, module organization, and build system.

## Table of Contents

1. [Project Overview](#project-overview)
2. [Module Structure](#module-structure)
3. [Build System](#build-system)
4. [File Organization](#file-organization)
5. [Build Configuration Files](#build-configuration-files)
6. [Key Directories](#key-directories)

## Project Overview

Shopkeepers is a multi-module Gradle project that builds a Paper/Spigot plugin for Minecraft. The project uses a modular architecture to separate concerns and enable better maintainability.

**Project Type:** Multi-module Gradle project  
**Build System:** Gradle with Kotlin DSL  
**Java Version:** 21+  
**Target Platform:** Paper 1.21.11+

## Module Structure

### Root Project (`/`)

The root project serves as the main build orchestrator and produces the final plugin JAR.

**Purpose:**
- Coordinates all submodules
- Builds the final shadowed and remapped plugin JAR
- Manages project-wide settings (version, group, etc.)
- Processes resources from all modules

**Key Files:**
- `build.gradle.kts` - Main build configuration
- `gradle.properties` - Project-wide properties (version, group, etc.)
- `settings.gradle.kts` - Project settings and module inclusion

### Module: `main` (`modules/main/`)

**Purpose:** Contains the core plugin implementation code.

**Responsibilities:**
- Main plugin class (`SKShopkeepersPlugin`)
- Bootstrapper and loader for Paper plugin structure
- Command system (Brigadier-based)
- Shopkeeper management and registry
- Trading system
- UI system
- Storage system
- Integration with optional dependencies (WorldGuard, Towny, Citizens, etc.)

**Key Components:**
- `SKShopkeepersPlugin.java` - Main plugin class
- `SKShopkeepersBootstrap.java` - Paper plugin bootstrapper
- `SKShopkeepersLoader.java` - Paper plugin loader
- `commands/` - Command implementations
- `shopkeeper/` - Shopkeeper core logic
- `shopobjects/` - Shop object implementations (entities, blocks, citizens)
- `ui/` - User interface system
- `storage/` - Data persistence
- `trading/` - Trading mechanics
- `config/` - Configuration management

**Build Output:** Shadowed JAR (included in final build)

### Module: `api` (`modules/api/`)

**Purpose:** Provides the public API for other plugins to interact with Shopkeepers.

**Responsibilities:**
- Public API interfaces and classes
- API documentation
- Stable API contracts for plugin developers

**Key Components:**
- `api/shopkeeper/` - Shopkeeper API interfaces
- `api/types/` - Type system API
- `api/events/` - Event API
- `api/internal/` - Internal API (for advanced use cases)

**Build Output:** `ShopkeepersAPI.jar` - Published separately for plugin developers

**Artifact ID:** `ShopkeepersAPI` (from `modules/api/gradle.properties`)

### Module: `external-annotations` (`modules/external-annotations/`)

**Purpose:** Provides Eclipse External Annotations (EEA) for null analysis.

**Responsibilities:**
- Null safety annotations for external libraries (Bukkit/Spigot API)
- Improves static analysis and IDE warnings
- Used by Checker Framework for null checking

**Key Components:**
- `src/main/resources/` - EEA files (`.eea` format)

**Build Output:** JAR with annotations (used during compilation)

**Artifact ID:** `ShopkeepersExternalAnnotations`

### Module: `v1_21_11` (`modules/v1_21_11/`)

**Purpose:** Version-specific compatibility code for Minecraft 1.21.11.

**Responsibilities:**
- NMS (Net Minecraft Server) compatibility layer
- Version-specific implementations
- Handles differences between Minecraft versions

**Key Components:**
- Compatibility provider implementation
- Version-specific utilities

**Build Output:** Shaded into final plugin JAR (not published separately)

**Note:** Uses Paperweight for version-specific mappings

### Module: `shared` (`modules/shared/`)

**Purpose:** Shared build scripts and helper functions used across modules.

**Responsibilities:**
- Common Gradle build functions
- Shared build configuration
- Reusable build logic

**Key Files:**
- `helperFunctions.gradle.kts` - Common Gradle functions (JAR configuration, Maven publishing, etc.)
- `nmsModulePaper.gradle.kts` - Build script for NMS/compatibility modules

**Note:** Not a source module - contains only build scripts

### Module: `test` (`modules/test/`)

**Purpose:** Test utilities and test code (if present).

**Note:** May not be actively used in current build

### Module: `dist` (`modules/dist/`)

**Purpose:** Distribution and packaging utilities (if present).

**Note:** May contain scripts for creating distributions

## Build System

### Build Process Flow

1. **Root Project Configuration**
   - Reads `gradle.properties` for version and project settings
   - Sets up source sets to include all module sources
   - Configures Paperweight for Minecraft version handling

2. **Module Compilation**
   - Each module compiles independently
   - API module produces `ShopkeepersAPI.jar`
   - Main module produces shadowed JAR (includes dependencies)

3. **Resource Processing**
   - Root project processes `plugin.yml` and `paper-plugin.yml` from main module
   - Version expansion happens during resource processing

4. **Final Assembly**
   - Root project creates shadow JAR (includes all dependencies)
   - Remaps JAR using Paperweight (Mojang mappings)
   - Outputs final plugin JAR: `Shopkeepers-{version}-paper.jar`

### Build Tasks

**Key Tasks:**
- `build` - Compiles all modules and creates final JAR
- `shadowJar` - Creates shadowed JAR with dependencies
- `reobfJar` - Remaps JAR to production mappings
- `assemble` - Assembles all artifacts
- `incrementPatchVersion` - Increments patch version in `gradle.properties`

## File Organization

### Root Level Files

```
Shopkeepers/
├── build.gradle.kts          # Main build configuration
├── gradle.properties         # Project-wide properties
├── settings.gradle.kts       # Project settings
├── gradlew                   # Gradle wrapper (Unix)
├── gradlew.bat               # Gradle wrapper (Windows)
├── README.md                 # Project readme
├── LICENSE                   # License file
├── TODO.txt                  # Development TODO list
└── docs/                     # Documentation
    ├── PROJECT_MAP.md        # This file
    ├── CHANGELOG.md          # Version changelog
    ├── features.md            # Features documentation
    ├── usage.md              # Usage guide
    └── configuration.md       # Configuration reference
```

### Module Structure

```
modules/
├── main/                     # Core plugin implementation
│   ├── build.gradle.kts
│   ├── gradle.properties
│   └── src/main/
│       ├── java/            # Java source code
│       └── resources/       # Resources (configs, plugin.yml, etc.)
├── api/                     # Public API
│   ├── build.gradle.kts
│   ├── gradle.properties
│   └── src/main/
│       ├── java/
│       └── resources/
├── external-annotations/    # Null analysis annotations
│   ├── build.gradle.kts
│   ├── gradle.properties
│   └── src/main/resources/  # EEA files
├── v1_21_11/               # Version compatibility
│   └── build.gradle.kts
├── shared/                 # Shared build scripts
│   ├── helperFunctions.gradle.kts
│   └── nmsModulePaper.gradle.kts
├── test/                   # Test utilities
└── dist/                   # Distribution scripts
```

## Build Configuration Files

### Root `build.gradle.kts`

**Purpose:** Main project build configuration

**Key Responsibilities:**
- Plugin declarations (Java, Shadow, Paperweight, Checker Framework)
- Repository configuration
- Global dependencies (Paper dev bundle, optional dependencies)
- Source set configuration (includes all module sources)
- Shadow JAR configuration
- Reobfuscation (Paperweight remapping)
- Resource processing (plugin.yml, paper-plugin.yml)
- Version management

**Note:** This is a single-module build that includes sources from submodules, not a traditional multi-project build.

### Module `build.gradle.kts` Files

Each module has its own `build.gradle.kts` for module-specific configuration:

- **`modules/main/build.gradle.kts`**: Main module dependencies, shadow JAR, resource processing
- **`modules/api/build.gradle.kts`**: API module dependencies, Javadoc generation, Maven publishing
- **`modules/external-annotations/build.gradle.kts`**: Annotations module configuration
- **`modules/v1_21_11/build.gradle.kts`**: Applies NMS module script, version-specific setup

### `gradle.properties` Files

**Root `gradle.properties`:**
- `version=2.26.9` - Project version
- `group=com.nisovin.shopkeepers` - Maven group ID
- `org.gradle.parallel=true` - Parallel build execution
- System properties for locale

**Module `gradle.properties`:**
- `artifactId={ModuleName}` - Maven artifact ID for each module
  - `modules/main/gradle.properties`: `artifactId=ShopkeepersMain`
  - `modules/api/gradle.properties`: `artifactId=ShopkeepersAPI`
  - `modules/external-annotations/gradle.properties`: `artifactId=ShopkeepersExternalAnnotations`

### `settings.gradle.kts`

**Purpose:** Defines project name and module inclusion

**Content:**
```kotlin
rootProject.name = "shopkeepers"
```

**Note:** Modules are included via source sets in root `build.gradle.kts`, not as separate Gradle projects.

## Key Directories

### Source Code Organization (`modules/main/src/main/java/com/nisovin/shopkeepers/`)

- **`commands/`** - Command implementations (Brigadier-based)
- **`compat/`** - Compatibility layer and version-specific code
- **`config/`** - Configuration loading and management
- **`container/`** - Container protection and management
- **`currency/`** - Currency system
- **`debug/`** - Debug utilities and tools
- **`dependencies/`** - Optional dependency integrations (WorldGuard, Towny, etc.)
- **`input/`** - User input handling (chat, interactions)
- **`internals/`** - Internal API implementation
- **`items/`** - Item handling and serialization
- **`lang/`** - Language and message system
- **`metrics/`** - bStats integration
- **`moving/`** - Shopkeeper movement/placement
- **`naming/`** - Shopkeeper naming system
- **`playershops/`** - Player shop management
- **`shopcreation/`** - Shopkeeper creation logic
- **`shopkeeper/`** - Core shopkeeper implementation
- **`shopobjects/`** - Shop object types (entities, blocks, citizens)
- **`storage/`** - Data persistence and storage
- **`text/`** - Text formatting and components
- **`tradelog/`** - Trade logging and history
- **`tradenotifications/`** - Trade notification system
- **`trading/`** - Trading mechanics
- **`types/`** - Type system (shop types, object types)
- **`ui/`** - User interface system (editors, trading UI)
- **`user/`** - User management
- **`util/`** - Utility classes
- **`villagers/`** - Regular villager editing
- **`world/`** - World-related utilities

### Resources (`modules/main/src/main/resources/`)

- **`plugin.yml`** - Bukkit plugin descriptor (permissions, metadata)
- **`paper-plugin.yml`** - Paper plugin descriptor (bootstrapper, loader, dependencies)
- **`config.yml`** - Default configuration file
- **`lang/`** - Language files (English, German, etc.)

## Build Output

### Final Artifacts

After building, the `build/` directory contains:

- **`Shopkeepers-{version}-paper.jar`** - Final plugin JAR (remapped, shadowed)
- **`ShopkeepersAPI-{version}.jar`** - API JAR for plugin developers
- **`ShopkeepersAPI-{version}-javadoc.jar`** - API Javadoc

### Build Process Notes

1. **Source Inclusion**: The root project includes sources from multiple modules via source sets, creating a single unified build rather than separate module builds.

2. **Resource Processing**: Both root and main module have `processResources` tasks. The root task processes resources for the final JAR, while the module task may be used if the module is built independently (though currently the module's JAR task is disabled).

3. **Version Management**: Version is defined in root `gradle.properties` and propagated to subprojects via `rootProject.extra["pluginVersion"]`.

4. **Paper Plugin Structure**: The project uses Paper's plugin structure with:
   - `paper-plugin.yml` for Paper-specific configuration
   - Bootstrapper (`SKShopkeepersBootstrap`) for early initialization
   - Loader (`SKShopkeepersLoader`) for classpath configuration
   - Commands registered via Brigadier (not in `plugin.yml`)

## Potential Redundancies

### Resource Processing

**Observation:** Both root `build.gradle.kts` and `modules/main/build.gradle.kts` have `processResources` tasks that process `plugin.yml` and `paper-plugin.yml`.

**Analysis:**
- Root project includes main module resources in its source sets
- Root's `processResources` will process these files for the final JAR
- Module's `processResources` may be redundant unless the module is built independently

**Recommendation:** The module's `processResources` is likely redundant since the root project handles resource processing. However, it may be kept for potential future use if the module needs to be built independently.

## Development Workflow

### Building the Project

```bash
# Build everything
./gradlew build

# Build only the plugin JAR
./gradlew shadowJar reobfJar

# Build API separately
./gradlew :shopkeepers-api:build
```

### IDE Setup

1. Import as Gradle project
2. Gradle will automatically configure source sets
3. All modules' sources will be available in the IDE
4. Use root project as the main project entry point

### Version Management

- Version is stored in `gradle.properties` at root
- Automatically incremented after successful builds (patch version)
- Propagated to all modules via `rootProject.extra["pluginVersion"]`
- Used in resource processing for `plugin.yml` and `paper-plugin.yml`

## Additional Notes

- The project uses **Kotlin DSL** for all Gradle build files (`.gradle.kts`)
- The old Groovy `build.gradle` file in `modules/main/` was removed during Paper plugin migration
- All modules use the same version from root `gradle.properties`
- The project structure follows Paper plugin best practices with bootstrapper and loader

