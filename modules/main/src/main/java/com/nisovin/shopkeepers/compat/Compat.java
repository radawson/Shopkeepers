package com.nisovin.shopkeepers.compat;

import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.util.bukkit.ServerUtils;
import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.logging.Log;

/**
 * Provides access to the {@link CompatProvider} implementation.
 * Paper-only build supporting 1.21.5+ - tries to load 1.21.11 provider first, falls back to
 * FallbackCompatProvider for 1.21.5-1.21.10.
 */
public final class Compat {

	private static final Map<String, CompatVersion> COMPAT_VERSIONS = new LinkedHashMap<>();

	private static void register(CompatVersion version) {
		var compatVersion = version.getCompatVersion();
		if (COMPAT_VERSIONS.containsKey(compatVersion)) {
			throw new IllegalArgumentException("CompatVersion '" + compatVersion
					+ "' is already registered!");
		}

		COMPAT_VERSIONS.put(compatVersion, version);
	}

	// We have to update and rebuild our compat code whenever the mappings changed.
	// Changes to the mappings version do not necessarily align with a bump of the CraftBukkit
	// version. If the mappings changed without a bump of the CraftBukkit version, we only support
	// the latest mappings version: Our modules can only depend on specific CraftBukkit versions,
	// and building different build versions of CraftBukkit that share the same artifact version
	// overwrite each other inside the Maven repository. Also, we want to limit the number of
	// CraftBukkit versions we depend on and build.
	// Although they look similar, our compat versions do not necessarily match CraftBukkit's
	// 'Minecraft Version': Our revision number (behind the 'R') is incremented for every new compat
	// module for a specific major Minecraft version, which usually aligns with mappings updates for
	// new minor Minecraft updates, whereas CraftBukkit may increment its 'Minecraft Version' less
	// frequently. Also, our compat version may include additional tags, such as whether the module
	// is paper-specific.
	// Note: On Paper, since 1.21.6, the mappings version is no longer supported and we use the
	// Minecraft version instead.
	static {
		// Registered in the order from latest to oldest.
		register(new CompatVersion("1_21_R9_paper", "1.21.11", "1.21.11"));
		register(new CompatVersion("1_21_R9", "1.21.11", "e3cd927e07e6ff434793a0474c51b2b9"));
		// 1.21.9: Not supported. Superseded by 1.21.10.
		register(new CompatVersion("1_21_R8_paper", "1.21.10", "1.21.10"));
		register(new CompatVersion("1_21_R8", "1.21.10", "614efe5192cd0510bc2ddc5feefa155d"));
		// 1.21.8: Mappings version has not changed. We can reuse the 1.21.7 compat modules.
		register(new CompatVersion("1_21_R7_paper", Arrays.asList(
				new ServerVersion("1.21.7", "1.21.7"),
				new ServerVersion("1.21.8", "1.21.8")
		)));
		register(new CompatVersion("1_21_R7", Arrays.asList(
				new ServerVersion("1.21.7", "98b42190c84edaa346fd96106ee35d6f"),
				new ServerVersion("1.21.8", "98b42190c84edaa346fd96106ee35d6f")
		)));
		register(new CompatVersion("1_21_R6_paper", "1.21.6", "1.21.6"));
		register(new CompatVersion("1_21_R6", "1.21.6", "164f8e872cb3dff744982fca079642b2"));
		register(new CompatVersion("1_21_R5_paper", "1.21.5", "7ecad754373a5fbc43d381d7450c53a5"));
		register(new CompatVersion("1_21_R5", "1.21.5", "7ecad754373a5fbc43d381d7450c53a5"));
		register(new CompatVersion(
				FallbackCompatProvider.VERSION_ID,
				FallbackCompatProvider.VERSION_ID,
				FallbackCompatProvider.VERSION_ID
		));
	}

	public static @Nullable CompatVersion getCompatVersion(String compatVersion) {
		return COMPAT_VERSIONS.get(compatVersion); // Null if not found
	}

	/**
	 * Searches for a matching {@link CompatVersion}.
	 * 
	 * @param mappingsVersion
	 *            the mappings version
	 * @param variant
	 *            the variant, or an empty String
	 * @return the matched {@link CompatVersion}, or <code>null</code> if not suited
	 *         {@link CompatVersion} is found
	 */
	private static @Nullable CompatVersion findCompatVersion(String mappingsVersion, String variant) {
		var compatVersion = COMPAT_VERSIONS.values().stream()
				.filter(x -> x.getVariant().equals(variant)
						&& x.getSupportedServerVersions().stream()
								.anyMatch(v -> v.getMappingsVersion().equals(mappingsVersion)))
				.findFirst()
				.orElse(null);
		if (compatVersion == null && !variant.isEmpty()) {
			// Check again but also match compat versions without any variant:
			// This allows us to reuse the older compatible compat version implementations for Paper
			// servers without having to copy them.
			compatVersion = COMPAT_VERSIONS.values().stream()
					.filter(x -> !x.hasVariant()
							&& x.getSupportedServerVersions().stream()
									.anyMatch(v -> v.getMappingsVersion().equals(mappingsVersion)))
					.findFirst()
					.orElse(null);
		}
		return compatVersion;
	}

	// ----

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

		if (isForceFallback(plugin)) {
			Log.warning("Force fallback: Shopkeepers is trying to run in 'fallback mode'.");
		} else {
			var mappingsVersion = ServerUtils.getMappingsVersion();
			var variant = ServerUtils.isPaper() ? CompatVersion.VARIANT_PAPER : "";

			var compatVersion = findCompatVersion(mappingsVersion, variant);
			if (compatVersion != null) {
				String compatVersionString = compatVersion.getCompatVersion();
				try {
					Class<?> clazz = Class.forName(
							"com.nisovin.shopkeepers.compat.v" + compatVersionString + ".CompatProviderImpl"
					);
					provider = (CompatProvider) clazz.getConstructor().newInstance();
					Log.info("Compatibility provider loaded: " + compatVersionString);
					return true; // Success
				} catch (Exception e) {
					Log.severe("Failed to load compatibility provider for version '"
							+ compatVersionString + "'!", e);
					// Continue with fallback.
				}
			}

			// Incompatible server version detected:
			Log.warning("Incompatible server version: " + Bukkit.getBukkitVersion() + " (mappings: "
					+ mappingsVersion + ", variant: " + (variant.isEmpty() ? "default" : variant)
					+ ")");
			Log.warning("Shopkeepers is trying to run in 'fallback mode'.");
			Log.info("Check for updates at: " + plugin.getDescription().getWebsite());
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

	private static boolean isForceFallback(Plugin plugin) {
		var pluginDataFolder = plugin.getDataFolder().toPath();
		var forceFallbackFile = pluginDataFolder.resolve(".force-fallback");
		return Files.exists(forceFallbackFile);
	}
}
