import org.example.LexicalAnalyzer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class LexicalAnalyzerFromStringTest {
    private LexicalAnalyzer lexer;
    @BeforeEach
    void setup() {
        lexer = new LexicalAnalyzer();
    }

    @ParameterizedTest
    @MethodSource("tokenCases")
    void testTokenRecognition(String input, String expectedValue, String expectedType) {
        LexicalAnalyzer lexer = new LexicalAnalyzer();
        List<?> tokens = lexer.analyzeFromString(input);
        assertEquals(1, tokens.size());
        assertEquals(new LexicalAnalyzer.Token(expectedValue, expectedType), tokens.get(0));
    }

    static Stream<Arguments> tokenCases() {
        return Stream.of(
            // Hexadecimal number
            Arguments.of("0x1F", "0x1F", "HEX_NUMBER"),

            // Octal number
            Arguments.of("0755", "0755", "OCTAL_NUMBER"),

            // Float number
            Arguments.of("3.14f", "3.14f", "FLOAT_NUMBER"),

            // Regular number
            Arguments.of("123", "123", "NUMBER"),

            // Long number with suffix L
            Arguments.of("123L", "123L", "NUMBER"),

            // Long number with suffix l
            Arguments.of("123l", "123l", "NUMBER"),

            // Float with suffix F
            Arguments.of("3.14F", "3.14F", "FLOAT_NUMBER"),

            // Double number (without suffix)
            Arguments.of("3.14", "3.14", "NUMBER"),

            // String literal
            Arguments.of("\"hello\"", "\"hello\"", "STRING"),

            // Char literal
            Arguments.of("'a'", "'a'", "CHAR"),

            // Single-line comment
            Arguments.of("// comment", "// comment", "COMMENT"),

            // Reserved keyword
            Arguments.of("if", "if", "RESERVED_KEYWORD"),
            Arguments.of("else", "else", "RESERVED_KEYWORD"),
            Arguments.of("for", "for", "RESERVED_KEYWORD"),
            Arguments.of("while", "while", "RESERVED_KEYWORD"),
            Arguments.of("int", "int", "RESERVED_KEYWORD"),
            Arguments.of("float", "float", "RESERVED_KEYWORD"),
            Arguments.of("double", "double", "RESERVED_KEYWORD"),
            Arguments.of("char", "char", "RESERVED_KEYWORD"),
            Arguments.of("public", "public", "RESERVED_KEYWORD"),
            Arguments.of("class", "class", "RESERVED_KEYWORD"),
            Arguments.of("static", "static", "RESERVED_KEYWORD"),
            Arguments.of("void", "void", "RESERVED_KEYWORD"),
            Arguments.of("return", "return", "RESERVED_KEYWORD"),

            // Operators
            Arguments.of("+", "+", "OPERATOR"),
            Arguments.of("-", "-", "OPERATOR"),
            Arguments.of("*", "*", "OPERATOR"),
            Arguments.of("/", "/", "OPERATOR"),
            Arguments.of("++", "++", "OPERATOR"),
            Arguments.of("--", "--", "OPERATOR"),

            // Delimiters
            Arguments.of(";", ";", "DELIMITER"),
            Arguments.of(",", ",", "DELIMITER"),

            // Identifiers
            Arguments.of("myVar", "myVar", "IDENTIFIER"),
            Arguments.of("myVar123", "myVar123", "IDENTIFIER"),

            // Invalid token
            Arguments.of("@@@", "@@@", "INVALID"),

            // Invalid number (with invalid suffix)
            Arguments.of("123a", "123a", "INVALID")
        );
    }
}

