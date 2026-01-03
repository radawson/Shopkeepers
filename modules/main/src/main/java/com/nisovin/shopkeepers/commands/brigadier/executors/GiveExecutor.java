package com.nisovin.shopkeepers.commands.brigadier.executors;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopcreation.ShopCreationItem;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.inventory.InventoryUtils;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Executor for the /shopkeeper give command.
 * <p>
 * Gives shop creation items to a player.
 */
@SuppressWarnings("UnstableApiUsage")
public class GiveExecutor {

	private static final String ARGUMENT_PLAYER = "player";
	private static final String ARGUMENT_AMOUNT = "amount";
	private static final int DEFAULT_AMOUNT = 1;

	/**
	 * Builds the give command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("give")
				.requires(source -> PermissionUtils.hasPermission(
						source.getSender(),
						ShopkeepersPlugin.GIVE_PERMISSION
				))
				// /shopkeeper give - Give to self
				.executes(this::executeGiveSelf)
				// /shopkeeper give <player> [amount]
				.then(Commands.argument(ARGUMENT_PLAYER, StringArgumentType.word())
						.suggests(this::suggestPlayers)
						.executes(this::executeGivePlayer)
						.then(Commands.argument(ARGUMENT_AMOUNT, IntegerArgumentType.integer(1, 1024))
								.executes(this::executeGivePlayerWithAmount)));
	}

	private CompletableFuture<Suggestions> suggestPlayers(
			CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder
	) {
		String remaining = builder.getRemainingLowerCase();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getName().toLowerCase().startsWith(remaining)) {
				builder.suggest(player.getName());
			}
		}
		return builder.buildFuture();
	}

	// Give to self
	private int executeGiveSelf(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();

		if (!(sender instanceof Player player)) {
			TextUtils.sendMessage(sender, Messages.commandPlayerOnly);
			return Command.SINGLE_SUCCESS;
		}

		giveShopCreationItem(sender, player, DEFAULT_AMOUNT);
		return Command.SINGLE_SUCCESS;
	}

	// Give to player
	private int executeGivePlayer(CommandContext<CommandSourceStack> context) {
		return executeGivePlayerInternal(context, DEFAULT_AMOUNT);
	}

	private int executeGivePlayerWithAmount(CommandContext<CommandSourceStack> context) {
		int amount = IntegerArgumentType.getInteger(context, ARGUMENT_AMOUNT);
		return executeGivePlayerInternal(context, amount);
	}

	private int executeGivePlayerInternal(CommandContext<CommandSourceStack> context, int amount) {
		CommandSender sender = context.getSource().getSender();
		String playerName = StringArgumentType.getString(context, ARGUMENT_PLAYER);

		Player targetPlayer = Bukkit.getPlayerExact(playerName);
		if (targetPlayer == null) {
			TextUtils.sendMessage(sender, Messages.playerNotFound.setPlaceholderArguments(
					"player", playerName
			));
			return Command.SINGLE_SUCCESS;
		}

		giveShopCreationItem(sender, targetPlayer, amount);
		return Command.SINGLE_SUCCESS;
	}

	private void giveShopCreationItem(CommandSender sender, Player target, int amount) {
		ItemStack item = ShopCreationItem.create(amount);
		var shopCreationItem = new ShopCreationItem(item);
		shopCreationItem.applyItemMeta();

		PlayerInventory inventory = target.getInventory();
		@Nullable ItemStack[] contents = Unsafe.castNonNull(inventory.getStorageContents());
		int remaining = InventoryUtils.addItems(contents, item);
		InventoryUtils.setStorageContents(inventory, contents);

		if (remaining > 0) {
			item.setAmount(remaining);
			target.getWorld().dropItem(target.getEyeLocation(), item);
		}

		// Send messages
		TextUtils.sendMessage(target, Messages.shopCreationItemsReceived.setPlaceholderArguments(
				"amount", amount
		));

		if (sender != target) {
			TextUtils.sendMessage(sender, Messages.shopCreationItemsGiven.setPlaceholderArguments(
					"player", TextUtils.getPlayerText(target),
					"amount", amount
			));
		}
	}
}
