package com.nisovin.shopkeepers.util.bukkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.UnsafeValues;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.util.logging.Log;

public final class ServerUtils {

	private static final boolean IS_PAPER;
	private static final String MAPPINGS_VERSION;

	static {
		boolean isPaper;
		try {
			Class.forName("io.papermc.paper.registry.RegistryAccess");
			isPaper = true;
		} catch (ClassNotFoundException e) {
			isPaper = false;
		}
	IS_PAPER = isPaper;

	String mappingsVersion;
	try {
		mappingsVersion = findMappingsVersion();
	} catch (Exception e) {
		// Since Paper 1.21.6, the server no longer supports the mappings version. We use the
		// Minecraft version instead.
		if (isPaper) {
			mappingsVersion = findPaperMinecraftVersion();
		} else {
			throw e;
		}
	}

	MAPPINGS_VERSION = mappingsVersion;
}

private static String findMappingsVersion() {
	UnsafeValues unsafeValues = Bukkit.getUnsafe();
	
	// First, try the new Paper API (ServerBuildInfo#minecraftVersionId)
	try {
		Class<?> serverBuildInfoClass = Class.forName("io.papermc.paper.ServerBuildInfo");
		Method getInstanceMethod = serverBuildInfoClass.getMethod("getInstance");
		Object serverBuildInfo = getInstanceMethod.invoke(null);
		Method getMinecraftVersionIdMethod = serverBuildInfoClass.getMethod("minecraftVersionId");
		String mappingsVersion = Unsafe.cast(getMinecraftVersionIdMethod.invoke(serverBuildInfo));
		return mappingsVersion;
	} catch (Exception e) {
		// New API not available, try old method
		try {
			Method getMappingsVersionMethod = unsafeValues.getClass().getDeclaredMethod("getMappingsVersion");
			return Unsafe.cast(getMappingsVersionMethod.invoke(unsafeValues));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e2) {
			throw new RuntimeException("Could not retrieve the server's mappings version!", e2);
		}
	}
}

private static String findPaperMinecraftVersion() {
	try {
		var serverBuildInfoClass = Class.forName("io.papermc.paper.ServerBuildInfo");
		var serverBuildInfo = serverBuildInfoClass.getMethod("buildInfo").invoke(null);
		var serverVersion = serverBuildInfoClass.getMethod("minecraftVersionId").invoke(serverBuildInfo);
		assert serverVersion != null;
		return Unsafe.assertNonNull(serverVersion.toString());
	} catch (Exception e) {
		throw new RuntimeException("Could not retrieve the server's Minecraft version!", e);
	}
	}

	/**
	 * Checks if the server has access to Paper-specific API.
	 * Paper-only builds always return true.
	 * 
	 * @return <code>true</code> if the server provides the Paper API
	 */
	public static boolean isPaper() {
		return IS_PAPER; // Always true for Paper-only builds
	}

	/**
	 * Gets the server's mappings version.
	 * <p>
	 * On Paper, since 1.21.6, the server no longer supports the mappings version, so this returns
	 * the Minecraft version instead.
	 * 
	 * @return the server's mappings version
	 */
	public static String getMappingsVersion() {
		return MAPPINGS_VERSION;
	}

	public static String getCraftBukkitPackage() {
		Package pkg = Unsafe.assertNonNull(Bukkit.getServer().getClass().getPackage());
		return pkg.getName();
	}

	/**
	 * Gets the server's current data version.
	 * <p>
	 * Prefers Paper API (ServerBuildInfo#dataVersion) with fallback to deprecated Bukkit API.
	 * 
	 * @return the server's data version
	 */
	public static int getDataVersion() {
		// Try Paper API first (ServerBuildInfo#dataVersion)
		try {
			Class<?> serverBuildInfoClass = Class.forName("io.papermc.paper.ServerBuildInfo");
			Method getInstanceMethod = serverBuildInfoClass.getMethod("getInstance");
			Object serverBuildInfo = getInstanceMethod.invoke(null);
			Method getDataVersionMethod = serverBuildInfoClass.getMethod("dataVersion");
			Object dataVersion = getDataVersionMethod.invoke(serverBuildInfo);
			if (dataVersion instanceof Integer) {
				return ((Integer) dataVersion).intValue();
			}
		} catch (Exception e) {
			// Paper API not available or failed, fall back to deprecated method
		}
		
		// Fallback to deprecated Bukkit API (still functional, just deprecated)
		@SuppressWarnings("deprecation")
		UnsafeValues unsafe = Bukkit.getUnsafe();
		return unsafe.getDataVersion();
	}

	private ServerUtils() {
	}
}
