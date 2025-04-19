import org.example.LexicalAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf(value = "RunTestsCondition#isTestsEnabled", disabledReason = "Disabled by config")
public class LexicalAnalyzerMainTest {
    @Test
    void testMainWithNoArguments() {
        var out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(out));

        LexicalAnalyzer.main(new String[0]);

        String output = out.toString();
        assertTrue(output.contains("Usage: java LexicalAnalyzer.java <file_path>"));
    }

    @Test
    void testMainWithMultipleArguments() {
        var out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(out));

        LexicalAnalyzer.main(new String[]{"a", "b"});

        String output = out.toString();
        assertTrue(output.contains("Usage: java LexicalAnalyzer.java <file_path>"));
    }


    @Test
    void testMainWithNonExistingFile() {
        var out = new ByteArrayOutputStream();
        System.setErr(new PrintStream(out));

        String[] args = {"non_existing_file.java"};
        LexicalAnalyzer.main(args);

        String output = out.toString();
        assertTrue(output.contains("Error: File does not exist"));
    }

    @Test
    void testMainNonExistingFile() {
        String[] args = {"non_existing_file.java"};

        Exception exception = assertThrows(RuntimeException.class, () -> {
            LexicalAnalyzer.main(args);
        });

        // // Verify that the exception message contains the expected error message
        // String expectedMessage = "Error: File does not exist: non_existing_file.java";
        // String actualMessage = exception.getMessage();
        //
        // // Assert the message is as expected
        // assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testMainWithDirectotyPath(@TempDir Path tempDir) {
        var out = new ByteArrayOutputStream();
        System.setErr(new PrintStream(out));

        String[] args = {tempDir.toString()};

        LexicalAnalyzer.main(args);

        String output = out.toString();
        assertTrue(output.contains("Error: Path is not a regular file"));
    }

    @Test
    void testMainWithUnreadableFile(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("test.java");
        Files.writeString(file, "public class Test {}");
        file.toFile().setReadable(false);

        var out = new ByteArrayOutputStream();
        System.setErr(new PrintStream(out));

        LexicalAnalyzer.main(new String[]{file.toString()});

        String output = out.toString();
        assertTrue(output.contains("Error: Cannot read file"));
    }

    @Test
    void testMainWithValidFile() throws IOException {
        Path tempFile = Files.createTempFile("sample_code", ".txt");
        Files.writeString(tempFile, "int x = 10;");

        var out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        LexicalAnalyzer.main(new String[]{tempFile.toString()});

        String output = out.toString();
        System.out.println(output);
        assertTrue(output.contains("<int, RESERVED_KEYWORD>"));
        assertTrue(output.contains("<10, NUMBER>"));
    }
}
