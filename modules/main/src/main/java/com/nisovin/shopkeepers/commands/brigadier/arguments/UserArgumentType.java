package com.nisovin.shopkeepers.commands.brigadier.arguments;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.papermc.paper.command.brigadier.argument.CustomArgumentType;

/**
 * Brigadier argument type for parsing users (players) by name or UUID.
 * <p>
 * This supports both online and offline players.
 */
@SuppressWarnings("UnstableApiUsage")
public class UserArgumentType implements CustomArgumentType.Converted<OfflinePlayer, String> {

	private static final DynamicCommandExceptionType PLAYER_NOT_FOUND = new DynamicCommandExceptionType(
			input -> () -> "Player not found: '" + input + "'"
	);

	private final boolean onlineOnly;

	/**
	 * Creates a UserArgumentType for any player (online or offline).
	 *
	 * @return a new instance
	 */
	public static UserArgumentType user() {
		return new UserArgumentType(false);
	}

	/**
	 * Creates a UserArgumentType for online players only.
	 *
	 * @return a new instance
	 */
	public static UserArgumentType onlinePlayer() {
		return new UserArgumentType(true);
	}

	/**
	 * Gets the player from the command context.
	 *
	 * @param context
	 *            the command context
	 * @param name
	 *            the argument name
	 * @return the offline player
	 */
	public static OfflinePlayer getUser(CommandContext<?> context, String name) {
		return context.getArgument(name, OfflinePlayer.class);
	}

	/**
	 * Gets the online player from the command context.
	 *
	 * @param context
	 *            the command context
	 * @param name
	 *            the argument name
	 * @return the player, or null if not online
	 */
	public static @Nullable Player getOnlinePlayer(CommandContext<?> context, String name) {
		OfflinePlayer offlinePlayer = getUser(context, name);
		return offlinePlayer.getPlayer();
	}

	private UserArgumentType(boolean onlineOnly) {
		this.onlineOnly = onlineOnly;
	}

	@Override
	public ArgumentType<String> getNativeType() {
		return StringArgumentType.word();
	}

	@Override
	public OfflinePlayer convert(String input) throws CommandSyntaxException {
		// Try to find online player first
		@Nullable Player onlinePlayer = Bukkit.getPlayerExact(input);
		if (onlinePlayer != null) {
			return onlinePlayer;
		}

		if (onlineOnly) {
			throw PLAYER_NOT_FOUND.create(input);
		}

		// Try parsing as UUID
		try {
			UUID uuid = UUID.fromString(input);
			return Bukkit.getOfflinePlayer(uuid);
		} catch (IllegalArgumentException ignored) {
			// Not a UUID
		}

		// Get offline player by name (creates a new one if not found)
		@SuppressWarnings("deprecation")
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input);
		return offlinePlayer;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(
			CommandContext<S> context,
			SuggestionsBuilder builder
	) {
		String remaining = builder.getRemainingLowerCase();

		// Suggest online players
		for (Player player : Bukkit.getOnlinePlayers()) {
			String name = player.getName();
			if (name.toLowerCase().startsWith(remaining)) {
				builder.suggest(name);
			}
		}

		return builder.buildFuture();
	}
}

