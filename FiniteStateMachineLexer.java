import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class FiniteStateMachineLexer {
    private enum State {
        START,
        NUMBER,
        STRING,
        CHAR,
        SINGLE_LINE_COMMENT,
        MULTI_LINE_COMMENT,
        RESERVED_WORD,
        IDENTIFIER,
        OPERATOR,
        SEPARATOR,
        ERROR
    }

    private State currentState;
    private final Stack<Character> bracketStack;
    private final StringBuilder currentToken;
    private final Map<String, String> reservedWords;

    public FiniteStateMachineLexer() {
        this.currentState = State.START;
        this.bracketStack = new Stack<>();
        this.currentToken = new StringBuilder();
        this.reservedWords = new HashMap<>();

        reservedWords.put("int", "RESERVED_WORD");
        reservedWords.put("float", "RESERVED_WORD");
        reservedWords.put("long", "RESERVED_WORD");

        reservedWords.put("class", "RESERVED_WORD");
    }

    public void analyze(String input) {
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            switch (currentState) {
                case START:
                    if (ch == '\n' || ch == ' ') {
                        continue;
                    } else if (Character.isDigit(ch)) {
                        currentToken.append(ch);
                        currentState = State.NUMBER;
                    } else if (ch == '"') {
                        currentState = State.STRING;
                    } else if (ch == '\'') {
                        currentState = State.CHAR;
                    } else if (ch == '/') {
                        if (i + 1 < input.length() && input.charAt(i + 1) == '/') {
                            currentState = State.SINGLE_LINE_COMMENT;
                            i++;
                        } else if (i + 1 < input.length() && input.charAt(i + 1) == '*') {
                            currentState = State.MULTI_LINE_COMMENT;
                            i++;
                        } else {
                            currentState = State.OPERATOR;
                            handleOperator(ch);
                        }
                    } else if (Character.isJavaIdentifierStart(ch)) {
                        currentToken.append(ch);
                        currentState = State.IDENTIFIER;
                    } else if (isOperator(ch)) {
                        handleOperator(ch);
                    } else if (isSeparator(ch)) {
                        handleSeparator(ch);
                    } else {
                        currentState = State.ERROR;
                    }
                    break;

                case NUMBER:
                    if (ch == 'x' || ch == 'X') {
                        currentToken.append(ch);
                        i++;
                        while (i < input.length() && (Character.isDigit(input.charAt(i)) ||
                                "abcdefABCDEF".indexOf(input.charAt(i)) >= 0) || input.charAt(i) == '_') {
                            currentToken.append(input.charAt(i));
                            i++;
                        }
                        System.out.println("< " + currentToken + ", HEX_NUMBER >");
                        currentToken.setLength(0);
                        currentState = State.START;
                        i--; // Reprocess the current character
                        break;
                    } else if (currentToken.charAt(0) == '0' && Character.isDigit(ch)) {
                        currentToken.append(ch); // Handle octal number
                        i++;
                        while (i < input.length() && ("01234567".indexOf(input.charAt(i)) >= 0 || input.charAt(i) == '_')) {
                            currentToken.append(input.charAt(i));
                            i++;
                        }
                        System.out.println("< " + currentToken + ", OCTAL_NUMBER >");
                        currentToken.setLength(0);
                        currentState = State.START;
                        i--; // Reprocess the current character
                        break;
                    }

                    if (Character.isDigit(ch) || ch == '_' || ch == '.') {
                        currentToken.append(ch);
                        continue;
                    } else if (ch == 'l' || ch == 'L') {
                        currentToken.append(ch);
                        System.out.println("< " + currentToken + ", NUMBER >");
                    } else if (ch == 'f' || ch == 'F') {
                        currentToken.append(ch);
                        System.out.println("< " + currentToken + ", NUMBER >");
                    } else {
                        System.out.println("< " + currentToken + ", NUMBER >");
                        i--; // Reprocess the current character
                    }
                    currentToken.setLength(0);
                    currentState = State.START;
                    break;

                case STRING:
                    currentToken.append(ch);
                    if (ch == '"') {
                        System.out.println("< " + currentToken + ", STRING >");
                        currentToken.setLength(0); // Clear the token
                        currentState = State.START;
                    }
                    break;

                case CHAR:
                    currentToken.append(ch);
                    if (ch == '\'') {
                        System.out.println("< " + currentToken + ", CHAR >");
                        currentToken.setLength(0); // Clear the token
                        currentState = State.START;
                    }
                    break;

                case SINGLE_LINE_COMMENT:
                    StringBuilder sb = new StringBuilder();
                    sb.append("//");
                    while (i < input.length() && ch != '\n') {
                        sb.append(ch);
                        i++;
                        if (i < input.length()) {
                            ch = input.charAt(i);
                        }
                    }
                    System.out.println("< " + sb + "; SINGLE_LINE_COMMENT >");
                    currentState = State.START;
                    break;

                case MULTI_LINE_COMMENT:
                    StringBuilder multiLineSb = new StringBuilder();
                    multiLineSb.append("/*");
                    while (i < input.length() - 1 && !(ch == '*' && input.charAt(i + 1) == '/')) {
                        multiLineSb.append(ch);
                        i++;
                        if (i < input.length()) {
                            ch = input.charAt(i);
                        }
                    }
                    if (i + 1 < input.length()) {
                        multiLineSb.append("*/");
                        i++; // Skip the closing */
                    }
                    System.out.println("< " + multiLineSb + "; MULTI_LINE_COMMENT >");
                    currentState = State.START;
                    break;

                case IDENTIFIER:
                    currentToken.append(ch);
                    if (!Character.isJavaIdentifierPart(ch)) {
                        String identifier = currentToken.toString();
                        if (reservedWords.containsKey(identifier)) {
                            System.out.println("< " + identifier + ", " + reservedWords.get(identifier) + " >");
                        } else {
                            System.out.println("< " + identifier + ", IDENTIFIER >");
                        }
                        currentToken.setLength(0); // Clear the token
                        currentState = State.START; // Reset to START
                        i--; // Reprocess the current character
                    }
                    break;

                case OPERATOR:
                    currentState = State.START; // Reset to START after processing
                    break;

                case ERROR:
                    System.out.println("Error: Unrecognized character " + ch);
                    currentState = State.START; // Reset to START after error
                    break;
            }
        }

        // Handle remaining token at the end of input
        if (currentState == State.IDENTIFIER) {
            String identifier = currentToken.toString();
            if (reservedWords.containsKey(identifier)) {
                System.out.println("< " + identifier + ", " + reservedWords.get(identifier) + " >");
            } else {
                System.out.println("< " + identifier + ", IDENTIFIER >");
            }
        } else if (currentState == State.NUMBER) {
            System.out.println("< " + currentToken + ", NUMBER >");
        }

        // Final check for unmatched brackets
        if (!bracketStack.isEmpty()) {
            System.out.println("Error: Unmatched opening bracket(s)");
        }
    }

    private void handleOperator(char ch) {
        System.out.println("< " + ch + ", OPERATOR >");
    }

    private void handleSeparator(char ch) {
        if (ch == ' ') return;
        if (ch == '(' || ch == '{' || ch == '[') {
            bracketStack.push(ch);
        } else if (ch == ')' || ch == '}' || ch == ']') {
            if (bracketStack.isEmpty()) {
                System.out.println("Error: Extra closing bracket " + ch);
            } else {
                char openBracket = bracketStack.pop();
                if (!isMatchingPair(openBracket, ch)) {
                    System.out.println("Error: Mismatched brackets " + openBracket + " and " + ch);
                }
            }
        }
        System.out.println("< " + ch + ", SEPARATOR >");
    }

    private boolean isOperator(char ch) {
        return "+-*/=<>!".indexOf(ch) >= 0;
    }

    private boolean isSeparator(char ch) {
        return "(){}[];, ".indexOf(ch) >= 0;
    }

    private boolean isMatchingPair(char open, char close) {
        return (open == '(' && close == ')') ||
                (open == '{' && close == '}') ||
                (open == '[' && close == ']');
    }

    public static void main(String[] args) {
        FiniteStateMachineLexer lexer = new FiniteStateMachineLexer();
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
        lexer.analyze(code);
    }
}
