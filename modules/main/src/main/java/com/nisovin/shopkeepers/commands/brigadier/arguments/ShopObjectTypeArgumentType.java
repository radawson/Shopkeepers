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
import com.nisovin.shopkeepers.api.shopobjects.ShopObjectType;
import com.nisovin.shopkeepers.api.shopobjects.ShopObjectTypesRegistry;

import io.papermc.paper.command.brigadier.argument.CustomArgumentType;

/**
 * Brigadier argument type for parsing shop object types by identifier.
 */
@SuppressWarnings("UnstableApiUsage")
public class ShopObjectTypeArgumentType implements CustomArgumentType.Converted<ShopObjectType<?>, String> {

	private static final DynamicCommandExceptionType UNKNOWN_OBJECT_TYPE = new DynamicCommandExceptionType(
			input -> () -> "Unknown shop object type: '" + input + "'"
	);

	/**
	 * Creates a new ShopObjectTypeArgumentType.
	 *
	 * @return a new instance
	 */
	public static ShopObjectTypeArgumentType shopObjectType() {
		return new ShopObjectTypeArgumentType();
	}

	/**
	 * Gets the shop object type from the command context.
	 *
	 * @param context
	 *            the command context
	 * @param name
	 *            the argument name
	 * @return the shop object type
	 */
	@SuppressWarnings("unchecked")
	public static ShopObjectType<?> getShopObjectType(CommandContext<?> context, String name) {
		return context.getArgument(name, ShopObjectType.class);
	}

	@Override
	public ArgumentType<String> getNativeType() {
		return StringArgumentType.word();
	}

	@Override
	public ShopObjectType<?> convert(String input) throws CommandSyntaxException {
		ShopObjectTypesRegistry<?> registry = ShopkeepersAPI.getShopObjectTypeRegistry();
		@Nullable ShopObjectType<?> objectType = registry.get(input);

		if (objectType == null) {
			throw UNKNOWN_OBJECT_TYPE.create(input);
		}

		return objectType;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(
			CommandContext<S> context,
			SuggestionsBuilder builder
	) {
		String remaining = builder.getRemainingLowerCase();
		ShopObjectTypesRegistry<?> registry = ShopkeepersAPI.getShopObjectTypeRegistry();

		for (ShopObjectType<?> objectType : registry.getRegisteredTypes()) {
			String identifier = objectType.getIdentifier();
			if (identifier.toLowerCase().startsWith(remaining)) {
				builder.suggest(identifier);
			}
		}

		return builder.buildFuture();
	}
}

