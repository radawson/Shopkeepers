# Shopkeepers Usage Guide

This guide covers how to use Shopkeepers commands, permissions, and common tasks.

## Table of Contents

1. [Basic Commands](#basic-commands)
2. [Creating Shopkeepers](#creating-shopkeepers)
3. [Managing Shopkeepers](#managing-shopkeepers)
4. [Trading](#trading)
5. [Permissions](#permissions)
6. [Common Tasks](#common-tasks)
7. [Troubleshooting](#troubleshooting)

## Basic Commands

### `/shopkeeper` or `/shopkeepers`
Main Shopkeepers command. Can be used to create shopkeepers or access subcommands.

**Usage:**
- `/shopkeeper [shop-type] [object-type] [category]` - Create a shopkeeper (requires `shopkeeper.create`)
- `/shopkeeper help` - Display help
- `/shopkeeper reload` - Reload plugin (admin)
- `/shopkeeper debug` - Toggle debug mode (admin)
- `/shopkeeper notify` - Toggle trade notifications (player)

**Permissions:**
- `shopkeeper.help` (default: true) - Display help
- `shopkeeper.reload` (default: op) - Reload plugin
- `shopkeeper.debug` (default: op) - Debug mode
- `shopkeeper.create` (default: op) - Create shopkeepers via command
- `shopkeeper.notify.trades` (default: true) - Toggle notifications

## Creating Shopkeepers

### Creating Admin Shops

1. Hold the shop creation item (default: Villager Spawn Egg)
2. Right-click to select shop type
3. Sneak + right-click to select shop object type (mob, sign, etc.)
4. Right-click again to place the shopkeeper
5. Sneak + right-click the shopkeeper to open editor
6. Set up trades in the editor

**Requirements:**
- Permission: `shopkeeper.admin`
- Shop creation item

**Shop Types:**
- Selling shop: Players buy items
- Buying shop: Players sell items
- Trading shop: Two-way item exchange
- Book shop: Sell written books

### Creating Player Shops

1. Place a container (chest, barrel, etc.)
2. Stock the container with items
3. Hold the shop creation item
4. Right-click the container
5. Select shop type
6. Set up trades in the editor

**Requirements:**
- Permission: `shopkeeper.player` or specific type (`shopkeeper.player.sell`, etc.)
- Container must be recently placed (if configured)
- Container must be within max distance

**Container Requirements:**
- Must be accessible
- Must have items (for selling shops)
- Must have space (for buying shops)

### Creating Sign Shops

1. Place a sign (wall sign, sign post, or hanging sign)
2. Hold the shop creation item
3. Right-click the sign
4. Select shop type
5. Set up trades in the editor

**Requirements:**
- Permission: `shopkeeper.sign` (wall signs), `shopkeeper.hanging-sign` (hanging signs)
- Sign must be placed
- Sign shops enabled in config

### Creating Citizens NPC Shops

1. Create a Citizens NPC
2. Hold the shop creation item
3. Right-click the NPC
4. Select shop type
5. Set up trades in the editor

**Requirements:**
- Permission: `shopkeeper.admin` or `shopkeeper.player`
- Citizens plugin installed
- Citizens NPC created
- Citizen shops enabled in config

## Managing Shopkeepers

### Editor Interface

Access the editor by sneaking and right-clicking a shopkeeper.

**Editor Options:**
- **Trade Setup**: Set up buy/sell/trade offers
- **Set Name**: Name the shopkeeper
- **Equipment**: Customize mob appearance (for living shops)
- **Move**: Move the shopkeeper (player shops only)
- **Open Container**: Access shop container (player shops only)
- **Trade Notifications**: Toggle trade notifications (player shops only)
- **Shop Information**: View shopkeeper details (ID, UUID, name, type, location, owner)
- **Open/Close Shop**: Temporarily close shop to prevent trading and hiring
- **Delete**: Delete the shopkeeper

### Listing Shopkeepers

**List Your Shops:**
- `/shopkeeper list` - List your player shops
- `/shopkeeper list [page]` - List your shops with pagination

**List Other Players' Shops:**
- `/shopkeeper list <player>` - List another player's shops (requires permission)
- `/shopkeeper list <player> [page]` - List with pagination
- `/shopkeeper list player <player-uuid>` - List by UUID

**List Admin Shops:**
- `/shopkeeper list admin [page]` - List all admin shops (admin only)

**List All Shops:**
- `/shopkeeper list all [page]` - List all shops (admin only)

**Permissions:**
- `shopkeeper.list.own` (default: true) - List own shops
- `shopkeeper.list.others` (default: op) - List others' shops
- `shopkeeper.list.admin` (default: op) - List admin shops

### Viewing Trading History

**View Your Trading History:**
- `/shopkeeper history` - View your trading history
- `/shopkeeper history self [shop=...|owner=...|'own'|'player'|'admin'|'all'] [page]` - View your history with filters
- `/shopkeeper history [player|'all'|'self'] [shop=...|owner=...|'own'|'player'|'admin'|'all'] [page]` - View history with filters

**Examples:**
- `/shopkeeper history` - Your trading history
- `/shopkeeper history all` - All players' trading history (admin)
- `/shopkeeper history <player>` - Specific player's history
- `/shopkeeper history shop=Test` - History with shop named "Test"
- `/shopkeeper history owner=<player>` - History with shops owned by player

**Note:** Requires `trade-log-storage: 'SQLITE'` in config.

**Permissions:**
- `shopkeeper.history.own` (default: true) - View own trading history
- `shopkeeper.history.admin` (default: op) - View others' and admin shop history

### Removing Shopkeepers

**Remove Your Shop:**
- `/shopkeeper remove` - Remove targeted shop (requires permission)
- `/shopkeeper remove <shopkeeper>` - Remove shopkeeper by name/ID

**Remove Other Players' Shops:**
- `/shopkeeper remove <player>` - Remove another player's shop (admin)

**Remove Admin Shops:**
- `/shopkeeper remove admin <id>` - Remove admin shop by ID (admin)

**Remove All Shops:**
- `/shopkeeper removeAll` - Remove all your shops
- `/shopkeeper removeAll <player>` - Remove all of another player's shops (admin)
- `/shopkeeper removeAll player` - Remove all player shops (admin)
- `/shopkeeper removeAll admin` - Remove all admin shops (admin)

**Permissions:**
- `shopkeeper.remove.own` (default: op) - Remove own shops
- `shopkeeper.remove.others` (default: op) - Remove others' shops
- `shopkeeper.remove.admin` (default: op) - Remove admin shops
- `shopkeeper.remove-all.own` (default: op) - Remove all own shops
- `shopkeeper.remove-all.others` (default: op) - Remove all of another player's shops
- `shopkeeper.remove-all.player` (default: op) - Remove all player shops
- `shopkeeper.remove-all.admin` (default: op) - Remove all admin shops

### Editing Shopkeepers

**Via Editor:**
1. Sneak + right-click shopkeeper
2. Use editor interface to modify

**Via Commands:**
- `/shopkeeper edit [shopkeeper]` - Open editor for targeted shopkeeper (admin)
- `/shopkeeper edit <shopkeeper>` - Open editor for shopkeeper by name/ID (admin)
- `/shopkeeper editVillager` - Edit regular villagers (admin)
- `/shopkeeper editWanderingTrader` - Edit wandering traders (admin)

**Permissions:**
- `shopkeeper.remoteedit` (default: op) - Remote edit access

### Other Management Commands

**Give Shop Creation Items:**
- `/shopkeeper give [player] [shop-type] [object-type] [amount]` - Give shop creation items
- Example: `/shopkeeper give Player1 sell villager 5` - Give 5 selling shop creation items

**Teleport to Shopkeepers:**
- `/shopkeeper teleport [shopkeeper] [player] [force]` - Teleport to shopkeeper
- `/shopkeeper tp [shopkeeper] [player] [force]` - Alias for teleport
- Use `force` to bypass teleportation restrictions

**Remotely Open Shops:**
- `/shopkeeper remote [shopkeeper] [player]` - Open shop trading window for player
- `/shopkeeper open [shopkeeper] [player]` - Alias for remote

**Transfer Shop Ownership:**
- `/shopkeeper transfer [shopkeeper] <new-owner>` - Transfer player shop to new owner
- `/shopkeeper transfer [shopkeeper] <new-owner-uuid>` - Transfer by UUID

**Set Shop for Hire:**
- `/shopkeeper setForHire [shopkeeper]` - Set player shop for hire (hold hire cost item)

**Set Trade Permissions:**
- `/shopkeeper setTradePerm [shopkeeper] <permission>` - Set trade permission for admin shop

**Set Traded Command:**
- `/shopkeeper setTradedCommand [shopkeeper]` - Set command to run when item is traded (hold item)

**Set Currency:**
- `/shopkeeper setCurrency [shopkeeper]` - Set currency for admin shop (hold currency item)

**Give Currency:**
- `/shopkeeper giveCurrency [player] [amount]` - Give currency items to player

**Update Items:**
- `/shopkeeper updateItems` - Update third-party items in shopkeeper data

**Permissions:**
- `shopkeeper.give` (default: op) - Give shop creation items
- `shopkeeper.teleport` (default: op) - Teleport to shopkeepers
- `shopkeeper.teleport.others` (default: op) - Teleport others
- `shopkeeper.remote` (default: op) - Remote open shops
- `shopkeeper.remote.otherplayers` (default: op) - Remote open for others
- `shopkeeper.transfer` (default: op) - Transfer shop ownership
- `shopkeeper.setforhire` (default: op) - Set shop for hire
- `shopkeeper.settradeperm` (default: op) - Set trade permissions
- `shopkeeper.settradedcommand` (default: op) - Set traded command
- `shopkeeper.setcurrency` (default: op) - Set currency
- `shopkeeper.givecurrency` (default: op) - Give currency
- `shopkeeper.updateitems` (default: op) - Update items

## Trading

### Trading with Shopkeepers

1. Right-click a shopkeeper
2. Trading interface opens
3. Place items in trade slots
4. Click trade button
5. Items are exchanged

### Trade Types

**Selling Trades (Player Buys):**
- Place currency/item in input slot
- Receive item from shop

**Buying Trades (Player Sells):**
- Place item in input slot
- Receive currency/item from shop

**Trading Trades (Item Exchange):**
- Place items in both input slots
- Receive items from shop

**Book Trades:**
- Specialized interface for written books

### Trade Restrictions

- Cannot trade with own shop (if configured)
- Cannot trade when owner is online (if configured)
- Must have required items
- Shop must have stock/money
- Must have permission: `shopkeeper.trade`

## Permissions

### Permission Hierarchy

```
shopkeeper.* (all permissions, default: op)
├── shopkeeper.help (default: true)
├── shopkeeper.reload (default: op)
├── shopkeeper.debug (default: op)
├── shopkeeper.list.own (default: true)
├── shopkeeper.list.others (default: op)
├── shopkeeper.list.admin (default: op)
├── shopkeeper.remove.own (default: op)
├── shopkeeper.remove.others (default: op)
├── shopkeeper.remove.admin (default: op)
├── shopkeeper.remove-all.own (default: op)
├── shopkeeper.remove-all.others (default: op)
├── shopkeeper.remove-all.player (default: op)
├── shopkeeper.remove-all.admin (default: op)
├── shopkeeper.trade (default: true)
├── shopkeeper.hire (default: true)
├── shopkeeper.bypass (default: op)
├── shopkeeper.maxshops.unlimited (default: op)
├── shopkeeper.maxshops.<count> (default: false)
├── shopkeeper.admin (default: op)
├── shopkeeper.player (default: true)
│   ├── shopkeeper.player.sell
│   ├── shopkeeper.player.buy
│   ├── shopkeeper.player.trade
│   └── shopkeeper.player.book
├── shopkeeper.sign (default: true)
├── shopkeeper.hanging-sign (default: true)
└── shopkeeper.trade-notifications.admin (default: op)
    shopkeeper.trade-notifications.player (default: op)
```

### Common Permission Setups

**Basic Player:**
```yaml
permissions:
  - shopkeeper.trade
  - shopkeeper.player
```

**Shop Owner:**
```yaml
permissions:
  - shopkeeper.trade
  - shopkeeper.player
  - shopkeeper.list.own
  - shopkeeper.remove.own
```

**Trader (Can Create Shops):**
```yaml
permissions:
  - shopkeeper.trade
  - shopkeeper.player
  - shopkeeper.player.sell
  - shopkeeper.player.buy
  - shopkeeper.maxshops.5
```

**Admin:**
```yaml
permissions:
  - shopkeeper.*
```

### Shopkeeper Snapshots

**Create Snapshot:**
- `/shopkeeper snapshot create <name>` - Create a snapshot of all shopkeepers

**List Snapshots:**
- `/shopkeeper snapshot list` - List all snapshots
- `/shopkeeper snapshots list` - Alias

**Restore Snapshot:**
- `/shopkeeper snapshot restore <name>` - Restore a snapshot (replaces current shopkeepers)

**Remove Snapshot:**
- `/shopkeeper snapshot remove <name>` - Delete a snapshot

**Permissions:**
- `shopkeeper.snapshot` (default: op) - Access snapshot commands

### Debug Commands

**Debug Mode:**
- `/shopkeeper debug` - Toggle debug mode

**Check Shopkeepers:**
- `/shopkeeper check [chunks|active]` - Check shopkeeper status and statistics

**Check Item:**
- `/shopkeeper checkItem` - Check held item for shop creation item tags

**Cleanup Citizens:**
- `/shopkeeper cleanupCitizenShopkeepers` - Clean up invalid Citizen shopkeepers

**Permissions:**
- `shopkeeper.debug` (default: op) - Debug commands
- `shopkeeper.cleanup-citizen-shopkeepers` (default: op) - Cleanup Citizens

## Common Tasks

### Setting Up a Player Shop

1. **Prepare Container:**
   - Place a chest, barrel, or other container
   - Stock it with items to sell (for selling shops)
   - Leave space for items (for buying shops)

2. **Create Shop:**
   - **Method 1 (Item):** Hold shop creation item, right-click the container, select shop type
   - **Method 2 (Command):** Use `/shopkeeper <shop-type> [object-type]` while targeting container (requires `shopkeeper.create`)

3. **Configure Shop:**
   - Sneak + right-click shopkeeper
   - Set up trades in editor
   - Name the shopkeeper (optional)
   - Customize appearance (if living shop)
   - Set shop information and options

4. **Test Shop:**
   - Right-click to test trading
   - Verify items/currency exchange correctly
   - Check shop information display

### Setting Up an Admin Shop

1. **Get Shop Creation Item:**
   - Use `/shopkeeper give [shop-type] [object-type] [amount]` to get creation item (admin)
   - Or use default creation item from config

2. **Create Shop:**
   - **Method 1 (Item):** Hold creation item, right-click to select type, sneak + right-click to select object type, right-click to place
   - **Method 2 (Command):** Use `/shopkeeper <shop-type> [object-type]` while targeting block (requires `shopkeeper.create`)

3. **Configure Shop:**
   - Sneak + right-click shopkeeper
   - Set up trades
   - Configure unlimited stock/money
   - Set trade permissions (optional)
   - Set traded commands (optional)

### Hiring a Player Shop

1. Find a player shop for sale
2. Right-click the shopkeeper
3. Click hire button
4. Pay hire cost
5. Shop becomes yours

**Requirements:**
- Permission: `shopkeeper.hire`
- Permission to create that shop type
- Required hire cost items

### Moving a Player Shop

1. Sneak + right-click your shopkeeper
2. Click move button (Ender Pearl icon)
3. Right-click new location
4. Shopkeeper moves to new location

**Requirements:**
- Permission: `shopkeeper.player`
- Shop movement enabled in config
- New location must be valid
- Container distance limits apply

### Setting Up Trades

1. **Open Editor:**
   - Sneak + right-click shopkeeper
   - Click trade setup button

2. **Add Trade:**
   - Click empty trade slot
   - Place items in input slots
   - Place items in result slot
   - Trade is saved automatically

3. **Multiple Trades:**
   - Use page navigation
   - Set up multiple trade pages
   - Up to 10 pages (configurable)

### Customizing Shopkeeper Appearance

1. **For Living Shops:**
   - Open editor
   - Click equipment button
   - Place items in equipment slots
   - Customize mob-specific attributes (variant, color, size, etc.)
   - Appearance updates immediately

2. **For Sign Shops:**
   - Edit sign text directly
   - Use color codes (if enabled)
   - Enable glowing text (if enabled)
   - Choose sign type (wall sign, sign post, hanging sign)

3. **For End Crystal Shops:**
   - Toggle bottom slab visibility
   - Note: End crystal shops disabled in The End by default

### Managing Trade Notifications

1. Open shopkeeper editor
2. Click trade notifications button
3. Toggle notifications on/off
4. Receive notifications when trades occur

**Or use command:**
- `/shopkeeper notify` - Toggle your trade notifications

**Notification Types:**
- Shop owner notifications (default: enabled)
- Admin notifications (requires permission)
- Player notifications (requires permission)

### Closing/Opening Shops

1. Open shopkeeper editor
2. Click open/close shop button
3. Toggle shop state
4. Closed shops cannot be traded with or hired

**Use Cases:**
- Temporarily disable shop
- Prevent trading during maintenance
- Control shop availability

## Troubleshooting

### Shopkeeper Not Creating

**Check Permissions:**
- Verify you have required permission (`shopkeeper.create` for command creation)
- Check permission plugin configuration
- Use `/shopkeeper help` to see available commands
- Note: Command creation requires `shopkeeper.create` permission (default: op)

**Check Requirements:**
- Container must be recently placed (if configured)
- Container must be within max distance
- Shop creation item must be correct
- Shop limits not exceeded
- Shop type and object type must be enabled and permitted

**Ambiguous Shopkeeper Targeting:**
- If multiple shopkeepers are targeted, you'll see: "Ambiguous shopkeeper. Specify the shopkeeper explicitly instead."
- Use shopkeeper name, ID, or UUID to target a specific shopkeeper

### Trading Not Working

**Check Shop Status:**
- Verify shopkeeper is active
- Check shop has stock/money
- Verify trades are set up

**Check Permissions:**
- Must have `shopkeeper.trade` permission
- Check trade restrictions (own shop, owner online)

**Check Items:**
- Items must match exactly (if strict comparison enabled)
- Must have required items
- Shop must have required items

### Container Issues

**Container Not Accessible:**
- Check container protection
- Verify container is not broken
- Check distance limits
- Verify container permissions

**Items Not Appearing:**
- Check container has items
- Verify container is linked to shop
- Check container protection settings

### Performance Issues

**Too Many Shopkeepers:**
- Increase `mob-behavior-tick-period` in config
- Reduce `gravity-chunk-range`
- Limit shopkeeper count per player
- Use sign shops instead of living shops

**Lag from Shopkeepers:**
- Disable unused features
- Reduce trade log detail
- Increase update intervals
- Use more efficient shop types

### Getting Help

1. Check this documentation
2. Review [Configuration Guide](configuration.md)
3. Check [Features Guide](features.md)
4. Search GitHub Issues
5. Create a new issue with:
   - Shopkeepers version
   - Server type and version
   - Error messages
   - Steps to reproduce

## Tips & Best Practices

1. **Start Simple**: Create a few test shops first
2. **Use Appropriate Types**: Choose the right shop type for your use case
3. **Protect Containers**: Keep container protection enabled
4. **Set Limits**: Configure shop limits appropriately
5. **Use Permissions**: Set up proper permission groups
6. **Regular Maintenance**: Clean up inactive shops periodically
7. **Test Integrations**: Test plugin integrations before production
8. **Monitor Performance**: Use debug mode to identify issues
9. **Backup Data**: Regular backups of shopkeeper data
10. **Document Shops**: Keep notes on shop locations and purposes

For more information, see the [Features Guide](features.md), [Configuration Guide](configuration.md), and [Changelog](CHANGELOG.md).


