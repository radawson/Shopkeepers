package com.nisovin.shopkeepers;

import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.nisovin.shopkeepers.api.internal.InternalShopkeepersAPI;
import com.nisovin.shopkeepers.compat.Compat;
import com.nisovin.shopkeepers.config.Settings;
import com.nisovin.shopkeepers.dependencies.worldguard.WorldGuardDependency;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.util.logging.Log;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;

/**
 * Bootstrapper for the Shopkeepers Paper plugin.
 * <p>
 * Handles early initialization before the plugin instance is created.
 */
public class SKShopkeepersBootstrap implements PluginBootstrap {

	@Override
	public void bootstrap(@NonNull BootstrapContext context) {
		// Note: Plugin instance is not yet available during bootstrap.
		// Early initialization that doesn't require the plugin instance happens here.
		// Most initialization will happen in onLoad() after the plugin instance is created.
		//
		// Logger initialization: The logger is set up in onLoad() when the plugin instance
		// is available. The BootstrapContext provides a ComponentLogger, but we use the
		// plugin instance's getLogger() in onLoad() for consistency and compatibility.
		// If early logging is needed during bootstrap, Log.setLoggerFromComponentLogger()
		// could be used here with context.getLogger(), but it's not necessary for the
		// current bootstrap implementation.
	}

	@Override
	public @NonNull JavaPlugin createPlugin(@NonNull PluginProviderContext context) {
		// Create and return the plugin instance
		// The plugin's onLoad() will be called after this
		return new SKShopkeepersPlugin();
	}
}

