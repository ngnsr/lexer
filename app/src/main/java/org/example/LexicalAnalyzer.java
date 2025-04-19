package org.example;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexicalAnalyzer {
    private static final Pattern PATTERN = Pattern.compile(
        "(?<HEX>0x[0-9A-Fa-f_]+[Ll]?)" +
        "|(?<OCTAL>0[0-7][0-7_]*)" +
        "|(?<FLOAT>\\b\\d+[\\d_]*\\.?[\\d_]*([eE][+-]?\\d+)?[fF]\\b)" +
        "|(?<NUMBER>\\b\\d+[\\d_]*\\.?[\\d_]*([eE][+-]?\\d+)?[Ll]?\\b)" +
        "|(?<STRING>\".*?\")" +
        "|(?<CHAR>'\\\\?.')" +
        "|(?<COMMENT>//.*|/\\*(.|\\R)*?\\*/)" +
        "|(?<RESERVED>\\b(if|else|for|while|int|float|double|char|public|class|static|void|return)\\b)" +
        "|(?<OPERATOR>[+\\-*/=<>!&|]+)" +
        "|(?<DELIMITER>[,;(){}\\[\\]])" +
        "|(?<IDENTIFIER>\\b[a-zA-Z_]\\w*\\b)" +
        "|(?<INVALID>\\S+)"
    );
    private Readable fileReader;

    public LexicalAnalyzer(){}

    public LexicalAnalyzer(Readable fileReader) {
        this.fileReader = fileReader;
    }

    public static class Token {
        public final String value;
        public final String type;

        public Token(String value, String type) {
            this.value = value;
            this.type = type;
        }

        @Override
        public String toString() {
            return "<" + value + ", " + type + ">";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Token token)) return false;
            return value.equals(token.value) && type.equals(token.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, type);
        }
    }

    public List<Token> analyzeFromString(String content) {
        List<Token> result = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(content);
        while (matcher.find()) {
            String token = matcher.group();
            if (matcher.group("HEX") != null) {
                result.add(new Token(token, "HEX_NUMBER"));
            } else if (matcher.group("OCTAL") != null) {
                result.add(new Token(token, "OCTAL_NUMBER"));
            } else if (matcher.group("FLOAT") != null) {
                result.add(new Token(token, "FLOAT_NUMBER"));
            } else if (matcher.group("NUMBER") != null) {
                result.add(new Token(token, "NUMBER"));
            } else if (matcher.group("STRING") != null) {
                result.add(new Token(token, "STRING"));
            } else if (matcher.group("CHAR") != null) {
                result.add( new Token(token, "CHAR"));
            } else if (matcher.group("COMMENT") != null) {
                result.add(new Token(token, "COMMENT"));
            } else if (matcher.group("RESERVED") != null) {
                result.add(new Token(token, "RESERVED_KEYWORD"));
            } else if (matcher.group("OPERATOR") != null) {
                result.add(new Token(token, "OPERATOR"));
            } else if (matcher.group("DELIMITER") != null) {
                result.add(new Token(token, "DELIMITER"));
            } else if (matcher.group("IDENTIFIER") != null) {
                result.add(new Token(token, "IDENTIFIER"));
            } else {
                result.add(new Token(token, "INVALID"));
            }
        }
        return result;
    }

    public List<Token> analyzeFromFile() throws IOException {
        String content = fileReader.read();
        return analyzeFromString(content);
    }

    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                throw new IllegalArgumentException("Usage: java LexicalAnalyzer.java <file_path>");
            }
            String filePath = args[0];
            Path path = Path.of(filePath);
            if (!Files.exists(path)) {
                throw new FileNotFoundException("Error: File does not exist: " + filePath);
            }
            if (!Files.isRegularFile(path)) {
                throw new IOException("Error: Path is not a regular file: " + filePath);
            }
            if (!Files.isReadable(path)) {
                throw new IOException("Error: Cannot read file: " + filePath);
            }
            Readable fileReader = new CustomFileReader(path);
            LexicalAnalyzer lexer = new LexicalAnalyzer(fileReader);
            List<Token> tokens = lexer.analyzeFromFile();
            tokens.forEach(System.out::println);
        } catch (IllegalArgumentException | IOException e) {
            System.err.println(e.getMessage());
        }
    }
}

