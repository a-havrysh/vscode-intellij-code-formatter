package com.intellij.formatter.bootstrap;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.log4j.Level;

/**
 * A silent logger implementation that suppresses all log output.
 *
 * <p>This logger is used during headless operation to prevent
 * IntelliJ Platform components from polluting stdout/stderr with
 * log messages. All logging methods are no-ops.</p>
 *
 * <p>The logger is registered via {@link Logger#setFactory(Logger.Factory)}
 * during bootstrap initialization.</p>
 *
 * @see FormatterBootstrap
 * @see Logger
 */
final class SilentLogger extends Logger {

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String message) {
        // Intentionally empty - suppress all debug output
    }

    @Override
    public void debug(Throwable t) {
        // Intentionally empty - suppress all debug output
    }

    @Override
    public void debug(String message, Throwable t) {
        // Intentionally empty - suppress all debug output
    }

    @Override
    public void info(String message) {
        // Intentionally empty - suppress all info output
    }

    @Override
    public void info(String message, Throwable t) {
        // Intentionally empty - suppress all info output
    }

    @Override
    public void warn(String message, Throwable t) {
        // Intentionally empty - suppress all warning output
    }

    @Override
    public void error(String message, Throwable t, String... details) {
        // Intentionally empty - suppress all error output
    }

    @Override
    public void setLevel(Level level) {
        // Intentionally empty - log level changes are ignored
    }
}
