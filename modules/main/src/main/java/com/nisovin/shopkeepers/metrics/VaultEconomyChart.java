package com.nisovin.shopkeepers.metrics;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.util.java.StringUtils;

import net.milkbowl.vault.economy.Economy;

/**
 * Checks whether and which economy is available on the server.
 * 
 * <p>Supports both ServiceIO (modern) and Vault (legacy) economy implementations.
 * ServiceIO implements the Vault Economy interface for compatibility.
 */
public class VaultEconomyChart extends Metrics.SimplePie {

	private static final String ECONOMY_SERVICE_CLASS_NAME = "net.milkbowl.vault.economy.Economy";

	public VaultEconomyChart() {
		super("vault_economy", () -> {
			// Check if Economy class is present (ServiceIO implements Vault Economy interface):
			Class<?> economyClass = null;
			try {
				economyClass = Class.forName(ECONOMY_SERVICE_CLASS_NAME);
			} catch (ClassNotFoundException e) {
			}
			String economyName = null;
			if (economyClass != null) {
				// Get the economy name, if an economy is present:
				// Works with both ServiceIO and Vault implementations
				RegisteredServiceProvider<Economy> registration = Bukkit.getServicesManager().getRegistration(Economy.class);
				Economy economy = (registration != null) ? registration.getProvider() : null;
				if (economy != null) {
					economyName = economy.getName();
				}
			}
			if (StringUtils.isEmpty(economyName)) {
				return "None";
			}
			return Unsafe.assertNonNull(economyName);
		});
	}
}
