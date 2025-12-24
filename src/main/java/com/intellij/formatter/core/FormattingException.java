package com.intellij.formatter.core;

/**
 * Exception thrown when code formatting fails.
 *
 * <p>This exception indicates that the IntelliJ formatting engine encountered
 * an error while processing the source code. Common causes include:</p>
 * <ul>
 *     <li>Unsupported file type or language</li>
 *     <li>Syntax errors in the source code preventing proper parsing</li>
 *     <li>Internal PSI (Program Structure Interface) errors</li>
 *     <li>Thread interruption during formatting</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * try {
 *     String formatted = StandaloneFormatter.formatCode(code, "MyClass.java");
 * } catch (FormattingException e) {
 *     System.err.println("Formatting failed: " + e.getMessage());
 * }
 * }</pre>
 *
 * @see StandaloneFormatter
 * @see CodeStyleLoadException
 */
public class FormattingException extends Exception {

    /**
     * Constructs a new formatting exception with the specified detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public FormattingException(String message) {
        super(message);
    }

    /**
     * Constructs a new formatting exception with the specified detail message and cause.
     *
     * @param message the detail message describing the cause of the exception
     * @param cause   the underlying cause of this exception
     */
    public FormattingException(String message, Throwable cause) {
        super(message, cause);
    }
}
