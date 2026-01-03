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
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.Validate;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Executor for the /shopkeeper remote command.
 * <p>
 * Remotely opens a shopkeeper's trading interface.
 */
@SuppressWarnings("UnstableApiUsage")
public class RemoteExecutor {

	private static final String ARGUMENT_SHOPKEEPER = "shopkeeper";
	private static final String ARGUMENT_PLAYER = "player";

	private final SKShopkeepersPlugin plugin;

	/**
	 * Creates a new RemoteExecutor.
	 *
	 * @param plugin
	 *            the plugin instance
	 */
	public RemoteExecutor(SKShopkeepersPlugin plugin) {
		Validate.notNull(plugin, "plugin is null");
		this.plugin = plugin;
	}

	/**
	 * Builds the remote command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("remote")
				.requires(source -> PermissionUtils.hasPermission(
						source.getSender(),
						ShopkeepersPlugin.REMOTE_PERMISSION
				))
				// /shopkeeper remote <shopkeeper> [player]
				.then(Commands.argument(ARGUMENT_SHOPKEEPER, ShopkeeperArgumentType.shopkeeper())
						.executes(this::executeWithShopkeeper)
						.then(Commands.argument(ARGUMENT_PLAYER, StringArgumentType.word())
								.requires(source -> PermissionUtils.hasPermission(
										source.getSender(),
										ShopkeepersPlugin.REMOTE_OTHER_PLAYERS_PERMISSION
								))
								.suggests(this::suggestPlayers)
								.executes(this::executeWithPlayer)));
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

	// Open trading for self
	private int executeWithShopkeeper(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();

		if (!(sender instanceof Player player)) {
			TextUtils.sendMessage(sender, Messages.commandPlayerOnly);
			return Command.SINGLE_SUCCESS;
		}

		Shopkeeper shopkeeper = ShopkeeperArgumentType.getShopkeeper(context, ARGUMENT_SHOPKEEPER);
		openTrading(sender, shopkeeper, player);

		return Command.SINGLE_SUCCESS;
	}

	// Open trading for another player
	private int executeWithPlayer(CommandContext<CommandSourceStack> context) {
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

		openTrading(sender, shopkeeper, targetPlayer);

		return Command.SINGLE_SUCCESS;
	}

	private void openTrading(CommandSender sender, Shopkeeper shopkeeper, Player targetPlayer) {
		if (shopkeeper instanceof AbstractShopkeeper abstractShopkeeper) {
			// Open the trading UI
			boolean opened = abstractShopkeeper.openTradingWindow(targetPlayer);
			if (!opened) {
				TextUtils.sendMessage(sender, Messages.cannotTradeNoOffers);
			}
		}
	}
}
