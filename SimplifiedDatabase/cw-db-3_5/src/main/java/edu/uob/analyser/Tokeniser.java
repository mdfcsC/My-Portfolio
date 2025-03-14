package edu.uob.analyser;

import java.util.Arrays;
import java.util.HashSet;

public class Tokeniser {
    private String input; // user's query
    private int position; // like a cursor
    private int length;
    private boolean isFirstToken = true; // mark if it is a first valid token

    /** SQL's 9 commands */
    private static final HashSet<String> COMMANDS = new HashSet<>(Arrays.asList(
        "USE","CREATE","DROP","ALTER","INSERT","SELECT","UPDATE","DELETE","JOIN"
    ));

    /** SQL keywords */
    private static final HashSet<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "FROM", "WHERE", "AND", "OR", "ON", "SET", "INTO", "VALUES", "TABLE", "DATABASE", "ADD", "DROP", "LIKE"
    ));

    /** boolean literal and null literal */
    private static final HashSet<String> BOOL_NULL_LITERALS = new HashSet<>(Arrays.asList(
        "TRUE", "FALSE", "NULL"
    ));

    /** SQL delimiter */
    private static final HashSet<Character> DELIMITERS = new HashSet<>(Arrays.asList(
        ';', ',', '(', ')'
    ));

    /** SQL operator */
    private static final HashSet<Character> OPERATORS = new HashSet<>(Arrays.asList(
        '>', '<', '=', '!', '+'
//            , '-'
    ));

    /** SQL quote */
    private static final HashSet<Character> QUOTES = new HashSet<>(Arrays.asList(
        '\'', '"'
    ));

    protected Tokeniser(String input) {
        this.input = input.trim(); // remove leading and trailing spaces
        this.position = 0;
        this.length = input.trim().length();
    }

    /** get a token */
    protected MyToken nextToken() {
        // mark if reaching the end of input
        if (position >= length) {
            return new MyToken(TokenType.EOF, "");
        }

        // skip the whitespace
        while (position < length && Character.isWhitespace(input.charAt(position))) {
            position++;
        }

        // true if it is the first valid token, false if not
        boolean isStartToken = isFirstToken;
        // reset the first token mark if having the first token
        if (isStartToken) {
            isFirstToken = false;
        }

        char c = input.charAt(position);

        // check type of the first char of a token
        // if it is a letter
        if (isMyLetter(c)) {
            return readSomething(isStartToken);
        }
        // check if it is a digit (including negative number）
        else if (isMyDigit(c) || (c == '-' && position + 1 < length && isMyDigit(input.charAt(position + 1)))) {
            return readNumber();
        }
        else if (QUOTES.contains(c)) {
            return readStringLiteral();
        }
        else if (OPERATORS.contains(c)) {
            return readOperator();
        }
        else if (c == '*') {
            position++;
            return new MyToken(TokenType.WILDCARD, "*");
        }
        // check if it is a , ; ( )
        else if (DELIMITERS.contains(c)) {
            position++;
            return new MyToken(TokenType.DELIMITER, String.valueOf(c));
        }
        else {
            position++;
            return new MyToken(TokenType.OTHER, String.valueOf(c));
        }
    }

    /** read something that could be COMMANDS or KEYWORDS or IDENTIFIERS or BOOL_NULL_LITERALS */
    private MyToken readSomething(boolean isStartToken) {
        int start = position;

        // read letters and digits into token, ignoring whitespace (e.g., `WHERE age>18`)
        while (position < length &&
                (isMyLetter(input.charAt(position)) || isMyDigit(input.charAt(position)))
        ) {
            position++;
        }
        String text = input.substring(start, position);
        // uppercase version of read token
        String upperText = text.toUpperCase();

        if (COMMANDS.contains(upperText) && isStartToken) { // DROP of COMMANDS should be at the query start
            return new MyToken(TokenType.COMMAND, upperText); // normalise the command
        }
        else if (KEYWORDS.contains(upperText) && !isStartToken) {  // DROP of KEYWORDS should not be at the query start
            return new MyToken(TokenType.KEYWORD, upperText); // normalise the keyword
        }
        else if (BOOL_NULL_LITERALS.contains(upperText)) {
            if (upperText.equals("NULL")) {
                return new MyToken(TokenType.NULL_LITERAL, upperText);
            }
            else {
                return new MyToken(TokenType.BOOLEAN_LITERAL, upperText);
            }
        }
        else {
            return new MyToken(TokenType.IDENTIFIER, text);
        }
    }

    // according to this assignment's BNF, name of database/table/attribute could start with digit and be composed entirely of digits
    private MyToken readNumber() {
        int start = position;
        boolean isFloat = false;

        // if it is a negative number
        if (input.charAt(start) == '-') {
            position++;
        }

        while (position < length && (
                isMyDigit(input.charAt(position)) || input.charAt(position) == '.'
        )) {
            if (input.charAt(position) == '.') {
                isFloat = true;
            }
            position++;
        }

        String number = input.substring(start, position);

        if (isFloat) {
            return new MyToken(TokenType.FLOAT_LITERAL, number);
        } else {
            return new MyToken(TokenType.INT_LITERAL, number);
        }
    }
//        int start = position;
//        boolean isFloat = false;
//
//        // if it is a negative number
//        if (input.charAt(position) == '-') {
//            position++;
//        }
//
//        while (position < length && isMyDigit(input.charAt(position))) {
//            position++;
//
//            // if it is a floating number
//            if (input.charAt(position) == '.') {
//                isFloat = true;
//                position++;
//            }
//            while (position < length && isMyDigit(input.charAt(position))) {
//                position++;
//            }
//        }
//        String number = input.substring(start, position);
//
//        if (isFloat) {
//            return new MyToken(TokenType.FLOAT_LITERAL, number);
//        }
//        else {
//            return new MyToken(TokenType.INT_LITERAL, number);
//        }
//    }

    private MyToken readStringLiteral() {
        // record the quote type then skip it
        char q = input.charAt(position);
        position++;

        int start = position;

        while (position < length) {
            char currentChar = input.charAt(position);

            // find the matching quote
            if (currentChar == q) {
                // skip the quotation mark, for better nextToken()
                position++;
                // substring() is like [ , )
                return new MyToken(TokenType.STRING_LITERAL, input.substring(start, position - 1));
            }

            // handle escape character
            if (currentChar == '\\' && position + 1 < length) {
                // skip the backslash and escaped character
                position += 2;
            } else {
                position++;
            }
        }
        return new MyToken(TokenType.INVALID, String.valueOf(q));
    }
//        // record the quote type then skip it
//        char q = input.charAt(position);
//        boolean closedQuote = false;
//        position++;
//
//        int start = position;
//
//        while (position < length && !closedQuote) {
//            // if there is an escape character in the quote
//            if (input.charAt(position) == '\\' && position + 1 < length) {
//                // skip the backslash and the escaped character
//                position += 2;
//                if (position < length && input.charAt(position) == q) {
//                    closedQuote = true;
//                }
//                position++;
//            }
//            else {
//                position++;
//            }
//        }
//
//        // record unclosing quotes
//        if (position >= length && !closedQuote) {
//            return new MyToken(TokenType.INVALID, String.valueOf(q));
//        }
//
//        // substring() is like [ , )
//        return new MyToken(TokenType.STRING_LITERAL, input.substring(start, position));
//    }

    private MyToken readOperator() {
        int start = position;
        char currentChar = input.charAt(position);
        position++;

        if (position < length) {
            char nextChar = input.charAt(position);

            // valid operator combinations: ==, !=, >=, <=
            if ((currentChar == '=' && nextChar == '=') ||
                (currentChar == '!' && nextChar == '=') ||
                (currentChar == '>' && nextChar == '=') ||
                (currentChar == '<' && nextChar == '=')
            ) {
                position++;
            }
        }

        return new MyToken(TokenType.OPERATOR, input.substring(start, position));
    }

    private boolean isMyDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isMyLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }
}
