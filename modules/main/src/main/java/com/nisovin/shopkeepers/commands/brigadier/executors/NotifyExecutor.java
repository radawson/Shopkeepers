package com.nisovin.shopkeepers.commands.brigadier.executors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.tradenotifications.NotificationUserPreferences;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Executor for the /shopkeeper notify command.
 * <p>
 * Toggles trade notifications for the executing player.
 */
@SuppressWarnings("UnstableApiUsage")
public class NotifyExecutor {

	/**
	 * Builds the notify command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("notify")
				.requires(source -> {
					CommandSender sender = source.getSender();
					return sender instanceof Player
							&& PermissionUtils.hasPermission(sender, ShopkeepersPlugin.NOTIFY_TRADES_PERMISSION);
				})
				// /shopkeeper notify trades - Toggle trade notifications
				.then(Commands.literal("trades")
						.executes(this::execute))
				// /shopkeeper notify - Default to trades
				.executes(this::execute);
	}

	/**
	 * Executes the notify command.
	 *
	 * @param context
	 *            the command context
	 * @return command success status
	 */
	private int execute(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();

		if (!(sender instanceof Player player)) {
			TextUtils.sendMessage(sender, Messages.commandPlayerOnly);
			return Command.SINGLE_SUCCESS;
		}

		// Toggle trade notifications for the player
		NotificationUserPreferences userPreferences = SKShopkeepersPlugin.getInstance()
				.getTradeNotifications()
				.getUserPreferences();

		boolean newState = !userPreferences.isNotifyOnTrades(player);
		userPreferences.setNotifyOnTrades(player, newState);

		if (newState) {
			TextUtils.sendMessage(player, Messages.tradeNotificationsEnabled);
		} else {
			TextUtils.sendMessage(player, Messages.tradeNotificationsDisabled);
		}

		return Command.SINGLE_SUCCESS;
	}
}
