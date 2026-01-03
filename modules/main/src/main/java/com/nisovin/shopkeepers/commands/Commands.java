package com.nisovin.shopkeepers.commands;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.commands.brigadier.BrigadierCommandManager;
import com.nisovin.shopkeepers.commands.brigadier.ShopkeepersCommandTree;
import com.nisovin.shopkeepers.util.java.Validate;

/**
 * Manages command registration and handling for the Shopkeepers plugin.
 * <p>
 * Commands are registered using Paper's Brigadier Command API, which provides:
 * <ul>
 * <li>Client-side command validation and error feedback</li>
 * <li>Rich tab-completion suggestions</li>
 * <li>Natural command tree structure</li>
 * <li>Permission-based command visibility</li>
 * </ul>
 */
public class Commands {

	private final SKShopkeepersPlugin plugin;
	private final Confirmations confirmations;
	private final BrigadierCommandManager brigadierManager;

	private @Nullable ShopkeepersCommandTree commandTree;

	/**
	 * Creates the Commands manager.
	 *
	 * @param plugin
	 *            the plugin instance
	 */
	public Commands(SKShopkeepersPlugin plugin) {
		this.plugin = plugin;
		this.confirmations = new Confirmations(plugin);
		this.brigadierManager = new BrigadierCommandManager(plugin);
	}

	/**
	 * Called when the plugin is enabled.
	 * <p>
	 * Registers all commands using Brigadier.
	 */
	public void onEnable() {
		confirmations.onEnable();
		// Register Brigadier commands
		brigadierManager.registerCommands(confirmations);
	}

	/**
	 * Called when the plugin is disabled.
	 * <p>
	 * Cleans up command registration.
	 */
	public void onDisable() {
		confirmations.onDisable();
		brigadierManager.unregisterCommands();
	}

	/**
	 * Called when a player quits.
	 * <p>
	 * Cleans up any pending confirmations for the player.
	 *
	 * @param player
	 *            the player who quit
	 */
	public void onPlayerQuit(Player player) {
		assert player != null;
		confirmations.onPlayerQuit(player);
	}

	/**
	 * Gets the confirmations handler.
	 *
	 * @return the confirmations handler
	 */
	public Confirmations getConfirmations() {
		return confirmations;
	}

	/**
	 * Gets the Brigadier command manager.
	 *
	 * @return the Brigadier command manager
	 */
	public BrigadierCommandManager getBrigadierManager() {
		return brigadierManager;
	}

	/**
	 * Gets the command tree.
	 *
	 * @return the command tree, or null if commands have not been registered yet
	 */
	public @Nullable ShopkeepersCommandTree getCommandTree() {
		return brigadierManager.getCommandTree();
	}
}
