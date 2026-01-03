package com.nisovin.shopkeepers.commands.brigadier.arguments;

import java.util.concurrent.CompletableFuture;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.shopkeeper.ShopType;
import com.nisovin.shopkeepers.api.shopkeeper.ShopTypesRegistry;

import io.papermc.paper.command.brigadier.argument.CustomArgumentType;

/**
 * Brigadier argument type for parsing shop types by identifier.
 */
@SuppressWarnings("UnstableApiUsage")
public class ShopTypeArgumentType implements CustomArgumentType.Converted<ShopType<?>, String> {

	private static final DynamicCommandExceptionType UNKNOWN_SHOP_TYPE = new DynamicCommandExceptionType(
			input -> () -> "Unknown shop type: '" + input + "'"
	);

	/**
	 * Creates a new ShopTypeArgumentType.
	 *
	 * @return a new instance
	 */
	public static ShopTypeArgumentType shopType() {
		return new ShopTypeArgumentType();
	}

	/**
	 * Gets the shop type from the command context.
	 *
	 * @param context
	 *            the command context
	 * @param name
	 *            the argument name
	 * @return the shop type
	 */
	@SuppressWarnings("unchecked")
	public static ShopType<?> getShopType(CommandContext<?> context, String name) {
		return context.getArgument(name, ShopType.class);
	}

	@Override
	public ArgumentType<String> getNativeType() {
		return StringArgumentType.word();
	}

	@Override
	public ShopType<?> convert(String input) throws CommandSyntaxException {
		ShopTypesRegistry<?> registry = ShopkeepersAPI.getShopTypeRegistry();
		@Nullable ShopType<?> shopType = registry.get(input);

		if (shopType == null) {
			throw UNKNOWN_SHOP_TYPE.create(input);
		}

		return shopType;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(
			CommandContext<S> context,
			SuggestionsBuilder builder
	) {
		String remaining = builder.getRemainingLowerCase();
		ShopTypesRegistry<?> registry = ShopkeepersAPI.getShopTypeRegistry();

		for (ShopType<?> shopType : registry.getRegisteredTypes()) {
			String identifier = shopType.getIdentifier();
			if (identifier.toLowerCase().startsWith(remaining)) {
				builder.suggest(identifier);
			}
		}

		return builder.buildFuture();
	}
}

