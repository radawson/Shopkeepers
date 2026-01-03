package com.nisovin.shopkeepers.commands.brigadier.executors;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import com.nisovin.shopkeepers.api.user.User;
import com.nisovin.shopkeepers.commands.brigadier.arguments.ShopkeeperArgumentType;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.TargetShopkeeperFilter;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.TargetShopkeepersResult;
import com.nisovin.shopkeepers.commands.util.UserArgumentUtils;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.player.AbstractPlayerShopkeeper;
import com.nisovin.shopkeepers.user.SKUser;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.Validate;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Executor for the /shopkeeper transfer command.
 * <p>
 * Transfers ownership of a player shopkeeper to another player.
 */
@SuppressWarnings("UnstableApiUsage")
public class TransferExecutor {

	private static final String ARGUMENT_SHOPKEEPER = "shopkeeper";
	private static final String ARGUMENT_NEW_OWNER = "new-owner";

	private final SKShopkeepersPlugin plugin;

	/**
	 * Creates a new TransferExecutor.
	 *
	 * @param plugin
	 *            the plugin instance
	 */
	public TransferExecutor(SKShopkeepersPlugin plugin) {
		Validate.notNull(plugin, "plugin is null");
		this.plugin = plugin;
	}

	/**
	 * Builds the transfer command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("transfer")
				.requires(source -> PermissionUtils.hasPermission(
						source.getSender(),
						ShopkeepersPlugin.TRANSFER_PERMISSION
				))
				// /shopkeeper transfer <shopkeeper> <new-owner>
				.then(Commands.argument(ARGUMENT_SHOPKEEPER, ShopkeeperArgumentType.shopkeeper())
						.then(Commands.argument(ARGUMENT_NEW_OWNER, StringArgumentType.word())
								.suggests(this::suggestPlayers)
								.executes(this::executeWithArgs)))
				// /shopkeeper transfer <new-owner> - Uses targeted shopkeeper
				.then(Commands.argument(ARGUMENT_NEW_OWNER, StringArgumentType.word())
						.suggests(this::suggestPlayers)
						.executes(this::executeWithTarget));
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

	// Transfer with explicit shopkeeper argument
	private int executeWithArgs(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();
		Shopkeeper shopkeeper = ShopkeeperArgumentType.getShopkeeper(context, ARGUMENT_SHOPKEEPER);
		String newOwnerName = StringArgumentType.getString(context, ARGUMENT_NEW_OWNER);

		return transferShopkeeper(sender, shopkeeper, newOwnerName);
	}

	// Transfer using targeted shopkeeper
	private int executeWithTarget(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();
		String newOwnerName = StringArgumentType.getString(context, ARGUMENT_NEW_OWNER);

		if (!(sender instanceof Player player)) {
			TextUtils.sendMessage(sender, Messages.commandPlayerOnly);
			return Command.SINGLE_SUCCESS;
		}

		// Find targeted shopkeeper
		TargetShopkeepersResult targetResult = ShopkeeperArgumentUtils.findTargetedShopkeepers(
				player,
				TargetShopkeeperFilter.PLAYER
		);

		if (!targetResult.isSuccess() || targetResult.getShopkeepers().isEmpty()) {
			TextUtils.sendMessage(sender, Messages.mustTargetPlayerShop);
			return Command.SINGLE_SUCCESS;
		}

		if (targetResult.getShopkeepers().size() > 1) {
			TextUtils.sendMessage(sender, Messages.ambiguousShopkeeper);
			return Command.SINGLE_SUCCESS;
		}

		Shopkeeper shopkeeper = targetResult.getShopkeepers().get(0);
		return transferShopkeeper(sender, shopkeeper, newOwnerName);
	}

	private int transferShopkeeper(CommandSender sender, Shopkeeper shopkeeper, String newOwnerName) {
		// Verify it's a player shopkeeper
		if (!(shopkeeper instanceof AbstractPlayerShopkeeper playerShopkeeper)) {
			TextUtils.sendMessage(sender, Messages.mustTargetPlayerShop);
			return Command.SINGLE_SUCCESS;
		}

		// Check that the sender can edit this shopkeeper
		if (!playerShopkeeper.canEdit(sender, false)) {
			return Command.SINGLE_SUCCESS;
		}

		// Find the new owner
		User newOwner = findUser(newOwnerName);
		if (newOwner == null) {
			TextUtils.sendMessage(sender, Messages.playerNotFound.setPlaceholderArguments(
					"player", newOwnerName
			));
			return Command.SINGLE_SUCCESS;
		}

		// Set new owner
		playerShopkeeper.setOwner(newOwner);

		// Success message
		TextUtils.sendMessage(sender, Messages.ownerSet.setPlaceholderArguments(
				"owner", TextUtils.getPlayerText(newOwner)
		));

		// Save
		ShopkeepersPlugin.getInstance().getShopkeeperStorage().save();

		return Command.SINGLE_SUCCESS;
	}

	private User findUser(String name) {
		// Try online player first
		Player onlinePlayer = Bukkit.getPlayerExact(name);
		if (onlinePlayer != null) {
			return SKUser.of(onlinePlayer);
		}

		// Try offline player
		@SuppressWarnings("deprecation")
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
		if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
			return SKUser.of(offlinePlayer.getUniqueId(), offlinePlayer.getName());
		}

		return null;
	}
}

