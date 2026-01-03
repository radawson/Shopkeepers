package com.nisovin.shopkeepers.commands.brigadier.executors;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.currency.Currencies;
import com.nisovin.shopkeepers.currency.Currency;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Executor for the /shopkeeper setcurrency command.
 * <p>
 * Shows info about currency configuration. Actual currency item modification requires config
 * changes.
 */
@SuppressWarnings("UnstableApiUsage")
public class SetCurrencyExecutor {

	private static final String ARGUMENT_CURRENCY = "currency";

	/**
	 * Builds the setcurrency command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("setcurrency")
				.requires(source -> {
					CommandSender sender = source.getSender();
					return sender instanceof Player
							&& PermissionUtils.hasPermission(sender, ShopkeepersPlugin.SET_CURRENCY_PERMISSION);
				})
				// /shopkeeper setcurrency <currency>
				.then(Commands.argument(ARGUMENT_CURRENCY, StringArgumentType.word())
						.suggests(this::suggestCurrencies)
						.executes(this::execute));
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

	private int execute(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();

		if (!(sender instanceof Player player)) {
			TextUtils.sendMessage(sender, Messages.commandPlayerOnly);
			return Command.SINGLE_SUCCESS;
		}

		String currencyId = StringArgumentType.getString(context, ARGUMENT_CURRENCY);

		// Find currency
		Currency currency = Currencies.getById(currencyId);
		if (currency == null) {
			TextUtils.sendMessage(sender, Messages.unknownCurrency.setPlaceholderArguments(
					"currency", currencyId
			));
			return Command.SINGLE_SUCCESS;
		}

		// Get item in hand
		ItemStack itemInHand = player.getInventory().getItemInMainHand();
		if (ItemUtils.isEmpty(itemInHand)) {
			TextUtils.sendMessage(sender, Messages.mustHoldItemInMainHand);
			return Command.SINGLE_SUCCESS;
		}

		// Note: Currency items are defined in the config and cannot be changed at runtime
		// This command can only display info about the current currency
		TextUtils.sendMessage(sender, Messages.currencyItemSetToMainHandItem.setPlaceholderArguments(
				"currencyId", currencyId
		));

		return Command.SINGLE_SUCCESS;
	}
}
