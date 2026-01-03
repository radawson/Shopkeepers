package com.nisovin.shopkeepers.commands.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.nisovin.shopkeepers.api.user.User;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;

/**
 * Handles ambiguous user names for commands.
 * <p>
 * When a user name matches multiple users, this handler helps format an error message
 * listing the possible matches.
 */
public final class AmbiguousUserNameHandler {

	private static final int DEFAULT_MAX_ENTRIES = 5;

	private final String name;
	private final List<User> matches;
	private final int maxEntries;

	/**
	 * Creates a new AmbiguousUserNameHandler with default max entries.
	 *
	 * @param name
	 *            the input name that was ambiguous
	 * @param matches
	 *            the matching users
	 */
	public AmbiguousUserNameHandler(String name, Iterable<? extends User> matches) {
		this(name, matches, DEFAULT_MAX_ENTRIES);
	}

	/**
	 * Creates a new AmbiguousUserNameHandler.
	 *
	 * @param name
	 *            the input name that was ambiguous
	 * @param matches
	 *            the matching users
	 * @param maxEntries
	 *            the maximum number of entries to display in the error message
	 */
	public AmbiguousUserNameHandler(String name, Iterable<? extends User> matches, int maxEntries) {
		this.name = name;
		this.matches = new ArrayList<>();
		matches.forEach(this.matches::add);
		this.maxEntries = maxEntries;
	}

	/**
	 * Checks if the input was ambiguous (matched multiple users).
	 *
	 * @return true if there are multiple matches
	 */
	public boolean isInputAmbiguous() {
		return matches.size() > 1;
	}

	/**
	 * Sends the error messages for an ambiguous user name.
	 *
	 * @param sender
	 *            the command sender to receive the error messages
	 */
	public void sendErrorMessages(CommandSender sender) {
		if (!isInputAmbiguous()) {
			return;
		}

		TextUtils.sendMessage(sender, Messages.ambiguousPlayerName.setPlaceholderArguments(
				"name", name
		));

		int count = 0;
		for (User match : matches) {
			if (count >= maxEntries) {
				TextUtils.sendMessage(sender, Messages.ambiguousPlayerNameMore);
				break;
			}
			TextUtils.sendMessage(sender, Messages.ambiguousPlayerNameEntry.setPlaceholderArguments(
					"name", match.getName(),
					"uuid", match.getUniqueId().toString()
			));
			count++;
		}
	}

	/**
	 * Handles ambiguous user names by checking if there are multiple matches and sending error
	 * messages if so.
	 *
	 * @param sender
	 *            the command sender to receive error messages
	 * @param name
	 *            the input name that was ambiguous
	 * @param matches
	 *            the matching users
	 * @return true if the input was ambiguous (multiple matches), false otherwise
	 */
	public static boolean handleAmbiguousUserName(
			CommandSender sender,
			String name,
			Iterable<? extends User> matches
	) {
		return handleAmbiguousUserName(sender, name, matches, DEFAULT_MAX_ENTRIES);
	}

	/**
	 * Handles ambiguous user names by checking if there are multiple matches and sending error
	 * messages if so.
	 *
	 * @param sender
	 *            the command sender to receive error messages
	 * @param name
	 *            the input name that was ambiguous
	 * @param matches
	 *            the matching users
	 * @param maxEntries
	 *            the maximum number of entries to display
	 * @return true if the input was ambiguous (multiple matches), false otherwise
	 */
	public static boolean handleAmbiguousUserName(
			CommandSender sender,
			String name,
			Iterable<? extends User> matches,
			int maxEntries
	) {
		AmbiguousUserNameHandler handler = new AmbiguousUserNameHandler(name, matches, maxEntries);
		if (handler.isInputAmbiguous()) {
			handler.sendErrorMessages(sender);
			return true;
		}
		return false;
	}
}
