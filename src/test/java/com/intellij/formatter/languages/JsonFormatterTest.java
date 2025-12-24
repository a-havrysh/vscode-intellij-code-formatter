package com.intellij.formatter.languages;

import com.intellij.formatter.core.FormattingException;
import com.intellij.formatter.core.StandaloneFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for JSON code formatting.
 * Note: JSON formatter preserves content structure without adding indentation/newlines.
 */
@DisplayName("JSON Formatter Tests")
class JsonFormatterTest {

    @Test
    @DisplayName("Simple object - content preserved")
    void simpleObject() throws FormattingException {
        var input = "{\"name\":\"John\",\"age\":30}";

        var actual = StandaloneFormatter.formatCode(input, "test.json");

        assertNotNull(actual);
        assertTrue(actual.contains("\"name\""));
        assertTrue(actual.contains("\"John\""));
        assertTrue(actual.contains("\"age\""));
        assertTrue(actual.contains("30"));
    }

    @Test
    @DisplayName("Nested objects - content preserved")
    void nestedObjects() throws FormattingException {
        var input = "{\"person\":{\"name\":\"John\",\"address\":{\"city\":\"NYC\"}}}";

        var actual = StandaloneFormatter.formatCode(input, "nested.json");

        assertNotNull(actual);
        assertTrue(actual.contains("\"person\""));
        assertTrue(actual.contains("\"address\""));
        assertTrue(actual.contains("\"city\""));
        assertTrue(actual.contains("\"NYC\""));
    }

    @Test
    @DisplayName("Array of primitives - content preserved")
    void arrayOfPrimitives() throws FormattingException {
        var input = "{\"numbers\":[1,2,3,4,5]}";

        var actual = StandaloneFormatter.formatCode(input, "numbers.json");

        assertNotNull(actual);
        assertTrue(actual.contains("\"numbers\""));
        assertTrue(actual.contains("1"));
        assertTrue(actual.contains("5"));
    }

    @Test
    @DisplayName("Array of objects - content preserved")
    void arrayOfObjects() throws FormattingException {
        var input = "{\"users\":[{\"name\":\"John\"},{\"name\":\"Jane\"}]}";

        var actual = StandaloneFormatter.formatCode(input, "users.json");

        assertNotNull(actual);
        assertTrue(actual.contains("\"users\""));
        assertTrue(actual.contains("\"John\""));
        assertTrue(actual.contains("\"Jane\""));
    }

    @Test
    @DisplayName("Boolean values - content preserved")
    void booleanValues() throws FormattingException {
        var input = "{\"active\":true,\"deleted\":false}";

        var actual = StandaloneFormatter.formatCode(input, "bools.json");

        assertNotNull(actual);
        assertTrue(actual.contains("true"));
        assertTrue(actual.contains("false"));
    }

    @Test
    @DisplayName("Null values - content preserved")
    void nullValues() throws FormattingException {
        var input = "{\"name\":null,\"value\":null}";

        var actual = StandaloneFormatter.formatCode(input, "nulls.json");

        assertNotNull(actual);
        assertTrue(actual.contains("null"));
    }

    @Test
    @DisplayName("String with special characters - content preserved")
    void stringWithSpecialCharacters() throws FormattingException {
        var input = "{\"text\":\"Hello\\nWorld\\t!\"}";

        var actual = StandaloneFormatter.formatCode(input, "special.json");

        assertNotNull(actual);
        assertTrue(actual.contains("\\n"));
        assertTrue(actual.contains("\\t"));
    }

    @Test
    @DisplayName("Numbers in different formats - content preserved")
    void numbersInDifferentFormats() throws FormattingException {
        var input = "{\"int\":42,\"float\":3.14,\"negative\":-10,\"exp\":1e10}";

        var actual = StandaloneFormatter.formatCode(input, "numbers.json");

        assertNotNull(actual);
        assertTrue(actual.contains("42"));
        assertTrue(actual.contains("3.14"));
        assertTrue(actual.contains("-10"));
    }

    @Test
    @DisplayName("Empty object - content preserved")
    void emptyObject() throws FormattingException {
        var input = "{}";

        var actual = StandaloneFormatter.formatCode(input, "empty.json");

        assertEquals("{}", actual);
    }

    @Test
    @DisplayName("Empty array - content preserved")
    void emptyArray() throws FormattingException {
        var input = "{\"items\":[]}";

        var actual = StandaloneFormatter.formatCode(input, "emptyarray.json");

        assertNotNull(actual);
        assertTrue(actual.contains("\"items\""));
        assertTrue(actual.contains("[]"));
    }

    @Test
    @DisplayName("Complex mixed structure - content preserved")
    void complexMixedStructure() throws FormattingException {
        var input = "{\"users\":[{\"id\":1,\"name\":\"John\",\"roles\":[\"admin\",\"user\"]}],\"count\":1}";

        var actual = StandaloneFormatter.formatCode(input, "complex.json");

        assertNotNull(actual);
        assertTrue(actual.contains("\"users\""));
        assertTrue(actual.contains("\"admin\""));
        assertTrue(actual.contains("\"count\""));
    }

    @Test
    @DisplayName("Unicode characters - content preserved")
    void unicodeCharacters() throws FormattingException {
        var input = "{\"greeting\":\"Привет мир!\",\"chinese\":\"你好\"}";

        var actual = StandaloneFormatter.formatCode(input, "unicode.json");

        assertNotNull(actual);
        assertTrue(actual.contains("Привет мир!"));
        assertTrue(actual.contains("你好"));
    }

    @Test
    @DisplayName("VSCode settings.json style - content preserved")
    void vscodeSettingsStyle() throws FormattingException {
        var input = "{\"editor.fontSize\":14,\"editor.tabSize\":2,\"files.autoSave\":\"afterDelay\"}";

        var actual = StandaloneFormatter.formatCode(input, "settings.json");

        assertNotNull(actual);
        assertTrue(actual.contains("\"editor.fontSize\""));
        assertTrue(actual.contains("14"));
    }

    @Test
    @DisplayName("Package.json style - content preserved")
    void packageJsonStyle() throws FormattingException {
        var input = "{\"name\":\"test\",\"version\":\"1.0.0\",\"dependencies\":{\"lodash\":\"^4.0.0\"}}";

        var actual = StandaloneFormatter.formatCode(input, "package.json");

        assertNotNull(actual);
        assertTrue(actual.contains("\"name\""));
        assertTrue(actual.contains("\"dependencies\""));
    }

    @Test
    @DisplayName("Deeply nested structure - content preserved")
    void deeplyNestedStructure() throws FormattingException {
        var input = "{\"a\":{\"b\":{\"c\":{\"d\":{\"e\":\"deep\"}}}}}";

        var actual = StandaloneFormatter.formatCode(input, "deep.json");

        assertNotNull(actual);
        assertTrue(actual.contains("\"deep\""));
    }
}
