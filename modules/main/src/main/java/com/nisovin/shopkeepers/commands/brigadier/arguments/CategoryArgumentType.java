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
import com.nisovin.shopkeepers.util.ItemCategory;

import io.papermc.paper.command.brigadier.argument.CustomArgumentType;

/**
 * Brigadier argument type for parsing item categories.
 */
@SuppressWarnings("UnstableApiUsage")
public class CategoryArgumentType implements CustomArgumentType.Converted<ItemCategory, String> {

	private static final DynamicCommandExceptionType UNKNOWN_CATEGORY = new DynamicCommandExceptionType(
			input -> () -> "Unknown category: '" + input + "'"
	);

	/**
	 * Creates a new CategoryArgumentType.
	 *
	 * @return a new instance
	 */
	public static CategoryArgumentType category() {
		return new CategoryArgumentType();
	}

	/**
	 * Gets the category from the command context.
	 *
	 * @param context
	 *            the command context
	 * @param name
	 *            the argument name
	 * @return the item category
	 */
	public static ItemCategory getCategory(CommandContext<?> context, String name) {
		return context.getArgument(name, ItemCategory.class);
	}

	@Override
	public ArgumentType<String> getNativeType() {
		return StringArgumentType.word();
	}

	@Override
	public ItemCategory convert(String input) throws CommandSyntaxException {
		@Nullable ItemCategory category = ItemCategory.fromIdentifier(input);

		if (category == null) {
			throw UNKNOWN_CATEGORY.create(input);
		}

		return category;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(
			CommandContext<S> context,
			SuggestionsBuilder builder
	) {
		String remaining = builder.getRemainingLowerCase();

		for (ItemCategory category : ItemCategory.values()) {
			String identifier = category.getIdentifier();
			if (identifier.toLowerCase().startsWith(remaining)) {
				builder.suggest(identifier);
			}
		}

		return builder.buildFuture();
	}
}

