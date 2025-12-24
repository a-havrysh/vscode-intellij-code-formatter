package com.intellij.formatter.languages;

import com.intellij.formatter.core.FormattingException;
import com.intellij.formatter.core.StandaloneFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Properties file formatting.
 * Note: Properties formatter preserves content structure.
 */
@DisplayName("Properties Formatter Tests")
class PropertiesFormatterTest {

    @Test
    @DisplayName("Simple key-value pairs - content preserved")
    void simpleKeyValuePairs() throws FormattingException {
        var input = "name=John\nage=30";

        var actual = StandaloneFormatter.formatCode(input, "test.properties");

        assertNotNull(actual);
        assertTrue(actual.contains("name="));
        assertTrue(actual.contains("age="));
    }

    @Test
    @DisplayName("Properties with spaces around equals - content preserved")
    void propertiesWithSpacesAroundEquals() throws FormattingException {
        var input = "name = John\nage = 30";

        var actual = StandaloneFormatter.formatCode(input, "spaced.properties");

        assertNotNull(actual);
        assertTrue(actual.contains("name"));
        assertTrue(actual.contains("John"));
    }

    @Test
    @DisplayName("Properties with colon separator - content preserved")
    void propertiesWithColonSeparator() throws FormattingException {
        var input = "name:John\nage:30";

        var actual = StandaloneFormatter.formatCode(input, "colon.properties");

        assertNotNull(actual);
        assertTrue(actual.contains("name"));
        assertTrue(actual.contains("John"));
    }

    @Test
    @DisplayName("Comments - content preserved")
    void comments() throws FormattingException {
        var input = "# This is a comment\nname=John\n! Another comment\nage=30";

        var actual = StandaloneFormatter.formatCode(input, "comments.properties");

        assertNotNull(actual);
        assertTrue(actual.contains("# This is a comment") || actual.contains("#"));
        assertTrue(actual.contains("name="));
    }

    @Test
    @DisplayName("Multi-line values with backslash - content preserved")
    void multiLineValues() throws FormattingException {
        var input = "message=Hello \\\nWorld";

        var actual = StandaloneFormatter.formatCode(input, "multiline.properties");

        assertNotNull(actual);
        assertTrue(actual.contains("message="));
    }

    @Test
    @DisplayName("Unicode escape sequences - content preserved")
    void unicodeEscapeSequences() throws FormattingException {
        var input = "greeting=\\u041F\\u0440\\u0438\\u0432\\u0435\\u0442";

        var actual = StandaloneFormatter.formatCode(input, "unicode.properties");

        assertNotNull(actual);
        assertTrue(actual.contains("greeting="));
    }

    @Test
    @DisplayName("Empty values - content preserved")
    void emptyValues() throws FormattingException {
        var input = "empty=\nblank=";

        var actual = StandaloneFormatter.formatCode(input, "empty.properties");

        assertNotNull(actual);
        assertTrue(actual.contains("empty="));
    }

    @Test
    @DisplayName("Keys with dots (hierarchical) - content preserved")
    void keysWithDots() throws FormattingException {
        var input = "app.name=MyApp\napp.version=1.0\napp.config.timeout=30";

        var actual = StandaloneFormatter.formatCode(input, "hierarchical.properties");

        assertNotNull(actual);
        assertTrue(actual.contains("app.name="));
        assertTrue(actual.contains("app.version="));
    }

    @Test
    @DisplayName("Special characters in values - content preserved")
    void specialCharactersInValues() throws FormattingException {
        var input = "path=C:\\\\Users\\\\test\nurl=http\\://example.com";

        var actual = StandaloneFormatter.formatCode(input, "special.properties");

        assertNotNull(actual);
        assertTrue(actual.contains("path="));
        assertTrue(actual.contains("url="));
    }

    @Test
    @DisplayName("Spring application properties style - content preserved")
    void springApplicationProperties() throws FormattingException {
        var input = "spring.datasource.url=jdbc:mysql://localhost:3306/db\nspring.datasource.username=root";

        var actual = StandaloneFormatter.formatCode(input, "application.properties");

        assertNotNull(actual);
        assertTrue(actual.contains("spring.datasource.url="));
        assertTrue(actual.contains("spring.datasource.username="));
    }

    @Test
    @DisplayName("Message bundle style - content preserved")
    void messageBundleStyle() throws FormattingException {
        var input = "error.notfound=Resource not found\nerror.unauthorized=Access denied";

        var actual = StandaloneFormatter.formatCode(input, "messages.properties");

        assertNotNull(actual);
        assertTrue(actual.contains("error.notfound="));
        assertTrue(actual.contains("error.unauthorized="));
    }

    @Test
    @DisplayName("Blank lines between sections - content preserved")
    void blankLinesBetweenSections() throws FormattingException {
        var input = "# Database\ndb.url=localhost\n\n# Server\nserver.port=8080";

        var actual = StandaloneFormatter.formatCode(input, "sections.properties");

        assertNotNull(actual);
        assertTrue(actual.contains("db.url="));
        assertTrue(actual.contains("server.port="));
    }

    @Test
    @DisplayName("Empty input - returns empty")
    void emptyInput() throws FormattingException {
        var input = "";

        var actual = StandaloneFormatter.formatCode(input, "empty.properties");

        assertTrue(actual.isEmpty());
    }
}
