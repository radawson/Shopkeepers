package com.nisovin.shopkeepers.util;

/**
 * Categories of items that can be used to pre-populate shops.
 */
public enum ItemCategory {
	ARMOR("armor"),
	TOOLS("tools"),
	FOOD("food"),
	WEAPONS("weapons"),
	BLOCKS("blocks"),
	REDSTONE("redstone"),
	TRANSPORTATION("transportation"),
	DECORATIVE("decorative");

	private final String identifier;

	ItemCategory(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Gets the identifier for this category.
	 * 
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Gets the category by its identifier.
	 * 
	 * @param identifier
	 *            the identifier
	 * @return the category, or <code>null</code> if not found
	 */
	public static ItemCategory fromIdentifier(String identifier) {
		if (identifier == null) return null;
		for (ItemCategory category : values()) {
			if (category.identifier.equalsIgnoreCase(identifier)) {
				return category;
			}
		}
		return null;
	}
}
