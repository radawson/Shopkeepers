package com.nisovin.shopkeepers.commands.brigadier.executors;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.CommandSender;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.config.Settings;
import com.nisovin.shopkeepers.debug.DebugOptions;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Executor for the /shopkeeper debug command.
 * <p>
 * Toggles debug mode or specific debug options.
 */
@SuppressWarnings("UnstableApiUsage")
public class DebugExecutor {

	private static final String ARGUMENT_OPTION = "option";

	/**
	 * Builds the debug command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("debug")
				.requires(source -> PermissionUtils.hasPermission(
						source.getSender(),
						ShopkeepersPlugin.DEBUG_PERMISSION
				))
				// /shopkeeper debug - Toggle debug mode
				.executes(this::executeToggle)
				// /shopkeeper debug <option> - Toggle specific option
				.then(Commands.argument(ARGUMENT_OPTION, StringArgumentType.word())
						.suggests(this::suggestOptions)
						.executes(this::executeWithOption));
	}

	/**
	 * Toggles the main debug mode.
	 */
	private int executeToggle(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();

		// Toggle debug mode by modifying Settings directly
		Settings.debug = !Settings.debug;
		Settings.onSettingsChanged();

		if (Settings.debug) {
			TextUtils.sendMessage(sender, Messages.debugModeEnabled);
		} else {
			TextUtils.sendMessage(sender, Messages.debugModeDisabled);
		}

		return Command.SINGLE_SUCCESS;
	}

	/**
	 * Toggles a specific debug option.
	 */
	private int executeWithOption(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();
		String optionName = StringArgumentType.getString(context, ARGUMENT_OPTION);

		// Find the debug option (case-insensitive)
		String matchingOption = null;
		for (String option : DebugOptions.getAll()) {
			if (option.equalsIgnoreCase(optionName)) {
				matchingOption = option;
				break;
			}
		}

		if (matchingOption == null) {
			TextUtils.sendMessage(sender, Messages.unknownDebugOption.setPlaceholderArguments(
					"option", optionName
			));
			return Command.SINGLE_SUCCESS;
		}

		// Toggle the option
		boolean enabled;
		if (Settings.debugOptions.contains(matchingOption)) {
			Settings.debugOptions.remove(matchingOption);
			enabled = false;
		} else {
			Settings.debugOptions.add(matchingOption);
			enabled = true;
		}
		Settings.onSettingsChanged();

		if (enabled) {
			TextUtils.sendMessage(sender, Messages.debugOptionEnabled.setPlaceholderArguments(
					"option", matchingOption
			));
		} else {
			TextUtils.sendMessage(sender, Messages.debugOptionDisabled.setPlaceholderArguments(
					"option", matchingOption
			));
		}

		return Command.SINGLE_SUCCESS;
	}

	/**
	 * Provides suggestions for debug options.
	 */
	private CompletableFuture<Suggestions> suggestOptions(
			CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder
	) {
		String remaining = builder.getRemainingLowerCase();

		for (String option : DebugOptions.getAll()) {
			if (option.toLowerCase().startsWith(remaining)) {
				builder.suggest(option);
			}
		}

		return builder.buildFuture();
	}
}
