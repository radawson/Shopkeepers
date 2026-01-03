# Shopkeepers TODO

This document tracks planned improvements, features, and tasks for Shopkeepers.

## High Priority

_(Currently empty)_

## Mid Priority

### ✅ Ambiguous Shopkeeper Error Message

**Status:** Completed

For any commands allowing for a targeted shopkeeper: Print 'Ambiguous shopkeeper. Specify the shopkeeper explicitly instead.' error in case more than one shopkeeper is targeted, instead of either only using the first shopkeeper, or applying the operation to all of them.

## Low Priority

### Config Key Consistency

**Status:** Partially Completed

Make all config keys consistent: Avoid spaces (e.g., 'owner uuid' → 'owner-uuid') and use hyphens rather than camelCase.

- ✅ Changed 'owner uuid' to 'owner-uuid'
- ⏳ Review other config keys for consistency

### German Translation

Rename all occurrences of 'Shop/Shops' with 'Laden/Läden/Händler'?

### ✅ Command Library Revamp

**Status:** Completed

The command system has been completely rewritten to use Paper's native Brigadier Command API:

- **Brigadier Integration**: All commands now use Minecraft's Brigadier command framework via Paper's API
- **Client-side Validation**: Commands are validated on the client before being sent, providing immediate feedback
- **Rich Suggestions**: Tab-completion suggestions are provided by the server with proper context awareness
- **Natural Command Trees**: Subcommands and arguments follow Brigadier's natural tree structure
- **Permission-based Visibility**: Commands are hidden from players who don't have permission to use them
- **Custom Argument Types**: Implemented custom argument types for shopkeepers, shop types, players, etc.

The new command system is located in `commands/brigadier/` with:
- `BrigadierCommandManager`: Handles command registration via Paper's Lifecycle API
- `ShopkeepersCommandTree`: Builds the complete command tree
- `arguments/`: Custom Brigadier argument types with suggestions
- `executors/`: Command execution logic for each subcommand

The old custom command library has been fully removed. Command utilities are now in `commands/util/`.

### ✅ Chest Sharing

**Status:** Completed (v3.0.0+)

Implemented tier system for player shops:
- **Tier 1 Shops**: Standard shops, one shop per container (default)
- **Tier 2 Shops**: VIP shops that can share containers with other tier 2 shops
- Requires `shopkeeper.player.tier2` permission for tier 2 shops
- Tier 1 and tier 2 shops cannot share containers (mixed tiers not allowed)
- Useful for VIP players managing multiple shops with shared inventory (e.g., with HyperConomy)

### Block Shop Types

- More types of block shops? → clicking button to open shop
- Virtual shops (command to edit and open trade menu), would allow tons of possibilities for other plugins / server admins to use shopkeeper shops in other contexts

### ✅ Editor Amount Per Click

**Status:** Completed

Change amount-per-click from 10 to 8 when clicking items in the player shopkeeper editor? (feels more intuitive due to Minecraft's stack sizes)

### Currency Compression

Compress currency items in the chest (low currency to high currency)? To maximize available storage capacity. This would also mean that the usage of the high-currency-min-cost setting would be limited to creating the trading recipes, and not be used when adding currency items to the shop chests.

### Compatibility Mode

Maybe prevent any unrecognized types of clicks if running in compatibility mode? To reduce the risk of Minecraft updates with new clicking actions causing issues.

### Editor Improvements

- Introduce separate editor window to be able to add new player editing options
- Add an option to reposition shops: button pressed > window closes + message > player clicks a block > runs new/updated/smarter placement logic there, checks distance to chest, option (default true) to not allow it for shops that are not directly placed on top of the shop chest (because those were probably created via command and it is unclear whether players are meant to be able to reposition those shops)
  - Or allow shops to be picked up (with all their data) inside an item (possible now via Bukkit API? Limit on amount of data that can be stored?)

### ✅ Code Improvements

**Status:** Completed

- ✅ Remove `AbstractType#matches` with aliases
- ✅ Set entity attributes (subtypes/look) before spawning the entity (avoids short flicker) - implemented via `applySubtypeAttributes()`
- ✅ Rename `registry#getShopkeeperByBlock()` to `getShopkeeperBySignBlock()` or similar
- ✅ Properly separate loading/unloading from activation/deactivation in debug messages/method names/etc

### Type Registration

Remove `AbstractType#isEnabled()` and instead dynamically register and unregister enabled/disabled types?

- Might change the order of the types dynamically though... determine the order differently, via config?

### Mob Shopkeeper Editor Options

- ✅ Allow equipping (and unequipping again) items to mobs - Equipment editor implemented
- Mobs riding other mobs?
- ✅ Enderman: carrying block - Implemented via equipment editor
- Phantom: size
- Vex: charged state?

### Editor Configuration

- Add individual config options for the different editor options?
- Allow changing the editor option button items

### Sign Features

- Allow changing the sign text color?
- Set villager trades and test if they display trade-able items when the player holds corresponding items

## Ideas

These are exploratory ideas that may or may not be implemented:

- **Per-Trade/Shopkeeper Settings via Written Books:**
  - By adding another row to the shopkeeper-editor inventory window, each trade option and shopkeeper could have a slot for a written-book
  - Which could contain additional meta-data, per-trade/shopkeeper settings, which could be used (e.g., by other plugins) to trigger certain actions when a specific trade is used

- **Separate Shop Options View:**
  - Maybe move shop options (like currently name, profession, etc.) into a separate inventory view to have additional space there

- **Zero-Cost Item Messages:**
  - Add message to default zero-currency items explaining how to increase/decrease costs
  - Add zero-cost items in trading shopkeeper, with lore which explains how to set up the trade

- **Chunked Save Files:**
  - Store shopkeeper data (save.yml) in smaller chunks? Maybe 1 file per world?
  - Makes only sense for very large numbers of shops, with many trades → TODO benchmark

