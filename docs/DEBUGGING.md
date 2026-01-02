# Debugging Shopkeepers

This guide covers debugging techniques for developing and troubleshooting Shopkeepers. For general user debugging commands, see the [Usage Guide](usage.md#debug-commands).

## Table of Contents

1. [Console Logging](#console-logging)
2. [Remote Debugging](#remote-debugging)
3. [Shopkeepers Debug Features](#shopkeepers-debug-features)
4. [IDE Setup](#ide-setup)
5. [Common Debugging Scenarios](#common-debugging-scenarios)

## Console Logging

### Using the Plugin Logger

The recommended way to print debug information is to use the plugin's logger:

```java
import com.nisovin.shopkeepers.util.logging.Log;

// Simple message
Log.info("Plugin initialized successfully");

// With exception
Log.severe("Error occurred!", exception);

// Debug messages (only shown when debug mode is enabled)
Log.debug("Debug information here");
Log.debug("item-updates", "Item was updated: " + item);
```

### Logger Levels

Different log levels can help you find messages in the console:

- `Log.info()` - General information (white/default)
- `Log.warning()` - Warnings (yellow in some consoles)
- `Log.severe()` - Errors (red in some consoles)
- `Log.debug()` - Debug messages (only when debug mode is enabled)

**Note:** Using `warning` level can help make your print statements stand out in colored consoles.

### Component Logger (Paper)

For Paper plugins, you can also use the Component Logger for Adventure components:

```java
plugin.getComponentLogger().debug(Component.text("Debug message"));
```

## Remote Debugging

Remote debugging allows you to connect a debugger from your IDE to a running server, enabling breakpoints, variable inspection, and step-through debugging.

### Setting Up Remote Debugging

#### IntelliJ IDEA

1. **Create Remote Debug Configuration:**
   - Click the dropdown next to the run button
   - Select `Edit Configurations...`
   - Click `+` and select `Remote JVM Debug`
   - Name it (e.g., "Shopkeepers Remote Debug")
   - Click `Apply`

2. **Copy Command Line Arguments:**
   - IntelliJ will show command line arguments like:
     ```
     -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
     ```
   - Copy these arguments

3. **Add to Server Startup Script:**
   - Add the arguments after `java` and before `-jar`:
     ```bash
     java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar paper-1.21.11.jar nogui
     ```

4. **Connect Debugger:**
   - Start your server with the debug arguments
   - In IntelliJ, click the bug icon (🐛) in the top right
   - The debugger will connect to the running server

#### Eclipse

1. **Create Remote Debug Configuration:**
   - Go to `Run > Debug Configurations...`
   - Right-click `Remote Java Application` and select `New`
   - Set:
     - **Name:** Shopkeepers Remote Debug
     - **Project:** Shopkeepers
     - **Connection Type:** Standard (Socket Attach)
     - **Host:** localhost
     - **Port:** 5005
   - Click `Apply`

2. **Add Debug Arguments to Server:**
   - Same as IntelliJ: add `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005` to server startup

3. **Connect:**
   - Start server with debug arguments
   - Run the debug configuration in Eclipse

### Using Breakpoints

Once connected, you can:

1. **Set Breakpoints:**
   - Click on the line number in your IDE
   - Code will pause when it reaches that line

2. **Inspect Variables:**
   - View variable values in the current scope
   - Evaluate expressions in the debugger

3. **Step Through Code:**
   - **Step Over (F8)**: Execute current line, move to next
   - **Step Into (F7)**: Enter method calls
   - **Step Out (Shift+F8)**: Exit current method
   - **Resume (F9)**: Continue execution

### Direct Debugging

For a more integrated experience, you can run the server directly from your IDE using JPenilla's Gradle plugin. See the [Paper documentation](https://docs.papermc.io/paper/dev/debugging/) for setup instructions.

## Shopkeepers Debug Features

Shopkeepers includes built-in debugging features accessible via commands and configuration.

### Debug Mode

Enable debug mode to see additional logging:

**Command:**
```
/shopkeeper debug
```

**Config:**
```yaml
debug: true
```

### Debug Options

Shopkeepers supports specific debug options for different subsystems:

**Command:**
```
/shopkeeper debug <option>
```

**Available Options:**
- `log-all-events` - Logs all Bukkit events
- `print-listeners` - Prints registered listeners for each event
- `shopkeeper-activation` - Debug shopkeeper activation/deactivation
- `regular-tick-activities` - Debug regular shopkeeper activities
- `visualize-shopkeeper-ticks` - Visualize ticking activities in-game
- `commands` - Debug command parsing and execution
- `owner-name-updates` - Log shop owner name updates
- `item-migrations` - Log item migrations
- `item-updates` - Log item updates via UpdateItemEvent
- `empty-trades` - Log detailed info for empty trade slots
- `text-components` - Debug text component creation
- `unsafe-teleports` - Debug unsafe teleport detection

**Config:**
```yaml
debug-options:
  - shopkeeper-activation
  - commands
  - item-updates
```

### Debug Commands

**Check Shopkeepers:**
```
/shopkeeper check [chunks|active]
```
- Shows shopkeeper statistics and status
- `chunks` - Information about chunk loading
- `active` - Information about active shopkeepers

**Check Item:**
```
/shopkeeper checkItem
```
- Checks held item for shop creation item tags
- Useful for debugging item creation issues

## IDE Setup

### Importing the Project

1. **IntelliJ IDEA:**
   - `File > Open` → Select the Shopkeepers root directory
   - IntelliJ will detect it as a Gradle project
   - Click `Import Gradle Project`

2. **Eclipse:**
   - `File > Import > Gradle > Existing Gradle Project`
   - Select the Shopkeepers root directory
   - Follow the import wizard

### Building from IDE

**IntelliJ IDEA:**
- Right-click `build.gradle.kts` → `Run Gradle Task`
- Or use the Gradle tool window

**Eclipse:**
- Right-click project → `Run As > Gradle Build`
- Or create a Gradle Task run configuration

### Running Tests

Tests can be run from the IDE or command line:

```bash
./gradlew test
```

## Common Debugging Scenarios

### Debugging Shopkeeper Creation

1. Enable debug mode: `/shopkeeper debug`
2. Enable relevant debug options:
   ```
   /shopkeeper debug commands
   /shopkeeper debug shopkeeper-activation
   ```
3. Attempt to create a shopkeeper
4. Check console for detailed logs

### Debugging Trading Issues

1. Enable debug options:
   ```
   /shopkeeper debug item-updates
   /shopkeeper debug empty-trades
   ```
2. Attempt the trade
3. Check console for item information and trade details

### Debugging Performance Issues

1. Use the `check` command:
   ```
   /shopkeeper check active
   ```
2. Enable `regular-tick-activities` to see what's happening each tick
3. Use a profiler (e.g., JProfiler, VisualVM) with remote debugging

### Debugging Configuration Issues

1. Check config version compatibility
2. Enable `item-migrations` to see if items are being migrated
3. Use `/shopkeeper checkItem` to verify item data

## Tips

1. **Use Debug Options Sparingly:** Some debug options can generate a lot of output. Enable only what you need.

2. **Remote Debugging for Production:** Be careful when using remote debugging on production servers. It can impact performance.

3. **Log Levels:** Use appropriate log levels. Don't use `severe` for informational messages.

4. **Breakpoints in Static Initializers:** Be careful with breakpoints in static initializers - they can cause issues during class loading.

5. **Paper Component Logger:** When using Paper's Component Logger, remember it uses Adventure components, not plain strings.

## Additional Resources

- [Paper Debugging Documentation](https://docs.papermc.io/paper/dev/debugging/)
- [Shopkeepers Usage Guide](usage.md) - User-facing debug commands
- [Shopkeepers Configuration Guide](configuration.md) - Debug configuration options

