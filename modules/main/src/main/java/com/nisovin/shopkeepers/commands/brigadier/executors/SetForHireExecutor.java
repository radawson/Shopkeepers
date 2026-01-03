package com.nisovin.shopkeepers.commands.brigadier.executors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.TargetShopkeeperFilter;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.TargetShopkeepersResult;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Executor for the /shopkeeper setforhire command.
 * <p>
 * Sets the targeted player shopkeeper for hire using the item in the player's hand.
 */
@SuppressWarnings("UnstableApiUsage")
public class SetForHireExecutor {

	/**
	 * Builds the setforhire command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("setforhire")
				.requires(source -> {
					CommandSender sender = source.getSender();
					return sender instanceof Player
							&& PermissionUtils.hasPermission(sender, ShopkeepersPlugin.SET_FOR_HIRE_PERMISSION);
				})
				.executes(this::execute);
	}

	private int execute(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();

		if (!(sender instanceof Player player)) {
			TextUtils.sendMessage(sender, Messages.commandPlayerOnly);
			return Command.SINGLE_SUCCESS;
		}

		// Find targeted shopkeeper
		TargetShopkeepersResult targetResult = ShopkeeperArgumentUtils.findTargetedShopkeepers(
				player,
				TargetShopkeeperFilter.PLAYER
		);

		if (!targetResult.isSuccess() || targetResult.getShopkeepers().isEmpty()) {
			TextUtils.sendMessage(sender, Messages.mustTargetPlayerShop);
			return Command.SINGLE_SUCCESS;
		}

		if (targetResult.getShopkeepers().size() > 1) {
			TextUtils.sendMessage(sender, Messages.ambiguousShopkeeper);
			return Command.SINGLE_SUCCESS;
		}

		Shopkeeper shopkeeper = targetResult.getShopkeepers().get(0);

		if (!(shopkeeper instanceof PlayerShopkeeper playerShopkeeper)) {
			TextUtils.sendMessage(sender, Messages.mustTargetPlayerShop);
			return Command.SINGLE_SUCCESS;
		}

		// Check if player can edit this shopkeeper (via AbstractShopkeeper)
		if (shopkeeper instanceof AbstractShopkeeper abstractShopkeeper) {
			if (!abstractShopkeeper.canEdit(player, false)) {
				return Command.SINGLE_SUCCESS;
			}
		}

		// Get item in hand
		PlayerInventory inventory = player.getInventory();
		ItemStack hireItem = inventory.getItemInMainHand();

		if (ItemUtils.isEmpty(hireItem)) {
			TextUtils.sendMessage(sender, Messages.mustHoldHireItem);
			return Command.SINGLE_SUCCESS;
		}

		// Set for hire
		playerShopkeeper.setForHire(hireItem);

		// Success message
		TextUtils.sendMessage(sender, Messages.setForHire);

		// Save
		ShopkeepersPlugin.getInstance().getShopkeeperStorage().save();

		return Command.SINGLE_SUCCESS;
	}
}
