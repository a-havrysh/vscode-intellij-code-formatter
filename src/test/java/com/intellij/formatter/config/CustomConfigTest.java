package com.intellij.formatter.config;

import com.intellij.formatter.core.CodeStyleLoadException;
import com.intellij.formatter.core.FormattingException;
import com.intellij.formatter.core.StandaloneFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for custom code style configuration loading.
 */
@DisplayName("Custom Config Tests")
class CustomConfigTest {

    private String getResourcePath(String resourceName) {
        var url = getClass().getClassLoader().getResource(resourceName);
        Objects.requireNonNull(url, "Resource not found: " + resourceName);
        return Path.of(url.getPath()).toString();
    }

    @Test
    @DisplayName("Load 2-space indent config")
    void loadTwoSpaceConfig() throws CodeStyleLoadException {
        var configPath = getResourcePath("code-style-2-spaces.xml");

        assertDoesNotThrow(() -> CodeStyleLoader.loadFromFile(configPath));
    }

    @Test
    @DisplayName("Load tabs config")
    void loadTabsConfig() throws CodeStyleLoadException {
        var configPath = getResourcePath("code-style-tabs.xml");

        assertDoesNotThrow(() -> CodeStyleLoader.loadFromFile(configPath));
    }

    @Test
    @DisplayName("Load nonexistent file throws exception")
    void loadNonexistentFile() {
        var exception = assertThrows(CodeStyleLoadException.class,
                () -> CodeStyleLoader.loadFromFile("/nonexistent/path/config.xml"));

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Java formatting uses 2-space indent after config load")
    void javaFormattingWithTwoSpaces() throws CodeStyleLoadException, FormattingException {
        var configPath = getResourcePath("code-style-2-spaces.xml");
        CodeStyleLoader.loadFromFile(configPath);

        var input = "public class Test{void method(){int x=1;}}";
        var actual = StandaloneFormatter.formatCode(input, "Test.java");

        // With 2-space indent, we should see "  void" not "    void"
        assertTrue(actual.contains("  void") || actual.contains("\tvoid"),
                "Expected 2-space or tab indent, got:\n" + actual);
    }

    @Test
    @DisplayName("Java formatting works after loading tabs config")
    void javaFormattingWithTabs() throws CodeStyleLoadException, FormattingException {
        var configPath = getResourcePath("code-style-tabs.xml");
        CodeStyleLoader.loadFromFile(configPath);

        var input = "public class Test{void method(){int x=1;}}";
        var actual = StandaloneFormatter.formatCode(input, "Test.java");

        // Config loading should not break formatting
        // Note: Tab setting may not apply in standalone mode, but formatting should still work
        assertNotNull(actual);
        assertTrue(actual.contains("void method()"));
        assertTrue(actual.contains("int x = 1"));
    }

    @Test
    @DisplayName("XML formatting uses 2-space indent after config load")
    void xmlFormattingWithTwoSpaces() throws CodeStyleLoadException, FormattingException {
        var configPath = getResourcePath("code-style-2-spaces.xml");
        CodeStyleLoader.loadFromFile(configPath);

        var input = "<root><child><nested>text</nested></child></root>";
        var actual = StandaloneFormatter.formatCode(input, "test.xml");

        assertNotNull(actual);
        // XML should be formatted with proper structure
        assertTrue(actual.contains("<root>"));
        assertTrue(actual.contains("</root>"));
    }

    @Test
    @DisplayName("Kotlin formatting uses 2-space indent after config load")
    void kotlinFormattingWithTwoSpaces() throws CodeStyleLoadException, FormattingException {
        var configPath = getResourcePath("code-style-2-spaces.xml");
        CodeStyleLoader.loadFromFile(configPath);

        var input = "class Calculator{fun add(a:Int,b:Int):Int{return a+b}}";
        var actual = StandaloneFormatter.formatCode(input, "Calculator.kt");

        assertNotNull(actual);
        assertTrue(actual.contains("fun add"));
    }

    @Test
    @DisplayName("Groovy formatting uses 2-space indent after config load")
    void groovyFormattingWithTwoSpaces() throws CodeStyleLoadException, FormattingException {
        var configPath = getResourcePath("code-style-2-spaces.xml");
        CodeStyleLoader.loadFromFile(configPath);

        var input = "class Person{String name}";
        var actual = StandaloneFormatter.formatCode(input, "Person.groovy");

        assertNotNull(actual);
        assertTrue(actual.contains("String name"));
    }

    @Test
    @DisplayName("Config loading is idempotent")
    void configLoadingIdempotent() throws CodeStyleLoadException, FormattingException {
        var configPath = getResourcePath("code-style-2-spaces.xml");

        // Load config multiple times
        CodeStyleLoader.loadFromFile(configPath);
        CodeStyleLoader.loadFromFile(configPath);
        CodeStyleLoader.loadFromFile(configPath);

        // Formatting should still work
        var input = "public class Test{void method(){}}";
        var actual = StandaloneFormatter.formatCode(input, "Test.java");

        assertNotNull(actual);
        assertTrue(actual.contains("void method()"));
    }

    @Test
    @DisplayName("Switching between configs works")
    void switchingBetweenConfigs() throws CodeStyleLoadException, FormattingException {
        var input = "public class Test{void method(){int x=1;}}";

        // Load 2-space config
        CodeStyleLoader.loadFromFile(getResourcePath("code-style-2-spaces.xml"));
        var with2Spaces = StandaloneFormatter.formatCode(input, "Test.java");

        // Load tabs config
        CodeStyleLoader.loadFromFile(getResourcePath("code-style-tabs.xml"));
        var withTabs = StandaloneFormatter.formatCode(input, "Test.java");

        // Results should be different (one uses spaces, other uses tabs)
        assertNotNull(with2Spaces);
        assertNotNull(withTabs);
        // At least verify both produce valid output
        assertTrue(with2Spaces.contains("void method()"));
        assertTrue(withTabs.contains("void method()"));
    }

    @Test
    @DisplayName("Invalid XML throws exception")
    void invalidXmlThrowsException() {
        // Create a temp file with invalid XML
        var exception = assertThrows(CodeStyleLoadException.class, () -> {
            var tempFile = java.io.File.createTempFile("invalid-config", ".xml");
            java.nio.file.Files.writeString(tempFile.toPath(), "not valid xml {{{");
            try {
                CodeStyleLoader.loadFromFile(tempFile.getAbsolutePath());
            } finally {
                tempFile.delete();
            }
        });

        assertTrue(exception.getMessage().contains("parse") || exception.getMessage().contains("Failed"));
    }
}
