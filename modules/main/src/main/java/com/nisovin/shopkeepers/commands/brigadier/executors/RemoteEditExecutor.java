package com.nisovin.shopkeepers.commands.brigadier.executors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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
 * Executor for the /shopkeeper remoteedit command.
 * <p>
 * Remotely opens a shopkeeper's editor interface.
 */
@SuppressWarnings("UnstableApiUsage")
public class RemoteEditExecutor {

	private static final String ARGUMENT_SHOPKEEPER = "shopkeeper";

	private final SKShopkeepersPlugin plugin;

	/**
	 * Creates a new RemoteEditExecutor.
	 *
	 * @param plugin
	 *            the plugin instance
	 */
	public RemoteEditExecutor(SKShopkeepersPlugin plugin) {
		Validate.notNull(plugin, "plugin is null");
		this.plugin = plugin;
	}

	/**
	 * Builds the remoteedit command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("remoteedit")
				.requires(source -> {
					CommandSender sender = source.getSender();
					return sender instanceof Player
							&& PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOTE_EDIT_PERMISSION);
				})
				// /shopkeeper remoteedit <shopkeeper>
				.then(Commands.argument(ARGUMENT_SHOPKEEPER, ShopkeeperArgumentType.shopkeeper())
						.executes(this::execute));
	}

	private int execute(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();

		if (!(sender instanceof Player player)) {
			TextUtils.sendMessage(sender, Messages.commandPlayerOnly);
			return Command.SINGLE_SUCCESS;
		}

		Shopkeeper shopkeeper = ShopkeeperArgumentType.getShopkeeper(context, ARGUMENT_SHOPKEEPER);

		if (shopkeeper instanceof AbstractShopkeeper abstractShopkeeper) {
			// Check if the player can edit this shopkeeper
			if (!abstractShopkeeper.canEdit(player, false)) {
				return Command.SINGLE_SUCCESS;
			}

			// Open the editor UI
			abstractShopkeeper.openEditorWindow(player);
		}

		return Command.SINGLE_SUCCESS;
	}
}

