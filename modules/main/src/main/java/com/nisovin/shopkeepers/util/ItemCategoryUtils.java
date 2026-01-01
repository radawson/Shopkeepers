package com.nisovin.shopkeepers.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.util.inventory.ItemUtils;

/**
 * Utility class for categorizing items and filtering special items.
 */
public final class ItemCategoryUtils {

	// Special items that should be excluded from category shops
	private static final Set<Material> SPECIAL_ITEMS = EnumSet.of(
			// Ender pearls and related
			Material.ENDER_PEARL,
			Material.ENDER_EYE,
			Material.ENDER_CHEST,
			// Command blocks
			Material.COMMAND_BLOCK,
			Material.CHAIN_COMMAND_BLOCK,
			Material.REPEATING_COMMAND_BLOCK,
			// Barrier and structure blocks
			Material.BARRIER,
			Material.STRUCTURE_BLOCK,
			Material.STRUCTURE_VOID,
			Material.JIGSAW,
			// Spawn eggs (special items)
			Material.SPAWNER,
			// Debug stick
			Material.DEBUG_STICK,
			// Knowledge book
			Material.KNOWLEDGE_BOOK
	);

	private ItemCategoryUtils() {
	}

	/**
	 * Checks if the given material is a special item that should be excluded from category shops.
	 * 
	 * @param material
	 *            the material to check
	 * @return <code>true</code> if the material is a special item
	 */
	public static boolean isSpecialItem(@Nullable Material material) {
		if (material == null) return true;
		return SPECIAL_ITEMS.contains(material);
	}

	/**
	 * Checks if the given item stack is a special item that should be excluded from category shops.
	 * 
	 * @param itemStack
	 *            the item stack to check
	 * @return <code>true</code> if the item is a special item
	 */
	public static boolean isSpecialItem(@Nullable ItemStack itemStack) {
		if (itemStack == null || ItemUtils.isEmpty(itemStack)) return true;
		return isSpecialItem(itemStack.getType());
	}

	/**
	 * Gets all materials that belong to the given category.
	 * 
	 * @param category
	 *            the category
	 * @return a list of materials in the category, excluding special items
	 */
	public static List<Material> getMaterialsForCategory(ItemCategory category) {
		if (category == null) return new ArrayList<>();

		List<Material> materials = new ArrayList<>();
		for (Material material : Material.values()) {
			if (isSpecialItem(material)) continue;
			if (!material.isItem()) continue; // Skip non-item materials like air, blocks without item form, etc.

			if (belongsToCategory(material, category)) {
				materials.add(material);
			}
		}
		return materials;
	}

	/**
	 * Checks if the given material belongs to the specified category.
	 * 
	 * @param material
	 *            the material to check
	 * @param category
	 *            the category
	 * @return <code>true</code> if the material belongs to the category
	 */
	public static boolean belongsToCategory(Material material, ItemCategory category) {
		if (material == null || category == null) return false;

		switch (category) {
		case ARMOR:
			return isArmor(material);
		case TOOLS:
			return isTool(material);
		case FOOD:
			return isFood(material);
		case WEAPONS:
			return isWeapon(material);
		case BLOCKS:
			return isBlock(material);
		case REDSTONE:
			return isRedstone(material);
		case TRANSPORTATION:
			return isTransportation(material);
		case DECORATIVE:
			return isDecorative(material);
		default:
			return false;
		}
	}

	private static boolean isArmor(Material material) {
		String name = material.name();
		return name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS")
				|| name.contains("BOOTS") || name.equals("ELYTRA") || name.equals("TURTLE_HELMET");
	}

	private static boolean isTool(Material material) {
		String name = material.name();
		return name.contains("PICKAXE") || name.contains("AXE") || name.contains("SHOVEL")
				|| name.contains("HOE") || name.equals("SHEARS") || name.equals("FLINT_AND_STEEL")
				|| name.equals("FISHING_ROD") || name.equals("COMPASS") || name.equals("CLOCK")
				|| name.equals("RECOVERY_COMPASS") || name.equals("BRUSH") || name.equals("SPYGLASS");
	}

	private static boolean isFood(Material material) {
		if (material.isEdible()) return true;
		// Also include food-related items that might not be directly edible
		String name = material.name();
		return name.contains("SEEDS") || name.contains("WHEAT") || name.contains("CARROT")
				|| name.contains("POTATO") || name.contains("BEETROOT") || name.contains("MELON")
				|| name.contains("PUMPKIN") || name.contains("COCOA") || name.contains("SUGAR")
				|| name.contains("HONEY") || name.contains("MILK") || name.contains("CAKE");
	}

	private static boolean isWeapon(Material material) {
		String name = material.name();
		return name.contains("SWORD") || name.contains("BOW") || name.contains("CROSSBOW")
				|| name.contains("TRIDENT") || name.equals("SHIELD");
	}

	private static boolean isBlock(Material material) {
		// Blocks that are commonly used for building
		if (!material.isBlock()) return false;
		String name = material.name();
		// Exclude special blocks
		if (name.contains("COMMAND") || name.contains("STRUCTURE") || name.contains("BARRIER")
				|| name.contains("SPAWNER") || name.contains("JIGSAW")) {
			return false;
		}
		// Include common building blocks
		return name.contains("STONE") || name.contains("WOOD") || name.contains("PLANK")
				|| name.contains("BRICK") || name.contains("CONCRETE") || name.contains("TERRACOTTA")
				|| name.contains("GLASS") || name.contains("SAND") || name.contains("GRAVEL")
				|| name.contains("DIRT") || name.contains("GRASS") || name.contains("CLAY")
				|| name.contains("ORE") || name.contains("LOG") || name.contains("LEAVES");
	}

	private static boolean isRedstone(Material material) {
		String name = material.name();
		return name.contains("REDSTONE") || name.contains("REPEATER") || name.contains("COMPARATOR")
				|| name.contains("PISTON") || name.contains("DISPENSER") || name.contains("DROPPER")
				|| name.contains("HOPPER") || name.contains("OBSERVER") || name.contains("DETECTOR")
				|| name.contains("LEVER") || name.contains("BUTTON") || name.contains("PRESSURE_PLATE")
				|| name.contains("TRAPDOOR") || name.contains("DOOR") || name.contains("FENCE_GATE")
				|| name.contains("NOTE_BLOCK") || name.contains("JUKEBOX") || name.contains("BELL")
				|| name.contains("DAYLIGHT_DETECTOR") || name.contains("TRIPWIRE") || name.contains("TARGET");
	}

	private static boolean isTransportation(Material material) {
		String name = material.name();
		return name.contains("BOAT") || name.contains("MINECART") || name.contains("RAIL")
				|| name.contains("SADDLE") || name.contains("HORSE_ARMOR") || name.equals("ELYTRA")
				|| name.equals("SADDLE") || name.contains("CARPET") || name.contains("BANNER");
	}

	private static boolean isDecorative(Material material) {
		// Decorative items that aren't in other categories
		if (!material.isItem()) return false;
		String name = material.name();
		return name.contains("BANNER") || name.contains("PAINTING") || name.contains("ITEM_FRAME")
				|| name.contains("FLOWER") || name.contains("PLANT") || name.contains("CORAL")
				|| name.contains("HEAD") || name.contains("SKULL") || name.contains("CANDLE")
				|| name.contains("LANTERN") || name.contains("TORCH") || name.contains("CAMPFIRE")
				|| name.contains("SIGN") || name.contains("BED") || name.contains("CARPET")
				|| name.contains("WALL_BANNER") || name.contains("WALL_SIGN");
	}
}
