# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the Wurst Client v7, a Fabric mod for Minecraft that adds various cheats and utilities. The mod is written in Java and uses the Fabric modding framework.

Key information:
- Minecraft version: 1.21.8
- Mod loader: Fabric
- Java version: 21
- License: GPL-3.0-or-later

## Common Development Commands

### Setup and Build Commands
- `./gradlew genSources eclipse` - Generate sources and setup for Eclipse
- `./gradlew genSources vscode` - Generate sources and setup for VSCode/Cursor
- `./gradlew build` - Build the project
- `./gradlew runClient` - Run the Minecraft client with the mod loaded

### Testing Commands
- `./gradlew test` - Run unit tests
- `./gradlew runEndToEndTest` - Run end-to-end tests

### Code Quality Commands
- `./gradlew spotlessCheck` - Check code formatting
- `./gradlew spotlessApply` - Apply code formatting
- `./gradlew check` - Run all verification tasks

## Code Architecture

### Core Structure
1. **Main Entry Point**: `WurstClient` enum (singleton) in `src/main/java/net/wurstclient/WurstClient.java`
2. **Features System**: 
   - Hacks (`src/main/java/net/wurstclient/hack/Hack.java` and implementations in `src/main/java/net/wurstclient/hacks/`)
   - Commands (`src/main/java/net/wurstclient/command/Command.java` and implementations in `src/main/java/net/wurstclient/commands/`)
   - Other Features (`src/main/java/net/wurstclient/other_feature/OtherFeature.java` and implementations in `src/main/java/net/wurstclient/other_features/`)

### Key Components
1. **Event System**: Event-driven architecture using listeners
2. **Settings System**: Each feature can have configurable settings (sliders, checkboxes, etc.)
3. **UI System**: ClickGUI and Navigator for user interface
4. **File Management**: JSON-based configuration files stored in `.minecraft/wurst/`

### Feature Implementation Pattern
1. Extend `Hack`, `Command`, or `OtherFeature`
2. Implement `onEnable()` and `onDisable()` methods for state changes
3. Add event listeners for functionality
4. Use settings system for configuration

### Testing
- Uses JUnit 5 for unit testing
- Tests are located in `src/test/java/`
- Run with `./gradlew test`

## Additional Development Information

### Internationalization (i18n)
- Translation files are located in `src/main/resources/assets/wurst/translations/`
- The main translation file is `zh_cn.json` for Chinese
- Automatic translation updates can be performed with `./gradlew i18nUpdater`

### Event System Details
- Events are managed through the `EventManager` class
- Features register listeners for specific events in their `onEnable()` method
- Events are fired automatically by the mod's mixins

### Settings System Details
- Settings are defined as fields in feature classes
- Various setting types are available (SliderSetting, CheckboxSetting, etc.)
- Settings are automatically saved to and loaded from JSON files