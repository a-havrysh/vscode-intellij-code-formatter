package com.intellij.formatter.bootstrap;

import lombok.experimental.UtilityClass;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Internal logger for bootstrap operations.
 *
 * <p>This logger is used during IntelliJ Platform initialization when the standard
 * logging infrastructure is not yet available. It provides debug-level logging
 * that can be enabled via system property {@code formatter.debug=true}.</p>
 *
 * <h2>Enabling Debug Output</h2>
 * <pre>{@code
 * java -Dformatter.debug=true -jar formatter.jar MyClass.java
 * }</pre>
 *
 * <p>In production mode, only warnings and errors are logged to stderr.
 * Debug and trace messages are suppressed unless explicitly enabled.</p>
 *
 * @see FormatterBootstrap
 */
@UtilityClass
public class BootstrapLogger {

    private static final boolean DEBUG_ENABLED = Boolean.getBoolean("formatter.debug");
    private static final boolean TRACE_ENABLED = Boolean.getBoolean("formatter.trace");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final PrintStream OUT = System.err;

    /**
     * Logs a debug message. Only printed when {@code -Dformatter.debug=true}.
     *
     * @param component the component name (e.g., "ServicesRegistrar")
     * @param message   the message to log
     */
    public static void debug(String component, String message) {
        if (DEBUG_ENABLED) {
            log("DEBUG", component, message);
        }
    }

    /**
     * Logs a trace message for detailed diagnostics.
     * Only printed when {@code -Dformatter.trace=true}.
     *
     * @param component the component name
     * @param message   the message to log
     */
    public static void trace(String component, String message) {
        if (TRACE_ENABLED) {
            log("TRACE", component, message);
        }
    }

    /**
     * Logs a warning message. Always printed to stderr.
     *
     * @param component the component name
     * @param message   the warning message
     */
    public static void warn(String component, String message) {
        log("WARN", component, message);
    }

    /**
     * Logs a warning with exception details.
     *
     * @param component the component name
     * @param message   the warning message
     * @param throwable the exception that caused the warning
     */
    public static void warn(String component, String message, Throwable throwable) {
        log("WARN", component, message + " - " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
        if (DEBUG_ENABLED) {
            throwable.printStackTrace(OUT);
        }
    }

    /**
     * Logs a skipped operation (expected failure during optional feature registration).
     * Only printed when debug is enabled.
     *
     * @param component the component name
     * @param operation the operation that was skipped
     * @param reason    why it was skipped (e.g., "class not found")
     */
    public static void skipped(String component, String operation, String reason) {
        if (TRACE_ENABLED) {
            log("SKIP", component, operation + " - " + reason);
        }
    }

    /**
     * Logs an error message. Always printed to stderr.
     *
     * @param component the component name
     * @param message   the error message
     * @param throwable the exception that caused the error
     */
    public static void error(String component, String message, Throwable throwable) {
        log("ERROR", component, message);
        if (DEBUG_ENABLED) {
            throwable.printStackTrace(OUT);
        }
    }

    private static void log(String level, String component, String message) {
        var timestamp = LocalDateTime.now().format(TIME_FORMAT);
        OUT.printf("[%s] %5s [%s] %s%n", timestamp, level, component, message);
    }
}
