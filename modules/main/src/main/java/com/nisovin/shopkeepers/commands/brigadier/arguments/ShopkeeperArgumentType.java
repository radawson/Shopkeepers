package com.nisovin.shopkeepers.commands.brigadier.arguments;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.shopkeeper.registry.SKShopkeeperRegistry;

import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;

/**
 * Brigadier argument type for parsing shopkeepers by name, ID, or UUID.
 * <p>
 * Provides suggestions based on shopkeeper names and IDs.
 */
@SuppressWarnings("UnstableApiUsage")
public class ShopkeeperArgumentType implements CustomArgumentType.Converted<Shopkeeper, String> {

	private static final DynamicCommandExceptionType SHOPKEEPER_NOT_FOUND = new DynamicCommandExceptionType(
			input -> () -> "No shopkeeper found for '" + input + "'"
	);

	private static final DynamicCommandExceptionType AMBIGUOUS_SHOPKEEPER = new DynamicCommandExceptionType(
			input -> () -> "Multiple shopkeepers found for '" + input + "'. Please specify by ID or UUID."
	);

	/**
	 * Creates a new ShopkeeperArgumentType.
	 *
	 * @return a new instance
	 */
	public static ShopkeeperArgumentType shopkeeper() {
		return new ShopkeeperArgumentType();
	}

	/**
	 * Gets the shopkeeper from the command context.
	 *
	 * @param context
	 *            the command context
	 * @param name
	 *            the argument name
	 * @return the shopkeeper
	 */
	public static Shopkeeper getShopkeeper(CommandContext<?> context, String name) {
		return context.getArgument(name, Shopkeeper.class);
	}

	@Override
	public ArgumentType<String> getNativeType() {
		return com.mojang.brigadier.arguments.StringArgumentType.greedyString();
	}

	@Override
	public Shopkeeper convert(String input) throws CommandSyntaxException {
		SKShopkeeperRegistry registry = SKShopkeepersPlugin.getInstance().getShopkeeperRegistry();

		// Try parsing as numeric ID first
		try {
			int id = Integer.parseInt(input);
			@Nullable Shopkeeper shopkeeper = registry.getShopkeeperById(id);
			if (shopkeeper != null) {
				return shopkeeper;
			}
		} catch (NumberFormatException ignored) {
			// Not an ID, continue with other methods
		}

		// Try parsing as UUID
		try {
			UUID uuid = UUID.fromString(input);
			@Nullable Shopkeeper shopkeeper = registry.getShopkeeperByUniqueId(uuid);
			if (shopkeeper != null) {
				return shopkeeper;
			}
		} catch (IllegalArgumentException ignored) {
			// Not a UUID, continue with name lookup
		}

		// Try finding by name
		@Nullable Shopkeeper matchedShopkeeper = null;
		for (Shopkeeper shopkeeper : registry.getAllShopkeepers()) {
			String name = shopkeeper.getName();
			if (name.equalsIgnoreCase(input)) {
				if (matchedShopkeeper != null) {
					// Ambiguous - multiple shopkeepers with this name
					throw AMBIGUOUS_SHOPKEEPER.create(input);
				}
				matchedShopkeeper = shopkeeper;
			}
		}

		if (matchedShopkeeper != null) {
			return matchedShopkeeper;
		}

		throw SHOPKEEPER_NOT_FOUND.create(input);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(
			CommandContext<S> context,
			SuggestionsBuilder builder
	) {
		String remaining = builder.getRemainingLowerCase();
		SKShopkeeperRegistry registry = SKShopkeepersPlugin.getInstance().getShopkeeperRegistry();

		int count = 0;
		final int MAX_SUGGESTIONS = 20;

		for (Shopkeeper shopkeeper : registry.getAllShopkeepers()) {
			if (count >= MAX_SUGGESTIONS) break;

			// Suggest by name
			String name = shopkeeper.getName();
			if (!name.isEmpty() && name.toLowerCase().startsWith(remaining)) {
				builder.suggest(name, () -> "ID: " + shopkeeper.getId());
				count++;
			}

			// Suggest by ID
			String idStr = String.valueOf(shopkeeper.getId());
			if (idStr.startsWith(remaining)) {
				String displayName = name.isEmpty() ? "Unnamed" : name;
				builder.suggest(idStr, () -> displayName);
				count++;
			}
		}

		return builder.buildFuture();
	}
}

