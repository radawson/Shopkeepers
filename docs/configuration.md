# Shopkeepers Configuration Guide

This guide covers all configuration options available in Shopkeepers' `config.yml` file.

## Table of Contents

1. [General Settings](#general-settings)
2. [Shopkeeper Data](#shopkeeper-data)
3. [Plugin Compatibility](#plugin-compatibility)
4. [Shop Creation](#shop-creation)
5. [Shop Object Types](#shop-object-types)
6. [Naming](#naming)
7. [Editor Menu](#editor-menu)
8. [Trading](#trading)
9. [Protection](#protection)
10. [Other Settings](#other-settings)

## General Settings

### Debug and Metrics

```yaml
config-version: 9                    # Config version (don't edit manually)
debug: false                        # Enable debug mode
debug-options: []                   # Additional debug options
enable-metrics: true                 # Enable bStats metrics
```

**Debug Options:**
- `log-all-events`: Log all events
- `print-listeners`: Print registered listeners
- `shopkeeper-activation`: Debug shopkeeper activation
- `regular-tick-activities`: Debug regular activities
- `visualize-shopkeeper-ticks`: Visualize ticking activities
- `commands`: Debug command output
- `owner-name-updates`: Log owner name updates
- `item-migrations`: Log item migrations
- `item-updates`: Log item updates
- `item-conversions`: Log item conversions
- `empty-trades`: Log empty trade details
- `text-components`: Debug text components

### Language

```yaml
language: en-default                 # Language file to use
```

Find community translations at: https://github.com/Shopkeepers/Language-Files

## Shopkeeper Data

### Saving

```yaml
save-instantly: true                 # Save immediately on changes
```

When `false`, data saves every 5 minutes and on shutdown.

### Item Conversion

```yaml
convert-player-items: false         # Convert player items to Spigot format
convert-all-player-items: true      # Convert all items (true) or only exceptions (false)
convert-player-items-exceptions: [] # Item exceptions list
```

Helps with items created on older Spigot versions or via Minecraft mechanisms.

## Plugin Compatibility

### Server Assumptions

```yaml
ignore-failed-server-assumption-tests: false  # Ignore failed server tests
```

### Spawn Blocking

```yaml
bypass-spawn-blocking: true         # Bypass mob spawn blocking plugins
check-shop-interaction-result: false # Check interaction results
```

### WorldGuard

```yaml
enable-world-guard-restrictions: false        # Enable WorldGuard restrictions
require-world-guard-allow-shop-flag: false    # Require allow-shop flag
register-world-guard-allow-shop-flag: true    # Register allow-shop flag
```

### Towny

```yaml
enable-towny-restrictions: false    # Enable Towny restrictions
```

### Inventory Verification

```yaml
disable-inventory-verification: false  # Disable inventory verification
```

## Shop Creation

### Creation Item

```yaml
shop-creation-item:
  type: VILLAGER_SPAWN_EGG          # Item type for creating shops
  display-name: '{"text":"Shopkeeper","italic":false,"color":"green"}'
```

### Item Tagging

```yaml
add-shop-creation-item-tag: true    # Add NBT tag to creation items
identify-shop-creation-item-by-tag: true  # Identify by tag instead of item type
prevent-shop-creation-item-regular-usage: true  # Prevent normal item usage
```

### Selection Controls

```yaml
invert-shop-type-and-object-type-selection: false  # Invert selection controls
```

### Player Shops

```yaml
create-player-shop-with-command: false  # Allow command-based creation
require-container-recently-placed: true  # Container must be recently placed
max-container-distance: 15              # Max distance from container
deleting-player-shop-returns-creation-item: false  # Return item on deletion
```

### Shop Limits

```yaml
max-shops-per-player: -1           # Max shops per player (-1 = unlimited)
max-shops-perm-options: 5,15,25    # Permission-based limit options
```

Use permission `shopkeeper.maxshops.<count>` or `shopkeeper.maxshops.unlimited`.

## Protection

### Container Protection

```yaml
protect-containers: true            # Protect shop containers
prevent-item-movement: true         # Prevent hopper/item movement
delete-shopkeeper-on-break-container: false  # Delete shop on container break
```

### Inactive Players

```yaml
player-shopkeeper-inactive-days: 0  # Days of inactivity before cleanup (0 = disabled)
```

## Shop Object Types

### Living Shops

```yaml
enabled-living-shops:              # List of allowed mob types
  - VILLAGER
  - ALLAY
  # ... many more
```

### Mob Behavior

```yaml
disable-gravity: false             # Disable gravity for shopkeepers
gravity-chunk-range: 4             # Gravity range in chunks
mob-behavior-tick-period: 3         # Behavior update period (ticks)
```

### Mob-Specific Settings

```yaml
shulker-peek-if-player-nearby: true  # Shulker peek behavior
shulker-peek-height: 0.3             # Shulker peek height
slime-max-size: 5                     # Max slime size (1-10)
magma-cube-max-size: 5                # Max magma cube size (1-10)
silence-living-shop-entities: true    # Silence mob sounds
```

### Nameplates

```yaml
show-nameplates: true              # Show nameplates
always-show-nameplates: false       # Always show (not just when looking)
```

### Citizens Integration

```yaml
enable-citizen-shops: true         # Enable Citizens NPC shops
default-citizen-npc-type: 'PLAYER' # Default NPC type
set-citizen-npc-owner-of-player-shops: false  # Set NPC owner
citizen-npc-fluid-pushable: false  # NPC fluid pushable
cancel-citizen-npc-interactions: true  # Cancel NPC interactions
save-citizen-npcs-instantly: false # Save NPCs instantly
snapshots-save-citizen-npc-data: true  # Save NPC data in snapshots
delete-invalid-citizen-shopkeepers: false  # Auto-delete invalid NPC shops
```

### Sign Shops

```yaml
enable-sign-shops: true            # Enable sign shops
enable-sign-post-shops: true       # Enable sign posts
enable-hanging-sign-shops: true   # Enable hanging signs
enable-glowing-sign-text: true     # Enable glowing text
```

## Naming

### Name Validation

```yaml
name-regex: "[A-Za-z0-9 ]{3,25}"   # Name validation regex
```

**Examples:**
- `"[A-Za-z0-9 ]{3,25}"`: Default (letters, numbers, spaces, 3-25 chars)
- `"[A-Za-z0-9&§# ]{3,25}"`: Allow color codes
- `"[\\p{L}0-9 ]{3,25}"`: Any language letters
- `".*"`: Match everything
- `"(?i)(?=[a-z0-9 ]{3,25})(?!.*bitch|dick|ass).*"`: Filter bad words

### Naming Options

```yaml
naming-of-player-shops-via-item: false  # Name via item (hide editor option)
allow-renaming-of-player-npc-shops: false  # Allow renaming player NPC shops
```

## Editor Menu

### Placeholder Items

```yaml
selling-empty-trade-result-item: GRAY_STAINED_GLASS_PANE
selling-empty-trade-item1: GRAY_STAINED_GLASS_PANE
selling-empty-trade-item2: GRAY_STAINED_GLASS_PANE
selling-empty-item1: BARRIER
selling-empty-item2: BARRIER

buying-empty-trade-result-item: GRAY_STAINED_GLASS_PANE
buying-empty-trade-item1: GRAY_STAINED_GLASS_PANE
buying-empty-trade-item2: AIR
buying-empty-result-item: BARRIER
buying-empty-item2: AIR

trading-empty-trade-result-item: GRAY_STAINED_GLASS_PANE
trading-empty-trade-item1: GRAY_STAINED_GLASS_PANE
trading-empty-trade-item2: GRAY_STAINED_GLASS_PANE
trading-empty-result-item: BARRIER
trading-empty-item1: BARRIER
trading-empty-item2: BARRIER

book-empty-trade-result-item: GRAY_STAINED_GLASS_PANE
book-empty-trade-item1: GRAY_STAINED_GLASS_PANE
book-empty-trade-item2: GRAY_STAINED_GLASS_PANE
book-empty-item1: BARRIER
book-empty-item2: BARRIER
```

### Trade Pages

```yaml
max-trades-pages: 5                 # Max trade pages (max 10)
```

### Editor Items

```yaml
previous-page-item: WRITABLE_BOOK
next-page-item: WRITABLE_BOOK
current-page-item: WRITABLE_BOOK
trade-setup-item: PAPER
placeholder-item: PAPER             # Placeholder for missing items
name-item: NAME_TAG                 # Name button/item
move-item: ENDER_PEARL              # Move button
container-item: CHEST               # Container button
trade-notifications-item: BELL       # Trade notifications button
delete-item: BONE                   # Delete button
```

### Editor Options

```yaml
enable-all-equipment-editor-slots: false  # Enable all equipment slots
enable-moving-of-player-shops: true       # Allow moving player shops
enable-container-option-on-player-shop: true  # Container button in editor
```

## Trading

### Trade Restrictions

```yaml
prevent-trading-with-own-shop: true      # Prevent trading with own shop
prevent-trading-while-owner-is-online: false  # Prevent trading when owner online
use-strict-item-comparison: false        # Strict item matching
```

### Statistics

```yaml
increment-villager-statistics: false     # Increment villager stats
```

### Sounds

```yaml
simulate-villager-trading-sounds: true   # Villager trading sounds
simulate-villager-ambient-sounds: false  # Villager ambient sounds
simulate-wandering-trader-trading-sounds: true  # Wandering trader sounds
simulate-wandering-trader-ambient-sounds: false  # Wandering trader ambient
simulate-trading-sounds-only-for-the-trading-player: true  # Sounds only for trader

trade-succeeded-sound:
  sound: 'minecraft:ui.button.click'
  pitch: 2.0
  volume: 0.3

trade-failed-sound:
  sound: 'minecraft:block.barrel.close'
  pitch: 2.0
  volume: 0.5
```

### Tax

```yaml
tax-rate: 0                          # Tax percentage (0-100)
tax-round-up: false                  # Round tax up instead of down
```

## Trade Notifications

```yaml
notify-players-about-trades: false  # Notify players with permission
trade-notification-sound: ""        # Notification sound (empty = disabled)

notify-shop-owners-about-trades: true  # Notify shop owners
shop-owner-trade-notification-sound:
  sound: 'minecraft:entity.experience_orb.pickup'
  volume: 0.25
```

## Trade Log

```yaml
trade-log-storage: 'DISABLED'       # Storage type: DISABLED, SQLITE, CSV
trade-log-merge-duration-ticks: 300 # Merge duration for equal trades
trade-log-next-merge-timeout-ticks: 100  # Timeout for merge
log-item-metadata: false            # Log item metadata
```

## Currencies

```yaml
currency-item: EMERALD               # Primary currency item
high-currency-item: EMERALD_BLOCK    # Secondary currency (AIR to disable)
high-currency-value: 9               # Secondary currency value
high-currency-min-cost: 20           # Minimum cost for secondary currency
```

## Regular Villagers

```yaml
disable-other-villagers: false      # Prevent trading with regular villagers
block-villager-spawns: false        # Block villager spawning
disable-zombie-villager-curing: false  # Prevent curing zombie villagers
hire-other-villagers: false         # Allow hiring regular villagers
edit-regular-villagers: false       # Allow editing regular villagers
```

## Wandering Traders

```yaml
disable-wandering-traders: false    # Prevent trading with wandering traders
block-wandering-trader-spawns: false  # Block wandering trader spawning
hire-wandering-traders: false       # Allow hiring wandering traders
edit-regular-wandering-traders: false  # Allow editing wandering traders
```

## Hiring

```yaml
hire-item: EMERALD                   # Item for hiring
hire-other-villagers-costs: 1        # Cost to hire regular villagers
hire-require-creation-permission: true  # Require creation permission to hire
```

## Configuration Best Practices

1. **Backup First**: Always backup `config.yml` before changes
2. **Test Changes**: Test on a test server first
3. **Document Custom Settings**: Keep notes on why settings were changed
4. **Performance**: Adjust tick periods and ranges for performance
5. **Security**: Keep container protection enabled
6. **Gradual Changes**: Make changes gradually and test each one

## Reloading Configuration

Most settings require a server restart. Some can be reloaded with `/shopkeeper reload` (if supported).

**Note:** Shopkeeper data and some settings always require a restart.

## Troubleshooting

**Configuration Not Loading:**
- Check YAML syntax
- Check file encoding (UTF-8)
- Check file permissions

**Settings Not Taking Effect:**
- Restart the server
- Check server logs for errors
- Verify configuration syntax

**Performance Issues:**
- Increase `mob-behavior-tick-period`
- Reduce `gravity-chunk-range`
- Disable unused features
- Reduce trade log detail

For more help, see the [Usage Guide](usage.md) or check GitHub Issues.

