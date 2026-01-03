package com.nisovin.shopkeepers.commands.util;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;

/**
 * Utility for handling ambiguous player name matches.
 * <p>
 * When a player name input matches multiple players, this class helps format and display
 * appropriate error messages to the user.
 */
public final class AmbiguousPlayerNameHandler {

	private static final int DEFAULT_MAX_ENTRIES = 5;

	/**
	 * Handles the case where a player name matches multiple players.
	 *
	 * @param sender
	 *            the command sender to receive feedback
	 * @param name
	 *            the ambiguous player name
	 * @param matches
	 *            the matching player entries (UUID to name)
	 * @return true if the input was ambiguous (multiple matches), false if there was 0 or 1 match
	 */
	public static boolean handleAmbiguousPlayerName(
			CommandSender sender,
			String name,
			Iterable<? extends Entry<? extends UUID, ? extends String>> matches
	) {
		return handleAmbiguousPlayerName(sender, name, matches, DEFAULT_MAX_ENTRIES);
	}

	/**
	 * Handles the case where a player name matches multiple players.
	 *
	 * @param sender
	 *            the command sender to receive feedback
	 * @param name
	 *            the ambiguous player name
	 * @param matches
	 *            the matching player entries (UUID to name)
	 * @param maxEntries
	 *            maximum number of entries to display
	 * @return true if the input was ambiguous (multiple matches), false if there was 0 or 1 match
	 */
	public static boolean handleAmbiguousPlayerName(
			CommandSender sender,
			String name,
			Iterable<? extends Entry<? extends UUID, ? extends String>> matches,
			int maxEntries
	) {
		Iterator<? extends Entry<? extends UUID, ? extends String>> iterator = matches.iterator();

		// Check if we have at least 2 matches
		if (!iterator.hasNext()) {
			return false; // No matches
		}
		Entry<? extends UUID, ? extends String> first = iterator.next();

		if (!iterator.hasNext()) {
			return false; // Only 1 match - not ambiguous
		}

		// Multiple matches - ambiguous
		TextUtils.sendMessage(sender, Messages.ambiguousPlayerName.setPlaceholderArguments(
				"name", name
		));

		// Display first entry
		TextUtils.sendMessage(sender, Messages.ambiguousPlayerNameEntry.setPlaceholderArguments(
				"name", first.getValue(),
				"uuid", first.getKey().toString()
		));

		// Display more entries up to the limit
		int displayedCount = 1;
		while (iterator.hasNext() && displayedCount < maxEntries) {
			Entry<? extends UUID, ? extends String> entry = iterator.next();
			TextUtils.sendMessage(sender, Messages.ambiguousPlayerNameEntry.setPlaceholderArguments(
					"name", entry.getValue(),
					"uuid", entry.getKey().toString()
			));
			displayedCount++;
		}

		// If there are more entries, show "..."
		if (iterator.hasNext()) {
			TextUtils.sendMessage(sender, Messages.ambiguousPlayerNameMore);
		}

		return true;
	}

	private AmbiguousPlayerNameHandler() {
	}
}

