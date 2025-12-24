package com.intellij.formatter.languages;

import com.intellij.formatter.core.FormattingException;
import com.intellij.formatter.core.StandaloneFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for Java code formatting.
 */
@DisplayName("Java Formatter Tests")
class JavaFormatterTest {

    @Test
    @DisplayName("Simple class with method")
    void simpleClassWithMethod() throws FormattingException {
        var input = "public class Test{void method(){int x=1;}}";
        var expected = """
                public class Test {
                    void method() {
                        int x = 1;
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Test.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Class with annotations")
    void classWithAnnotations() throws FormattingException {
        var input = "@SuppressWarnings(\"all\")public class Test{@Override public String toString(){return\"\";}}";
        var expected = """
                @SuppressWarnings("all")
                public class Test {
                    @Override
                    public String toString() {
                        return "";
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Test.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Class with generics")
    void classWithGenerics() throws FormattingException {
        var input = "import java.util.*;public class Test<T>{private List<Map<String,T>>data;}";
        var expected = """
                import java.util.*;

                public class Test<T> {
                    private List<Map<String, T>> data;
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Test.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Lambda expressions")
    void lambdaExpressions() throws FormattingException {
        var input = "import java.util.function.*;public class Test{Runnable r=()->System.out.println(\"hello\");Function<String,Integer>f=s->s.length();}";
        var expected = """
                import java.util.function.*;

                public class Test {
                    Runnable r = () -> System.out.println("hello");
                    Function<String, Integer> f = s -> s.length();
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Test.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Switch expressions")
    void switchExpressions() throws FormattingException {
        var input = "public class Test{String get(int x){return switch(x){case 1->\"one\";case 2->\"two\";default->\"other\";};}}";
        var expected = """
                public class Test {
                    String get(int x) {
                        return switch (x) {
                            case 1 -> "one";
                            case 2 -> "two";
                            default -> "other";
                        };
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Test.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Record declaration")
    void recordDeclaration() throws FormattingException {
        var input = "public record Person(String name,int age){}";
        var expected = """
                public record Person(String name, int age) {
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Person.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Sealed class")
    void sealedClass() throws FormattingException {
        var input = "public sealed class Shape permits Circle,Square{}final class Circle extends Shape{}final class Square extends Shape{}";
        var expected = """
                public sealed class Shape permits Circle, Square {
                }

                final class Circle extends Shape {
                }

                final class Square extends Shape {
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Shape.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Pattern matching instanceof")
    void patternMatchingInstanceof() throws FormattingException {
        var input = "public class Test{void m(Object o){if(o instanceof String s&&s.length()>0){System.out.println(s);}}}";
        var expected = """
                public class Test {
                    void m(Object o) {
                        if (o instanceof String s && s.length() > 0) {
                            System.out.println(s);
                        }
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Test.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Method references")
    void methodReferences() throws FormattingException {
        var input = "import java.util.*;public class Test{void m(){var list=new ArrayList<String>();list.forEach(System.out::println);list.stream().map(String::toUpperCase).toList();}}";
        var expected = """
                import java.util.*;

                public class Test {
                    void m() {
                        var list = new ArrayList<String>();
                        list.forEach(System.out::println);
                        list.stream().map(String::toUpperCase).toList();
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Test.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Javadoc comments")
    void javadocComments() throws FormattingException {
        var input = """
                public class Test{
                /**
                 * Method description.
                 * @param x input
                 * @return result
                 */
                int calc(int x){return x*2;}}""";
        var expected = """
                public class Test {
                    /**
                     * Method description.
                     * @param x input
                     * @return result
                     */
                    int calc(int x) {
                        return x * 2;
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Test.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Text blocks")
    void textBlocks() throws FormattingException {
        var input = "public class Test{String json=\"\"\"\n{\"key\":\"value\"}\n\"\"\";}";
        var expected = """
                public class Test {
                    String json = \"""
                            {"key":"value"}
                            \""";
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Test.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("For loop")
    void forLoop() throws FormattingException {
        var input = "public class Test{void m(){for(int i=0;i<10;i++){System.out.println(i);}}}";
        var expected = """
                public class Test {
                    void m() {
                        for (int i = 0; i < 10; i++) {
                            System.out.println(i);
                        }
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Test.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Try-catch-finally")
    void tryCatchFinally() throws FormattingException {
        var input = "public class Test{void m(){try{throw new Exception();}catch(Exception e){e.printStackTrace();}finally{System.out.println(\"done\");}}}";
        var expected = """
                public class Test {
                    void m() {
                        try {
                            throw new Exception();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            System.out.println("done");
                        }
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Test.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Interface with default method")
    void interfaceWithDefaultMethod() throws FormattingException {
        var input = "public interface Service{void process();default void log(){System.out.println(\"log\");}}";
        var expected = """
                public interface Service {
                    void process();

                    default void log() {
                        System.out.println("log");
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Service.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Enum with constructor")
    void enumWithConstructor() throws FormattingException {
        var input = "public enum Status{ACTIVE(1),INACTIVE(0);private final int code;Status(int code){this.code=code;}}";
        var expected = """
                public enum Status {
                    ACTIVE(1), INACTIVE(0);
                    private final int code;

                    Status(int code) {
                        this.code = code;
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Status.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Static and instance initializer blocks")
    void initializerBlocks() throws FormattingException {
        var input = "public class Test{static{System.out.println(\"static\");}{System.out.println(\"instance\");}}";
        var expected = """
                public class Test {
                    static {
                        System.out.println("static");
                    }

                    {
                        System.out.println("instance");
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Test.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Empty input")
    void emptyInput() throws FormattingException {
        var input = "";
        var expected = "";

        var actual = StandaloneFormatter.formatCode(input, "Empty.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Unicode characters in strings")
    void unicodeCharacters() throws FormattingException {
        var input = "public class Test{String s=\"Привет мир! 你好世界!\";}";
        var expected = """
                public class Test {
                    String s = "Привет мир! 你好世界!";
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Test.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Multiple statements on one line")
    void multipleStatementsOnOneLine() throws FormattingException {
        var input = "public class Test{void m(){int a=1;int b=2;int c=3;}}";
        var expected = """
                public class Test {
                    void m() {
                        int a = 1;
                        int b = 2;
                        int c = 3;
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Test.java");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ternary operator")
    void ternaryOperator() throws FormattingException {
        var input = "public class Test{int m(boolean b){return b?1:0;}}";
        var expected = """
                public class Test {
                    int m(boolean b) {
                        return b ? 1 : 0;
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Test.java");
        assertEquals(expected, actual);
    }
}
