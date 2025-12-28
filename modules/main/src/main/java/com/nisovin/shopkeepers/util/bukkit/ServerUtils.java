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

		// Try to get mappings version - handle deprecated method gracefully
		String mappingsVersion = "unknown";
		UnsafeValues unsafeValues = Bukkit.getUnsafe();
		
		// First, try the new Paper API (ServerBuildInfo#minecraftVersionId)
		try {
			Class<?> serverBuildInfoClass = Class.forName("io.papermc.paper.ServerBuildInfo");
			Method getInstanceMethod = serverBuildInfoClass.getMethod("getInstance");
			Object serverBuildInfo = getInstanceMethod.invoke(null);
			Method getMinecraftVersionIdMethod = serverBuildInfoClass.getMethod("minecraftVersionId");
			mappingsVersion = Unsafe.cast(getMinecraftVersionIdMethod.invoke(serverBuildInfo));
		} catch (Exception e) {
			// New API not available, try old method
			try {
				Method getMappingsVersionMethod = unsafeValues.getClass().getDeclaredMethod("getMappingsVersion");
				mappingsVersion = Unsafe.cast(getMappingsVersionMethod.invoke(unsafeValues));
			} catch (NoSuchMethodException | SecurityException e2) {
				// Method doesn't exist - use fallback
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e2) {
				// Method exists but throws exception (e.g., UnsupportedOperationException)
				// This is expected on newer Paper versions (1.21.10+) - method is deprecated
				// Use fallback value "unknown"
			}
		}
		
		MAPPINGS_VERSION = mappingsVersion;
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

	public static String getMappingsVersion() {
		return MAPPINGS_VERSION;
	}

	public static String getCraftBukkitPackage() {
		Package pkg = Unsafe.assertNonNull(Bukkit.getServer().getClass().getPackage());
		return pkg.getName();
	}

	private ServerUtils() {
	}
}
