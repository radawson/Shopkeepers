package com.nisovin.shopkeepers.commands.brigadier.executors;

import org.bukkit.command.CommandSender;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.Validate;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Executor for the /shopkeeper reload command.
 * <p>
 * Reloads the plugin's configuration.
 */
@SuppressWarnings("UnstableApiUsage")
public class ReloadExecutor {

	private final SKShopkeepersPlugin plugin;

	/**
	 * Creates a new ReloadExecutor.
	 *
	 * @param plugin
	 *            the plugin instance
	 */
	public ReloadExecutor(SKShopkeepersPlugin plugin) {
		Validate.notNull(plugin, "plugin is null");
		this.plugin = plugin;
	}

	/**
	 * Builds the reload command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("reload")
				.requires(source -> PermissionUtils.hasPermission(
						source.getSender(),
						ShopkeepersPlugin.RELOAD_PERMISSION
				))
				.executes(this::execute);
	}

	/**
	 * Executes the reload command.
	 *
	 * @param context
	 *            the command context
	 * @return command success status
	 */
	private int execute(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();

		// Reload the plugin
		plugin.reload();
		TextUtils.sendMessage(sender, Messages.configReloaded);

		return Command.SINGLE_SUCCESS;
	}
}

