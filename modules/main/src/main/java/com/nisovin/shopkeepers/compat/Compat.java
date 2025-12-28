package com.nisovin.shopkeepers.compat;

import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.logging.Log;

/**
 * Provides access to the {@link CompatProvider} implementation.
 * Paper-only build targeting 1.21.11 - loads provider directly without version detection.
 */
public final class Compat {

	private static @Nullable CompatProvider provider;

	public static boolean hasProvider() {
		return (provider != null);
	}

	public static CompatProvider getProvider() {
		return Validate.State.notNull(provider, "Compat provider is not set up!");
	}

	/**
	 * Loads the compatibility provider for Paper 1.21.11.
	 * 
	 * @param plugin
	 *            the plugin instance
	 * @return <code>true</code> if the provider was successfully loaded
	 */
	public static boolean load(Plugin plugin) {
		if (provider != null) {
			throw new IllegalStateException("Provider already loaded!");
		}

		try {
			// Directly load the 1.21.11 provider - no version detection needed
			Class<?> clazz = Class.forName(
					"com.nisovin.shopkeepers.compat.v1_21_11.CompatProviderImpl"
			);
			provider = (CompatProvider) clazz.getConstructor().newInstance();
			Log.info("Compatibility provider loaded: 1_21_11");
			return true; // Success
		} catch (Exception e) {
			Log.severe("Failed to load compatibility provider for Paper 1.21.11!", e);
			Log.severe("Shopkeepers requires Paper 1.21.11 or later.");
			return false;
		}
	}
}
