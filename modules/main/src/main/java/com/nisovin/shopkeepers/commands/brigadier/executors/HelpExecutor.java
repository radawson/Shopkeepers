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
 * Executor for the /shopkeeper help command.
 * <p>
 * Displays help information about available Shopkeepers commands.
 */
@SuppressWarnings("UnstableApiUsage")
public class HelpExecutor {

	private final SKShopkeepersPlugin plugin;

	/**
	 * Creates a new HelpExecutor.
	 *
	 * @param plugin
	 *            the plugin instance
	 */
	public HelpExecutor(SKShopkeepersPlugin plugin) {
		Validate.notNull(plugin, "plugin is null");
		this.plugin = plugin;
	}

	/**
	 * Builds the help command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("help")
				.requires(source -> PermissionUtils.hasPermission(
						source.getSender(),
						ShopkeepersPlugin.HELP_PERMISSION
				))
				.executes(this::execute);
	}

	/**
	 * Executes the help command.
	 *
	 * @param context
	 *            the command context
	 * @return command success status
	 */
	public int execute(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();

		// Send help header
		TextUtils.sendMessage(sender, Messages.commandHelpTitle.setPlaceholderArguments(
				"version", plugin.getDescription().getVersion()
		));

		// Send command descriptions
		sendHelpEntry(sender, "/shopkeeper help", Messages.commandDescriptionHelp);
		
		if (PermissionUtils.hasPermission(sender, ShopkeepersPlugin.RELOAD_PERMISSION)) {
			sendHelpEntry(sender, "/shopkeeper reload", Messages.commandDescriptionReload);
		}
		
		if (PermissionUtils.hasPermission(sender, ShopkeepersPlugin.DEBUG_PERMISSION)) {
			sendHelpEntry(sender, "/shopkeeper debug [option]", Messages.commandDescriptionDebug);
		}
		
		if (PermissionUtils.hasPermission(sender, ShopkeepersPlugin.NOTIFY_TRADES_PERMISSION)) {
			sendHelpEntry(sender, "/shopkeeper notify", Messages.commandDescriptionNotify);
		}
		
		if (PermissionUtils.hasPermission(sender, ShopkeepersPlugin.LIST_OWN_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.LIST_OTHERS_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.LIST_ADMIN_PERMISSION)) {
			sendHelpEntry(sender, "/shopkeeper list [all|admin|<player>] [page]", Messages.commandDescriptionList);
		}
		
		if (PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_OWN_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_OTHERS_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_ADMIN_PERMISSION)) {
			sendHelpEntry(sender, "/shopkeeper remove [shopkeeper]", Messages.commandDescriptionRemove);
		}
		
		if (PermissionUtils.hasPermission(sender, ShopkeepersPlugin.GIVE_PERMISSION)) {
			sendHelpEntry(sender, "/shopkeeper give [player] [amount]", Messages.commandDescriptionGive);
		}
		
		if (PermissionUtils.hasPermission(sender, ShopkeepersPlugin.GIVE_CURRENCY_PERMISSION)) {
			sendHelpEntry(sender, "/shopkeeper givecurrency [player] [amount]", Messages.commandDescriptionGiveCurrency);
		}

		return Command.SINGLE_SUCCESS;
	}

	/**
	 * Sends a single help entry to the sender.
	 */
	private void sendHelpEntry(CommandSender sender, String usage, com.nisovin.shopkeepers.text.Text description) {
		TextUtils.sendMessage(sender, Messages.commandHelpUsageFormat.setPlaceholderArguments(
				"usage", usage
		));
		TextUtils.sendMessage(sender, Messages.commandHelpDescriptionFormat.setPlaceholderArguments(
				"description", description
		));
	}
}

