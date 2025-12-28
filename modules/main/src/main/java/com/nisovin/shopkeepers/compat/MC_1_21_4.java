package com.nisovin.shopkeepers.compat;

import org.bukkit.Material;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Material constants for MC 1.21.4+ features.
 * Since we only support Paper 1.21.11+, these should be available, but we use CompatUtils
 * for safety in case the Material enum hasn't been updated yet.
 */
public final class MC_1_21_4 {

	public static final @Nullable Material PALE_OAK_SIGN = CompatUtils.getMaterial("PALE_OAK_SIGN");
	public static final @Nullable Material PALE_OAK_WALL_SIGN = CompatUtils.getMaterial("PALE_OAK_WALL_SIGN");
	public static final @Nullable Material PALE_OAK_HANGING_SIGN = CompatUtils.getMaterial("PALE_OAK_HANGING_SIGN");
	public static final @Nullable Material PALE_OAK_WALL_HANGING_SIGN = CompatUtils.getMaterial("PALE_OAK_WALL_HANGING_SIGN");

	private MC_1_21_4() {
	}
}
