package com.nisovin.shopkeepers.commands.brigadier.executors;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack;
import com.nisovin.shopkeepers.config.Settings.DerivedSettings;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.tradelog.data.PlayerRecord;
import com.nisovin.shopkeepers.tradelog.data.ShopRecord;
import com.nisovin.shopkeepers.tradelog.data.TradeRecord;
import com.nisovin.shopkeepers.tradelog.history.PlayerSelector;
import com.nisovin.shopkeepers.tradelog.history.ShopSelector;
import com.nisovin.shopkeepers.tradelog.history.TradingHistoryProvider;
import com.nisovin.shopkeepers.tradelog.history.TradingHistoryRequest;
import com.nisovin.shopkeepers.tradelog.history.TradingHistoryResult;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.Range;
import com.nisovin.shopkeepers.util.java.TimeUtils;
import com.nisovin.shopkeepers.util.java.Validate;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Executor for the /shopkeeper history command.
 * <p>
 * Shows trading history:
 * <ul>
 * <li>/shopkeeper history - Show own trading history (default: own shops)</li>
 * <li>/shopkeeper history own [page] - Show trades with own shops</li>
 * <li>/shopkeeper history all [page] - Show all trades (admin permission)</li>
 * <li>/shopkeeper history admin [page] - Show trades with admin shops</li>
 * <li>/shopkeeper history &lt;player&gt; [page] - Show trades by a specific player</li>
 * </ul>
 */
@SuppressWarnings("UnstableApiUsage")
public class HistoryExecutor {

	private static final int ENTRIES_PER_PAGE = 10;
	private static final String ARGUMENT_PAGE = "page";
	private static final String ARGUMENT_PLAYER = "player";

	private final SKShopkeepersPlugin plugin;

	/**
	 * Creates a new HistoryExecutor.
	 *
	 * @param plugin
	 *            the plugin instance
	 */
	public HistoryExecutor(SKShopkeepersPlugin plugin) {
		Validate.notNull(plugin, "plugin is null");
		this.plugin = plugin;
	}

	/**
	 * Builds the history command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("history")
				.requires(this::hasHistoryPermission)
				// /shopkeeper history - Default (own shops)
				.executes(this::executeOwn)
				// /shopkeeper history own [page]
				.then(Commands.literal("own")
						.executes(this::executeOwn)
						.then(Commands.argument(ARGUMENT_PAGE, IntegerArgumentType.integer(1))
								.executes(this::executeOwnWithPage)))
				// /shopkeeper history all [page]
				.then(Commands.literal("all")
						.requires(source -> PermissionUtils.hasPermission(
								source.getSender(),
								ShopkeepersPlugin.HISTORY_ADMIN_PERMISSION
						))
						.executes(this::executeAll)
						.then(Commands.argument(ARGUMENT_PAGE, IntegerArgumentType.integer(1))
								.executes(this::executeAllWithPage)))
				// /shopkeeper history admin [page]
				.then(Commands.literal("admin")
						.requires(source -> PermissionUtils.hasPermission(
								source.getSender(),
								ShopkeepersPlugin.HISTORY_ADMIN_PERMISSION
						))
						.executes(this::executeAdmin)
						.then(Commands.argument(ARGUMENT_PAGE, IntegerArgumentType.integer(1))
								.executes(this::executeAdminWithPage)))
				// /shopkeeper history <player> [page]
				.then(Commands.argument(ARGUMENT_PLAYER, StringArgumentType.word())
						.requires(source -> PermissionUtils.hasPermission(
								source.getSender(),
								ShopkeepersPlugin.HISTORY_ADMIN_PERMISSION
						))
						.suggests(this::suggestPlayers)
						.executes(this::executePlayer)
						.then(Commands.argument(ARGUMENT_PAGE, IntegerArgumentType.integer(1))
								.executes(this::executePlayerWithPage)));
	}

	private boolean hasHistoryPermission(CommandSourceStack source) {
		CommandSender sender = source.getSender();
		return PermissionUtils.hasPermission(sender, ShopkeepersPlugin.HISTORY_OWN_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.HISTORY_ADMIN_PERMISSION);
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

	// Own shops history
	private int executeOwn(CommandContext<CommandSourceStack> context) {
		return executeOwnInternal(context, 1);
	}

	private int executeOwnWithPage(CommandContext<CommandSourceStack> context) {
		int page = IntegerArgumentType.getInteger(context, ARGUMENT_PAGE);
		return executeOwnInternal(context, page);
	}

	private int executeOwnInternal(CommandContext<CommandSourceStack> context, int page) {
		CommandSender sender = context.getSource().getSender();

		if (!(sender instanceof Player player)) {
			TextUtils.sendMessage(sender, Messages.commandPlayerOnly);
			return Command.SINGLE_SUCCESS;
		}

		if (!PermissionUtils.hasPermission(sender, ShopkeepersPlugin.HISTORY_OWN_PERMISSION)) {
			TextUtils.sendMessage(sender, Messages.noPermission);
			return Command.SINGLE_SUCCESS;
		}

		// Query history for own shops
		ShopSelector shopSelector = new ShopSelector.ByOwnerUUID(
				player.getUniqueId(),
				player.getName()
		);

		queryAndDisplayHistory(sender, PlayerSelector.ALL, shopSelector, page);
		return Command.SINGLE_SUCCESS;
	}

	// All history
	private int executeAll(CommandContext<CommandSourceStack> context) {
		return executeAllInternal(context, 1);
	}

	private int executeAllWithPage(CommandContext<CommandSourceStack> context) {
		int page = IntegerArgumentType.getInteger(context, ARGUMENT_PAGE);
		return executeAllInternal(context, page);
	}

	private int executeAllInternal(CommandContext<CommandSourceStack> context, int page) {
		CommandSender sender = context.getSource().getSender();
		queryAndDisplayHistory(sender, PlayerSelector.ALL, ShopSelector.ALL, page);
		return Command.SINGLE_SUCCESS;
	}

	// Admin shops history
	private int executeAdmin(CommandContext<CommandSourceStack> context) {
		return executeAdminInternal(context, 1);
	}

	private int executeAdminWithPage(CommandContext<CommandSourceStack> context) {
		int page = IntegerArgumentType.getInteger(context, ARGUMENT_PAGE);
		return executeAdminInternal(context, page);
	}

	private int executeAdminInternal(CommandContext<CommandSourceStack> context, int page) {
		CommandSender sender = context.getSource().getSender();
		queryAndDisplayHistory(sender, PlayerSelector.ALL, ShopSelector.ADMIN_SHOPS, page);
		return Command.SINGLE_SUCCESS;
	}

	// Player history
	private int executePlayer(CommandContext<CommandSourceStack> context) {
		return executePlayerInternal(context, 1);
	}

	private int executePlayerWithPage(CommandContext<CommandSourceStack> context) {
		int page = IntegerArgumentType.getInteger(context, ARGUMENT_PAGE);
		return executePlayerInternal(context, page);
	}

	private int executePlayerInternal(CommandContext<CommandSourceStack> context, int page) {
		CommandSender sender = context.getSource().getSender();
		String playerName = StringArgumentType.getString(context, ARGUMENT_PLAYER);

		// Find player
		@Nullable UUID playerUUID = null;
		Player onlinePlayer = Bukkit.getPlayerExact(playerName);
		if (onlinePlayer != null) {
			playerUUID = onlinePlayer.getUniqueId();
			playerName = onlinePlayer.getName();
		}

		PlayerSelector playerSelector = new PlayerSelector.ByUUID(playerUUID, playerName);
		queryAndDisplayHistory(sender, playerSelector, ShopSelector.ALL, page);
		return Command.SINGLE_SUCCESS;
	}

	private void queryAndDisplayHistory(
			CommandSender sender,
			PlayerSelector playerSelector,
			ShopSelector shopSelector,
			int page
	) {
		TradingHistoryProvider historyProvider = plugin.getTradingHistoryProvider();
		if (historyProvider == null) {
			TextUtils.sendMessage(sender, Messages.historyDisabled);
			return;
		}

		Range range = new Range.PageRange(page, ENTRIES_PER_PAGE);
		TradingHistoryRequest request = new TradingHistoryRequest(playerSelector, shopSelector, range);

		historyProvider.getTradingHistory(request)
				.thenAcceptAsync(result -> {
					displayHistory(sender, request, result);
				}, plugin.getSyncExecutor())
				.exceptionally(exception -> {
					TextUtils.sendMessage(sender, Text.parse("&cError retrieving trading history!"));
					return null;
				});
	}

	private void displayHistory(
			CommandSender sender,
			TradingHistoryRequest request,
			TradingHistoryResult result
	) {
		int totalTrades = result.getTotalTradesCount();
		int startIndex = request.range.getStartIndex(totalTrades);
		int page = (startIndex / ENTRIES_PER_PAGE) + 1;
		int maxPage = Math.max(1, (int) Math.ceil((double) totalTrades / ENTRIES_PER_PAGE));

		// Build header
		Map<String, Object> headerArgs = new HashMap<>();
		headerArgs.put("page", page);
		headerArgs.put("maxPage", maxPage);
		headerArgs.put("tradesCount", totalTrades);

		// Player text
		Text headerPlayers;
		if (request.playerSelector == PlayerSelector.ALL) {
			headerPlayers = Messages.historyHeaderAllPlayers;
		} else if (request.playerSelector instanceof PlayerSelector.ByUUID byUuid) {
			headerPlayers = Messages.historyHeaderSpecificPlayer;
			headerPlayers.setPlaceholderArguments("player",
					TextUtils.getPlayerText(byUuid.getPlayerName(), byUuid.getPlayerUUID()));
		} else {
			headerPlayers = Messages.historyHeaderAllPlayers;
		}
		headerArgs.put("players", headerPlayers);

		// Shop text
		Text headerShops;
		if (request.shopSelector == ShopSelector.ALL) {
			headerShops = Messages.historyHeaderAllShops;
		} else if (request.shopSelector == ShopSelector.ADMIN_SHOPS) {
			headerShops = Messages.historyHeaderAdminShops;
		} else if (request.shopSelector == ShopSelector.PLAYER_SHOPS) {
			headerShops = Messages.historyHeaderPlayerShops;
		} else if (request.shopSelector instanceof ShopSelector.ByOwnerUUID byOwner) {
			headerShops = Messages.historyHeaderAllOwnedShops;
			headerShops.setPlaceholderArguments("owner",
					TextUtils.getPlayerText(byOwner.getOwnerName(), byOwner.getOwnerUUID()));
		} else {
			headerShops = Messages.historyHeaderAllShops;
		}
		headerArgs.put("shops", headerShops);

		TextUtils.sendMessage(sender, Messages.historyHeader, headerArgs);

		if (totalTrades == 0) {
			TextUtils.sendMessage(sender, Messages.historyNoTradesFound);
			return;
		}

		// Display entries
		int index = startIndex;
		for (TradeRecord trade : result.getTrades()) {
			Map<String, Object> entryArgs = new HashMap<>();

			PlayerRecord player = trade.getPlayer();
			Instant timestamp = trade.getTimestamp();
			ShopRecord shop = trade.getShop();
			@Nullable PlayerRecord shopOwner = shop.getOwner();
			UnmodifiableItemStack item1 = trade.getItem1();
			@Nullable UnmodifiableItemStack item2 = trade.getItem2();
			UnmodifiableItemStack resultItem = trade.getResultItem();

			Text entryMsg = item2 == null ? Messages.historyEntryOneItem : Messages.historyEntryTwoItems;

			entryArgs.put("index", index + 1);
			entryArgs.put("player", TextUtils.getPlayerText(player));
			entryArgs.put("item1Amount", item1.getAmount());
			entryArgs.put("item1", TextUtils.getItemText(item1));
			entryArgs.put("resultItemAmount", resultItem.getAmount());
			entryArgs.put("resultItem", TextUtils.getItemText(resultItem));

			var formattedTimestamp = DerivedSettings.dateTimeFormatter.format(timestamp);
			entryArgs.put("timeAgo", Text.hoverEvent(Text.of(formattedTimestamp))
					.childText(TimeUtils.getTimeAgoString(timestamp)).buildRoot());

			if (item2 != null) {
				entryArgs.put("item2Amount", item2.getAmount());
				entryArgs.put("item2", TextUtils.getItemText(item2));
			}

			Text tradeCountText = Text.EMPTY;
			if (trade.getTradeCount() > 1) {
				tradeCountText = Messages.historyEntryTradeCount;
				tradeCountText.setPlaceholderArguments("count", trade.getTradeCount());
			}
			entryArgs.put("trade_count", tradeCountText);

			Text shopText;
			if (shopOwner == null) {
				shopText = Messages.historyEntryAdminShop;
			} else {
				shopText = Messages.historyEntryPlayerShop;
				shopText.setPlaceholderArguments("owner", TextUtils.getPlayerText(shopOwner));
			}
			entryArgs.put("shop", TextUtils.getShopText(shopText, shop.getUniqueId()));

			TextUtils.sendMessage(sender, entryMsg, entryArgs);
			index++;
		}
	}
}

