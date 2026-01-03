package com.nisovin.shopkeepers.commands.brigadier.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.ShopkeeperRegistry;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import com.nisovin.shopkeepers.commands.util.AmbiguousPlayerNameHandler;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.OwnedPlayerShopsResult;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.Validate;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Executor for the /shopkeeper list command.
 * <p>
 * Lists shopkeepers based on filter criteria:
 * <ul>
 * <li>/shopkeeper list - Lists own shops (for players) or all shops (for console)</li>
 * <li>/shopkeeper list all - Lists all shops</li>
 * <li>/shopkeeper list admin - Lists admin shops</li>
 * <li>/shopkeeper list &lt;player&gt; - Lists shops owned by a player</li>
 * </ul>
 */
@SuppressWarnings("UnstableApiUsage")
public class ListExecutor {

	private static final int ENTRIES_PER_PAGE = 8;
	private static final String ARGUMENT_PAGE = "page";
	private static final String ARGUMENT_PLAYER = "player";

	private final ShopkeeperRegistry shopkeeperRegistry;

	/**
	 * Creates a new ListExecutor.
	 *
	 * @param shopkeeperRegistry
	 *            the shopkeeper registry
	 */
	public ListExecutor(ShopkeeperRegistry shopkeeperRegistry) {
		Validate.notNull(shopkeeperRegistry, "shopkeeperRegistry is null");
		this.shopkeeperRegistry = shopkeeperRegistry;
	}

	/**
	 * Builds the list command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("list")
				.requires(this::hasListPermission)
				// /shopkeeper list - List own shops (or all for console)
				.executes(this::executeListOwn)
				// /shopkeeper list [page]
				.then(Commands.argument(ARGUMENT_PAGE, IntegerArgumentType.integer(1))
						.executes(this::executeListOwnWithPage))
				// /shopkeeper list all [page]
				.then(Commands.literal("all")
						.requires(source -> hasPermission(source, ShopkeepersPlugin.LIST_ADMIN_PERMISSION)
								&& hasPermission(source, ShopkeepersPlugin.LIST_OTHERS_PERMISSION))
						.executes(this::executeListAll)
						.then(Commands.argument(ARGUMENT_PAGE, IntegerArgumentType.integer(1))
								.executes(this::executeListAllWithPage)))
				// /shopkeeper list admin [page]
				.then(Commands.literal("admin")
						.requires(source -> hasPermission(source, ShopkeepersPlugin.LIST_ADMIN_PERMISSION))
						.executes(this::executeListAdmin)
						.then(Commands.argument(ARGUMENT_PAGE, IntegerArgumentType.integer(1))
								.executes(this::executeListAdminWithPage)))
				// /shopkeeper list <player> [page]
				.then(Commands.argument(ARGUMENT_PLAYER, StringArgumentType.word())
						.requires(source -> hasPermission(source, ShopkeepersPlugin.LIST_OWN_PERMISSION)
								|| hasPermission(source, ShopkeepersPlugin.LIST_OTHERS_PERMISSION))
						.suggests(this::suggestPlayers)
						.executes(this::executeListPlayer)
						.then(Commands.argument(ARGUMENT_PAGE, IntegerArgumentType.integer(1))
								.executes(this::executeListPlayerWithPage)));
	}

	private boolean hasListPermission(CommandSourceStack source) {
		CommandSender sender = source.getSender();
		return PermissionUtils.hasPermission(sender, ShopkeepersPlugin.LIST_OWN_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.LIST_OTHERS_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.LIST_ADMIN_PERMISSION);
	}

	private boolean hasPermission(CommandSourceStack source, String permission) {
		return PermissionUtils.hasPermission(source.getSender(), permission);
	}

	private CompletableFuture<Suggestions> suggestPlayers(
			CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder
	) {
		String remaining = builder.getRemainingLowerCase();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getName().toLowerCase().startsWith(remaining)) {
				builder.suggest(player.getName());
			}
		}
		return builder.buildFuture();
	}

	// List own shops (default)
	private int executeListOwn(CommandContext<CommandSourceStack> context) {
		return executeListOwnInternal(context, 1);
	}

	private int executeListOwnWithPage(CommandContext<CommandSourceStack> context) {
		int page = IntegerArgumentType.getInteger(context, ARGUMENT_PAGE);
		return executeListOwnInternal(context, page);
	}

	private int executeListOwnInternal(CommandContext<CommandSourceStack> context, int page) {
		CommandSender sender = context.getSource().getSender();

		if (sender instanceof Player player) {
			// Check permission
			if (!PermissionUtils.hasPermission(sender, ShopkeepersPlugin.LIST_OWN_PERMISSION)) {
				TextUtils.sendMessage(sender, Messages.noPermission);
				return Command.SINGLE_SUCCESS;
			}

			// Get shops owned by the player
			OwnedPlayerShopsResult result = ShopkeeperArgumentUtils.getOwnedPlayerShops(
					player.getUniqueId(),
					player.getName()
			);
			listPlayerShops(sender, player.getName(), player.getUniqueId(), result.getShops(), page);
		} else {
			// Console: list all shops
			if (!PermissionUtils.hasPermission(sender, ShopkeepersPlugin.LIST_ADMIN_PERMISSION)) {
				TextUtils.sendMessage(sender, Messages.noPermission);
				return Command.SINGLE_SUCCESS;
			}
			listAllShops(sender, page);
		}

		return Command.SINGLE_SUCCESS;
	}

	// List all shops
	private int executeListAll(CommandContext<CommandSourceStack> context) {
		return executeListAllInternal(context, 1);
	}

	private int executeListAllWithPage(CommandContext<CommandSourceStack> context) {
		int page = IntegerArgumentType.getInteger(context, ARGUMENT_PAGE);
		return executeListAllInternal(context, page);
	}

	private int executeListAllInternal(CommandContext<CommandSourceStack> context, int page) {
		CommandSender sender = context.getSource().getSender();
		listAllShops(sender, page);
		return Command.SINGLE_SUCCESS;
	}

	// List admin shops
	private int executeListAdmin(CommandContext<CommandSourceStack> context) {
		return executeListAdminInternal(context, 1);
	}

	private int executeListAdminWithPage(CommandContext<CommandSourceStack> context) {
		int page = IntegerArgumentType.getInteger(context, ARGUMENT_PAGE);
		return executeListAdminInternal(context, page);
	}

	private int executeListAdminInternal(CommandContext<CommandSourceStack> context, int page) {
		CommandSender sender = context.getSource().getSender();
		listAdminShops(sender, page);
		return Command.SINGLE_SUCCESS;
	}

	// List player shops
	private int executeListPlayer(CommandContext<CommandSourceStack> context) {
		return executeListPlayerInternal(context, 1);
	}

	private int executeListPlayerWithPage(CommandContext<CommandSourceStack> context) {
		int page = IntegerArgumentType.getInteger(context, ARGUMENT_PAGE);
		return executeListPlayerInternal(context, page);
	}

	private int executeListPlayerInternal(CommandContext<CommandSourceStack> context, int page) {
		CommandSender sender = context.getSource().getSender();
		String playerName = StringArgumentType.getString(context, ARGUMENT_PLAYER);

		// Check if it's the sender's own shops
		Player senderPlayer = sender instanceof Player ? (Player) sender : null;
		boolean isOwnShops = senderPlayer != null && senderPlayer.getName().equalsIgnoreCase(playerName);

		if (isOwnShops) {
			if (!PermissionUtils.hasPermission(sender, ShopkeepersPlugin.LIST_OWN_PERMISSION)) {
				TextUtils.sendMessage(sender, Messages.noPermission);
				return Command.SINGLE_SUCCESS;
			}
		} else {
			if (!PermissionUtils.hasPermission(sender, ShopkeepersPlugin.LIST_OTHERS_PERMISSION)) {
				TextUtils.sendMessage(sender, Messages.noPermission);
				return Command.SINGLE_SUCCESS;
			}
		}

		// Find player UUID
		UUID playerUUID = null;
		Player onlinePlayer = Bukkit.getPlayerExact(playerName);
		if (onlinePlayer != null) {
			playerUUID = onlinePlayer.getUniqueId();
			playerName = onlinePlayer.getName(); // Get exact case
		}

		// Get shops owned by the player
		OwnedPlayerShopsResult result = ShopkeeperArgumentUtils.getOwnedPlayerShops(playerUUID, playerName);

		// Handle ambiguous player names
		Map<? extends UUID, ? extends String> matchingOwners = result.getMatchingShopOwners();
		if (matchingOwners.size() > 1) {
			boolean ambiguous = AmbiguousPlayerNameHandler.handleAmbiguousPlayerName(
					sender,
					playerName,
					matchingOwners.entrySet()
			);
			if (ambiguous) {
				return Command.SINGLE_SUCCESS;
			}
		}

		listPlayerShops(sender, result.getPlayerName(), result.getPlayerUUID(), result.getShops(), page);
		return Command.SINGLE_SUCCESS;
	}

	// Helper methods for listing

	private void listAllShops(CommandSender sender, int page) {
		List<? extends Shopkeeper> shops = new ArrayList<>(shopkeeperRegistry.getAllShopkeepers());
		int shopsCount = shops.size();
		int maxPage = Math.max(1, (int) Math.ceil((double) shopsCount / ENTRIES_PER_PAGE));
		page = Math.max(1, Math.min(page, maxPage));

		TextUtils.sendMessage(sender, Messages.listAllShopsHeader,
				"shopsCount", shopsCount,
				"page", page,
				"maxPage", maxPage
		);

		listShopEntries(sender, shops, page);
	}

	private void listAdminShops(CommandSender sender, int page) {
		List<Shopkeeper> adminShops = new ArrayList<>();
		for (Shopkeeper shopkeeper : shopkeeperRegistry.getAllShopkeepers()) {
			if (shopkeeper instanceof AdminShopkeeper) {
				adminShops.add(shopkeeper);
			}
		}

		int shopsCount = adminShops.size();
		int maxPage = Math.max(1, (int) Math.ceil((double) shopsCount / ENTRIES_PER_PAGE));
		page = Math.max(1, Math.min(page, maxPage));

		TextUtils.sendMessage(sender, Messages.listAdminShopsHeader,
				"shopsCount", shopsCount,
				"page", page,
				"maxPage", maxPage
		);

		listShopEntries(sender, adminShops, page);
	}

	private void listPlayerShops(
			CommandSender sender,
			String playerName,
			UUID playerUUID,
			List<? extends Shopkeeper> shops,
			int page
	) {
		int shopsCount = shops.size();
		int maxPage = Math.max(1, (int) Math.ceil((double) shopsCount / ENTRIES_PER_PAGE));
		page = Math.max(1, Math.min(page, maxPage));

		TextUtils.sendMessage(sender, Messages.listPlayerShopsHeader,
				"player", TextUtils.getPlayerText(playerName, playerUUID),
				"shopsCount", shopsCount,
				"page", page,
				"maxPage", maxPage
		);

		listShopEntries(sender, shops, page);
	}

	private void listShopEntries(CommandSender sender, List<? extends Shopkeeper> shops, int page) {
		int startIndex = (page - 1) * ENTRIES_PER_PAGE;
		int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, shops.size());

		for (int index = startIndex; index < endIndex; index++) {
			Shopkeeper shopkeeper = shops.get(index);
			String shopName = shopkeeper.getName();

			TextUtils.sendMessage(sender, Messages.listShopsEntry,
					"shopIndex", (index + 1),
					"shopUUID", shopkeeper.getUniqueId().toString(),
					"shopSessionId", shopkeeper.getId(),
					"shopId", shopkeeper.getId(),
					"shopName", (shopName.isEmpty() ? "" : (shopName + " ")),
					"location", shopkeeper.getPositionString(),
					"shopType", shopkeeper.getType().getIdentifier(),
					"objectType", shopkeeper.getShopObject().getType().getIdentifier()
			);
		}
	}
}

