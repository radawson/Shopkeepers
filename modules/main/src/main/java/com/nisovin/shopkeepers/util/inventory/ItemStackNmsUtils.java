package com.nisovin.shopkeepers.util.inventory;

import java.lang.reflect.Field;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.util.annotations.ReadOnly;
import com.nisovin.shopkeepers.util.bukkit.ServerUtils;
import com.nisovin.shopkeepers.util.data.container.DataContainer;
import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.logging.Log;

import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;

/**
 * Utility class for NMS (net.minecraft.server) item stack operations.
 * <p>
 * This class provides direct access to Minecraft server internals for item stack operations that
 * cannot be done via the Bukkit API. Since this is a Paper-only build, we can use Mojang mappings
 * directly.
 */
@NonNullByDefault
public final class ItemStackNmsUtils {

	private static final Field CRAFT_ITEM_STACK_HANDLE_FIELD;

	static {
		Field field = null;
		try {
			field = CraftItemStack.class.getDeclaredField("handle");
			field.setAccessible(true);
		} catch (Exception e) {
			Log.severe("Failed to initialize CraftItemStack handle field!", e);
		}
		CRAFT_ITEM_STACK_HANDLE_FIELD = field;
	}

	private static final TagParser<Tag> TAG_PARSER = Unsafe.castNonNull(
			TagParser.create(NbtOps.INSTANCE)
	);

	/**
	 * Gets the underlying NMS item stack from a CraftItemStack without making a copy.
	 * Falls back to creating a copy if the direct access fails.
	 * 
	 * @param itemStack
	 *            the Bukkit item stack
	 * @return the NMS item stack
	 */
	private static net.minecraft.world.item.ItemStack asNMSItemStack(ItemStack itemStack) {
		assert itemStack != null;
		if (itemStack instanceof CraftItemStack && CRAFT_ITEM_STACK_HANDLE_FIELD != null) {
			try {
				return Unsafe.castNonNull(CRAFT_ITEM_STACK_HANDLE_FIELD.get(itemStack));
			} catch (Exception e) {
				Log.severe("Failed to retrieve the underlying Minecraft ItemStack!", e);
			}
		}
		return Unsafe.assertNonNull(CraftItemStack.asNMSCopy(itemStack));
	}

	/**
	 * Gets the NBT tag for an NMS item stack.
	 * 
	 * @param nmsItem
	 *            the NMS item stack
	 * @return the item stack tag
	 */
	private static CompoundTag getItemStackTag(net.minecraft.world.item.ItemStack nmsItem) {
		// Use codec-based serialization for 1.21.11+
		var holderLookupProvider = CraftRegistry.getMinecraftRegistry();
		var serializationContext = holderLookupProvider.createSerializationContext(NbtOps.INSTANCE);
		var itemTagResult = net.minecraft.world.item.ItemStack.CODEC.encodeStart(
				serializationContext,
				nmsItem
		);
		var itemTag = (CompoundTag) itemTagResult.getOrThrow();
		assert itemTag != null;
		return itemTag;
	}

	/**
	 * Checks if the provided item stack fulfills the requirements of a trading recipe requiring the
	 * given required item stack.
	 * <p>
	 * This mimics Minecraft's item comparison: This checks if the item stacks are either both empty,
	 * or of same type and the provided item stack's metadata contains all the contents of the
	 * required item stack's metadata (with any list metadata being equal).
	 * 
	 * @param provided
	 *            the provided item stack
	 * @param required
	 *            the required item stack
	 * @return <code>true</code> if the provided item stack matches the required item stack
	 */
	public static boolean matches(
			@ReadOnly @Nullable ItemStack provided,
			@ReadOnly @Nullable ItemStack required
	) {
		if (provided == required) return true;
		// If the required item is empty, then the provided item has to be empty as well:
		if (ItemUtils.isEmpty(required)) return ItemUtils.isEmpty(provided);
		else if (ItemUtils.isEmpty(provided)) return false;
		assert required != null && provided != null;
		if (provided.getType() != required.getType()) return false;
		net.minecraft.world.item.ItemStack nmsProvided = asNMSItemStack(provided);
		net.minecraft.world.item.ItemStack nmsRequired = asNMSItemStack(required);
		DataComponentMap requiredComponents = PatchedDataComponentMap.fromPatch(
				DataComponentMap.EMPTY,
				nmsRequired.getComponentsPatch()
		);
		// Compare the components according to Minecraft's matching rules (imprecise):
		return DataComponentExactPredicate.allOf(requiredComponents).test(nmsProvided);
	}

	/**
	 * Gets the item stack meta tag (NBT components) for the given item stack.
	 * 
	 * @param itemStack
	 *            the item stack
	 * @return the meta tag, or a tag with null NBT if the item stack is empty
	 */
	public static ItemStackMetaTag getItemStackMetaTag(@ReadOnly @Nullable ItemStack itemStack) {
		if (ItemUtils.isEmpty(itemStack)) {
			return new ItemStackMetaTag(null);
		}
		assert itemStack != null;

		var nmsItem = asNMSItemStack(itemStack);
		var itemTag = getItemStackTag(nmsItem);
		var componentsTag = (CompoundTag) itemTag.get("components");
		return new ItemStackMetaTag(componentsTag);
	}

	/**
	 * Checks if two item stack meta tags match.
	 * 
	 * @param provided
	 *            the provided meta tag
	 * @param required
	 *            the required meta tag
	 * @param matchPartialLists
	 *            whether to match partial lists
	 * @return <code>true</code> if the meta tags match
	 */
	public static boolean matches(
			ItemStackMetaTag provided,
			ItemStackMetaTag required,
			boolean matchPartialLists
	) {
		Validate.notNull(provided, "provided is null");
		Validate.notNull(required, "required is null");
		var providedTag = (Tag) provided.getNmsTag();
		var requiredTag = (Tag) required.getNmsTag();
		return NbtUtils.compareNbt(requiredTag, providedTag, matchPartialLists);
	}

	/**
	 * Gets the item stack components data for the given item stack.
	 * 
	 * @param itemStack
	 *            the item stack
	 * @return the components data, or <code>null</code> if the item stack is empty or has no
	 *         components
	 */
	public static @Nullable ItemStackComponentsData getItemStackComponentsData(
			@ReadOnly ItemStack itemStack
	) {
		Validate.notNull(itemStack, "itemStack is null!");
		if (ItemUtils.isEmpty(itemStack)) {
			return null;
		}

		var nmsItem = asNMSItemStack(itemStack);
		var itemTag = getItemStackTag(nmsItem);

		var componentsTag = (CompoundTag) itemTag.get("components");
		if (componentsTag == null) {
			return null;
		}

		var componentsData = ItemStackComponentsData.ofNonNull(DataContainer.create());
		componentsTag.forEach((componentKey, componentValue) -> {
			assert componentKey != null;
			// Serialized as SNBT:
			componentsData.set(componentKey, componentValue.toString());
		});
		return componentsData;
	}

	/**
	 * Deserializes an item stack from the given data.
	 * 
	 * @param dataVersion
	 *            the data version when the item stack was saved
	 * @param id
	 *            the item ID
	 * @param count
	 *            the item count
	 * @param componentsData
	 *            the components data, can be <code>null</code>
	 * @return the deserialized item stack
	 */
	public static ItemStack deserializeItemStack(
			int dataVersion,
			NamespacedKey id,
			int count,
			@Nullable ItemStackComponentsData componentsData
	) {
		Validate.notNull(id, "id is null!");
		var itemTag = new CompoundTag();
		itemTag.putString("id", id.toString());
		itemTag.putInt("count", count);

		var componentValues = componentsData != null ? componentsData.getValues() : null;
		if (componentValues != null && !componentValues.isEmpty()) {
			var componentsTag = new CompoundTag();
			componentValues.forEach((componentKey, componentValue) -> {
				assert componentKey != null;
				assert componentValue != null;
				var componentSnbt = componentValue.toString();

				Tag componentTag;
				try {
					componentTag = Unsafe.assertNonNull(TAG_PARSER.parseFully(componentSnbt));
				} catch (CommandSyntaxException e) {
					throw new IllegalArgumentException(
							"Error parsing item stack component: '" + componentSnbt + "'",
							e
					);
				}
				componentsTag.put(componentKey.toString(), componentTag);
			});
			itemTag.put("components", componentsTag);
		}

		var currentDataVersion = ServerUtils.getDataVersion();
		var convertedItemTag = (CompoundTag) DataFixers.getDataFixer().update(
				References.ITEM_STACK,
				new Dynamic<>(Unsafe.castNonNull(NbtOps.INSTANCE), itemTag),
				dataVersion,
				currentDataVersion
		).getValue();

		if (convertedItemTag.getStringOr("id", "minecraft:air").equals("minecraft:air")) {
			return new ItemStack(Material.AIR);
		}

		// Use codec-based deserialization for 1.21.11+
		var holderLookupProvider = CraftRegistry.getMinecraftRegistry();
		var serializationContext = holderLookupProvider.createSerializationContext(NbtOps.INSTANCE);
		var nmsItemResult = net.minecraft.world.item.ItemStack.CODEC.parse(
				serializationContext,
				convertedItemTag
		);
		var nmsItem = nmsItemResult.getOrThrow();
		return Unsafe.assertNonNull(CraftItemStack.asCraftMirror(nmsItem));
	}

	private ItemStackNmsUtils() {
	}
}

