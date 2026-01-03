package com.nisovin.shopkeepers.commands.brigadier.executors;

import org.bukkit.command.CommandSender;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.nisovin.shopkeepers.commands.Confirmations;
import com.nisovin.shopkeepers.util.java.Validate;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * Executor for the /shopkeeper confirm command.
 * <p>
 * Confirms a pending action that requires user confirmation.
 */
@SuppressWarnings("UnstableApiUsage")
public class ConfirmExecutor {

	private final Confirmations confirmations;

	/**
	 * Creates a new ConfirmExecutor.
	 *
	 * @param confirmations
	 *            the confirmations handler
	 */
	public ConfirmExecutor(Confirmations confirmations) {
		Validate.notNull(confirmations, "confirmations is null");
		this.confirmations = confirmations;
	}

	/**
	 * Builds the confirm command node.
	 *
	 * @return the command builder
	 */
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("confirm")
				// No specific permission required - confirmation is tied to the original action
				.executes(this::execute);
	}

	/**
	 * Executes the confirm command.
	 *
	 * @param context
	 *            the command context
	 * @return command success status
	 */
	private int execute(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();

		// Handle confirmation - this will execute the pending action or show "nothing to confirm"
		confirmations.handleConfirmation(sender);

		return Command.SINGLE_SUCCESS;
	}
}
