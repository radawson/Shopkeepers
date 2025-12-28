package com.nisovin.shopkeepers.compat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Salmon;

/**
 * Constants for MC 1.21.3+ features.
 * Since we only support Paper 1.21.11+, these are always available.
 */
public final class MC_1_21_3 {

	public static final List<String> SALMON_VARIANTS = Collections.unmodifiableList(Arrays.asList(
			"SMALL", "MEDIUM", "LARGE"
	));

	private MC_1_21_3() {
	}
}
