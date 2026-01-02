package com.nisovin.shopkeepers.commands.shopkeepers;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.shopkeeper.DefaultShopTypes;
import com.nisovin.shopkeepers.api.shopkeeper.ShopCreationData;
import com.nisovin.shopkeepers.api.shopkeeper.ShopType;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopCreationData;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopType;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopCreationData;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopType;
import com.nisovin.shopkeepers.api.shopobjects.ShopObjectType;
import com.nisovin.shopkeepers.commands.Confirmations;
import com.nisovin.shopkeepers.commands.arguments.CategoryArgument;
import com.nisovin.shopkeepers.commands.arguments.ShopObjectTypeArgument;
import com.nisovin.shopkeepers.commands.arguments.ShopTypeArgument;
import com.nisovin.shopkeepers.commands.lib.BaseCommand;
import com.nisovin.shopkeepers.commands.lib.CommandException;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.CommandRegistry;
import com.nisovin.shopkeepers.commands.lib.NoPermissionException;
import com.nisovin.shopkeepers.commands.lib.commands.PlayerCommand;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.commands.shopkeepers.snapshot.CommandSnapshot;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopcreation.ShopkeeperPlacement;
import com.nisovin.shopkeepers.shopkeeper.registry.SKShopkeeperRegistry;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.admin.regular.RegularAdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.offers.PriceOffer;
import com.nisovin.shopkeepers.api.shopkeeper.offers.TradeOffer;
import com.nisovin.shopkeepers.api.shopkeeper.player.buy.BuyingPlayerShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.sell.SellingPlayerShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.trade.TradingPlayerShopkeeper;
import com.nisovin.shopkeepers.currency.Currencies;
import com.nisovin.shopkeepers.util.ItemCategory;
import com.nisovin.shopkeepers.util.ItemCategoryUtils;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;
import com.nisovin.shopkeepers.util.java.ObjectUtils;

public class ShopkeepersCommand extends BaseCommand {

	private static final String COMMAND_NAME = "shopkeeper";

	private static final String ARGUMENT_SHOP_TYPE = "shop-type";
	private static final String ARGUMENT_OBJECT_TYPE = "object-type";
	private static final String ARGUMENT_CATEGORY = "category";

	private final SKShopkeepersPlugin plugin;
	private final SKShopkeeperRegistry shopkeeperRegistry;

	public ShopkeepersCommand(SKShopkeepersPlugin plugin, Confirmations confirmations) {
		super(plugin, COMMAND_NAME);
		this.plugin = plugin;
		this.shopkeeperRegistry = plugin.getShopkeeperRegistry();

		// Permission gets checked by testPermission and during execution.

		// Description:
		this.setDescription(Messages.commandDescriptionShopkeeper);

		// Formatting:
		this.setHelpTitleFormat(Messages.commandHelpTitle.setPlaceholderArguments(
				"version", plugin.getDescription().getVersion()
		));
		this.setHelpUsageFormat(Messages.commandHelpUsageFormat);
		this.setHelpDescFormat(Messages.commandHelpDescriptionFormat);

		// Arguments for shopkeeper creation:
		this.addArgument(new ShopTypeArgument(ARGUMENT_SHOP_TYPE).optional());
		this.addArgument(new ShopObjectTypeArgument(ARGUMENT_OBJECT_TYPE).optional());
		this.addArgument(new CategoryArgument(ARGUMENT_CATEGORY).optional());

		// Register child commands:
		CommandRegistry childCommands = this.getChildCommands();
		childCommands.register(new CommandHelp(Unsafe.initialized(this)));
		childCommands.register(new CommandReload(plugin));
		childCommands.register(new CommandDebug());
		childCommands.register(new CommandNotify());
		childCommands.register(new CommandList(shopkeeperRegistry));
		childCommands.register(new CommandHistory(plugin));
		childCommands.register(new CommandRemove(confirmations));
		childCommands.register(new CommandRemoveAll(plugin, shopkeeperRegistry, confirmations));
		childCommands.register(new CommandGive());
		childCommands.register(new CommandGiveCurrency());
		childCommands.register(new CommandSetCurrency());
		childCommands.register(new CommandUpdateItems());
		childCommands.register(new CommandRemote());
		childCommands.register(new CommandEdit());
		childCommands.register(new CommandTeleport());
		childCommands.register(new CommandTransfer());
		childCommands.register(new CommandSetTradePerm());
		childCommands.register(new CommandSetTradedCommand());
		childCommands.register(new CommandSetForHire());
		childCommands.register(new CommandSnapshot(confirmations));
		childCommands.register(new CommandEditVillager());
		// Hidden commands:
		childCommands.register(new CommandConfirm(confirmations));
		// Hidden debugging / utility commands:
		childCommands.register(new CommandReplaceAllWithVanillaVillagers(plugin, shopkeeperRegistry,
				confirmations));
		childCommands.register(new CommandCleanupCitizenShopkeepers());
		childCommands.register(new CommandCheck(plugin));
		childCommands.register(new CommandCheckItem());
		childCommands.register(new CommandYaml());
		childCommands.register(new CommandDebugCreateShops(plugin));
		childCommands.register(new CommandTestDamage(plugin));
		childCommands.register(new CommandTestSpawn(plugin));
	}

	// This also hides the command from the help page if the player shop creation via command is
	// disabled.
	@Override
	public boolean testPermission(CommandSender sender) {
		if (!super.testPermission(sender)) return false;
		return PermissionUtils.hasPermission(sender, ShopkeepersPlugin.CREATE_PERMISSION);
	}

	@Override
	protected NoPermissionException noPermissionException() {
		// Custom no-permission message to note about using the shop creation item to create player
		// shops:
		return new NoPermissionException(Messages.commandCreateNoPermission);
	}

	@Override
	protected void execute(CommandInput input, CommandContextView context) throws CommandException {
		Player player = ObjectUtils.castOrNull(input.getSender(), Player.class);
		if (player == null) {
			throw PlayerCommand.createCommandSourceRejectedException(input.getSender());
		}

		// Creating new shopkeeper:

		// Get targeted block information:
		// If the player is underwater or inside lava, we ignore fluid collisions to allow the
		// placement of shopkeepers underwater or inside lava. Otherwise, we take fluids into
		// account, which allows the placement of shopkeepers on top of water or lava, such as for
		// example for striders.
		Block playerBlock = player.getEyeLocation().getBlock();
		FluidCollisionMode fluidCollisionMode;
		if (playerBlock.isLiquid()) {
			fluidCollisionMode = FluidCollisionMode.NEVER;
		} else {
			fluidCollisionMode = FluidCollisionMode.ALWAYS;
		}

		// This takes passable blocks (including fluids, depending on the collision mode) into
		// account (i.e. collides with them):
		RayTraceResult targetBlockInfo = player.rayTraceBlocks(10.0D, fluidCollisionMode);

		// Check for valid targeted block:
		if (targetBlockInfo == null) {
			TextUtils.sendMessage(player, Messages.mustTargetBlock);
			return;
		}

		Block targetBlock = Unsafe.assertNonNull(targetBlockInfo.getHitBlock());
		assert !targetBlock.isEmpty();
		BlockFace targetBlockFace = Unsafe.assertNonNull(targetBlockInfo.getHitBlockFace());

		ShopType<?> shopType = context.getOrNull(ARGUMENT_SHOP_TYPE);
		ShopObjectType<?> shopObjectType = context.getOrNull(ARGUMENT_OBJECT_TYPE);

		// We use different defaults depending on whether the player might be trying to create a
		// player or admin shop:
		boolean containerTargeted = ItemUtils.isContainer(targetBlock.getType());
		boolean maybeCreatePlayerShop = containerTargeted;
		if (maybeCreatePlayerShop) {
			// Default shop type and shop object type: First usable player shop type and shop object
			// type.
			if (shopType == null) {
				shopType = plugin.getShopTypeRegistry().getDefaultSelection(player);
			}
		} else {
			// Default shop type and shop object type:
			if (shopType == null) {
				shopType = DefaultShopTypes.ADMIN_REGULAR();
				// Note: Shop type permissions are checked afterwards during shop creation.
			}
		}

		if (shopObjectType == null) {
			shopObjectType = plugin.getShopObjectTypeRegistry().getDefaultSelection(player);
		}
		if (shopType == null || shopObjectType == null) {
			// The player cannot create shops at all:
			TextUtils.sendMessage(player, Messages.noPermission);
			return;
		}
		assert shopType != null && shopObjectType != null;
		boolean isPlayerShopType = (shopType instanceof PlayerShopType);

		if (isPlayerShopType) {
			if (!containerTargeted) {
				TextUtils.sendMessage(player, Messages.mustTargetContainer);
				return;
			}
			// Note: We check if the targeted container is valid / supported during shop creation.
		}

		// Determine spawn location:
		ShopkeeperPlacement shopkeeperPlacement = plugin.getShopkeeperCreation().getShopkeeperPlacement();
		Location spawnLocation = shopkeeperPlacement.determineSpawnLocation(
				player,
				targetBlock,
				targetBlockFace
		);

		// Shop creation data:
		ShopCreationData shopCreationData;
		if (isPlayerShopType) {
			// Create player shopkeeper:
			shopCreationData = PlayerShopCreationData.create(
					player,
					(PlayerShopType<?>) shopType,
					shopObjectType,
					spawnLocation,
					targetBlockFace,
					targetBlock
			);
		} else {
			// Create admin shopkeeper:
			shopCreationData = AdminShopCreationData.create(
					player,
					(AdminShopType<?>) shopType,
					shopObjectType,
					spawnLocation, targetBlockFace
			);
		}
		assert shopCreationData != null;

		// Get category if specified:
		ItemCategory category = context.getOrNull(ARGUMENT_CATEGORY);

		// Handle shopkeeper creation:
		Shopkeeper shopkeeper = plugin.handleShopkeeperCreation(shopCreationData);

		// If a category was specified, populate the shop with items from that category:
		if (shopkeeper != null && category != null) {
			populateShopWithCategory(shopkeeper, category);
		}
	}

	/**
	 * Populates the given shopkeeper with items from the specified category.
	 * 
	 * @param shopkeeper
	 *            the shopkeeper to populate
	 * @param category
	 *            the item category
	 */
	private void populateShopWithCategory(Shopkeeper shopkeeper, ItemCategory category) {
		if (shopkeeper == null || category == null) return;

		// Get materials for the category:
		List<Material> materials = ItemCategoryUtils.getMaterialsForCategory(category);
		if (materials.isEmpty()) {
			// No items found for this category
			return;
		}

		// Default price for items (can be edited later by the user):
		int defaultPrice = 1;

		// Create offers based on shop type:
		if (shopkeeper instanceof SellingPlayerShopkeeper) {
			// Selling shop: player buys items
			SellingPlayerShopkeeper sellingShop = (SellingPlayerShopkeeper) shopkeeper;
			List<PriceOffer> offers = new ArrayList<>();
			for (Material material : materials) {
				ItemStack itemStack = new ItemStack(material);
				PriceOffer offer = PriceOffer.create(itemStack, defaultPrice);
				offers.add(offer);
			}
			sellingShop.addOffers(offers);
		} else if (shopkeeper instanceof BuyingPlayerShopkeeper) {
			// Buying shop: player sells items
			BuyingPlayerShopkeeper buyingShop = (BuyingPlayerShopkeeper) shopkeeper;
			List<PriceOffer> offers = new ArrayList<>();
			for (Material material : materials) {
				ItemStack itemStack = new ItemStack(material);
				PriceOffer offer = PriceOffer.create(itemStack, defaultPrice);
				offers.add(offer);
			}
			buyingShop.addOffers(offers);
		} else if (shopkeeper instanceof TradingPlayerShopkeeper) {
			// Trading shop: item-for-item exchange
			// For trading shops, we'll create simple 1:1 trades
			TradingPlayerShopkeeper tradingShop = (TradingPlayerShopkeeper) shopkeeper;
			List<TradeOffer> offers = new ArrayList<>();
			for (Material material : materials) {
				ItemStack itemStack = new ItemStack(material);
				// Simple 1:1 trade (item for itself)
				TradeOffer offer = TradeOffer.create(itemStack, itemStack, null);
				offers.add(offer);
			}
			tradingShop.addOffers(offers);
		} else if (shopkeeper instanceof RegularAdminShopkeeper) {
			// Admin shop: can trade items
			RegularAdminShopkeeper adminShop = (RegularAdminShopkeeper) shopkeeper;
			List<TradeOffer> offers = new ArrayList<>();
			for (Material material : materials) {
				ItemStack itemStack = new ItemStack(material);
				// For admin shops, use the base currency item
				ItemStack currencyItem = Currencies.getBase().getItemData().createItemStack(defaultPrice);
				TradeOffer offer = TradeOffer.create(itemStack, currencyItem, null);
				offers.add(offer);
			}
			adminShop.addOffers(offers);
		}
		// Note: Book shops are not supported for category population
	}
}
