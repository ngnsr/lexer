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
        "|(?<DELIMITER>[,;(){}])" +
        "|(?<IDENTIFIER>\\b[a-zA-Z_]\\w*\\b)" +
        "|(?<INVALID>[^\\s])"
    );

    private final Stack<Character> bracketStack = new Stack<>();  // Стек для дужок
    private final Stack<Integer> positionStack = new Stack<>();   // Стек для позицій дужок

    public void analyze(String input) {
        Matcher matcher = PATTERN.matcher(input);
        int position;

        while (matcher.find()) {
            position = matcher.start();  // Поточна позиція

            if (matcher.group("HEX") != null) {
                System.out.println("<" + matcher.group("HEX") + ", HEX_NUMBER>");
            } else if (matcher.group("OCTAL") != null) {
                System.out.println("<" + matcher.group("OCTAL") + ", OCTAL_NUMBER>");
            } else if (matcher.group("FLOAT") != null) {
                System.out.println("<" + matcher.group("FLOAT") + ", FLOAT_NUMBER>");
            } else if (matcher.group("NUMBER") != null) {
                System.out.println("<" + matcher.group("NUMBER") + ", NUMBER>");
            } else if (matcher.group("STRING") != null) {
                handleString(matcher.group("STRING"));
            } else if (matcher.group("CHAR") != null) {
                handleChar(matcher.group("CHAR"));
            } else if (matcher.group("COMMENT") != null) {
                System.out.println("<" + matcher.group("COMMENT") + ", COMMENT>");
            } else if (matcher.group("RESERVED") != null) {
                System.out.println("<" + matcher.group("RESERVED") + ", RESERVED_WORD>");
            } else if (matcher.group("OPERATOR") != null) {
                System.out.println("<" + matcher.group("OPERATOR") + ", OPERATOR>");
            } else if (matcher.group("DELIMITER") != null) {
                handleDelimiters(matcher.group("DELIMITER"), position);
            } else if (matcher.group("IDENTIFIER") != null) {
                System.out.println("<" + matcher.group("IDENTIFIER") + ", IDENTIFIER>");
            } else if (matcher.group("INVALID") != null) {
                System.out.println("Error at position " + position + ": Unrecognized symbol '" + matcher.group("INVALID") + "'");
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
                positionStack.lastElement();
                if ((openBracket == '{' && delimiter.equals("}")) || (openBracket == '(' && delimiter.equals(")"))) {
                    System.out.println("<" + delimiter + ", DELIMITER>");
                    bracketStack.pop();
                    positionStack.pop();
                } else {
                    if(bracketStack.size() >= 2 && isMatchingPair(bracketStack.get(bracketStack.size()-2), delimiter.charAt(0))){
                        var a = bracketStack.pop();
                        positionStack.pop();
                        bracketStack.pop();
                        positionStack.pop();
                        System.out.println("Error at position " + position + ": Mismatched opening bracket '" + a + "'");
                        return;
                    }
                    System.out.println("Error at position " + position + ": Mismatched closing bracket '" + delimiter + "'");
                }
            } else {
                System.out.println("Error at position " + position + ": Unmatched closing bracket '" + delimiter + "'");
            }
        } else {
            System.out.println("<" + delimiter + ", DELIMITER>");
        }
    }

    private boolean isMatchingPair(char open, char close) {
        return (open == '(' && close == ')') ||
                (open == '{' && close == '}') ||
                (open == '[' && close == ']');
    }

    public static void main(String[] args) {
        String code = """
            public class Example {
                // This is a comment
                long x = 100_100L;
                float y = 3.14f;
                int z = 0x1A_FF;
                int o = 0123;
                
                int a = (7) * 8 / 2;
            }
            """;

        LexicalAnalyzer analyzer = new LexicalAnalyzer();
        analyzer.analyze(code);
    }
}
