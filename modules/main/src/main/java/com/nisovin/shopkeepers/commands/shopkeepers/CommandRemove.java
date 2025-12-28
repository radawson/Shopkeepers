package com.nisovin.shopkeepers.commands.shopkeepers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.events.PlayerDeleteShopkeeperEvent;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import com.nisovin.shopkeepers.api.ui.DefaultUITypes;
import com.nisovin.shopkeepers.commands.Confirmations;
import com.nisovin.shopkeepers.commands.arguments.ShopkeeperArgument;
import com.nisovin.shopkeepers.commands.arguments.ShopkeeperFilter;
import com.nisovin.shopkeepers.commands.arguments.TargetShopkeeperFallback;
import com.nisovin.shopkeepers.commands.lib.Command;
import com.nisovin.shopkeepers.commands.lib.CommandException;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.argument.filter.ArgumentFilter;
import com.nisovin.shopkeepers.commands.lib.argument.filter.ArgumentRejectedException;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.TargetShopkeeperFilter;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.TargetShopkeepersResult;
import com.nisovin.shopkeepers.event.ShopkeeperEventHelper;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.ObjectUtils;

class CommandRemove extends Command {

	private static final String ARGUMENT_SHOPKEEPER = "shopkeeper";
	// Filter for the explicit argument (ShopkeeperArgument expects ArgumentFilter)
	private static final ArgumentFilter<? super Shopkeeper> EXPLICIT_ARG_FILTER = ShopkeeperFilter.withAccess(DefaultUITypes.EDITOR());
	// Filter for the target re-fetch (findTargetedShopkeepers expects TargetShopkeeperFilter)
	private static final TargetShopkeeperFilter TARGET_REFETCH_FILTER = new TargetShopkeeperFilter() {
		@Override
		public boolean test(Shopkeeper shopkeeper) {
			// Combine the base target requirement (ANY) with the specific editor access check
			// Pass null for input and context as they are likely not needed by this filter
			return TargetShopkeeperFilter.ANY.test(shopkeeper) && EXPLICIT_ARG_FILTER.test(null, null, shopkeeper);
		}

		@Override
		public Text getNoTargetErrorMsg() {
			// Use a generic message, context might be ambiguous
			return Messages.mustTargetShop;
		}

		@Override
		public Text getInvalidTargetErrorMsg(Shopkeeper shopkeeper) {
			// Use a generic message, specific error came from the original ArgumentRejectedException
			return Messages.cannotRemoveTargetedShops; // Re-use one of the new message keys
		}
	};

	private final Confirmations confirmations;

	CommandRemove(Confirmations confirmations) {
		super("remove", Arrays.asList("delete"));
		this.confirmations = confirmations;

		// Permission gets checked by testPermission and during execution.

		// Set description:
		this.setDescription(Messages.commandDescriptionRemove);

		// Arguments:
		// Shopkeeper filter: Ignored for non-player command senders. Also, when deleting the shops
		// of another player, the command only lists the shops that the executing player has editing
		// access to as well, i.e. the executing player may require the bypass permission to see the
		// shops owned by other players.
		this.addArgument(new TargetShopkeeperFallback(
				new ShopkeeperArgument(ARGUMENT_SHOPKEEPER, EXPLICIT_ARG_FILTER),
				TargetShopkeeperFilter.ANY // Fallback uses ANY, specific filtering happens later if ambiguous
		));
	}

	@Override
	public boolean testPermission(CommandSender sender) {
		if (!super.testPermission(sender)) return false;
		return PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_OWN_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_OTHERS_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.REMOVE_ADMIN_PERMISSION);
	}

	@Override
	protected void execute(CommandInput input, CommandContextView context) throws CommandException {
		CommandSender sender = input.getSender();
		Player senderPlayer = ObjectUtils.castOrNull(sender, Player.class);

		try {
			// --- Handle Single Shopkeeper Case ---
			final AbstractShopkeeper shopkeeper = context.get(ARGUMENT_SHOPKEEPER);

			// Check edit access and permissions
			if (!checkSingleShopPermissions(sender, senderPlayer, shopkeeper)) {
				return; // Error message already sent by check method
			}

			// Request confirmation for single shop removal
			confirmations.awaitConfirmation(sender, () -> {
				deleteShopkeeper(sender, senderPlayer, shopkeeper, false); // isMultiDelete = false
			});

			TextUtils.sendMessage(sender, Messages.confirmRemoveShop);
			TextUtils.sendMessage(sender, Messages.confirmationRequired);

		} catch (ArgumentRejectedException e) {
			// --- Handle Ambiguous Target Case ---
			if (senderPlayer == null) {
				// Targeting requires a player sender
				TextUtils.sendMessage(sender, e.getMessageText()); // Show original rejection error
				return;
			}

			// Re-fetch targeted shopkeepers using the intended filter
			TargetShopkeepersResult targetResult = ShopkeeperArgumentUtils.findTargetedShopkeepers(
					senderPlayer,
					TARGET_REFETCH_FILTER // Use the combined filter for re-fetch
			);

			if (!targetResult.isSuccess() || targetResult.getShopkeepers().isEmpty()) {
				Text errorMsg = targetResult.getErrorMessage();
				if (errorMsg == null) {
					errorMsg = Messages.noShopkeepersFound; // Fallback message
				}
				TextUtils.sendMessage(sender, errorMsg);
				return;
			}

			List<? extends Shopkeeper> targetedShopkeepers = targetResult.getShopkeepers();

			// Further filter based on permissions
			List<AbstractShopkeeper> shopsToRemove = targetedShopkeepers.stream()
					.filter(s -> s instanceof AbstractShopkeeper) // Ensure correct type
					.map(s -> (AbstractShopkeeper) s)
					.filter(s -> {
						try {
							// Check permissions, return false if check fails or throws exception
							return checkSingleShopPermissions(sender, senderPlayer, s);
						} catch (CommandException permEx) {
							// Exception likely means no permission, filter out the shop
							// Log the exception? Send a message? For now, just filter out.
							return false; 
						}
					})
					.collect(Collectors.toList());

			if (shopsToRemove.isEmpty()) {
				TextUtils.sendMessage(sender, Messages.cannotRemoveTargetedShops); 
				return;
			}

			// Format list for confirmation message
			String shopListStr = shopsToRemove.stream()
					.map(s -> s.getName() != null ? s.getName() : "ID:" + s.getId())
					.collect(Collectors.joining(", "));

			Text confirmationMsg = Messages.confirmRemoveMultipleShops // Use setPlaceholderArguments
					.setPlaceholderArguments("count", shopsToRemove.size())
					.setPlaceholderArguments("list", shopListStr);

			// Request confirmation for multiple shop removal
			confirmations.awaitConfirmation(sender, () -> {
				int successCount = 0;
				int failCount = 0;
				for (AbstractShopkeeper shopToRemove : shopsToRemove) {
					if (deleteShopkeeper(sender, senderPlayer, shopToRemove, true)) { // isMultiDelete = true
						successCount++;
					} else {
						failCount++;
					}
				}
				TextUtils.sendMessage(sender, Messages.removedMultipleShops // Use setPlaceholderArguments
						.setPlaceholderArguments("success", successCount)
						.setPlaceholderArguments("fail", failCount));
			});

			TextUtils.sendMessage(sender, confirmationMsg);
			TextUtils.sendMessage(sender, Messages.confirmationRequired);

		} catch (CommandException e) {
			// Catch other command exceptions (like permission errors from checkSingleShopPermissions)
			// and let them propagate or handle as needed.
			// In this case, the exception message should already be sent by the framework.
			throw e;
		}
	}

	/**
	 * Checks if the sender has permission to remove the given shopkeeper.
	 * Sends error messages to the sender if checks fail.
	 * @param sender The command sender.
	 * @param senderPlayer The player sender, or null.
	 * @param shopkeeper The shopkeeper to check.
	 * @return true if permissions are sufficient, false otherwise.
	 */
	private boolean checkSingleShopPermissions(CommandSender sender, Player senderPlayer, AbstractShopkeeper shopkeeper) throws CommandException {
		// Check that the sender can edit this shop:
		if (!shopkeeper.canEdit(sender, false)) {
			// canEdit should send its own message
			return false;
		}

		// Command permission checks:
		try {
			if (shopkeeper instanceof PlayerShopkeeper) {
				PlayerShopkeeper playerShop = (PlayerShopkeeper) shopkeeper;
				if (senderPlayer != null && playerShop.isOwner(senderPlayer)) {
					this.checkPermission(sender, ShopkeepersPlugin.REMOVE_OWN_PERMISSION);
				} else {
					this.checkPermission(sender, ShopkeepersPlugin.REMOVE_OTHERS_PERMISSION);
				}
			} else {
				this.checkPermission(sender, ShopkeepersPlugin.REMOVE_ADMIN_PERMISSION);
			}
		} catch (CommandException e) {
			// Let the caller handle permission exceptions
			throw e;
		}
		return true;
	}

	/**
	 * Performs the deletion of a single shopkeeper, including event calls and messages.
	 * @param sender The command sender.
	 * @param senderPlayer The player sender, or null.
	 * @param shopkeeper The shopkeeper to delete.
	 * @param isMultiDelete Flag indicating if this is part of a multi-delete operation.
	 * @return true if deletion was successful, false otherwise.
	 */
	private boolean deleteShopkeeper(CommandSender sender, Player senderPlayer, AbstractShopkeeper shopkeeper, boolean isMultiDelete) {
		if (!shopkeeper.isValid()) {
			// The shopkeeper has been removed in the meantime.
			// Don't send a message here, aggregate results in the multi-delete case.
			return false;
		}

		if (senderPlayer != null) {
			// Call event:
			PlayerDeleteShopkeeperEvent deleteEvent = ShopkeeperEventHelper.callPlayerDeleteShopkeeperEvent(
					shopkeeper,
					senderPlayer
			);
			if (deleteEvent.isCancelled()) {
				// Don't send a message here, aggregate results.
				return false;
			}
		}

		// Delete and save:
		shopkeeper.delete(senderPlayer);
		shopkeeper.save();

		// Only send individual success message if it's a single delete confirmation
		if (!isMultiDelete) {
			TextUtils.sendMessage(sender, Messages.shopRemoved);
		}
		return true;
	}
}
