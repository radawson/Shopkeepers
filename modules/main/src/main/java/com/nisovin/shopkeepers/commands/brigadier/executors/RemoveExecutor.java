package com.nisovin.shopkeepers.commands.brigadier.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.events.PlayerDeleteShopkeeperEvent;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import com.nisovin.shopkeepers.commands.Confirmations;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.TargetShopkeeperFilter;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.TargetShopkeepersResult;
import com.nisovin.shopkeepers.event.ShopkeeperEventHelper;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopkeeper.registry.SKShopkeeperRegistry;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.Validate;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Executor for the /shopkeeper remove command.
 * <p>
 * Removes shopkeepers:
 * <ul>
 * <li>/shopkeeper remove - Removes the targeted shopkeeper</li>
 * <li>/shopkeeper remove &lt;shopkeeper&gt; - Removes a specific shopkeeper by name/id</li>
 * </ul>
 */
@SuppressWarnings("UnstableApiUsage")
public class RemoveExecutor {

	private static final String ARGUMENT_SHOPKEEPER = "shopkeeper";

	private final SKShopkeepersPlugin plugin;
	private final Confirmations confirmations;

	/**
	 * Creates a new RemoveExecutor.
	 *
	 * @param plugin
	 *            the plugin instance
	 * @param confirmations
	 *            the confirmations handler
	 */
	public RemoveExecutor(SKShopkeepersPlugin plugin, Confirmations confirmations) {
		Validate.notNull(plugin, "plugin is null");
		Validate.notNull(confirmations, "confirmations is null");
		this.plugin = plugin;
		this.confirmations = confirmations;
	}

	/**
	 * Builds the remove command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("remove")
				.requires(this::hasRemovePermission)
				// /shopkeeper remove - Remove targeted shopkeeper
				.executes(this::executeRemoveTarget)
				// /shopkeeper remove <shopkeeper> - Remove by name/id
				.then(Commands.argument(ARGUMENT_SHOPKEEPER, StringArgumentType.greedyString())
						.suggests(this::suggestShopkeepers)
						.executes(this::executeRemoveNamed));
	}

	private boolean hasRemovePermission(CommandSourceStack source) {
		CommandSender sender = source.getSender();
		return PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_OWN_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_OTHERS_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_ADMIN_PERMISSION);
	}

	private CompletableFuture<Suggestions> suggestShopkeepers(
			CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder
	) {
		String remaining = builder.getRemainingLowerCase();
		SKShopkeeperRegistry registry = plugin.getShopkeeperRegistry();

		for (Shopkeeper shopkeeper : registry.getAllShopkeepers()) {
			String name = shopkeeper.getName();
			if (!name.isEmpty() && name.toLowerCase().startsWith(remaining)) {
				builder.suggest(name);
			}
			// Also suggest by ID
			String id = String.valueOf(shopkeeper.getId());
			if (id.startsWith(remaining)) {
				builder.suggest(id);
			}
		}

		return builder.buildFuture();
	}

	// Remove targeted shopkeeper
	private int executeRemoveTarget(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();

		if (!(sender instanceof Player player)) {
			TextUtils.sendMessage(sender, Messages.commandPlayerOnly);
			return Command.SINGLE_SUCCESS;
		}

		// Find targeted shopkeeper
		TargetShopkeepersResult targetResult = ShopkeeperArgumentUtils.findTargetedShopkeepers(
				player,
				TargetShopkeeperFilter.ANY
		);

		if (!targetResult.isSuccess() || targetResult.getShopkeepers().isEmpty()) {
			TextUtils.sendMessage(sender, Messages.mustTargetShop);
			return Command.SINGLE_SUCCESS;
		}

		List<? extends Shopkeeper> targeted = targetResult.getShopkeepers();

		if (targeted.size() == 1) {
			// Single target
			Shopkeeper shopkeeper = targeted.get(0);
			if (shopkeeper instanceof AbstractShopkeeper abstractShopkeeper) {
				requestRemovalConfirmation(sender, player, abstractShopkeeper);
			}
		} else {
			// Multiple targets - require explicit selection
			TextUtils.sendMessage(sender, Messages.ambiguousShopkeeper);
		}

		return Command.SINGLE_SUCCESS;
	}

	// Remove by name/id
	private int executeRemoveNamed(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();
		String identifier = StringArgumentType.getString(context, ARGUMENT_SHOPKEEPER);

		// Try to find shopkeeper by ID first
		SKShopkeeperRegistry registry = plugin.getShopkeeperRegistry();
		Shopkeeper shopkeeper = null;

		try {
			int id = Integer.parseInt(identifier);
			shopkeeper = registry.getShopkeeperById(id);
		} catch (NumberFormatException e) {
			// Not an ID, try by name
		}

		if (shopkeeper == null) {
			// Try by name
			for (Shopkeeper sk : registry.getAllShopkeepers()) {
				if (sk.getName().equalsIgnoreCase(identifier)) {
					if (shopkeeper != null) {
						// Ambiguous
						TextUtils.sendMessage(sender, Messages.ambiguousShopkeeperName.setPlaceholderArguments(
								"name", identifier
						));
						return Command.SINGLE_SUCCESS;
					}
					shopkeeper = sk;
				}
			}
		}

		if (shopkeeper == null) {
			TextUtils.sendMessage(sender, Messages.unknownShopkeeper.setPlaceholderArguments(
					"shopkeeper", identifier
			));
			return Command.SINGLE_SUCCESS;
		}

		if (shopkeeper instanceof AbstractShopkeeper abstractShopkeeper) {
			Player senderPlayer = sender instanceof Player ? (Player) sender : null;
			requestRemovalConfirmation(sender, senderPlayer, abstractShopkeeper);
		}

		return Command.SINGLE_SUCCESS;
	}

	private void requestRemovalConfirmation(
			CommandSender sender,
			Player senderPlayer,
			AbstractShopkeeper shopkeeper
	) {
		// Check permissions
		if (!checkRemovePermission(sender, senderPlayer, shopkeeper)) {
			return;
		}

		// Request confirmation
		confirmations.awaitConfirmation(sender, () -> {
			deleteShopkeeper(sender, senderPlayer, shopkeeper);
		});

		TextUtils.sendMessage(sender, Messages.confirmRemoveShop);
		TextUtils.sendMessage(sender, Messages.confirmationRequired);
	}

	private boolean checkRemovePermission(
			CommandSender sender,
			Player senderPlayer,
			AbstractShopkeeper shopkeeper
	) {
		// Check edit access
		if (!shopkeeper.canEdit(sender, false)) {
			return false;
		}

		// Check specific permission based on shop type
		if (shopkeeper instanceof PlayerShopkeeper playerShop) {
			if (senderPlayer != null && playerShop.isOwner(senderPlayer)) {
				if (!PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_OWN_PERMISSION)) {
					TextUtils.sendMessage(sender, Messages.noPermission);
					return false;
				}
			} else {
				if (!PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_OTHERS_PERMISSION)) {
					TextUtils.sendMessage(sender, Messages.noPermission);
					return false;
				}
			}
		} else {
			if (!PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_ADMIN_PERMISSION)) {
				TextUtils.sendMessage(sender, Messages.noPermission);
				return false;
			}
		}

		return true;
	}

	private void deleteShopkeeper(CommandSender sender, Player senderPlayer, AbstractShopkeeper shopkeeper) {
		if (!shopkeeper.isValid()) {
			TextUtils.sendMessage(sender, Messages.shopAlreadyRemoved);
			return;
		}

		// Call event if player is removing
		if (senderPlayer != null) {
			PlayerDeleteShopkeeperEvent deleteEvent = ShopkeeperEventHelper.callPlayerDeleteShopkeeperEvent(
					shopkeeper,
					senderPlayer
			);
			if (deleteEvent.isCancelled()) {
				TextUtils.sendMessage(sender, Messages.shopRemovalCancelled);
				return;
			}
		}

		// Delete and save
		shopkeeper.delete(senderPlayer);
		shopkeeper.save();

		TextUtils.sendMessage(sender, Messages.shopRemoved);
	}
}

