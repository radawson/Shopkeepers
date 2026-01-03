package com.nisovin.shopkeepers.commands.brigadier.executors;

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
import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.commands.brigadier.arguments.ShopkeeperArgumentType;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.TargetShopkeeperFilter;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.TargetShopkeepersResult;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.teleporting.ShopkeeperTeleporter;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.Validate;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Executor for the /shopkeeper teleport command.
 * <p>
 * Teleports a player to a shopkeeper's location.
 */
@SuppressWarnings("UnstableApiUsage")
public class TeleportExecutor {

	private static final String ARGUMENT_SHOPKEEPER = "shopkeeper";
	private static final String ARGUMENT_PLAYER = "player";

	private final SKShopkeepersPlugin plugin;

	/**
	 * Creates a new TeleportExecutor.
	 *
	 * @param plugin
	 *            the plugin instance
	 */
	public TeleportExecutor(SKShopkeepersPlugin plugin) {
		Validate.notNull(plugin, "plugin is null");
		this.plugin = plugin;
	}

	/**
	 * Builds the teleport command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("teleport")
				.requires(source -> PermissionUtils.hasPermission(
						source.getSender(),
						ShopkeepersPlugin.TELEPORT_PERMISSION
				))
				// /shopkeeper teleport - Teleport to targeted shopkeeper
				.executes(this::executeToTarget)
				// /shopkeeper teleport <shopkeeper> [player] [force]
				.then(Commands.argument(ARGUMENT_SHOPKEEPER, ShopkeeperArgumentType.shopkeeper())
						.executes(this::executeWithShopkeeper)
						.then(Commands.argument(ARGUMENT_PLAYER, StringArgumentType.word())
								.suggests(this::suggestPlayers)
								.executes(this::executeWithPlayer)
								.then(Commands.literal("force")
										.executes(this::executeWithForce))));
	}

	/**
	 * Also register the 'tp' alias.
	 *
	 * @return the command builder for the alias
	 */
	public LiteralArgumentBuilder<CommandSourceStack> buildAlias() {
		return Commands.literal("tp")
				.requires(source -> PermissionUtils.hasPermission(
						source.getSender(),
						ShopkeepersPlugin.TELEPORT_PERMISSION
				))
				.executes(this::executeToTarget)
				.then(Commands.argument(ARGUMENT_SHOPKEEPER, ShopkeeperArgumentType.shopkeeper())
						.executes(this::executeWithShopkeeper)
						.then(Commands.argument(ARGUMENT_PLAYER, StringArgumentType.word())
								.suggests(this::suggestPlayers)
								.executes(this::executeWithPlayer)
								.then(Commands.literal("force")
										.executes(this::executeWithForce))));
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

	// Teleport to targeted shopkeeper
	private int executeToTarget(CommandContext<CommandSourceStack> context) {
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

		if (targetResult.getShopkeepers().size() > 1) {
			TextUtils.sendMessage(sender, Messages.ambiguousShopkeeper);
			return Command.SINGLE_SUCCESS;
		}

		Shopkeeper shopkeeper = targetResult.getShopkeepers().get(0);
		ShopkeeperTeleporter.teleport(player, shopkeeper, false, sender);

		return Command.SINGLE_SUCCESS;
	}

	// Teleport to specified shopkeeper
	private int executeWithShopkeeper(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();

		if (!(sender instanceof Player player)) {
			TextUtils.sendMessage(sender, Messages.commandPlayerOnly);
			return Command.SINGLE_SUCCESS;
		}

		Shopkeeper shopkeeper = ShopkeeperArgumentType.getShopkeeper(context, ARGUMENT_SHOPKEEPER);
		ShopkeeperTeleporter.teleport(player, shopkeeper, false, sender);

		return Command.SINGLE_SUCCESS;
	}

	// Teleport another player to shopkeeper
	private int executeWithPlayer(CommandContext<CommandSourceStack> context) {
		return executeWithPlayerInternal(context, false);
	}

	// Teleport with force
	private int executeWithForce(CommandContext<CommandSourceStack> context) {
		return executeWithPlayerInternal(context, true);
	}

	private int executeWithPlayerInternal(CommandContext<CommandSourceStack> context, boolean force) {
		CommandSender sender = context.getSource().getSender();
		Shopkeeper shopkeeper = ShopkeeperArgumentType.getShopkeeper(context, ARGUMENT_SHOPKEEPER);
		String playerName = StringArgumentType.getString(context, ARGUMENT_PLAYER);

		Player targetPlayer = Bukkit.getPlayerExact(playerName);
		if (targetPlayer == null) {
			TextUtils.sendMessage(sender, Messages.playerNotFound.setPlaceholderArguments(
					"player", playerName
			));
			return Command.SINGLE_SUCCESS;
		}

		// Check permission to teleport others
		if (targetPlayer != sender) {
			if (!PermissionUtils.hasPermission(sender, ShopkeepersPlugin.TELEPORT_OTHERS_PERMISSION)) {
				TextUtils.sendMessage(sender, Messages.noPermission);
				return Command.SINGLE_SUCCESS;
			}
		}

		ShopkeeperTeleporter.teleport(targetPlayer, shopkeeper, force, sender);

		return Command.SINGLE_SUCCESS;
	}
}

