package com.intellij.formatter;

import com.intellij.formatter.config.CodeStyleLoader;
import com.intellij.formatter.core.CodeStyleLoadException;
import com.intellij.formatter.core.FormattingException;
import com.intellij.formatter.core.StandaloneFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.intellij.formatter.bootstrap.FormatterBootstrap.initialize;

/**
 * Command-line application for formatting source code using IntelliJ IDEA's formatting engine.
 *
 * <p>This application provides a CLI interface to format files in place with optional
 * support for custom IntelliJ code styles and line range formatting.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * java -jar formatter.jar [options] <file>
 *
 * Options:
 *   --style, -s <path>    Load IntelliJ code style from XML file
 *   --lines <start:end>   Format only lines in range (1-based)
 *   --help, -h            Show this help message
 * }</pre>
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * # Format entire file
 * java -jar formatter.jar MyClass.java
 *
 * # Format with custom code style
 * java -jar formatter.jar --style codestyle.xml MyClass.java
 *
 * # Format specific lines (10-25)
 * java -jar formatter.jar --lines 10:25 MyClass.java
 * }</pre>
 *
 * @see StandaloneFormatter
 * @see CodeStyleLoader
 */
public final class JetbrainsFormatterApplication {

    private static final String VERSION = "2025.3.1";

    private JetbrainsFormatterApplication() {
        // Application entry point - prevent instantiation
    }

    /**
     * Main entry point for the command-line formatter.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        String filePath = null;
        String stylePath = null;
        var startLine = -1;
        var endLine = -1;

        for (var i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help", "-h" -> {
                    printHelp();
                    System.exit(0);
                }
                case "--version", "-v" -> {
                    System.out.println("idea-format " + VERSION);
                    System.exit(0);
                }
                case "--lines" -> {
                    if (i + 1 < args.length) {
                        var parts = args[++i].split(":");
                        if (parts.length == 2) {
                            startLine = Integer.parseInt(parts[0]);
                            endLine = Integer.parseInt(parts[1]);
                        }
                    }
                }
                case "--style", "-s" -> {
                    if (i + 1 < args.length) {
                        stylePath = args[++i];
                    }
                }
                default -> {
                    if (!args[i].startsWith("-")) {
                        filePath = args[i];
                    }
                }
            }
        }

        if (filePath == null) {
            System.err.println("Error: No file path specified");
            printUsage();
            System.exit(1);
        }

        try {
            System.err.println("Initializing IntelliJ formatting engine...");
            initialize();
            System.err.println("Engine initialized");

            if (stylePath != null) {
                System.err.println("Loading code style from: " + stylePath);
                try {
                    CodeStyleLoader.loadFromFile(stylePath);
                    System.err.println("Code style loaded successfully");
                } catch (CodeStyleLoadException e) {
                    System.err.println("Warning: Failed to load code style: " + e.getMessage());
                }
            }

            var path = Path.of(filePath);
            if (!Files.exists(path)) {
                System.err.println("Error: File not found: " + filePath);
                System.exit(1);
            }

            var content = Files.readString(path);
            var fileName = path.getFileName().toString();

            if (startLine > 0 && endLine > 0) {
                System.err.println("Formatting lines " + startLine + "-" + endLine + " of: " + filePath);
            } else {
                System.err.println("Formatting: " + filePath);
            }

            var formatted = (startLine > 0 && endLine > 0)
                    ? StandaloneFormatter.formatCodeRange(content, fileName, startLine, endLine)
                    : StandaloneFormatter.formatCode(content, fileName);

            Files.writeString(path, formatted);
            System.err.println("Formatting completed successfully");
        } catch (FormattingException e) {
            System.err.println("Formatting failed: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.err.println("Usage: idea-format [options] <file>");
        System.err.println("Try 'idea-format --help' for more information.");
    }

    private static void printHelp() {
        System.out.println("idea-format " + VERSION + " - IntelliJ IDEA Code Formatter");
        System.out.println();
        System.out.println("Format source code using IntelliJ IDEA's formatting engine.");
        System.out.println();
        System.out.println("USAGE:");
        System.out.println("    idea-format [OPTIONS] <file>");
        System.out.println();
        System.out.println("ARGUMENTS:");
        System.out.println("    <file>              Path to the file to format (formatted in place)");
        System.out.println();
        System.out.println("OPTIONS:");
        System.out.println("    -s, --style <path>  Load IntelliJ code style from XML file");
        System.out.println("    --lines <start:end> Format only specified line range (1-based)");
        System.out.println("    -h, --help          Show this help message");
        System.out.println("    -v, --version       Show version information");
        System.out.println();
        System.out.println("SUPPORTED LANGUAGES:");
        System.out.println("    Java, Kotlin, Groovy, XML, HTML, JSON, YAML, Properties");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("    idea-format MyClass.java");
        System.out.println("    idea-format --style codestyle.xml MyClass.java");
        System.out.println("    idea-format --lines 10:25 MyClass.java");
        System.out.println();
        System.out.println("NOTES:");
        System.out.println("    - Requires Java 21 or later");
        System.out.println("    - First run may take 2-3 seconds for JVM warmup");
    }
}
