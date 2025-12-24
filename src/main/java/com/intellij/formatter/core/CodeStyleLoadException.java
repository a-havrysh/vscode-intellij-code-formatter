package com.intellij.formatter.core;

/**
 * Exception thrown when loading or applying code style settings fails.
 *
 * <p>This exception is thrown during code style configuration loading from XML files,
 * typically exported from IntelliJ IDEA. Common causes include:</p>
 * <ul>
 *     <li>Code style file not found at the specified path</li>
 *     <li>Invalid XML format in the code style file</li>
 *     <li>Missing required code style elements in the configuration</li>
 *     <li>Failure to apply settings to the project code style manager</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * try {
 *     CodeStyleLoader.loadFromFile("/path/to/code-style.xml");
 * } catch (CodeStyleLoadException e) {
 *     System.err.println("Failed to load code style: " + e.getMessage());
 * }
 * }</pre>
 *
 * @see com.intellij.formatter.config.CodeStyleLoader
 * @see FormattingException
 */
public class CodeStyleLoadException extends Exception {

    /**
     * Constructs a new code style load exception with the specified detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public CodeStyleLoadException(String message) {
        super(message);
    }

    /**
     * Constructs a new code style load exception with the specified detail message and cause.
     *
     * @param message the detail message describing the cause of the exception
     * @param cause   the underlying cause of this exception
     */
    public CodeStyleLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
