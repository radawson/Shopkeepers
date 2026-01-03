package com.nisovin.shopkeepers.commands.brigadier;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.commands.Confirmations;
import com.nisovin.shopkeepers.commands.brigadier.executors.*;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.Validate;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Builds and manages the main Shopkeepers command tree using Brigadier.
 * <p>
 * The command tree structure follows the pattern:
 * <pre>
 * /shopkeeper - Main command (shows help)
 *     help - Show help
 *     reload - Reload configuration
 *     debug [option] - Toggle debug mode
 *     notify [trades] - Toggle trade notifications
 *     confirm - Confirm pending action
 *     list [all|admin|player] [page] - List shopkeepers
 *     remove [shopkeeper] - Remove a shopkeeper
 *     removeall [admin|player|&lt;player&gt;] - Remove all shops
 *     give [player] [amount] - Give shop creation items
 *     givecurrency [player] [amount] [currency] - Give currency items
 *     setcurrency &lt;currency&gt; - Set currency to held item
 *     transfer [shopkeeper] &lt;new-owner&gt; - Transfer ownership
 *     teleport [shopkeeper] [player] [force] - Teleport to shopkeeper
 *     remote &lt;shopkeeper&gt; [player] - Open trading remotely
 *     remoteedit &lt;shopkeeper&gt; - Open editor remotely
 *     setforhire - Set shop for hire
 *     history [own|all|admin|&lt;player&gt;] [page] - Show trading history
 *     updateitems - Update all items in shops
 * </pre>
 */
@SuppressWarnings("UnstableApiUsage")
public class ShopkeepersCommandTree {

	/** The main command name. */
	public static final String COMMAND_NAME = "shopkeeper";
	/** Command aliases. */
	public static final List<String> COMMAND_ALIASES = Arrays.asList("shopkeepers", "shop", "sk");

	private final SKShopkeepersPlugin plugin;
	private final Confirmations confirmations;

	// Executors for various commands
	private final HelpExecutor helpExecutor;
	private final ReloadExecutor reloadExecutor;
	private final DebugExecutor debugExecutor;
	private final NotifyExecutor notifyExecutor;
	private final ConfirmExecutor confirmExecutor;
	private final ListExecutor listExecutor;
	private final RemoveExecutor removeExecutor;
	private final RemoveAllExecutor removeAllExecutor;
	private final GiveExecutor giveExecutor;
	private final GiveCurrencyExecutor giveCurrencyExecutor;
	private final SetCurrencyExecutor setCurrencyExecutor;
	private final TransferExecutor transferExecutor;
	private final TeleportExecutor teleportExecutor;
	private final RemoteExecutor remoteExecutor;
	private final RemoteEditExecutor remoteEditExecutor;
	private final SetForHireExecutor setForHireExecutor;
	private final HistoryExecutor historyExecutor;
	private final UpdateItemsExecutor updateItemsExecutor;

	/**
	 * Creates a new ShopkeepersCommandTree.
	 *
	 * @param plugin
	 *            the plugin instance
	 * @param confirmations
	 *            the confirmations handler
	 */
	public ShopkeepersCommandTree(SKShopkeepersPlugin plugin, Confirmations confirmations) {
		Validate.notNull(plugin, "plugin is null");
		Validate.notNull(confirmations, "confirmations is null");
		this.plugin = plugin;
		this.confirmations = confirmations;

		// Initialize executors
		this.helpExecutor = new HelpExecutor(plugin);
		this.reloadExecutor = new ReloadExecutor(plugin);
		this.debugExecutor = new DebugExecutor();
		this.notifyExecutor = new NotifyExecutor();
		this.confirmExecutor = new ConfirmExecutor(confirmations);
		this.listExecutor = new ListExecutor(plugin.getShopkeeperRegistry());
		this.removeExecutor = new RemoveExecutor(plugin, confirmations);
		this.removeAllExecutor = new RemoveAllExecutor(plugin.getShopkeeperRegistry(), confirmations);
		this.giveExecutor = new GiveExecutor();
		this.giveCurrencyExecutor = new GiveCurrencyExecutor();
		this.setCurrencyExecutor = new SetCurrencyExecutor();
		this.transferExecutor = new TransferExecutor(plugin);
		this.teleportExecutor = new TeleportExecutor(plugin);
		this.remoteExecutor = new RemoteExecutor(plugin);
		this.remoteEditExecutor = new RemoteEditExecutor(plugin);
		this.setForHireExecutor = new SetForHireExecutor();
		this.historyExecutor = new HistoryExecutor(plugin);
		this.updateItemsExecutor = new UpdateItemsExecutor();
	}

	/**
	 * Registers the command tree with Paper's command system.
	 *
	 * @param commands
	 *            the Paper commands registrar
	 */
	public void register(Commands commands) {
		Validate.notNull(commands, "commands is null");

		LiteralArgumentBuilder<CommandSourceStack> shopkeeperCommand = buildCommandTree();

		// Register with aliases
		commands.register(
				shopkeeperCommand.build(),
				"Main command for the Shopkeepers plugin",
				COMMAND_ALIASES
		);
	}

	/**
	 * Builds the complete command tree.
	 *
	 * @return the root command builder
	 */
	private LiteralArgumentBuilder<CommandSourceStack> buildCommandTree() {
		return Commands.literal(COMMAND_NAME)
				// Default execution (show help)
				.executes(this::executeDefault)
				// Subcommands
				.then(helpExecutor.build())
				.then(reloadExecutor.build())
				.then(debugExecutor.build())
				.then(notifyExecutor.build())
				.then(confirmExecutor.build())
				.then(listExecutor.build())
				.then(removeExecutor.build())
				.then(removeAllExecutor.build())
				.then(removeAllExecutor.buildAlias()) // 'deleteall' alias
				.then(giveExecutor.build())
				.then(giveCurrencyExecutor.build())
				.then(setCurrencyExecutor.build())
				.then(transferExecutor.build())
				.then(teleportExecutor.build())
				.then(teleportExecutor.buildAlias()) // 'tp' alias as subcommand
				.then(remoteExecutor.build())
				.then(remoteEditExecutor.build())
				.then(setForHireExecutor.build())
				.then(historyExecutor.build())
				.then(updateItemsExecutor.build());
	}

	/**
	 * Default execution when /shopkeeper is called without subcommands.
	 * Shows help information.
	 */
	private int executeDefault(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();

		// Check if sender has any shopkeeper permission
		if (!hasAnyPermission(sender)) {
			TextUtils.sendMessage(sender, Messages.noPermission);
			return Command.SINGLE_SUCCESS;
		}

		// Show help
		return helpExecutor.execute(context);
	}

	/**
	 * Checks if the sender has any shopkeeper-related permission.
	 */
	private boolean hasAnyPermission(CommandSender sender) {
		return PermissionUtils.hasPermission(sender, ShopkeepersPlugin.HELP_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.ADMIN_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.CREATE_PERMISSION);
	}

	/**
	 * Gets the plugin instance.
	 *
	 * @return the plugin
	 */
	public SKShopkeepersPlugin getPlugin() {
		return plugin;
	}

	/**
	 * Gets the confirmations handler.
	 *
	 * @return the confirmations handler
	 */
	public Confirmations getConfirmations() {
		return confirmations;
	}
}
