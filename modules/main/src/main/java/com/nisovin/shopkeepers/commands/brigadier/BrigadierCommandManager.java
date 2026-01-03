package com.nisovin.shopkeepers.commands.brigadier;

import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.commands.Confirmations;
import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.logging.Log;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

/**
 * Manages registration of Brigadier commands for the Shopkeepers plugin.
 * <p>
 * This class handles the registration of all plugin commands using Paper's Brigadier Command API,
 * which provides client-side command validation, rich suggestions, and a natural command tree
 * structure.
 */
public class BrigadierCommandManager {

	private final SKShopkeepersPlugin plugin;
	private @Nullable ShopkeepersCommandTree commandTree;

	/**
	 * Creates a new BrigadierCommandManager.
	 *
	 * @param plugin
	 *            the plugin instance, not null
	 */
	public BrigadierCommandManager(SKShopkeepersPlugin plugin) {
		Validate.notNull(plugin, "plugin is null");
		this.plugin = plugin;
	}

	/**
	 * Registers all plugin commands with Paper's command system.
	 * <p>
	 * This should be called during plugin enable. Commands are registered via Paper's Lifecycle
	 * event system which integrates with Brigadier.
	 *
	 * @param confirmations
	 *            the confirmations handler for commands that require confirmation
	 */
	@SuppressWarnings("UnstableApiUsage")
	public void registerCommands(Confirmations confirmations) {
		Validate.notNull(confirmations, "confirmations is null");

		Log.info("Registering Brigadier commands...");

		LifecycleEventManager<Plugin> manager = plugin.getLifecycleManager();

		manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
			Commands commands = event.registrar();

			// Create the command tree
			commandTree = new ShopkeepersCommandTree(plugin, confirmations);

			// Register the main shopkeeper command
			commandTree.register(commands);

			Log.info("Brigadier commands registered successfully.");
		});
	}

	/**
	 * Gets the command tree, if registered.
	 *
	 * @return the command tree, or null if not yet registered
	 */
	public @Nullable ShopkeepersCommandTree getCommandTree() {
		return commandTree;
	}

	/**
	 * Unregisters all plugin commands.
	 * <p>
	 * Note: Brigadier commands registered via Paper's Lifecycle API are automatically unregistered
	 * when the plugin is disabled. This method is provided for explicit cleanup if needed.
	 */
	public void unregisterCommands() {
		commandTree = null;
		Log.info("Brigadier commands unregistered.");
	}
}
