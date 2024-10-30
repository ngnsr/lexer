import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
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
                    "|(?<INVALID>\\S+)" // Match any unrecognized token as invalid
    );

    private final Stack<Character> bracketStack = new Stack<>();
    private final Stack<Integer> positionStack = new Stack<>();

    public void analyze(List<String> tokens) {
        for (String token : tokens) {
            Matcher matcher = PATTERN.matcher(token);
            int position = 0;

            if (matcher.matches()) {
                if (matcher.group("HEX") != null) {
                    System.out.println("<" + token + ", HEX_NUMBER>");
                } else if (matcher.group("OCTAL") != null) {
                    System.out.println("<" + token + ", OCTAL_NUMBER>");
                } else if (matcher.group("FLOAT") != null) {
                    System.out.println("<" + token + ", FLOAT_NUMBER>");
                } else if (matcher.group("NUMBER") != null) {
                    System.out.println("<" + token + ", NUMBER>");
                } else if (matcher.group("STRING") != null) {
                    handleString(token);
                } else if (matcher.group("CHAR") != null) {
                    handleChar(token);
                } else if (matcher.group("COMMENT") != null) {
                    System.out.println("<" + token + ", COMMENT>");
                } else if (matcher.group("RESERVED") != null) {
                    System.out.println("<" + token + ", RESERVED_WORD>");
                } else if (matcher.group("OPERATOR") != null) {
                    System.out.println("<" + token + ", OPERATOR>");
                } else if (matcher.group("DELIMITER") != null) {
                    handleDelimiters(token, position);
                } else if (matcher.group("IDENTIFIER") != null) {
                    System.out.println("<" + token + ", IDENTIFIER>");
                } else {
                    System.out.println("Unrecognized symbol '" + token + "'");
                }
            } else {
                System.out.println("Unrecognized symbol '" + token + "'");
            }

        }

        while (!bracketStack.isEmpty()) {
            char unclosed = bracketStack.pop();
            int unclosedPos = positionStack.pop();
            System.out.println("Error at position " + unclosedPos + ": Unmatched opening bracket '" + unclosed + "'");
        }
    }

    private void handleString(String str) {
        if (str.endsWith("\"")) {
            System.out.println("<" + str + ", STRING>");
        } else {
            System.out.println("Error: Unclosed string constant");
        }
    }

    private void handleChar(String ch) {
        if (ch.length() == 3 && ch.charAt(0) == '\'' && ch.charAt(2) == '\'') {
            System.out.println("<" + ch + ", CHAR>");
        } else {
            System.out.println("Error: Invalid char constant");
        }
    }

    private void handleDelimiters(String delimiter, int position) {
        if (delimiter.equals("{") || delimiter.equals("(")) {
            bracketStack.push(delimiter.charAt(0));
            positionStack.push(position);
            System.out.println("<" + delimiter + ", DELIMITER>");
        } else if (delimiter.equals("}") || delimiter.equals(")")) {
            if (!bracketStack.isEmpty()) {
                char openBracket = bracketStack.lastElement();
                if ((openBracket == '{' && delimiter.equals("}")) || (openBracket == '(' && delimiter.equals(")"))) {
                    System.out.println("<" + delimiter + ", DELIMITER>");
                    bracketStack.pop();
                    positionStack.pop();
                } else {
                    System.out.println("Error at position " + position + ": Mismatched closing bracket '" + delimiter + "'");
                }
            } else {
                System.out.println("Error at position " + position + ": Unmatched closing bracket '" + delimiter + "'");
            }
        } else {
            System.out.println("<" + delimiter + ", DELIMITER>");
        }
    }

    private List<String> tokenizeFile(String content) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = Pattern.compile("//.*|/\\\\*.*?\\\\*/|\\d+\\.\\d+[fFdD]?|[(){}\\[\\],;]|[+\\-*/=<>!&|]|\\w+").matcher(content);

        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java LexicalAnalyzer <file_path>");
            return;
        }

        String filePath = args[0];

        String content = Files.readString(Path.of(filePath));

        LexicalAnalyzer lexer = new LexicalAnalyzer();
        List<String> tokens = lexer.tokenizeFile(content);
//        System.out.println(Arrays.toString(tokens.toArray()));

        lexer.analyze(tokens);
    }
}
