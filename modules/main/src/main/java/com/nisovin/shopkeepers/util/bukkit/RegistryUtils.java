package com.nisovin.shopkeepers.util.bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.util.java.CollectionUtils;
import com.nisovin.shopkeepers.util.java.PredicateUtils;
import com.nisovin.shopkeepers.util.logging.Log;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;

public class RegistryUtils {

	private static final Map<Class<?>, RegistryKey<?>> CLASS_TO_REGISTRY_KEY = new HashMap<>();

	static {
		try {
			for (var field : RegistryKey.class.getFields()) {
				if (field.getType() != RegistryKey.class) {
					continue;
				}

				// Get the type from the RegistryKey generic parameter on the field:
				var fieldType = (ParameterizedType) field.getGenericType();
				var typeArgument = fieldType.getActualTypeArguments()[0];
				Class<?> registryClass;
				if (typeArgument instanceof Class<?> typeArgumentClass) {
					registryClass = typeArgumentClass;
				} else if (typeArgument instanceof ParameterizedType typeArgumentParameterized) {
					registryClass = Unsafe.castNonNull(typeArgumentParameterized.getRawType());
				} else {
					throw new RuntimeException("Unexpected RegistryKey type parameter for field: "
							+ field.getName());
				}

				RegistryKey<?> registryKey = Unsafe.castNonNull(field.get(null));
				CLASS_TO_REGISTRY_KEY.put(registryClass, registryKey);
			}
		} catch (ReflectiveOperationException ex) {
			Log.severe("Failed to initialize registry key mapping!", ex);
		}
	}

	// Spigot has introduced RegistryAware for some Keyed types and deprecated their getKey method
	// in favor of RegistryAware#getKeyOrThrow. But this interface was never ported to Paper, so we
	// cannot use RegistryAware to resolve the deprecation warning without breaking compatibility
	// with Paper.
	// However, these objects still implement Keyed as before and simply behave like
	// RegistryAware#getKeyOrThrow internally for both Spigot and Paper. So we can avoid the
	// deprecation warning by using this method to call Keyed#getKey instead of the type's specific
	// deprecated #getKey method.
	public static NamespacedKey getKeyOrThrow(Keyed keyed) {
		return keyed.getKey();
	}

	public static <T extends Keyed> List<@NonNull T> getValues(Registry<@NonNull T> registry) {
		return registry.stream().toList();
	}

	public static <T extends Keyed> List<NamespacedKey> getKeys(Registry<@NonNull T> registry) {
		return registry.stream().map(Keyed::getKey).toList();
	}

	/**
	 * Gets the registry for the given type using Paper's Registry API.
	 * 
	 * @param <T>
	 *            the registry type
	 * @param clazz
	 *            the registry class
	 * @return the registry
	 */
	public static <T extends Keyed> Registry<T> getRegistry(Class<T> clazz) {
		// Non-null: Expected to only be used with known registry types.
		RegistryKey<T> registryKey = Unsafe.castNonNull(CLASS_TO_REGISTRY_KEY.get(clazz));
		return Unsafe.castNonNull(RegistryAccess.registryAccess().getRegistry(registryKey));
	}

	public static <T extends Keyed> @NonNull T cycleKeyed(
			Class<T> type,
			@NonNull T current,
			boolean backwards
	) {
		return cycleKeyed(type, current, backwards, PredicateUtils.alwaysTrue());
	}

	// Cycled through all values but none got accepted: Returns current value.
	public static <T extends Keyed> @NonNull T cycleKeyed(
			Class<T> type,
			@NonNull T current,
			boolean backwards,
			Predicate<? super @NonNull T> predicate
	) {
		var registry = getRegistry(type);
		// TODO Cache the registry values? Or use the iterator/stream directly to get the next
		// value.
		List<@NonNull T> values = getValues(registry);
		return CollectionUtils.cycleValue(values, current, backwards, predicate);
	}

	private RegistryUtils() {
	}
}
