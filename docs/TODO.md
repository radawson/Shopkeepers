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

### Command Library Revamp

This is a large architectural change that would improve the command system:

- Reverse the setup of arguments in parsing chains:
  - Currently the parent argument delegates to child arguments, making the setup unnatural since fallbacks need to be specified first
  - Instead, allow specifying the chain of arguments more naturally: `argument.fallback(fallback).fallback(fallback)` etc.
- Support advanced text features (e.g., in helps page)
- Allow arguments to change the following arguments, similar to how subcommands have different chains of arguments
  - E.g., `/shopkeeper info ['own'|'admin'|<player>] <shopkeeper>`: Different suggestions/allowed arguments for the second argument depending on the first argument
  - But: Avoid duplicate definition of common argument chains, if different chains are not required/used
    - E.g., by defining an argument chain once and then reusing it for different sub-commands/arguments
  - Maybe even dynamically, by allowing arguments that get parameterized by the arguments parsed before
    - E.g., `/region <region> removeMember <member>`: Only suggest members that are part of the region
- Bind executing code directly to an argument chain, to avoid having to manually check the CommandContext for which arguments got actually parsed
- Allow for easier debugging of command execution and argument parsing (without introducing plugin-specific logging dependencies into the library classes).
  - Maybe define separate logging interface for the command lib, currently the Log class is used directly
  - The same applies for any other utilities and plugin-specific stuff
- Dealing with ambiguities:
  - Example: `/list [player (default:sender)] [page (default:1)]`
    - `/list 123` ("123" is a valid player name!)
    - Also: `/list 123 2` would currently try to parse "123" as page number and then print an error due to the unexpected "2" due to the fallback mechanic used for the player argument
  - Warning if command/arguments allow for ambiguity? E.g., by letting arguments provide a list of examples and evaluating all possible combinations
  - Parse all possible argument assignments and print a warning if the input is ambiguous?
  - Allow resolving ambiguities by explicitly binding arguments, e.g., `/list player=blablubbabc page=3`
    - Or `::` or `:=` or `!=`? `=` might likely be used/useful in various arguments as well, and `:` alone is ambiguous because the use in namespaces
    - Or `/list -player blablubbabc -page 2`? Or `/list !player=blablubbabc !page=2`?
      - Or `/list --player blablubbabc --page 2`
    - But: requires users to know the exact argument name: issue when arguments can have aliases? (e.g., for literals, especially if the command format uses an alias instead of the actual argument name)
    - Also: what about sub-arguments? Is it required to be able to explicitly specify which sub-argument an arg is meant to bind to? sub-arguments are usually an implementation detail
    - Or: `/list '' '2'` (quotes contain the arg(s) that get bound to specific arguments; including marking empty arguments explicitly)
    - Or: use some separator that marks which arg(s) get bound to which arguments, e.g.: `/list | | 2`
    - Or: use special character/marker to explicitly mark missing arguments: `/list ! 2`, or `/list _ 2`
      - However: Does not allow resolving all kinds of ambiguities! E.g., if arguments parse multiple args: `/cmd [multiple args (here] and there)` ('here' may be the last arg of the first argument, or the first arg of the second argument)
      - → Special character marks 'end of current argument': Allows marking missing arguments, as well as using it as argument delimiter
  - Also allow for multi-part arguments: `/text text='some text'`
  - And empty/missing argument: `/list player='' 1` (player argument is optional)
  - Problem: FirstOf arguments with literals can internally have ambiguities as well, e.g., `/list <'all'|player>` (with an existing player named 'all')
    - This is only a problem if the sub arguments are joined to a single name in the argument format (which unusual if literal arguments are involved)
- Allow quoted string parsing `'some text'` (accepting `""` and `''` and `` ` ``)
  - Also: `{some: map, like: data}`

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

