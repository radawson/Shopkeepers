package com.nisovin.shopkeepers.util.bukkit;

import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Golem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.MainHand;
import org.bukkit.profile.PlayerProfile;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.nisovin.shopkeepers.shopobjects.entity.base.EntityAI;
import com.nisovin.shopkeepers.util.logging.Log;

import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;

/**
 * Utility class for NMS (net.minecraft.server) entity operations.
 * <p>
 * This class provides direct access to Minecraft server internals for entity manipulation that
 * cannot be done via the Bukkit API. Since this is a Paper-only build, we can use Mojang mappings
 * directly.
 */
@NonNullByDefault
public final class EntityNmsUtils {

	/**
	 * Overwrites the AI of the given living entity to make it look at nearby players.
	 * <p>
	 * This clears all existing AI goals and replaces them with a simple "look at player" goal.
	 * 
	 * @param entity
	 *            the living entity, must be a {@link Mob}
	 */
	public static void overwriteLivingEntityAI(LivingEntity entity) {
		// Example: Armor stands are living, but not insentient/Mob.
		if (!(entity instanceof Mob)) return;
		try {
			net.minecraft.world.entity.Mob mcMob = ((CraftMob) entity).getHandle();

			// Overwrite the goal selector:
			GoalSelector goalSelector = mcMob.goalSelector;

			// Clear the old goals: Removes all goals from the "availableGoals". During the next
			// tick, the "lockedFlags" (active goals) are updated as well.
			goalSelector.removeAllGoals(goal -> true);

			// Add new goals:
			goalSelector.addGoal(
					0,
					new LookAtPlayerGoal(
							mcMob,
							net.minecraft.world.entity.player.Player.class,
							EntityAI.LOOK_RANGE,
							1.0F
					)
			);

			// Overwrite the target selector:
			GoalSelector targetSelector = mcMob.targetSelector;

			// Clear old target goals:
			targetSelector.removeAllGoals(goal -> true);
		} catch (Exception e) {
			Log.severe("Failed to override mob AI!", e);
		}
	}

	/**
	 * Ticks the AI of the given living entity for the specified number of ticks.
	 * <p>
	 * This is used to manually control entity AI behavior, such as making entities look at nearby
	 * players.
	 * 
	 * @param entity
	 *            the living entity, must be a {@link Mob}
	 * @param ticks
	 *            the number of ticks to simulate
	 */
	public static void tickAI(LivingEntity entity, int ticks) {
		net.minecraft.world.entity.LivingEntity mcLivingEntity = ((CraftLivingEntity) entity).getHandle();
		// Example: Armor stands are living, but not insentient/Mob.
		if (!(mcLivingEntity instanceof net.minecraft.world.entity.Mob)) return;
		net.minecraft.world.entity.Mob mcMob = (net.minecraft.world.entity.Mob) mcLivingEntity;

		// Clear the sensing cache. This sensing cache is reused for the individual ticks.
		mcMob.getSensing().tick();
		for (int i = 0; i < ticks; ++i) {
			mcMob.goalSelector.tick();
			if (!mcMob.getLookControl().isLookingAtTarget()) {
				// If there is no target to look at, the entity rotates towards its current body
				// rotation.
				// We reset the entity's body rotation here to the initial yaw it was spawned with,
				// causing it to rotate back towards this initial direction whenever it has no
				// target to look at anymore.
				// This rotating back towards its initial orientation only works if the entity is
				// still ticked: Since we only tick shopkeeper mobs near players, the entity may
				// remain in its previous rotation whenever the last nearby player teleports away,
				// until the ticking resumes when a player comes close again.

				// Setting the body rotation also ensures that it initially matches the entity's
				// intended yaw, because CraftBukkit itself does not automatically set the body
				// rotation when spawning the entity (only its yRot and head rotation are set).
				// Omitting this would therefore cause the entity to initially rotate towards some
				// random direction if it is being ticked and has no target to look at.
				mcMob.setYBodyRot(mcMob.getYRot());
			}
			// Tick the look controller:
			// This makes the entity's head (and indirectly also its body) rotate towards the
			// current target.
			mcMob.getLookControl().tick();
		}
		mcMob.getSensing().tick(); // Clear the sensing cache
	}

	/**
	 * Sets whether the entity is on the ground.
	 * 
	 * @param entity
	 *            the entity
	 * @param onGround
	 *            whether the entity is on the ground
	 */
	public static void setOnGround(Entity entity, boolean onGround) {
		net.minecraft.world.entity.Entity mcEntity = ((CraftEntity) entity).getHandle();
		mcEntity.setOnGround(onGround);
	}

	/**
	 * Sets the entity to have no physics (noclip).
	 * 
	 * @param entity
	 *            the entity
	 */
	public static void setNoclip(Entity entity) {
		net.minecraft.world.entity.Entity mcEntity = ((CraftEntity) entity).getHandle();
		mcEntity.noPhysics = true;
	}

	/**
	 * Performs any version-specific setup that needs to happen before the entity is spawned.
	 * <p>
	 * The available operations may be limited during this phase of the entity spawning.
	 * 
	 * @param entity
	 *            the entity
	 */
	public static void prepareEntity(Entity entity) {
		// No special preparation needed for 1.21.11+
	}

	/**
	 * Performs any version-specific setup of the entity that needs to happen right after the entity
	 * was spawned.
	 * 
	 * @param entity
	 *            the entity
	 */
	public static void setupSpawnedEntity(Entity entity) {
		// No special setup needed for 1.21.11+
	}

	/**
	 * Returns whether NoAI disables gravity on this version.
	 * <p>
	 * On some MC versions (e.g. MC 1.9, 1.10) NoAI only disables AI.
	 * 
	 * @return <code>true</code> if NoAI disables gravity
	 */
	public static boolean isNoAIDisablingGravity() {
		return true; // 1.21.11+ always disables gravity with NoAI
	}

	// MC 1.21.9+ Copper Golem features

	/**
	 * Sets the weather state of a copper golem.
	 * <p>
	 * Note: This is a placeholder implementation. The actual NMS structure for copper golems
	 * needs to be determined.
	 * 
	 * @param golem
	 *            the copper golem
	 * @param weatherState
	 *            the weather state ("UNAFFECTED", "EXPOSED", "WEATHERED", "OXIDIZED")
	 */
	public static void setCopperGolemWeatherState(Golem golem, String weatherState) {
		// TODO: Implement using NMS once structure is known
		// For now, log that this was called
		Log.debug("Setting copper golem weather state to: " + weatherState);
	}

	/**
	 * Sets the next weathering tick for a copper golem.
	 * <p>
	 * Use -2 to disable weathering state changes (waxed).
	 * <p>
	 * Note: This is a placeholder implementation.
	 * 
	 * @param golem
	 *            the copper golem
	 * @param tick
	 *            the next weathering tick, or -2 to disable
	 */
	public static void setCopperGolemNextWeatheringTick(Golem golem, int tick) {
		// TODO: Implement using NMS once structure is known
		Log.debug("Setting copper golem next weathering tick to: " + tick);
	}

	// MC 1.21.9+ Mannequin features

	/**
	 * Sets whether the mannequin's description is hidden.
	 * <p>
	 * Note: This is a placeholder implementation.
	 * 
	 * @param mannequin
	 *            the mannequin entity
	 * @param hideDescription
	 *            whether to hide the description
	 */
	public static void setMannequinHideDescription(LivingEntity mannequin, boolean hideDescription) {
		// TODO: Implement using NMS once structure is known
		Log.debug("Setting mannequin hide description to: " + hideDescription);
	}

	/**
	 * Sets the description of a mannequin.
	 * <p>
	 * Note: This is a placeholder implementation.
	 * 
	 * @param mannequin
	 *            the mannequin entity
	 * @param description
	 *            the description, or <code>null</code> to clear
	 */
	public static void setMannequinDescription(LivingEntity mannequin, @Nullable String description) {
		// TODO: Implement using NMS once structure is known
		Log.debug("Setting mannequin description to: " + description);
	}

	/**
	 * Sets the main hand of a mannequin.
	 * <p>
	 * Note: This is a placeholder implementation. The actual NMS structure for mannequins
	 * needs to be determined.
	 * 
	 * @param mannequin
	 *            the mannequin entity
	 * @param mainHand
	 *            the main hand
	 */
	public static void setMannequinMainHand(LivingEntity mannequin, MainHand mainHand) {
		// TODO: Implement using NMS once structure is known
		// Mannequins in 1.21.9+ may have a different structure than armor stands
		Log.debug("Setting mannequin main hand to: " + mainHand);
	}

	/**
	 * Sets the pose of a mannequin.
	 * <p>
	 * This uses the Bukkit API directly.
	 * 
	 * @param mannequin
	 *            the mannequin entity
	 * @param pose
	 *            the pose
	 */
	public static void setMannequinPose(LivingEntity mannequin, Pose pose) {
		try {
			// Use Bukkit API directly - Pose is available in Bukkit API
			mannequin.setPose(pose);
		} catch (Exception e) {
			Log.severe("Failed to set mannequin pose!", e);
		}
	}

	/**
	 * Sets the player profile of a mannequin.
	 * <p>
	 * Note: This is a placeholder implementation. Paper 1.21.9+ may have API support for this.
	 * 
	 * @param mannequin
	 *            the mannequin entity
	 * @param profile
	 *            the player profile, or <code>null</code> to clear
	 */
	@SuppressWarnings("deprecation")
	public static void setMannequinProfile(LivingEntity mannequin, @Nullable PlayerProfile profile) {
		// TODO: Implement using Paper API or NMS once available
		Log.debug("Setting mannequin profile");
	}

	// MC 1.21.11+ Zombie Nautilus features

	/**
	 * Sets the variant of a zombie nautilus.
	 * <p>
	 * Note: This is a placeholder implementation.
	 * 
	 * @param zombieNautilus
	 *            the zombie nautilus entity
	 * @param variant
	 *            the variant namespaced key
	 */
	public static void setZombieNautilusVariant(LivingEntity zombieNautilus, NamespacedKey variant) {
		// TODO: Implement using NMS once structure is known
		Log.debug("Setting zombie nautilus variant to: " + variant);
	}

	/**
	 * Cycles the zombie nautilus variant.
	 * <p>
	 * Note: This is a placeholder implementation.
	 * 
	 * @param variant
	 *            the current variant
	 * @param backwards
	 *            whether to cycle backwards
	 * @return the next variant
	 */
	public static NamespacedKey cycleZombieNautilusVariant(NamespacedKey variant, boolean backwards) {
		// TODO: Implement using RegistryUtils to cycle through variants
		Log.debug("Cycling zombie nautilus variant: " + variant + " backwards: " + backwards);
		return variant; // Return original for now
	}

	private EntityNmsUtils() {
	}
}

