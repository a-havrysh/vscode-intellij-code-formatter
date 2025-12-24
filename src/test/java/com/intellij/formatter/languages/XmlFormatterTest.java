package com.intellij.formatter.languages;

import com.intellij.formatter.core.FormattingException;
import com.intellij.formatter.core.StandaloneFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for XML code formatting.
 * Note: XML formatter preserves content structure.
 */
@DisplayName("XML Formatter Tests")
class XmlFormatterTest {

    @Test
    @DisplayName("Simple XML element - content preserved")
    void simpleXmlElement() throws FormattingException {
        var input = "<root><child>text</child></root>";

        var actual = StandaloneFormatter.formatCode(input, "test.xml");

        assertNotNull(actual);
        assertTrue(actual.contains("<root>"));
        assertTrue(actual.contains("<child>text</child>"));
        assertTrue(actual.contains("</root>"));
    }

    @Test
    @DisplayName("Nested elements - content preserved")
    void nestedElements() throws FormattingException {
        var input = "<root><level1><level2><level3>deep</level3></level2></level1></root>";

        var actual = StandaloneFormatter.formatCode(input, "nested.xml");

        assertNotNull(actual);
        assertTrue(actual.contains("<root>"));
        assertTrue(actual.contains("<level3>deep</level3>"));
    }

    @Test
    @DisplayName("XML with attributes - content preserved")
    void xmlWithAttributes() throws FormattingException {
        var input = "<person name=\"John\" age=\"30\"><address city=\"NYC\" zip=\"10001\"/></person>";

        var actual = StandaloneFormatter.formatCode(input, "person.xml");

        assertNotNull(actual);
        assertTrue(actual.contains("name=\"John\""));
        assertTrue(actual.contains("age=\"30\""));
        assertTrue(actual.contains("city=\"NYC\""));
    }

    @Test
    @DisplayName("XML declaration - content preserved")
    void xmlDeclaration() throws FormattingException {
        var input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><item>content</item></root>";

        var actual = StandaloneFormatter.formatCode(input, "declaration.xml");

        assertNotNull(actual);
        assertTrue(actual.contains("<?xml"));
        assertTrue(actual.contains("version=\"1.0\""));
        assertTrue(actual.contains("<root>"));
    }

    @Test
    @DisplayName("XML with comments - content preserved")
    void xmlWithComments() throws FormattingException {
        var input = "<root><!-- comment --><child>text</child></root>";

        var actual = StandaloneFormatter.formatCode(input, "comments.xml");

        assertNotNull(actual);
        assertTrue(actual.contains("<!-- comment -->"));
        assertTrue(actual.contains("<child>text</child>"));
    }

    @Test
    @DisplayName("Mixed content - content preserved")
    void mixedContent() throws FormattingException {
        var input = "<p>This is <b>bold</b> and <i>italic</i> text.</p>";

        var actual = StandaloneFormatter.formatCode(input, "mixed.xml");

        assertNotNull(actual);
        assertTrue(actual.contains("<b>bold</b>"));
        assertTrue(actual.contains("<i>italic</i>"));
    }

    @Test
    @DisplayName("Self-closing tags - content preserved")
    void selfClosingTags() throws FormattingException {
        var input = "<config><setting name=\"debug\" value=\"true\"/><setting name=\"level\" value=\"5\"/></config>";

        var actual = StandaloneFormatter.formatCode(input, "config.xml");

        assertNotNull(actual);
        assertTrue(actual.contains("name=\"debug\""));
        assertTrue(actual.contains("value=\"true\""));
    }

    @Test
    @DisplayName("CDATA section - content preserved")
    void cdataSection() throws FormattingException {
        var input = "<script><![CDATA[function test() { return 1; }]]></script>";

        var actual = StandaloneFormatter.formatCode(input, "cdata.xml");

        assertNotNull(actual);
        assertTrue(actual.contains("<![CDATA["));
        assertTrue(actual.contains("function test()"));
    }

    @Test
    @DisplayName("Namespaced elements - content preserved")
    void namespacedElements() throws FormattingException {
        var input = "<ns:root xmlns:ns=\"http://example.com\"><ns:child>text</ns:child></ns:root>";

        var actual = StandaloneFormatter.formatCode(input, "namespace.xml");

        assertNotNull(actual);
        assertTrue(actual.contains("xmlns:ns="));
        assertTrue(actual.contains("<ns:child>"));
    }

    @Test
    @DisplayName("Multiple root siblings - content preserved")
    void multipleRootSiblings() throws FormattingException {
        var input = "<items><item>1</item><item>2</item><item>3</item></items>";

        var actual = StandaloneFormatter.formatCode(input, "items.xml");

        assertNotNull(actual);
        assertTrue(actual.contains("<item>1</item>"));
        assertTrue(actual.contains("<item>2</item>"));
        assertTrue(actual.contains("<item>3</item>"));
    }

    @Test
    @DisplayName("Empty element - content preserved")
    void emptyElement() throws FormattingException {
        var input = "<root><empty></empty></root>";

        var actual = StandaloneFormatter.formatCode(input, "empty.xml");

        assertNotNull(actual);
        assertTrue(actual.contains("<empty>"));
    }

    @Test
    @DisplayName("XML with DTD - content preserved")
    void xmlWithDtd() throws FormattingException {
        var input = "<?xml version=\"1.0\"?><!DOCTYPE root SYSTEM \"test.dtd\"><root><child/></root>";

        var actual = StandaloneFormatter.formatCode(input, "dtd.xml");

        assertNotNull(actual);
        assertTrue(actual.contains("<!DOCTYPE"));
        assertTrue(actual.contains("<root>"));
    }

    @Test
    @DisplayName("Processing instruction - content preserved")
    void processingInstruction() throws FormattingException {
        var input = "<?xml version=\"1.0\"?><?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?><root/>";

        var actual = StandaloneFormatter.formatCode(input, "pi.xml");

        assertNotNull(actual);
        assertTrue(actual.contains("<?xml-stylesheet"));
        assertTrue(actual.contains("<root/>"));
    }

    @Test
    @DisplayName("Maven POM file - content preserved")
    void mavenPomFile() throws FormattingException {
        var input = "<project><groupId>com.example</groupId><artifactId>test</artifactId><version>1.0</version></project>";

        var actual = StandaloneFormatter.formatCode(input, "pom.xml");

        assertNotNull(actual);
        assertTrue(actual.contains("<groupId>com.example</groupId>"));
        assertTrue(actual.contains("<artifactId>test</artifactId>"));
    }

    @Test
    @DisplayName("Empty input - returns empty")
    void emptyInput() throws FormattingException {
        var input = "";

        var actual = StandaloneFormatter.formatCode(input, "empty.xml");

        assertTrue(actual.isEmpty());
    }
}
