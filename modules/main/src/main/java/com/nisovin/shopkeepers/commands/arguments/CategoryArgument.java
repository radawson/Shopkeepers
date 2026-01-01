package com.nisovin.shopkeepers.commands.arguments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.argument.ArgumentParseException;
import com.nisovin.shopkeepers.commands.lib.argument.ArgumentsReader;
import com.nisovin.shopkeepers.commands.lib.argument.CommandArgument;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.util.ItemCategory;
import com.nisovin.shopkeepers.util.java.StringUtils;

public class CategoryArgument extends CommandArgument<ItemCategory> {

	public CategoryArgument(String name) {
		super(name);
	}

	@Override
	protected com.nisovin.shopkeepers.text.Text getInvalidArgumentErrorMsgText() {
		return com.nisovin.shopkeepers.text.Text.of("Invalid category!");
	}

	@Override
	public ItemCategory parseValue(
			CommandInput input,
			CommandContextView context,
			ArgumentsReader argsReader
	) throws ArgumentParseException {
		if (!argsReader.hasNext()) {
			throw this.missingArgumentError();
		}
		String argument = argsReader.next();
		ItemCategory category = ItemCategory.fromIdentifier(argument);
		if (category == null) {
			throw this.invalidArgumentError(argument);
		}
		return category;
	}

	@Override
	public List<? extends String> complete(
			CommandInput input,
			CommandContextView context,
			ArgumentsReader argsReader
	) {
		if (argsReader.getRemainingSize() != 1) {
			return Collections.emptyList();
		}

		List<String> suggestions = new ArrayList<>();
		String partialArg = StringUtils.normalize(argsReader.next());
		for (ItemCategory category : ItemCategory.values()) {
			if (suggestions.size() >= MAX_SUGGESTIONS) break;

			String identifier = category.getIdentifier();
			if (identifier.toLowerCase(Locale.ROOT).startsWith(partialArg)) {
				suggestions.add(identifier);
			}
		}
		return Collections.unmodifiableList(suggestions);
	}
}
