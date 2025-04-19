import org.example.LexicalAnalyzer;
import org.example.Readable;

import java.util.List;
import java.io.IOException;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Files;


// JUnit 5
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Mockito
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class LexicalAnalyzerMockTest {

    @Mock
    Readable fileReader;

    LexicalAnalyzer lexer;

    @BeforeEach
    void setup() {
        lexer = new LexicalAnalyzer(fileReader);
    }

    @Test
    void testAnalyzeFromFileSuccess() throws IOException {
        String mockCode = "int x = 42;";
        Path mockPath = Path.of("test.java");

        when(fileReader.read()).thenReturn(mockCode);

        List<LexicalAnalyzer.Token> tokens = lexer.analyzeFromFile();

        assertFalse(tokens.isEmpty());
        verify(fileReader, times(1)).read();
    }

    @Test
    void testAnalyzeFromFileIOException() throws IOException {
        when(fileReader.read()).thenThrow(new IOException("File error"));

        assertThrows(IOException.class, () -> lexer.analyzeFromFile());

        verify(fileReader).read();
    }

    @Test
    void testAnalyzeFromFileCalledTwiceInOrder() throws IOException {
        Readable reader1 = mock(Readable.class);
        Readable reader2 = mock(Readable.class);
        when(reader1.read()).thenReturn("int x = 1;");
        when(reader2.read()).thenReturn("int y = 2;");

        LexicalAnalyzer lexer1 = new LexicalAnalyzer(reader1);
        LexicalAnalyzer lexer2 = new LexicalAnalyzer(reader2);

        var tokens1 = lexer1.analyzeFromFile();
        var tokens2 = lexer2.analyzeFromFile();

        assertFalse(tokens1.isEmpty());
        assertFalse(tokens2.isEmpty());
        assertNotEquals(tokens1, tokens2);

        InOrder inOrder = inOrder(reader1, reader2);
        inOrder.verify(reader1).read();
        inOrder.verify(reader2).read();
    }

    @Test
    void testAnalyzeFromFileWithArgumentMatcher() throws IOException {
        Path javaPath = Path.of("SomeClass.java");
        Path txtPath = Path.of("notes.txt");

        Readable customReader = mock(Readable.class);

        when(customReader.read()).thenReturn("return 42;");

        LexicalAnalyzer customLexer = new LexicalAnalyzer(customReader);

        if (javaPath.toString().endsWith(".java")) {
            List<LexicalAnalyzer.Token> tokens = customLexer.analyzeFromFile();
            assertFalse(tokens.isEmpty());
        }

        verify(customReader, times(1)).read();
    }

    @Test
    void testAnalyzeFromDifferentInputsUsingMatchers() throws IOException {
        LexicalAnalyzer analyzer = spy(new LexicalAnalyzer(fileReader));

        doReturn(List.of(new LexicalAnalyzer.Token("int", "KEYWORD")))
                .when(analyzer).analyzeFromString(argThat(input -> input.contains("int")));

        doReturn(List.of(new LexicalAnalyzer.Token("x", "IDENTIFIER")))
                .when(analyzer).analyzeFromString(argThat(input -> input.contains("x")));

        when(fileReader.read())
                .thenReturn("int", "x");

        var tokens1 = analyzer.analyzeFromFile();
        var tokens2 = analyzer.analyzeFromFile();

        assertEquals("KEYWORD", tokens1.get(0).type);
        assertEquals("IDENTIFIER", tokens2.get(0).type);

        verify(analyzer, times(2)).analyzeFromString(anyString());
    }

    @Test
    void testAnalyzeFromFileInvalidPath() throws IOException {
        when(fileReader.read()).thenThrow(new IOException("File not found"));

        assertThrows(IOException.class, () -> lexer.analyzeFromFile());
    }
}

