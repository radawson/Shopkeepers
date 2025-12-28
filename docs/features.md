# Shopkeepers Features

This document provides a comprehensive overview of all features available in Shopkeepers.

## Table of Contents

1. [Shopkeeper Types](#shopkeeper-types)
2. [Shop Object Types](#shop-object-types)
3. [Trading System](#trading-system)
4. [Container Integration](#container-integration)
5. [Plugin Integrations](#plugin-integrations)
6. [Editor System](#editor-system)
7. [Protection Features](#protection-features)
8. [API](#api)

## Shopkeeper Types

### Admin Shops
Admin shops are server-controlled shops with infinite supply.

**Features:**
- Unlimited stock
- Unlimited money
- Admin-only creation and editing
- Perfect for server-run shops

**Use Cases:**
- Server marketplaces
- Resource distribution
- Economy stabilization
- Admin shops for special events

### Player Shops
Player-owned shops that pull items from containers.

**Features:**
- Stock from player containers (chests, barrels, etc.)
- Player-controlled pricing
- Owner permissions
- Trade notifications
- Shop limits per player

**Use Cases:**
- Player-to-player trading
- Player-run businesses
- Decentralized economy
- Community markets

**Shop Types:**
- **Selling Shops**: Players buy items from the shop
- **Buying Shops**: Players sell items to the shop
- **Trading Shops**: Two-way item exchange
- **Book Shops**: Sell written books

## Shop Object Types

### Living Entity Shops
Shopkeepers can appear as various mob types.

**Supported Entities:**
- Villagers (default)
- Allays, Armadillos, Axolotls
- Bees, Blazes, Boggled, Breeze
- Cats, Chickens, Cows
- Creepers, Dolphins, Donkeys
- Endermen, Evokers, Foxes
- And many more...

**Features:**
- Customizable appearance
- Equipment support
- Nameplate display
- Gravity control
- AI behavior customization
- Sound control

### Sign Shops
Shopkeepers can be represented as signs.

**Types:**
- **Wall Signs**: Attached to walls
- **Sign Posts**: Free-standing signs
- **Hanging Signs**: Ceiling-attached signs

**Features:**
- Glowing text option
- Compact shop design
- Easy placement
- No entity overhead

### Citizens NPC Shops
Integration with Citizens plugin for NPC shopkeepers.

**Features:**
- Use Citizens NPCs as shopkeepers
- Full Citizens NPC functionality
- Customizable NPC appearance
- NPC commands and actions

## Trading System

### Trade Types

**Selling Trades:**
- Player gives currency/item
- Shop gives item
- Standard purchase transaction

**Buying Trades:**
- Player gives item
- Shop gives currency/item
- Standard sell transaction

**Trading Trades:**
- Two-way item exchange
- No currency required
- Item-for-item trading

**Book Trades:**
- Specialized for written books
- Book-specific interface

### Trade Management

**Features:**
- Multiple trade pages (up to 10)
- Trade setup interface
- Trade validation
- Trade notifications
- Trade history (optional)

**Trade Limits:**
- Configurable max trades per page
- Configurable max pages
- Per-shop trade limits

## Container Integration

### Container Types
Player shops require containers for stock management.

**Supported Containers:**
- Chests
- Barrels
- Shulker boxes
- Double chests
- And other container blocks

### Container Features

**Protection:**
- Automatic container protection
- Prevents unauthorized access
- Prevents item extraction (hoppers, etc.)
- Configurable protection settings

**Distance Limits:**
- Maximum distance from container
- Configurable range
- Prevents shop-container separation

**Requirements:**
- Container must be recently placed (optional)
- Container must be accessible
- Container must have space

## Plugin Integrations

### WorldGuard
Integration with WorldGuard for region-based restrictions.

**Features:**
- Region-based shop placement
- Allow-shop flag
- Build permission checks
- Container access checks

### Towny
Integration with Towny for town-based restrictions.

**Features:**
- Commercial area requirements
- Town-based shop placement
- Town permission integration

### Citizens
Integration with Citizens for NPC shopkeepers.

**Features:**
- NPC shopkeeper creation
- NPC customization
- NPC command integration
- NPC owner settings

### Economy Plugins
Integration with economy plugins for currency.

**Supported:**
- ServiceIO (recommended, modern)
- Vault (legacy support)
- Any Vault-compatible economy

**Features:**
- Currency-based trades
- Balance checks
- Transaction processing
- Multi-currency support

### Other Integrations
- **Gringotts**: Economy integration
- **ChestShop**: Compatibility
- **Multiverse-Core**: Multi-world support
- **My Worlds**: World management
- **Transporter**: Teleportation integration

## Editor System

### Editor Interface
Graphical interface for managing shopkeepers.

**Features:**
- Trade setup
- Shop naming
- Equipment editing
- Container access
- Shop deletion
- Shop movement

### Editor Access
- Right-click shopkeeper while sneaking
- Requires appropriate permissions
- Owner-only for player shops
- Admin-only for admin shops

### Equipment Editor
Customize shopkeeper appearance with equipment.

**Features:**
- Armor slots
- Hand items
- Off-hand items
- Mob-specific equipment
- Visual customization

## Protection Features

### Shopkeeper Protection

**Features:**
- Damage protection
- Despawn prevention
- Teleportation protection
- Interaction protection
- Container protection

### Container Protection

**Features:**
- Access restrictions
- Break protection
- Item movement prevention
- Hopper protection
- Minecart protection

### Player Shop Protection

**Features:**
- Owner-only editing
- Owner-only container access
- Trade restrictions
- Shop limits
- Inactive player cleanup

## API

Shopkeepers provides an API for plugin developers.

### API Features

**Registry:**
- Shopkeeper registry
- Query loaded shopkeepers
- Shopkeeper lookup
- Shopkeeper creation

**Events:**
- Shopkeeper creation events
- Trade events
- Editor events
- Shopkeeper removal events

**Limitations:**
- API is still evolving
- Some features not yet exposed
- Custom shopkeeper types not yet supported
- Trade processing not fully customizable

### Using the API

**Maven Dependency:**
```xml
<dependency>
  <groupId>com.nisovin.shopkeepers</groupId>
  <artifactId>ShopkeepersAPI</artifactId>
  <version>2.15.1</version>
  <scope>provided</scope>
</dependency>
```

**Entry Point:**
```java
ShopkeepersAPI api = ShopkeepersAPI.getInstance();
ShopkeeperRegistry registry = api.getShopkeeperRegistry();
```

**Checking if Entity is Shopkeeper:**
```java
if (entity.hasMetadata("shopkeeper")) {
    // Entity is a shopkeeper
}
```

### API Documentation
See: https://github.com/Shopkeepers/Shopkeepers/tree/master/modules/api/src/main/java/com/nisovin/shopkeepers/api

## Additional Features

### Naming System
- Custom shopkeeper names
- Name validation (regex)
- Nameplate display
- Renaming support

### Hiring System
- Hire player shops
- Hire regular villagers
- Hire cost configuration
- Permission requirements

### Trade Notifications
- Trade completion notifications
- Owner notifications
- Configurable notification settings

### Shop Limits
- Per-player shop limits
- Permission-based limits
- Unlimited permission option
- Default limit configuration

### Inactive Player Cleanup
- Automatic cleanup of inactive player shops
- Configurable inactivity period
- Startup cleanup option

### Shop Movement
- Move player shops
- Teleport shopkeepers
- Location updates
- Container distance checks

### Debug Features
- Debug mode
- Debug options
- Performance monitoring
- Error tracking

## Best Practices

1. **Use Appropriate Shop Types**: Choose the right shop type for your use case
2. **Protect Containers**: Keep container protection enabled
3. **Set Reasonable Limits**: Configure shop limits appropriately
4. **Use Permissions**: Set up proper permission groups
5. **Regular Maintenance**: Clean up inactive shops periodically
6. **Test Integrations**: Test plugin integrations before production use
7. **Monitor Performance**: Use debug mode to identify performance issues
8. **Backup Data**: Regular backups of shopkeeper data

For configuration details, see the [Configuration Guide](configuration.md). For usage instructions, see the [Usage Guide](usage.md).

