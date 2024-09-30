package net.p455w0rd.wirelesscraftingterminal.common.utils;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WCTLog {

    public static final Logger log = LogManager.getLogger("Wireless Crafting Terminal");

    /**
     * Logs a debug statement.
     */
    public static void debug(final String format, final Object... data) {
        log.debug(String.format(format, data));
    }

    /**
     * Logs basic info.
     */
    public static void info(final String format, final Object... data) {
        log.info(String.format(format, data));
    }

    /**
     * Logs an error.
     */
    public static void severe(final String format, final Object... data) {
        log.error(String.format(format, data));
    }

    /**
     * Logs a warning.
     */
    public static void warning(final String format, final Object... data) {
        log.warn(String.format(format, data));
    }

    public static void integration(@Nonnull final Throwable exception) {
        log.debug(exception);
    }
}
