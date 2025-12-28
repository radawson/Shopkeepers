package com.nisovin.shopkeepers.compat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.logging.Log;

/**
 * Provides access to the {@link CompatProvider} implementation.
 * Paper-only build supporting 1.21.5+ - tries to load 1.21.11 provider first, falls back to
 * FallbackCompatProvider for 1.21.5-1.21.10.
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
	 * Loads the compatibility provider for Paper 1.21.5+.
	 * Tries to load the 1.21.11 provider first, then falls back to FallbackCompatProvider
	 * for earlier 1.21.x versions.
	 * 
	 * @param plugin
	 *            the plugin instance
	 * @return <code>true</code> if the provider was successfully loaded
	 */
	public static boolean load(Plugin plugin) {
		if (provider != null) {
			throw new IllegalStateException("Provider already loaded!");
		}

		// First, try to load the 1.21.11 provider (should work for all 1.21.x versions)
		try {
			Class<?> clazz = Class.forName(
					"com.nisovin.shopkeepers.compat.v1_21_11.CompatProviderImpl"
			);
			provider = (CompatProvider) clazz.getConstructor().newInstance();
			Log.info("Compatibility provider loaded: 1_21_11");
			return true;
		} catch (ClassNotFoundException e) {
			// Provider class not found - try fallback for 1.21.5-1.21.10
			Log.debug("1.21.11 provider not found, trying fallback for 1.21.5-1.21.10...");
		} catch (Exception e) {
			// Other error loading 1.21.11 provider - log and try fallback
			Log.debug("Failed to load 1.21.11 provider: " + e.getMessage() + ", trying fallback...");
		}

		// Fallback: Check if server is 1.21.5+ and use FallbackCompatProvider
		if (isServerVersionAtLeast(1, 21, 5)) {
			try {
				provider = new FallbackCompatProvider();
				Log.info("Compatibility provider loaded: FallbackCompatProvider (for Paper 1.21.5-1.21.10)");
				return true;
			} catch (Exception e) {
				Log.severe("Failed to load fallback compatibility provider!", e);
				Log.severe("Shopkeepers requires Paper 1.21.5 or later.");
				return false;
			}
		} else {
			Log.severe("Failed to load compatibility provider!");
			Log.severe("Shopkeepers requires Paper 1.21.5 or later.");
			return false;
		}
	}

	/**
	 * Checks if the server version is at least the specified version.
	 * 
	 * @param major
	 *            the major version (e.g., 1)
	 * @param minor
	 *            the minor version (e.g., 21)
	 * @param patch
	 *            the patch version (e.g., 5)
	 * @return <code>true</code> if the server version is at least the specified version
	 */
	private static boolean isServerVersionAtLeast(int major, int minor, int patch) {
		try {
			String version = Bukkit.getVersion();
			// Version string format: "git-Paper-1.21.10-129-3e25649 (MC: 1.21.10)"
			// Extract MC version from parentheses
			int mcStart = version.indexOf("(MC: ");
			if (mcStart >= 0) {
				String mcVersion = version.substring(mcStart + 5, version.indexOf(")", mcStart));
				return compareVersion(mcVersion, major, minor, patch) >= 0;
			}
			// Fallback: try to extract from version string directly
			// Look for pattern like "1.21.10"
			java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
			java.util.regex.Matcher matcher = pattern.matcher(version);
			if (matcher.find()) {
				int vMajor = Integer.parseInt(matcher.group(1));
				int vMinor = Integer.parseInt(matcher.group(2));
				int vPatch = Integer.parseInt(matcher.group(3));
				return compareVersion(vMajor, vMinor, vPatch, major, minor, patch) >= 0;
			}
		} catch (Exception e) {
			Log.debug("Could not parse server version: " + e.getMessage());
		}
		// If we can't determine version, assume it's supported (optimistic)
		return true;
	}

	/**
	 * Compares a version string to target version numbers.
	 * 
	 * @param version
	 *            version string (e.g., "1.21.10")
	 * @param targetMajor
	 *            target major version
	 * @param targetMinor
	 *            target minor version
	 * @param targetPatch
	 *            target patch version
	 * @return negative if version is less, 0 if equal, positive if greater
	 */
	private static int compareVersion(String version, int targetMajor, int targetMinor, int targetPatch) {
		String[] parts = version.split("\\.");
		if (parts.length < 3) return -1;
		try {
			int major = Integer.parseInt(parts[0]);
			int minor = Integer.parseInt(parts[1]);
			int patch = Integer.parseInt(parts[2]);
			return compareVersion(major, minor, patch, targetMajor, targetMinor, targetPatch);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	/**
	 * Compares two version numbers.
	 * 
	 * @return negative if v1 < v2, 0 if equal, positive if v1 > v2
	 */
	private static int compareVersion(int major1, int minor1, int patch1, int major2, int minor2, int patch2) {
		if (major1 != major2) return major1 - major2;
		if (minor1 != minor2) return minor1 - minor2;
		return patch1 - patch2;
	}
}
