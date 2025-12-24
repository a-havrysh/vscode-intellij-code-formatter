package com.intellij.formatter.languages;

import com.intellij.formatter.core.FormattingException;
import com.intellij.formatter.core.StandaloneFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for Kotlin code formatting.
 */
@DisplayName("Kotlin Formatter Tests")
class KotlinFormatterTest {

    @Test
    @DisplayName("Simple class with properties")
    void simpleClassWithProperties() throws FormattingException {
        var input = "class Person(val name:String,var age:Int)";
        var expected = "class Person(val name: String, var age: Int)";

        var actual = StandaloneFormatter.formatCode(input, "Person.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Class with function")
    void classWithFunction() throws FormattingException {
        var input = "class Calculator{fun add(a:Int,b:Int):Int{return a+b}}";
        var expected = """
                class Calculator {
                    fun add(a: Int, b: Int): Int {
                        return a + b
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Calculator.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Data class")
    void dataClass() throws FormattingException {
        var input = "data class User(val id:Long,val name:String,val email:String)";
        var expected = "data class User(val id: Long, val name: String, val email: String)";

        var actual = StandaloneFormatter.formatCode(input, "User.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Extension function")
    void extensionFunction() throws FormattingException {
        var input = "fun String.addExclamation()=this+\"!\"";
        var expected = "fun String.addExclamation() = this + \"!\"";

        var actual = StandaloneFormatter.formatCode(input, "Extensions.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Lambda expressions")
    void lambdaExpressions() throws FormattingException {
        var input = "val doubled=listOf(1,2,3).map{it*2}";
        var expected = "val doubled = listOf(1, 2, 3).map { it * 2 }";

        var actual = StandaloneFormatter.formatCode(input, "Lambdas.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("When expression")
    void whenExpression() throws FormattingException {
        var input = "fun describe(x:Any):String=when(x){is Int->\"Int\"\nis String->\"String\"\nelse->\"Unknown\"}";
        var expected = """
                fun describe(x: Any): String = when (x) {
                    is Int -> "Int"
                    is String -> "String"
                    else -> "Unknown"
                }""";

        var actual = StandaloneFormatter.formatCode(input, "When.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Sealed class")
    void sealedClass() throws FormattingException {
        var input = "sealed class Result<out T>\ndata class Success<T>(val data:T):Result<T>()\ndata class Error(val message:String):Result<Nothing>()";
        var expected = """
                sealed class Result<out T>
                data class Success<T>(val data: T) : Result<T>()
                data class Error(val message: String) : Result<Nothing>()""";

        var actual = StandaloneFormatter.formatCode(input, "Result.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Null safety operators")
    void nullSafetyOperators() throws FormattingException {
        var input = "fun getLength(s:String?):Int=s?.length?:0";
        var expected = "fun getLength(s: String?): Int = s?.length ?: 0";

        var actual = StandaloneFormatter.formatCode(input, "NullSafe.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Coroutines suspend function")
    void coroutinesSuspendFunction() throws FormattingException {
        var input = "suspend fun fetchData():String{return\"data\"}";
        var expected = """
                suspend fun fetchData(): String {
                    return "data"
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Coroutines.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Object declaration")
    void objectDeclaration() throws FormattingException {
        var input = "object Singleton{val name=\"singleton\"\nfun greet()=println(name)}";
        var expected = """
                object Singleton {
                    val name = "singleton"
                    fun greet() = println(name)
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Singleton.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Companion object")
    void companionObject() throws FormattingException {
        var input = "class MyClass{companion object{const val TAG=\"MyClass\"\nfun create():MyClass=MyClass()}}";
        var expected = """
                class MyClass {
                    companion object {
                        const val TAG = "MyClass"
                        fun create(): MyClass = MyClass()
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "MyClass.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Interface with default implementation")
    void interfaceWithDefaultImplementation() throws FormattingException {
        var input = "interface Printable{fun print()\nfun log(){println(\"Logging\")}}";
        var expected = """
                interface Printable {
                    fun print()
                    fun log() {
                        println("Logging")
                    }
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Printable.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Kotlin script (.kts)")
    void kotlinScript() throws FormattingException {
        var input = "val message=\"Hello\"\nprintln(message)\nfun double(x:Int)=x*2";
        var expected = """
                val message = "Hello"
                println(message)
                fun double(x: Int) = x * 2""";

        var actual = StandaloneFormatter.formatCode(input, "script.kts");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Gradle Kotlin DSL (.gradle.kts)")
    void gradleKotlinDsl() throws FormattingException {
        var input = "plugins{id(\"java\")}\ndependencies{implementation(\"org.example:lib:1.0\")}";
        var expected = """
                plugins { id("java") }
                dependencies { implementation("org.example:lib:1.0") }""";

        var actual = StandaloneFormatter.formatCode(input, "build.gradle.kts");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("String templates")
    void stringTemplates() throws FormattingException {
        var input = "val name=\"World\"\nval greeting=\"Hello, $name!\"\nval complex=\"Result: ${1+2}\"";
        var expected = """
                val name = "World"
                val greeting = "Hello, $name!"
                val complex = "Result: ${1 + 2}\"""";

        var actual = StandaloneFormatter.formatCode(input, "Templates.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Destructuring declarations")
    void destructuringDeclarations() throws FormattingException {
        var input = "data class Point(val x:Int,val y:Int)\nfun main(){val(x,y)=Point(1,2)}";
        var expected = """
                data class Point(val x: Int, val y: Int)

                fun main() {
                    val (x, y) = Point(1, 2)
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Destructuring.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Inline class (value class)")
    void inlineClass() throws FormattingException {
        var input = "@JvmInline\nvalue class Password(private val value:String)";
        var expected = """
                @JvmInline
                value class Password(private val value: String)""";

        var actual = StandaloneFormatter.formatCode(input, "Password.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Type aliases")
    void typeAliases() throws FormattingException {
        var input = "typealias StringList=List<String>\ntypealias Predicate<T>=(T)->Boolean";
        var expected = """
                typealias StringList = List<String>
                typealias Predicate<T>=(T)->Boolean""";

        var actual = StandaloneFormatter.formatCode(input, "TypeAliases.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Try-catch expression")
    void tryCatchExpression() throws FormattingException {
        var input = "fun parse(s:String):Int=try{s.toInt()}catch(e:Exception){0}";
        var expected = """
                fun parse(s: String): Int = try {
                    s.toInt()
                } catch (e: Exception) {
                    0
                }""";

        var actual = StandaloneFormatter.formatCode(input, "TryCatch.kt");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Operator overloading")
    void operatorOverloading() throws FormattingException {
        var input = "data class Vector(val x:Int,val y:Int){operator fun plus(other:Vector)=Vector(x+other.x,y+other.y)}";
        var expected = """
                data class Vector(val x: Int, val y: Int) {
                    operator fun plus(other: Vector) = Vector(x + other.x, y + other.y)
                }""";

        var actual = StandaloneFormatter.formatCode(input, "Vector.kt");
        assertEquals(expected, actual);
    }
}
