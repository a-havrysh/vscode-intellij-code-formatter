package com.intellij.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for line ending handling (LF vs CRLF).
 *
 * These tests demonstrate bugs with CRLF line endings on Windows.
 */
@DisplayName("Line Ending Tests")
class LineEndingTest {

    @Test
    @DisplayName("Format code with CRLF line endings")
    void formatCodeWithCRLF() throws FormattingException {
        // Create unformatted Java code with CRLF line endings (Windows style)
        var input = "public class Test{\r\n" +
                    "void method(){\r\n" +
                    "int x=1;\r\n" +
                    "}\r\n" +
                    "}";

        var result = StandaloneFormatter.formatCode(input, "Test.java");

        // Should format correctly regardless of line endings
        assertTrue(result.contains("int x = 1;"), "Code should be formatted with spaces");
    }

    @Test
    @DisplayName("Format range with CRLF line endings - demonstrates bug")
    void formatRangeWithCRLF() throws FormattingException {
        // Create multi-line Java code with CRLF line endings
        // Line 1: public class Test{
        // Line 2: void method1(){int x=1;}
        // Line 3: void method2(){int y=2;}
        // Line 4: void method3(){int z=3;}
        // Line 5: }
        var input = "public class Test{\r\n" +
                    "void method1(){int x=1;}\r\n" +
                    "void method2(){int y=2;}\r\n" +
                    "void method3(){int z=3;}\r\n" +
                    "}";

        System.out.println("=== Input code (with CRLF) ===");
        System.out.println(input.replace("\r", "\\r").replace("\n", "\\n\n"));
        System.out.println();

        // Try to format ONLY lines 3-4 (method2 and method3)
        System.out.println("=== Attempting to format lines 3-4 ===");
        var result = StandaloneFormatter.formatCodeRange(input, "Test.java", 3, 4);

        System.out.println("=== Result ===");
        System.out.println(result);
        System.out.println();

        // Check if method2 and method3 are formatted
        boolean method2Formatted = result.contains("int y = 2;");
        boolean method3Formatted = result.contains("int z = 3;");

        System.out.println("method2 formatted correctly: " + method2Formatted);
        System.out.println("method3 formatted correctly: " + method3Formatted);

        // With CRLF bug, these assertions will likely fail because
        // getLineStartOffset/getLineEndOffset count offsets incorrectly
        assertEquals(true, method2Formatted,
            "Line 3 (method2) should be formatted with spaces");
        assertEquals(true, method3Formatted,
            "Line 4 (method3) should be formatted with spaces");
    }

    @Test
    @DisplayName("Format range with LF line endings - should work")
    void formatRangeWithLF() throws FormattingException {
        // Same code but with LF line endings (Unix style)
        var input = "public class Test{\n" +
                    "void method1(){int x=1;}\n" +
                    "void method2(){int y=2;}\n" +
                    "void method3(){int z=3;}\n" +
                    "}";

        System.out.println("=== Input code (with LF) ===");
        System.out.println(input.replace("\n", "\\n\n"));
        System.out.println();

        // Try to format ONLY lines 3-4 (method2 and method3)
        System.out.println("=== Attempting to format lines 3-4 ===");
        var result = StandaloneFormatter.formatCodeRange(input, "Test.java", 3, 4);

        System.out.println("=== Result ===");
        System.out.println(result);
        System.out.println();

        // With LF, this should work correctly
        boolean method2Formatted = result.contains("int y = 2;");
        boolean method3Formatted = result.contains("int z = 3;");

        System.out.println("method2 formatted correctly: " + method2Formatted);
        System.out.println("method3 formatted correctly: " + method3Formatted);

        assertEquals(true, method2Formatted,
            "Line 3 (method2) should be formatted with spaces");
        assertEquals(true, method3Formatted,
            "Line 4 (method3) should be formatted with spaces");
    }

    @Test
    @DisplayName("Compare CRLF vs LF offset calculation")
    void compareOffsets() {
        // Simple 3-line text
        var textCRLF = "line1\r\nline2\r\nline3";
        var textLF = "line1\nline2\nline3";

        System.out.println("=== Offset Analysis ===");
        System.out.println("Text with CRLF:");
        System.out.println("  Total length: " + textCRLF.length());
        System.out.println("  Bytes: " + textCRLF.replace("\r", "\\r").replace("\n", "\\n"));
        System.out.println();

        System.out.println("Text with LF:");
        System.out.println("  Total length: " + textLF.length());
        System.out.println("  Bytes: " + textLF.replace("\n", "\\n"));
        System.out.println();

        // With CRLF: each line ending is 2 bytes
        // line1\r\n = 7 bytes (5 chars + 2 for \r\n)
        // line2\r\n = 7 bytes (5 chars + 2 for \r\n)
        // line3 = 5 bytes
        // Total = 19 bytes

        // With LF: each line ending is 1 byte
        // line1\n = 6 bytes (5 chars + 1 for \n)
        // line2\n = 6 bytes (5 chars + 1 for \n)
        // line3 = 5 bytes
        // Total = 17 bytes

        assertEquals(19, textCRLF.length(), "CRLF text should be 19 bytes");
        assertEquals(17, textLF.length(), "LF text should be 17 bytes");

        System.out.println("Line 2 should start at different offsets:");
        System.out.println("  With CRLF: offset 7 (after 'line1\\r\\n')");
        System.out.println("  With LF: offset 6 (after 'line1\\n')");
        System.out.println();
        System.out.println("But getLineStartOffset() only counts \\n, so:");
        System.out.println("  For CRLF text, it would incorrectly calculate offset");
        System.out.println("  treating \\r as a regular character!");
    }
}
