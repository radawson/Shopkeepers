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
import com.nisovin.shopkeepers.currency.Currencies;
import com.nisovin.shopkeepers.currency.Currency;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.inventory.InventoryUtils;
import com.nisovin.shopkeepers.util.java.StringUtils;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Executor for the /shopkeeper givecurrency command.
 * <p>
 * Gives currency items to a player.
 */
@SuppressWarnings("UnstableApiUsage")
public class GiveCurrencyExecutor {

	private static final String ARGUMENT_PLAYER = "player";
	private static final String ARGUMENT_AMOUNT = "amount";
	private static final String ARGUMENT_CURRENCY = "currency";
	private static final int DEFAULT_AMOUNT = 1;

	/**
	 * Builds the givecurrency command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("givecurrency")
				.requires(source -> PermissionUtils.hasPermission(
						source.getSender(),
						ShopkeepersPlugin.GIVE_CURRENCY_PERMISSION
				))
				// /shopkeeper givecurrency - Give to self
				.executes(this::executeGiveSelf)
				// /shopkeeper givecurrency <player> [amount] [currency]
				.then(Commands.argument(ARGUMENT_PLAYER, StringArgumentType.word())
						.suggests(this::suggestPlayers)
						.executes(this::executeGivePlayer)
						.then(Commands.argument(ARGUMENT_AMOUNT, IntegerArgumentType.integer(1, 1024))
								.executes(this::executeGivePlayerWithAmount)
								.then(Commands.argument(ARGUMENT_CURRENCY, StringArgumentType.word())
										.suggests(this::suggestCurrencies)
										.executes(this::executeGivePlayerWithAmountAndCurrency))));
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

	private CompletableFuture<Suggestions> suggestCurrencies(
			CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder
	) {
		String remaining = builder.getRemainingLowerCase();
		for (Currency currency : Currencies.getAll()) {
			if (currency.getId().toLowerCase().startsWith(remaining)) {
				builder.suggest(currency.getId());
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

		giveCurrency(sender, player, DEFAULT_AMOUNT, Currencies.getBase());
		return Command.SINGLE_SUCCESS;
	}

	// Give to player
	private int executeGivePlayer(CommandContext<CommandSourceStack> context) {
		return executeGivePlayerInternal(context, DEFAULT_AMOUNT, null);
	}

	private int executeGivePlayerWithAmount(CommandContext<CommandSourceStack> context) {
		int amount = IntegerArgumentType.getInteger(context, ARGUMENT_AMOUNT);
		return executeGivePlayerInternal(context, amount, null);
	}

	private int executeGivePlayerWithAmountAndCurrency(CommandContext<CommandSourceStack> context) {
		int amount = IntegerArgumentType.getInteger(context, ARGUMENT_AMOUNT);
		String currencyId = StringArgumentType.getString(context, ARGUMENT_CURRENCY);
		return executeGivePlayerInternal(context, amount, currencyId);
	}

	private int executeGivePlayerInternal(
			CommandContext<CommandSourceStack> context,
			int amount,
			@Nullable String currencyId
	) {
		CommandSender sender = context.getSource().getSender();
		String playerName = StringArgumentType.getString(context, ARGUMENT_PLAYER);

		Player targetPlayer = Bukkit.getPlayerExact(playerName);
		if (targetPlayer == null) {
			TextUtils.sendMessage(sender, Messages.playerNotFound.setPlaceholderArguments(
					"player", playerName
			));
			return Command.SINGLE_SUCCESS;
		}

		Currency currency;
		if (currencyId != null) {
			currency = Currencies.getById(StringUtils.normalize(currencyId));
			if (currency == null) {
				TextUtils.sendMessage(sender, Messages.unknownCurrency.setPlaceholderArguments(
						"currency", currencyId
				));
				return Command.SINGLE_SUCCESS;
			}
		} else {
			currency = Currencies.getBase();
		}

		giveCurrency(sender, targetPlayer, amount, currency);
		return Command.SINGLE_SUCCESS;
	}

	private void giveCurrency(CommandSender sender, Player target, int amount, Currency currency) {
		ItemStack item = currency.getItemData().createItemStack(amount);

		PlayerInventory inventory = target.getInventory();
		@Nullable ItemStack[] contents = Unsafe.castNonNull(inventory.getStorageContents());
		int remaining = InventoryUtils.addItems(contents, item);
		InventoryUtils.setStorageContents(inventory, contents);

		if (remaining > 0) {
			item.setAmount(remaining);
			target.getWorld().dropItem(target.getEyeLocation(), item);
		}

		// Send messages
		TextUtils.sendMessage(target, Messages.currencyItemsReceived.setPlaceholderArguments(
				"amount", amount,
				"currency", currency.getDisplayName(),
				"currencyId", currency.getId()
		));

		if (sender != target) {
			TextUtils.sendMessage(sender, Messages.currencyItemsGiven.setPlaceholderArguments(
					"player", TextUtils.getPlayerText(target),
					"amount", amount,
					"currency", currency.getDisplayName(),
					"currencyId", currency.getId()
			));
		}
	}
}
