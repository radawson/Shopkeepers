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
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.events.PlayerDeleteShopkeeperEvent;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.ShopkeeperRegistry;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import com.nisovin.shopkeepers.commands.Confirmations;
import com.nisovin.shopkeepers.commands.util.AmbiguousPlayerNameHandler;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.OwnedPlayerShopsResult;
import com.nisovin.shopkeepers.event.ShopkeeperEventHelper;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.Validate;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Executor for the /shopkeeper removeall command.
 * <p>
 * Removes multiple shopkeepers:
 * <ul>
 * <li>/shopkeeper removeall admin - Remove all admin shops</li>
 * <li>/shopkeeper removeall player - Remove all player shops</li>
 * <li>/shopkeeper removeall &lt;player-name&gt; - Remove all shops of a specific player</li>
 * </ul>
 */
@SuppressWarnings("UnstableApiUsage")
public class RemoveAllExecutor {

	private static final String ARGUMENT_PLAYER = "player";

	private final ShopkeeperRegistry shopkeeperRegistry;
	private final Confirmations confirmations;

	/**
	 * Creates a new RemoveAllExecutor.
	 *
	 * @param shopkeeperRegistry
	 *            the shopkeeper registry
	 * @param confirmations
	 *            the confirmations handler
	 */
	public RemoveAllExecutor(ShopkeeperRegistry shopkeeperRegistry, Confirmations confirmations) {
		Validate.notNull(shopkeeperRegistry, "shopkeeperRegistry is null");
		Validate.notNull(confirmations, "confirmations is null");
		this.shopkeeperRegistry = shopkeeperRegistry;
		this.confirmations = confirmations;
	}

	/**
	 * Builds the removeall command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("removeall")
				.requires(this::hasRemoveAllPermission)
				// /shopkeeper removeall admin
				.then(Commands.literal("admin")
						.requires(source -> PermissionUtils.hasPermission(
								source.getSender(),
								ShopkeepersPlugin.REMOVE_ALL_ADMIN_PERMISSION
						))
						.executes(this::executeRemoveAllAdmin))
				// /shopkeeper removeall player (all player shops)
				.then(Commands.literal("player")
						.requires(source -> PermissionUtils.hasPermission(
								source.getSender(),
								ShopkeepersPlugin.REMOVE_ALL_PLAYER_PERMISSION
						))
						.executes(this::executeRemoveAllPlayer))
				// /shopkeeper removeall <player-name>
				.then(Commands.argument(ARGUMENT_PLAYER, StringArgumentType.word())
						.suggests(this::suggestPlayers)
						.executes(this::executeRemovePlayerShops));
	}

	/**
	 * Also builds the 'deleteall' alias.
	 *
	 * @return the command builder for the alias
	 */
	public LiteralArgumentBuilder<CommandSourceStack> buildAlias() {
		return Commands.literal("deleteall")
				.requires(this::hasRemoveAllPermission)
				.then(Commands.literal("admin")
						.requires(source -> PermissionUtils.hasPermission(
								source.getSender(),
								ShopkeepersPlugin.REMOVE_ALL_ADMIN_PERMISSION
						))
						.executes(this::executeRemoveAllAdmin))
				.then(Commands.literal("player")
						.requires(source -> PermissionUtils.hasPermission(
								source.getSender(),
								ShopkeepersPlugin.REMOVE_ALL_PLAYER_PERMISSION
						))
						.executes(this::executeRemoveAllPlayer))
				.then(Commands.argument(ARGUMENT_PLAYER, StringArgumentType.word())
						.suggests(this::suggestPlayers)
						.executes(this::executeRemovePlayerShops));
	}

	private boolean hasRemoveAllPermission(CommandSourceStack source) {
		CommandSender sender = source.getSender();
		return PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_ALL_OWN_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_ALL_OTHERS_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_ALL_PLAYER_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_ALL_ADMIN_PERMISSION);
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

	// Remove all admin shops
	private int executeRemoveAllAdmin(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();
		Player senderPlayer = sender instanceof Player ? (Player) sender : null;

		// Collect admin shops
		List<Shopkeeper> adminShops = new ArrayList<>();
		for (Shopkeeper shopkeeper : shopkeeperRegistry.getAllShopkeepers()) {
			if (shopkeeper instanceof AdminShopkeeper) {
				adminShops.add(shopkeeper);
			}
		}

		if (adminShops.isEmpty()) {
			TextUtils.sendMessage(sender, Messages.noShopsFound);
			return Command.SINGLE_SUCCESS;
		}

		int shopsCount = adminShops.size();

		// Request confirmation
		confirmations.awaitConfirmation(sender, () -> {
			int removed = removeShops(senderPlayer, adminShops);
			TextUtils.sendMessage(sender, Messages.adminShopsRemoved.setPlaceholderArguments(
					"shopsCount", removed
			));
			ShopkeepersPlugin.getInstance().getShopkeeperStorage().save();
		});

		TextUtils.sendMessage(sender, Messages.confirmRemoveAllAdminShops.setPlaceholderArguments(
				"shopsCount", shopsCount
		));
		TextUtils.sendMessage(sender, Messages.confirmationRequired);

		return Command.SINGLE_SUCCESS;
	}

	// Remove all player shops
	private int executeRemoveAllPlayer(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();
		Player senderPlayer = sender instanceof Player ? (Player) sender : null;

		// Collect player shops
		List<Shopkeeper> playerShops = new ArrayList<>();
		for (Shopkeeper shopkeeper : shopkeeperRegistry.getAllShopkeepers()) {
			if (shopkeeper instanceof PlayerShopkeeper) {
				playerShops.add(shopkeeper);
			}
		}

		if (playerShops.isEmpty()) {
			TextUtils.sendMessage(sender, Messages.noShopsFound);
			return Command.SINGLE_SUCCESS;
		}

		int shopsCount = playerShops.size();

		// Request confirmation
		confirmations.awaitConfirmation(sender, () -> {
			int removed = removeShops(senderPlayer, playerShops);
			TextUtils.sendMessage(sender, Messages.playerShopsRemoved.setPlaceholderArguments(
					"shopsCount", removed
			));
			ShopkeepersPlugin.getInstance().getShopkeeperStorage().save();
		});

		TextUtils.sendMessage(sender, Messages.confirmRemoveAllPlayerShops.setPlaceholderArguments(
				"shopsCount", shopsCount
		));
		TextUtils.sendMessage(sender, Messages.confirmationRequired);

		return Command.SINGLE_SUCCESS;
	}

	// Remove specific player's shops
	private int executeRemovePlayerShops(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();
		Player senderPlayer = sender instanceof Player ? (Player) sender : null;
		String targetPlayerName = StringArgumentType.getString(context, ARGUMENT_PLAYER);

		// Check if targeting own shops
		boolean targetOwnShops = senderPlayer != null
				&& senderPlayer.getName().equalsIgnoreCase(targetPlayerName);

		if (targetOwnShops) {
			if (!PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_ALL_OWN_PERMISSION)) {
				TextUtils.sendMessage(sender, Messages.noPermission);
				return Command.SINGLE_SUCCESS;
			}
		} else {
			if (!PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_ALL_OTHERS_PERMISSION)) {
				TextUtils.sendMessage(sender, Messages.noPermission);
				return Command.SINGLE_SUCCESS;
			}
		}

		// Get target player info
		UUID targetPlayerUUID = null;
		Player onlinePlayer = Bukkit.getPlayerExact(targetPlayerName);
		if (onlinePlayer != null) {
			targetPlayerUUID = onlinePlayer.getUniqueId();
			targetPlayerName = onlinePlayer.getName();
		}

		// Get owned shops
		OwnedPlayerShopsResult result = ShopkeeperArgumentUtils.getOwnedPlayerShops(
				targetPlayerUUID,
				targetPlayerName
		);

		// Handle ambiguous player names
		Map<? extends UUID, ? extends String> matchingOwners = result.getMatchingShopOwners();
		if (matchingOwners.size() > 1) {
			boolean ambiguous = AmbiguousPlayerNameHandler.handleAmbiguousPlayerName(
					sender,
					targetPlayerName,
					matchingOwners.entrySet()
			);
			if (ambiguous) {
				return Command.SINGLE_SUCCESS;
			}
		}

		List<? extends Shopkeeper> shops = result.getShops();
		if (shops.isEmpty()) {
			TextUtils.sendMessage(sender, Messages.noShopsFound);
			return Command.SINGLE_SUCCESS;
		}

		int shopsCount = shops.size();
		String finalPlayerName = result.getPlayerName();
		UUID finalPlayerUUID = result.getPlayerUUID();

		// Request confirmation
		confirmations.awaitConfirmation(sender, () -> {
			int removed = removeShops(senderPlayer, shops);
			TextUtils.sendMessage(sender, Messages.shopsOfPlayerRemoved.setPlaceholderArguments(
					"player", TextUtils.getPlayerText(finalPlayerName, finalPlayerUUID),
					"shopsCount", removed
			));
			ShopkeepersPlugin.getInstance().getShopkeeperStorage().save();
		});

		if (targetOwnShops) {
			TextUtils.sendMessage(sender, Messages.confirmRemoveAllOwnShops.setPlaceholderArguments(
					"shopsCount", shopsCount
			));
		} else {
			TextUtils.sendMessage(sender, Messages.confirmRemoveAllShopsOfPlayer.setPlaceholderArguments(
					"player", TextUtils.getPlayerText(finalPlayerName, finalPlayerUUID),
					"shopsCount", shopsCount
			));
		}
		TextUtils.sendMessage(sender, Messages.confirmationRequired);

		return Command.SINGLE_SUCCESS;
	}

	private int removeShops(Player senderPlayer, List<? extends Shopkeeper> shops) {
		int removed = 0;
		for (Shopkeeper shopkeeper : shops) {
			if (!shopkeeper.isValid()) continue;

			if (senderPlayer != null) {
				PlayerDeleteShopkeeperEvent deleteEvent = ShopkeeperEventHelper.callPlayerDeleteShopkeeperEvent(
						shopkeeper,
						senderPlayer
				);
				if (deleteEvent.isCancelled()) continue;
			}

			shopkeeper.delete(senderPlayer);
			removed++;
		}
		return removed;
	}
}

