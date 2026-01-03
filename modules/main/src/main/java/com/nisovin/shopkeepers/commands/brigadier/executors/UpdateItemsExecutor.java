package com.nisovin.shopkeepers.commands.brigadier.executors;

import java.util.concurrent.TimeUnit;

import org.bukkit.command.CommandSender;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.debug.DebugOptions;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.logging.Log;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Executor for the /shopkeeper updateitems command.
 * <p>
 * Updates all items in all shopkeeper trades by triggering the UpdateItemEvent.
 */
@SuppressWarnings("UnstableApiUsage")
public class UpdateItemsExecutor {

	/**
	 * Builds the updateitems command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("updateitems")
				.requires(source -> PermissionUtils.hasPermission(
						source.getSender(),
						ShopkeepersPlugin.UPDATE_ITEMS_PERMISSION
				))
				.executes(this::execute);
	}

	private int execute(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();

		long startNanos = System.nanoTime();
		int updatedItems = ShopkeepersAPI.updateItems();
		long durationMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

		Log.debug(DebugOptions.itemUpdates, "Updated " + updatedItems + " items (" + durationMillis
				+ " ms).");

		TextUtils.sendMessage(sender, Messages.itemsUpdated.setPlaceholderArguments(
				"count", updatedItems
		));

		return Command.SINGLE_SUCCESS;
	}
}
