package com.nisovin.shopkeepers.util.logging;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.debug.Debug;
import com.nisovin.shopkeepers.util.java.Validate;

public final class Log {

	// Gets set early on plugin startup and then (ideally) never unset.
	// Made volatile for thread-safety, especially important during plugin initialization
	// when multiple threads might access the logger.
	private static volatile @Nullable Logger logger = null;

	/**
	 * Sets the logger instance.
	 * <p>
	 * Should be called early during plugin initialization (typically in onLoad()).
	 *
	 * @param logger the logger instance, or <code>null</code> to unset
	 */
	public static void setLogger(@Nullable Logger logger) {
		Log.logger = logger;
	}

	/**
	 * Gets the logger instance.
	 * <p>
	 * Throws an exception if the logger has not been initialized.
	 *
	 * @return the logger instance, never <code>null</code>
	 * @throws IllegalStateException if the logger has not been initialized
	 */
	public static Logger getLogger() {
		return Validate.State.notNull(logger, "No logger instance set!");
	}

	/**
	 * Gets the logger instance safely, with fallback to NullLogger if not initialized.
	 * <p>
	 * This method is useful in scenarios where logging might be attempted before
	 * the logger is fully initialized, such as during static initialization or in
	 * test environments.
	 *
	 * @return the logger instance, or NullLogger if not initialized
	 */
	public static Logger getLoggerSafe() {
		Logger currentLogger = logger;
		if (currentLogger == null) {
			// Fallback to NullLogger to prevent NPE and allow graceful degradation
			return NullLogger.getInstance();
		}
		return currentLogger;
	}

	/**
	 * Checks if the logger has been initialized.
	 *
	 * @return <code>true</code> if the logger has been set, <code>false</code> otherwise
	 */
	public static boolean isInitialized() {
		return logger != null;
	}

	/**
	 * Sets the logger from a Paper ComponentLogger.
	 * <p>
	 * This is a convenience method for Paper plugins. ComponentLogger provides
	 * a logger() method that returns a standard Logger, which is then used.
	 * <p>
	 * This method is optional - the standard {@link #setLogger(Logger)} method
	 * can be used directly if a Logger is already available.
	 *
	 * @param componentLogger the ComponentLogger instance, or <code>null</code> to unset
	 */
	public static void setLoggerFromComponentLogger(@Nullable Object componentLogger) {
		if (componentLogger == null) {
			setLogger(null);
			return;
		}
		// ComponentLogger has a logger() method that returns Logger
		// Use reflection to access it safely
		try {
			java.lang.reflect.Method loggerMethod = componentLogger.getClass().getMethod("logger");
			Logger standardLogger = (Logger) loggerMethod.invoke(componentLogger);
			setLogger(standardLogger);
		} catch (Exception e) {
			// If reflection fails, fall back to NullLogger
			// This should not happen in normal operation, but provides graceful degradation
			setLogger(NullLogger.getInstance());
		}
	}

	public static void info(String message) {
		getLogger().info(message);
	}

	public static void info(Supplier<@Nullable String> msgSupplier) {
		getLogger().info(msgSupplier);
	}

	public static void info(String message, @Nullable Throwable throwable) {
		getLogger().log(Level.INFO, message, throwable);
	}

	public static void info(@Nullable Throwable throwable, Supplier<@Nullable String> msgSupplier) {
		getLogger().log(Level.INFO, throwable, msgSupplier);
	}

	public static void debug(String message) {
		debug(null, message);
	}

	public static void debug(Supplier<@Nullable String> msgSupplier) {
		debug((String) null, msgSupplier);
	}

	public static void debug(String message, @Nullable Throwable throwable) {
		debug(null, message, throwable);
	}

	public static void debug(@Nullable Throwable throwable, Supplier<@Nullable String> msgSupplier) {
		debug(null, throwable, msgSupplier);
	}

	public static void debug(@Nullable String debugOption, String message) {
		if (Debug.isDebugging(debugOption)) {
			info(message);
		}
	}

	public static void debug(@Nullable String debugOption, Supplier<@Nullable String> msgSupplier) {
		if (Debug.isDebugging(debugOption)) {
			info(msgSupplier);
		}
	}

	public static void debug(
			@Nullable String debugOption,
			String message,
			@Nullable Throwable throwable
	) {
		if (Debug.isDebugging(debugOption)) {
			info(message, throwable);
		}
	}

	public static void debug(
			@Nullable String debugOption,
			@Nullable Throwable throwable,
			Supplier<@Nullable String> msgSupplier
	) {
		if (Debug.isDebugging(debugOption)) {
			info(throwable, msgSupplier);
		}
	}

	public static void warning(String message) {
		getLogger().warning(message);
	}

	public static void warning(Supplier<@Nullable String> msgSupplier) {
		getLogger().warning(msgSupplier);
	}

	public static void warning(String message, @Nullable Throwable throwable) {
		getLogger().log(Level.WARNING, message, throwable);
	}

	public static void warning(@Nullable Throwable throwable, Supplier<@Nullable String> msgSupplier) {
		getLogger().log(Level.WARNING, throwable, msgSupplier);
	}

	public static void severe(String message) {
		getLogger().severe(message);
	}

	public static void severe(Supplier<@Nullable String> msgSupplier) {
		getLogger().severe(msgSupplier);
	}

	public static void severe(String message, @Nullable Throwable throwable) {
		getLogger().log(Level.SEVERE, message, throwable);
	}

	public static void severe(@Nullable Throwable throwable, Supplier<@Nullable String> msgSupplier) {
		getLogger().log(Level.SEVERE, throwable, msgSupplier);
	}

	private Log() {
	}
}
