package com.intellij.formatter.languages;

import com.intellij.formatter.core.FormattingException;
import com.intellij.formatter.core.StandaloneFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for YAML code formatting.
 * Note: YAML formatter preserves content structure.
 */
@DisplayName("YAML Formatter Tests")
class YamlFormatterTest {

    @Test
    @DisplayName("Simple key-value pairs - content preserved")
    void simpleKeyValuePairs() throws FormattingException {
        var input = "name: John\nage: 30";

        var actual = StandaloneFormatter.formatCode(input, "test.yaml");

        assertNotNull(actual);
        assertTrue(actual.contains("name:"));
        assertTrue(actual.contains("age:"));
    }

    @Test
    @DisplayName("Nested structure - content preserved")
    void nestedStructure() throws FormattingException {
        var input = "person:\n  name: John\n  address:\n    city: NYC";

        var actual = StandaloneFormatter.formatCode(input, "nested.yaml");

        assertNotNull(actual);
        assertTrue(actual.contains("person:"));
        assertTrue(actual.contains("name:"));
        assertTrue(actual.contains("city:"));
    }

    @Test
    @DisplayName("List items - content preserved")
    void listItems() throws FormattingException {
        var input = "items:\n  - one\n  - two\n  - three";

        var actual = StandaloneFormatter.formatCode(input, "list.yaml");

        assertNotNull(actual);
        assertTrue(actual.contains("items:"));
        assertTrue(actual.contains("- one"));
        assertTrue(actual.contains("- two"));
    }

    @Test
    @DisplayName("Inline list - content preserved")
    void inlineList() throws FormattingException {
        var input = "numbers: [1, 2, 3]";

        var actual = StandaloneFormatter.formatCode(input, "inline.yaml");

        assertNotNull(actual);
        assertTrue(actual.contains("numbers:"));
        assertTrue(actual.contains("["));
    }

    @Test
    @DisplayName("Inline map - content preserved")
    void inlineMap() throws FormattingException {
        var input = "person: {name: John, age: 30}";

        var actual = StandaloneFormatter.formatCode(input, "inlinemap.yaml");

        assertNotNull(actual);
        assertTrue(actual.contains("person:"));
        assertTrue(actual.contains("{"));
    }

    @Test
    @DisplayName("Boolean values - content preserved")
    void booleanValues() throws FormattingException {
        var input = "enabled: true\ndisabled: false";

        var actual = StandaloneFormatter.formatCode(input, "bools.yaml");

        assertNotNull(actual);
        assertTrue(actual.contains("true"));
        assertTrue(actual.contains("false"));
    }

    @Test
    @DisplayName("Null values - content preserved")
    void nullValues() throws FormattingException {
        var input = "value: null\nempty: ~";

        var actual = StandaloneFormatter.formatCode(input, "nulls.yaml");

        assertNotNull(actual);
        assertTrue(actual.contains("null") || actual.contains("~"));
    }

    @Test
    @DisplayName("Multi-line string (literal block) - content preserved")
    void multiLineStringLiteral() throws FormattingException {
        var input = "description: |\n  This is a\n  multi-line string";

        var actual = StandaloneFormatter.formatCode(input, "literal.yaml");

        assertNotNull(actual);
        assertTrue(actual.contains("description:"));
        assertTrue(actual.contains("|"));
    }

    @Test
    @DisplayName("Multi-line string (folded block) - content preserved")
    void multiLineStringFolded() throws FormattingException {
        var input = "description: >\n  This is a\n  multi-line string";

        var actual = StandaloneFormatter.formatCode(input, "folded.yaml");

        assertNotNull(actual);
        assertTrue(actual.contains("description:"));
        assertTrue(actual.contains(">"));
    }

    @Test
    @DisplayName("Comments - content preserved")
    void comments() throws FormattingException {
        var input = "# This is a comment\nname: John";

        var actual = StandaloneFormatter.formatCode(input, "comments.yaml");

        assertNotNull(actual);
        assertTrue(actual.contains("# This is a comment"));
        assertTrue(actual.contains("name:"));
    }

    @Test
    @DisplayName("Anchors and aliases - content preserved")
    void anchorsAndAliases() throws FormattingException {
        var input = "defaults: &defaults\n  timeout: 30\nproduction:\n  <<: *defaults\n  timeout: 60";

        var actual = StandaloneFormatter.formatCode(input, "anchors.yaml");

        assertNotNull(actual);
        assertTrue(actual.contains("&defaults"));
        assertTrue(actual.contains("*defaults"));
    }

    @Test
    @DisplayName("Docker Compose style - content preserved")
    void dockerComposeStyle() throws FormattingException {
        var input = "version: '3'\nservices:\n  web:\n    image: nginx\n    ports:\n      - 80:80";

        var actual = StandaloneFormatter.formatCode(input, "docker-compose.yaml");

        assertNotNull(actual);
        assertTrue(actual.contains("version:"));
        assertTrue(actual.contains("services:"));
        assertTrue(actual.contains("image:"));
    }

    @Test
    @DisplayName("GitHub Actions style - content preserved")
    void githubActionsStyle() throws FormattingException {
        var input = "name: CI\non: push\njobs:\n  build:\n    runs-on: ubuntu-latest";

        var actual = StandaloneFormatter.formatCode(input, "ci.yml");

        assertNotNull(actual);
        assertTrue(actual.contains("name:"));
        assertTrue(actual.contains("jobs:"));
        assertTrue(actual.contains("runs-on:"));
    }

    @Test
    @DisplayName("Kubernetes style - content preserved")
    void kubernetesStyle() throws FormattingException {
        var input = "apiVersion: v1\nkind: Pod\nmetadata:\n  name: nginx";

        var actual = StandaloneFormatter.formatCode(input, "pod.yaml");

        assertNotNull(actual);
        assertTrue(actual.contains("apiVersion:"));
        assertTrue(actual.contains("kind:"));
        assertTrue(actual.contains("metadata:"));
    }

    @Test
    @DisplayName("Empty input - returns empty")
    void emptyInput() throws FormattingException {
        var input = "";

        var actual = StandaloneFormatter.formatCode(input, "empty.yaml");

        assertTrue(actual.isEmpty());
    }
}
