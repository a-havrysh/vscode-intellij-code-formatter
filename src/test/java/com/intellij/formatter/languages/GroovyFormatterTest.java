package com.intellij.formatter.languages;

import com.intellij.formatter.core.FormattingException;
import com.intellij.formatter.core.StandaloneFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for Groovy code formatting.
 */
@DisplayName("Groovy Formatter Tests")
class GroovyFormatterTest {

    @Test
    @DisplayName("Simple class with properties")
    void simpleClassWithProperties() throws FormattingException {
        var input = "class Person{String name\nint age}";
        var expected = """
                class Person {
                    String name
                    int age
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Person.groovy");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Class with method")
    void classWithMethod() throws FormattingException {
        var input = "class Calculator{def add(a,b){return a+b}}";
        var expected = """
                class Calculator {
                    def add(a, b) {
                        return a + b
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Calculator.groovy");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Closures")
    void closures() throws FormattingException {
        var input = "def list=[1,2,3]\ndef doubled=list.collect{it*2}";
        var expected = """
                def list = [1, 2, 3]
                def doubled = list.collect {
                    it * 2
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Closures.groovy");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("GString interpolation")
    void gstringInterpolation() throws FormattingException {
        var input = "def name=\"World\"\ndef greeting=\"Hello, ${name}!\"";
        var expected = """
                def name = "World"
                def greeting = "Hello, ${name}!\"""";

        var actual = StandaloneFormatter.formatCode(input, "GString.groovy");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Maps and lists")
    void mapsAndLists() throws FormattingException {
        var input = "def map=[name:'John',age:30]\ndef list=[1,2,3,4,5]";
        var expected = """
                def map = [name: 'John', age: 30]
                def list = [1, 2, 3, 4, 5]""";

        var actual = StandaloneFormatter.formatCode(input, "Collections.groovy");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Gradle build file (.gradle)")
    void gradleBuildFile() throws FormattingException {
        var input = "plugins{id'java'}\ndependencies{implementation'org.example:lib:1.0'}";
        var expected = """
                plugins {
                    id 'java'
                }
                dependencies {
                    implementation 'org.example:lib:1.0'
                }""";

        var actual = StandaloneFormatter.formatCode(input, "build.gradle");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Elvis operator")
    void elvisOperator() throws FormattingException {
        var input = "def name=null\ndef result=name?:\"default\"";
        var expected = """
                def name = null
                def result = name ?: "default\"""";

        var actual = StandaloneFormatter.formatCode(input, "Elvis.groovy");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Safe navigation operator")
    void safeNavigationOperator() throws FormattingException {
        var input = "def person=null\ndef name=person?.name";
        var expected = """
                def person = null
                def name = person?.name""";

        var actual = StandaloneFormatter.formatCode(input, "SafeNav.groovy");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Spread operator")
    void spreadOperator() throws FormattingException {
        var input = "def people=[[name:'John'],[name:'Jane']]\ndef names=people*.name";
        var expected = """
                def people = [[name: 'John'], [name: 'Jane']]
                def names = people*.name""";

        var actual = StandaloneFormatter.formatCode(input, "Spread.groovy");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Multiple assignments")
    void multipleAssignments() throws FormattingException {
        var input = "def(a,b,c)=[1,2,3]";
        var expected = "def (a, b, c) = [1, 2, 3]";

        var actual = StandaloneFormatter.formatCode(input, "MultiAssign.groovy");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Range operator")
    void rangeOperator() throws FormattingException {
        var input = "def range=1..10\ndef exclusive=1..<10";
        var expected = """
                def range = 1..10
                def exclusive = 1..<10""";

        var actual = StandaloneFormatter.formatCode(input, "Range.groovy");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Trait")
    void trait() throws FormattingException {
        var input = "trait Flyable{abstract void fly()\ndef land(){println\"Landing\"}}";
        var expected = """
                trait Flyable {
                    abstract void fly()

                    def land() {
                        println "Landing"
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Flyable.groovy");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Annotations")
    void annotations() throws FormattingException {
        var input = "@Singleton\nclass MyService{@Override String toString(){return\"MyService\"}}";
        var expected = """
                @Singleton
                class MyService {
                    @Override
                    String toString() {
                        return "MyService"
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "MyService.groovy");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Switch statement")
    void switchStatement() throws FormattingException {
        var input = "def describe(x){switch(x){case 1:return\"one\"\ncase 2:return\"two\"\ndefault:return\"other\"}}";
        var expected = """
                def describe(x) {
                    switch (x) {
                        case 1: return "one"
                        case 2: return "two"
                        default: return "other"
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Switch.groovy");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Try-catch")
    void tryCatch() throws FormattingException {
        var input = "def parse(s){try{return Integer.parseInt(s)}catch(Exception e){return 0}}";
        var expected = """
                def parse(s) {
                    try {
                        return Integer.parseInt(s)
                    } catch (Exception e) {
                        return 0
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "TryCatch.groovy");
        assertEquals(expected, actual);
    }
}
