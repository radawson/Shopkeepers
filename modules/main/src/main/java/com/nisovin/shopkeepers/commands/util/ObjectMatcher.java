package com.nisovin.shopkeepers.commands.util;

import java.util.stream.Stream;

/**
 * Matches objects by a string input.
 * <p>
 * This functional interface is used to find objects based on user input strings,
 * typically for command argument parsing and suggestions.
 *
 * @param <T>
 *            the type of objects being matched
 */
@FunctionalInterface
public interface ObjectMatcher<T> {

	/**
	 * Matches objects by the given input string.
	 *
	 * @param input
	 *            the input string to match against, not null
	 * @return a stream of matching objects, not null but may be empty
	 */
	Stream<? extends T> match(String input);
}

