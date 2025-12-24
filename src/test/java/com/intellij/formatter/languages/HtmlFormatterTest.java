package com.intellij.formatter.languages;

import com.intellij.formatter.core.FormattingException;
import com.intellij.formatter.core.StandaloneFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for HTML code formatting.
 */
@DisplayName("HTML Formatter Tests")
class HtmlFormatterTest {

    @Test
    @DisplayName("Simple HTML document")
    void simpleHtmlDocument() throws FormattingException {
        var input = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";

        var actual = StandaloneFormatter.formatCode(input, "test.html");

        assertNotNull(actual);
        assertTrue(actual.contains("<html>"));
        assertTrue(actual.contains("<head>"));
        assertTrue(actual.contains("<title>Test</title>"));
        assertTrue(actual.contains("<body>"));
        assertTrue(actual.contains("<p>Hello</p>"));
    }

    @Test
    @DisplayName("HTML with attributes")
    void htmlWithAttributes() throws FormattingException {
        var input = "<div class=\"container\" id=\"main\"><span style=\"color:red\">Text</span></div>";

        var actual = StandaloneFormatter.formatCode(input, "attributes.html");

        assertNotNull(actual);
        assertTrue(actual.contains("class=\"container\""));
        assertTrue(actual.contains("id=\"main\""));
        assertTrue(actual.contains("style=\"color:red\""));
    }

    @Test
    @DisplayName("HTML form elements")
    void htmlFormElements() throws FormattingException {
        var input = "<form action=\"/submit\" method=\"post\"><input type=\"text\" name=\"username\"><button type=\"submit\">Submit</button></form>";

        var actual = StandaloneFormatter.formatCode(input, "form.html");

        assertNotNull(actual);
        assertTrue(actual.contains("<form"));
        assertTrue(actual.contains("<input"));
        assertTrue(actual.contains("<button"));
    }

    @Test
    @DisplayName("HTML table")
    void htmlTable() throws FormattingException {
        var input = "<table><tr><th>Name</th><th>Age</th></tr><tr><td>John</td><td>30</td></tr></table>";

        var actual = StandaloneFormatter.formatCode(input, "table.html");

        assertNotNull(actual);
        assertTrue(actual.contains("<table>"));
        assertTrue(actual.contains("<tr>"));
        assertTrue(actual.contains("<th>Name</th>"));
        assertTrue(actual.contains("<td>John</td>"));
    }

    @Test
    @DisplayName("HTML list")
    void htmlList() throws FormattingException {
        var input = "<ul><li>Item 1</li><li>Item 2</li><li>Item 3</li></ul>";

        var actual = StandaloneFormatter.formatCode(input, "list.html");

        assertNotNull(actual);
        assertTrue(actual.contains("<ul>"));
        assertTrue(actual.contains("<li>Item 1</li>"));
        assertTrue(actual.contains("<li>Item 2</li>"));
    }

    @Test
    @DisplayName("HTML5 semantic elements")
    void html5SemanticElements() throws FormattingException {
        var input = "<article><header><h1>Title</h1></header><section><p>Content</p></section><footer>Footer</footer></article>";

        var actual = StandaloneFormatter.formatCode(input, "semantic.html");

        assertNotNull(actual);
        assertTrue(actual.contains("<article>"));
        assertTrue(actual.contains("<header>"));
        assertTrue(actual.contains("<section>"));
        assertTrue(actual.contains("<footer>"));
    }

    @Test
    @DisplayName("HTML with doctype")
    void htmlWithDoctype() throws FormattingException {
        var input = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body></body></html>";

        var actual = StandaloneFormatter.formatCode(input, "doctype.html");

        assertNotNull(actual);
        assertTrue(actual.contains("<!DOCTYPE html>") || actual.contains("<!doctype html>"));
        assertTrue(actual.contains("<html>"));
    }

    @Test
    @DisplayName("HTML comments")
    void htmlComments() throws FormattingException {
        var input = "<div><!-- This is a comment --><p>Text</p></div>";

        var actual = StandaloneFormatter.formatCode(input, "comments.html");

        assertNotNull(actual);
        assertTrue(actual.contains("<!-- This is a comment -->"));
        assertTrue(actual.contains("<p>Text</p>"));
    }

    @Test
    @DisplayName("Self-closing tags")
    void selfClosingTags() throws FormattingException {
        var input = "<div><img src=\"image.png\" alt=\"Image\"><br><hr></div>";

        var actual = StandaloneFormatter.formatCode(input, "selfclosing.html");

        assertNotNull(actual);
        assertTrue(actual.contains("<img"));
        assertTrue(actual.contains("src=\"image.png\""));
    }

    @Test
    @DisplayName("HTML with inline styles and scripts")
    void htmlWithInlineStylesAndScripts() throws FormattingException {
        var input = "<html><head><style>body{margin:0}</style></head><body><script>console.log('test')</script></body></html>";

        var actual = StandaloneFormatter.formatCode(input, "inline.html");

        assertNotNull(actual);
        assertTrue(actual.contains("<style>"));
        assertTrue(actual.contains("<script>"));
    }

    @Test
    @DisplayName("Empty input - returns empty")
    void emptyInput() throws FormattingException {
        var input = "";

        var actual = StandaloneFormatter.formatCode(input, "empty.html");

        assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("XHTML document")
    void xhtmlDocument() throws FormattingException {
        var input = "<?xml version=\"1.0\"?><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test</title></head><body><p>Hello</p></body></html>";

        var actual = StandaloneFormatter.formatCode(input, "test.xhtml");

        assertNotNull(actual);
        assertTrue(actual.contains("<html"));
        assertTrue(actual.contains("xmlns="));
    }

    @Test
    @DisplayName("XHTML self-closing tags")
    void xhtmlSelfClosingTags() throws FormattingException {
        var input = "<div><br/><hr/><img src=\"test.png\"/></div>";

        var actual = StandaloneFormatter.formatCode(input, "selfclosing.xhtml");

        assertNotNull(actual);
        assertTrue(actual.contains("<div>"));
        assertTrue(actual.contains("src=\"test.png\""));
    }
}
