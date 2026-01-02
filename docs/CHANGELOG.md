# Shopkeepers Changelog

This document provides a summary of major changes and new features in Shopkeepers. For the complete changelog, see the main [CHANGELOG.md](../CHANGELOG.md) file.

## v2.26.0 (TBA)
### Supported MC versions: 1.21.11, 1.21.10, 1.21.8, 1.21.7, 1.21.6, 1.21.5

**Major Features:**
- **New Permission System**: Added `shopkeeper.create` permission for command-based shop creation
- **Pre-defined Shop Creation Items**: Shop creation items can now be configured with specific shop and object types
- **End Crystal Shops**: New shop object type using end crystals
- **Shop Closing Feature**: Shops can now be temporarily closed, preventing trading and hiring
- **Shop Information Display**: Editor now shows shopkeeper information (ID, UUID, name, type, location, owner)
- **Flying Mob Gravity**: Flying mobs (allay, bat, bee, etc.) can now be placed in the air without falling
- **World-Specific Settings**: Per-world configuration for villager and wandering trader settings

**Improvements:**
- Config migration to version 10
- Renamed settings: `silence-living-shop-entities` → `silence-shop-entities`, `mobe-behavior-tick-period` → `entity-behavior-tick-period`
- Better error handling for shopkeeper spawning failures
- Improved bat shopkeeper behavior (awake unless block above)

**API Changes:**
- Added `SelectableTypeRegistry#canBeSelected`

## v2.25.0 (2025-12-14)
### Supported MC versions: 1.21.11, 1.21.10, 1.21.8, 1.21.7, 1.21.6, 1.21.5

**Major Features:**
- **Trading History Command**: New `/shopkeeper history` command to view trading history
  - View your own trading history
  - View history of specific players or shops
  - Requires SQLite trade log storage
- **New Mob Types**: Support for camel husk, nautilus, zombie nautilus, and parched
- **Netherite Horse Armor**: Support for netherite horse armor in horse shopkeepers

**Breaking Changes:**
- Dropped support for Minecraft versions below 1.21.5
- Modernized plugin by building against newer Bukkit version
- Reduced plugin size by bundling fewer compatibility modules

**Improvements:**
- Fixed baby zombies mounting chickens
- Improved command argument parsing with better error messages
- Reduced trade log IO buffering delay (30s → 10s)
- Better handling of copper chests as shop containers

## v2.24.0 (2025-10-16)
### Supported MC versions: 1.21.10, 1.21.8, 1.21.7, 1.21.6, 1.21.5, 1.21.4, 1.21.3, 1.21.1, 1.21, 1.20.6

**Major Features:**
- **Copper Golem Shops**: New mob type with oxidation level control
- **Mannequin Shops**: New mob type with pose and skin customization
- **Armor Stand Shops**: New mob type with equipment and pose options
- **New Item Data Format**: Modernized config item format using Minecraft item type IDs
- **Shopkeeper Snapshots**: Create, list, restore, and remove shopkeeper snapshots

**Improvements:**
- Better item data matching using Minecraft's NBT comparison
- Improved config migration system
- Enhanced debug output for item checking

## Recent Version History

For detailed changelogs of older versions, see the main [CHANGELOG.md](../CHANGELOG.md) file.

### Key Features Across Versions

**Shop Types:**
- Admin shops (unlimited stock/money)
- Player shops (selling, buying, trading, book)
- Sign shops (wall signs, sign posts, hanging signs)
- Citizens NPC shops

**Shop Object Types:**
- Living entity shops (villagers, mobs, etc.)
- Sign shops
- Hanging sign shops
- End crystal shops (v2.26.0+)
- Citizens NPC shops

**Commands:**
- `/shopkeeper` - Main command with subcommands
- `/shopkeeper list` - List shopkeepers
- `/shopkeeper history` - View trading history (v2.25.0+)
- `/shopkeeper remove` - Remove shopkeepers
- `/shopkeeper give` - Give shop creation items
- `/shopkeeper snapshot` - Manage shopkeeper snapshots (v2.24.0+)
- And many more...

**Protection Features:**
- Container protection
- Shopkeeper protection
- Trade restrictions
- Shop limits per player
- Inactive player cleanup

**Integrations:**
- WorldGuard
- Towny
- Citizens
- Economy plugins (Vault, ServiceIO)
- And more...

## Migration Notes

When updating between major versions:
1. **Backup your data**: Always backup `save.yml` and `config.yml`
2. **Check config version**: Config will auto-migrate, but review changes
3. **Review permissions**: New permissions may be added
4. **Test on test server**: Test updates before applying to production
5. **Check compatibility**: Verify plugin integrations still work

For specific migration instructions, see the main CHANGELOG.md file.

