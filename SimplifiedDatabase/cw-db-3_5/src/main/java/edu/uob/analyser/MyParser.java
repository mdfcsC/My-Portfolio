package edu.uob.analyser;

import edu.uob.exception.MyError;
import edu.uob.executor.*;
import edu.uob.executor.condition.MyCompoundCondition;
import edu.uob.executor.condition.MyCondition;
import edu.uob.executor.condition.MySimpleCondition;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

public class MyParser {
    private Tokeniser tokeniser;
    private MyToken currentToken;

    public MyParser(String input) {
        this.tokeniser = new Tokeniser(input);
        this.currentToken = this.tokeniser.nextToken();
    }

    public DbCmd parseInput() {
        // determine command type according to first token of input
        if (currentToken.getType() == TokenType.COMMAND) {
            String command = currentToken.getValue();
            switch (command) {
                case "USE":
                    return parseUseCommand();
                case "CREATE":
                    return parseCreateCommand();
                case "DROP":
                    return parseDropCommand();
                case "ALTER":
                    return parseAlterCommand();
                case "INSERT":
                    return parseInsertCommand();
                case "SELECT":
                    return parseSelectCommand();
                case "UPDATE":
                    return parseUpdateCommand();
                case "DELETE":
                    return parseDeleteCommand();
                case "JOIN":
                    return parseJoinCommand();
                default:
                    throw new MyError("[ERROR] Parsing invalid command!");
            }
        }
        throw new MyError("[ERROR] First parsed token is not a command!");
    }

    /** false if get to EOF or semicolon */
    private boolean getNextToken() {
        currentToken = tokeniser.nextToken();
        // indicate if successfully read a token or read the end
        if (currentToken.getType() == TokenType.EOF || currentToken.getValue().equals(";")) {
            return false;
        }
        return true;
    }

    private DbCmd parseUseCommand() {
        getNextToken();
        String databaseName = handleName();
        if (databaseName == null) {
            throw new MyError("[ERROR] Invalid name! Unable to USE this database!");
        }

        // check query ends with semicolon
        if (!currentToken.getValue().equals(";")) {
            throw new MyError("[ERROR] Query must ends with semicolon!");
        }

        return new UseCommand(databaseName.toLowerCase());
    }

    private DbCmd parseCreateCommand() {
        getNextToken();
        return switch (currentToken.getValue()) {
            case "DATABASE" -> parseCrtDatabaseCmd();
            case "TABLE" -> parseCrtTableCmd();
            default -> throw new MyError("[ERROR] Only DATABASE or TABLE can be created!");
        };
    }

    private DbCmd parseCrtDatabaseCmd() {
        getNextToken();
        String databaseName = handleName();
        if (databaseName == null) {
            throw new MyError("[ERROR] Invalid name! Unable to create this database!");
        }

        // check query ends with semicolon
        if (!currentToken.getValue().equals(";")) {
            throw new MyError("[ERROR] Query must ends with semicolon!");
        }

        return new CreateDatabaseCmd(databaseName.toLowerCase());
    }

    private DbCmd parseCrtTableCmd() {
        getNextToken();
        String tableName = handleName();
        if (tableName == null) {
            throw new MyError("[ERROR] Invalid name! Unable to CREATE this table!");
        }

        // case that there are attributes
        if (currentToken.getValue().equals("(")) {
            ArrayList<String> attributesNames = new ArrayList<>();
            getNextToken();
            while (!currentToken.getValue().equals(")")) {
                if (currentToken.getType() == TokenType.IDENTIFIER || currentToken.getType() == TokenType.INT_LITERAL) {
                    if (currentToken.getValue().equalsIgnoreCase("id")) {
                        throw new MyError("[ERROR] Manual id addition is not allowed!");
                    }
                    attributesNames.add(handleName());
                } else if (currentToken.getValue().equals(",")) {
                    getNextToken();
                } else {
                    throw new MyError("[ERROR] Invalid attribute name when creating table!");
                }
            }
            if (attributesNames.isEmpty()) {
                throw new MyError("[ERROR] No attribute in parentheses when creating table!");
            }

            // check query ends with semicolon
            getNextToken();
            if (!currentToken.getValue().equals(";")) {
                throw new MyError("[ERROR] Query must ends with semicolon!");
            }

            return new CreateTableCmd(tableName.toLowerCase(), attributesNames);
        }
        // case that there is no attribute
//        else if (currentToken.getType() == TokenType.EOF || currentToken.getValue().equals(";")) {
        else if (currentToken.getValue().equals(";")) {
            return new CreateTableCmd(tableName.toLowerCase());
        }
        throw new MyError("[ERROR] Invalid CREATE TABLE command!");
    }

    private DbCmd parseDropCommand() {
        getNextToken();
        String dropName;
        switch (currentToken.getValue()) {
            case "DATABASE":
                getNextToken();
                dropName = handleName();
                if (dropName == null) {
                    throw new MyError("[ERROR] Invalid name! Unable to DROP this database!");
                }

                // check query ends with semicolon
                if (!currentToken.getValue().equals(";")) {
                    throw new MyError("[ERROR] Query must ends with semicolon!");
                }

                return new DropDatabaseCmd(dropName.toLowerCase());
            case "TABLE":
                getNextToken();
                dropName = handleName();
                if (dropName == null) {
                    throw new MyError("[ERROR] Invalid name! Unable to DROP this table!");
                }

                // check query ends with semicolon
                if (!currentToken.getValue().equals(";")) {
                    throw new MyError("[ERROR] Query must ends with semicolon!");
                }

                return new DropTableCmd(dropName.toLowerCase());
            default:
                throw new MyError("[ERROR] DROP a database or table!");
        }
    }

    /* <Alter>  ::=  "ALTER " "TABLE " [TableName] " " <AlterationType> " " [AttributeName]
       <AlterationType>  ::=  "ADD" | "DROP"
    */
    private DbCmd parseAlterCommand() {
        // the second token of the input should be keyword TABLE
        getNextToken();
        if (!currentToken.getValue().equals("TABLE")) {
            throw new MyError("[ERROR] Only can ALTER a TABLE!");
        }

        // the third token of the input should be the table name
        getNextToken();
        String tableName = handleName();
        if (tableName == null) {
            throw new MyError("[ERROR] Invalid name! Unable to ALTER this table!");
        }

        // ADD or DROP
        String alterationType = currentToken.getValue();

        // [AttributeName]
        getNextToken();
        String attributeName = handleName();
        // not allowed to add or drop id column manually
        if (attributeName == null || attributeName.equalsIgnoreCase("id")) {
            throw new MyError("[ERROR] Invalid attribute name!");
        }

        // check query ends with semicolon
        if (!currentToken.getValue().equals(";")) {
            throw new MyError("[ERROR] Query must ends with semicolon!");
        }


        return switch (alterationType) {
            case "ADD" -> new AltTbAddColumnCmd(attributeName, tableName.toLowerCase());
            case "DROP" -> new AltTbDropColumnCmd(attributeName, tableName.toLowerCase());
            default -> throw new MyError("[ERROR] Invalid ALTER command!");
        };
    }

    /* <Insert>  ::=  "INSERT " "INTO " [TableName] " VALUES" "(" <ValueList> ")"
       <ValueList>  ::=  [Value] | [Value] "," <ValueList>
       [Value]  ::=  "'" [StringLiteral] "'" | [BooleanLiteral] | [FloatLiteral] | [IntegerLiteral] | "NULL"
    */
    private DbCmd parseInsertCommand() {
        getNextToken();
        if (!currentToken.getValue().equals("INTO")) {
            throw new MyError("[ERROR] Invalid insert command!");
        }

        getNextToken();
        String tableName = handleName();
        if (tableName == null) {
            throw new MyError("[ERROR] Invalid name! Unable to INSERT INTO this table!");
        }

        if (!currentToken.getValue().equals("VALUES")) {
            throw new MyError("[ERROR] Invalid insert command!");
        }

        getNextToken();
        if (!currentToken.getValue().equals("(")) {
            throw new MyError("[ERROR] Invalid insert command!");
        }

        getNextToken();
        ArrayList<String> values = new ArrayList<>();
        while (!currentToken.getValue().equals(")")) {
            if (currentToken.getType() == TokenType.STRING_LITERAL ||
                currentToken.getType() == TokenType.BOOLEAN_LITERAL ||
                currentToken.getType() == TokenType.FLOAT_LITERAL ||
                currentToken.getType() == TokenType.INT_LITERAL ||
                currentToken.getValue().equals("NULL")
            ) {
                values.add(currentToken.getValue());
                getNextToken();
            } else if (currentToken.getValue().equals(",")) {
                getNextToken();
            } else {
                throw new MyError("[ERROR] Invalid INSERT value!");
            }
        }
        if (values.isEmpty()) {
            throw new MyError("[ERROR] Must insert at least one value!");
        }

        // check query ends with semicolon
        getNextToken();
        if (!currentToken.getValue().equals(";")) {
            throw new MyError("[ERROR] Query must ends with semicolon!");
        }

        return new InsertCommand(values, tableName.toLowerCase());
    }

    /* <Select>  ::=  "SELECT " <WildAttribList> " FROM " [TableName] |
                      "SELECT " <WildAttribList> " FROM " [TableName] " WHERE " <Condition>
       <WildAttribList>  ::=  <AttributeList> | "*"
       <AttributeList>  ::=  [AttributeName] | [AttributeName] "," <AttributeList>
       <Condition>  ::=  "(" <Condition> ")" |
                         <FirstCondition> <BoolOperator> <SecondCondition> |
                         [AttributeName] <Comparator> [Value]
       <FirstCondition>  ::=  <Condition> " " | "(" <Condition> ")"
       <SecondCondition> ::=  " " <Condition> | "(" <Condition> ")"
       <BoolOperator>    ::= "AND" | "OR"
       <Comparator>      ::=  "==" | ">" | "<" | ">=" | "<=" | "!=" | " LIKE "
    */
    private DbCmd parseSelectCommand() {
        // <WildAttribList>
        getNextToken();
        ArrayList<String> chosenAttribs = new ArrayList<>();
            // <AttributeList>
        if (currentToken.getType() == TokenType.IDENTIFIER || currentToken.getType() == TokenType.INT_LITERAL) {
            while (!currentToken.getValue().equals("FROM")) {
                if (currentToken.getType() == TokenType.IDENTIFIER || currentToken.getType() == TokenType.INT_LITERAL) {
                    String attribName = handleName();
                    if (attribName == null) {
                        throw new MyError("[ERROR] Invalid attribute name!");
                    }
                    chosenAttribs.add(attribName);
                } else if (currentToken.getValue().equals(",")) {
                    getNextToken();
                } else {
                    throw new MyError("[ERROR] Invalid select command!");
                }
            }
        }
            // "*"
        else if (currentToken.getType() == TokenType.WILDCARD) {
            chosenAttribs.add(currentToken.getValue());
            getNextToken();
        }
        else {
            throw new MyError("[ERROR] Invalid select command!");
        }
            // check <WildAttribList> is not null
        if (chosenAttribs.isEmpty()) {
            throw new MyError("[ERROR] Must select at least one attribute!");
        }

        // "FROM"
        if (!currentToken.getValue().equals("FROM")) {
            throw new MyError("[ERROR] Invalid select command!");
        }
        getNextToken();

        // [TableName]
        String tableName = handleName();
        if (tableName == null) {
            throw new MyError("[ERROR] Invalid name! Unable to SELECT FROM this table!");
        }

//        if (currentToken.getType() == TokenType.EOF || currentToken.getValue().equals(";")) {
        if (currentToken.getValue().equals(";")) {
            return new SelectCommand(chosenAttribs, tableName.toLowerCase(), null);
        }
        // option: " WHERE " <Condition>
        else if (currentToken.getValue().equals("WHERE")) {
            getNextToken(); // so conditionTokens won't contain WHERE
            ArrayList<MyToken> conditionTokens = new ArrayList<>();
            while (currentToken.getType() != TokenType.EOF && !currentToken.getValue().equals(";")) {
                conditionTokens.add(currentToken);
                getNextToken();
            }
            if (conditionTokens.isEmpty()) {
                throw new MyError("[ERROR] Invalid select command! Input at least one condition after WHERE!");
            }

            // check query ends with semicolon
            if (!currentToken.getValue().equals(";")) {
                throw new MyError("[ERROR] Query must ends with semicolon!");
            }

            MyCondition sltCondition = parseCondition(conditionTokens, 0);
            return new SelectCommand(chosenAttribs, tableName.toLowerCase(), sltCondition);
        }
        else {
            throw new MyError("[ERROR] Invalid select command!");
        }
    }

    /* parse the whole condition statements

       <Condition>       ::=  "(" <Condition> ")" |
                              <FirstCondition> <BoolOperator> <SecondCondition> |
                              [AttributeName] <Comparator> [Value]
       <FirstCondition>  ::=  <Condition> " " | "(" <Condition> ")"
       <SecondCondition> ::=  " " <Condition> | "(" <Condition> ")"
       <BoolOperator>    ::= "AND" | "OR"
       <Comparator>      ::=  "==" | ">" | "<" | ">=" | "<=" | "!=" | " LIKE "
    */
    private MyCondition parseCondition(ArrayList<MyToken> conditionTokens, int startIndex) {
        if (startIndex >= conditionTokens.size() || startIndex < 0) {
            throw new MyError("[ERROR] Failed to parse condition! Index is out of bounds!");
        }

        // just to double-check
        if (conditionTokens.isEmpty()) {
            throw new MyError("[ERROR] Incomplete condition!");
        }

        // situation 1: handle round brackets and priority issues
        if (Objects.equals(conditionTokens.get(startIndex).getValue(), "(")) {
            if (startIndex + 1 >= conditionTokens.size()) {
                throw new MyError("[ERROR] Incomplete condition! Must have something after a round bracket!");
            }
            int endIndex = findPairParenthesis(conditionTokens, startIndex + 1);
            if (endIndex == -1) {
                throw new MyError("[ERROR] Unbalanced parentheses!");
            }

            // recursively call parseCondition() until no ( at the start of this sub condition
            MyCondition innerCondition = parseCondition(conditionTokens, startIndex + 1);

            // check if ")" followed by AND / OR
            if (endIndex + 1 < conditionTokens.size() &&
                isBoolOperator(conditionTokens.get(endIndex + 1)) &&
                endIndex + 2 < conditionTokens.size()
            ) {
                String boolOperator = conditionTokens.get(endIndex + 1).getValue();
                // parse a sub condition on the right of AND / OR
                MyCondition rightCondition = parseCondition(conditionTokens, endIndex + 2);
                return new MyCompoundCondition(innerCondition, boolOperator, rightCondition);
            } else {
                return innerCondition;
            }
        }

        // situation 2: handle simple basic condition like x<3 without round brackets
        if (conditionTokens.get(startIndex).getType() == TokenType.IDENTIFIER ||
            conditionTokens.get(startIndex).getType() == TokenType.INT_LITERAL
        ) {
            /** check type of this index token is IDENTIFIER or INT_LITERAL<br>
             *  if so, return a string, could be uppercase or lowercase; or null<br>
             *  keep checking when next is IDENTIFIER or INT_LITERAL
             */
            StringBuilder nameBuilder = new StringBuilder();
            if (conditionTokens.get(startIndex).getType() == TokenType.IDENTIFIER) {
                nameBuilder.append(conditionTokens.get(startIndex).getValue());
                startIndex++;
            }
            else if (conditionTokens.get(startIndex).getType() == TokenType.INT_LITERAL) {
                while (startIndex < conditionTokens.size() &&
                      (conditionTokens.get(startIndex).getType() == TokenType.IDENTIFIER || conditionTokens.get(startIndex).getType() == TokenType.INT_LITERAL)
                ) {
                    nameBuilder.append(conditionTokens.get(startIndex).getValue());
                    startIndex++;
                }
            }
            else {
                throw new MyError("[ERROR] Invalid attribute name in condition!");
            }
            String attributeName = nameBuilder.toString();

            int comparatorIndex = startIndex;
            int valueIndex = startIndex + 1;

            if (comparatorIndex >= conditionTokens.size() || valueIndex >= conditionTokens.size()) {
                throw new MyError("[ERROR] Missing comparison operator or value!");
            }

            MyToken comparatorToken = conditionTokens.get(comparatorIndex);
            MyToken valueToken = conditionTokens.get(valueIndex);

            if (!isComparator(comparatorToken)) {
                throw new MyError("[ERROR] Invalid comparison operator!");
            }

            // base case
            String iptCmpType = switch (valueToken.getType()) {
                case NULL_LITERAL -> "NULL";
                case BOOLEAN_LITERAL -> "BOOLEAN";
                case INT_LITERAL -> "INTEGER";
                case FLOAT_LITERAL -> "FLOAT";
                case STRING_LITERAL -> "STRING";
                default -> throw new MyError("[ERROR] Invalid comparison value type!");
            };
            MyCondition leftCondition;
            // handle it if condition's value is regex
            if (comparatorToken.getValue().equals("LIKE")) {
                Pattern pattern = Pattern.compile(valueToken.getValue());
                leftCondition = new MySimpleCondition(attributeName, "LIKE", pattern, iptCmpType);
            }
            // no regex condition value
            else {
                leftCondition = new MySimpleCondition(attributeName, comparatorToken.getValue(), valueToken.getValue(), iptCmpType);
            }

            // check if this simple basic condition followed by AND / OR
            if (valueIndex + 1 < conditionTokens.size() &&
                isBoolOperator(conditionTokens.get(valueIndex + 1)) &&
                valueIndex + 2 < conditionTokens.size()
            ) {
                String boolOperator = conditionTokens.get(valueIndex + 1).getValue();
                MyCondition rightCondition = parseCondition(conditionTokens, valueIndex + 2);
                return new MyCompoundCondition(leftCondition, boolOperator, rightCondition);
            }
            // return base case
            else {
                return leftCondition;
            }
        }
        // situation 3: no round bracket or attribute name at the start of condition, error!
        else {
            throw new MyError("[ERROR] Invalid condition command!");
        }
    }

    private int findPairParenthesis(ArrayList<MyToken> conditionTokens, int checkIndex) {
        if (checkIndex >= conditionTokens.size() || checkIndex < 0) {
            throw new MyError("[ERROR] Failed to find parentheses! Index is out of bounds!");
        }

        while (checkIndex < conditionTokens.size()) {
            if (conditionTokens.get(checkIndex).getValue().equals(")")) {
                return checkIndex;
            }
            checkIndex++;
        }
        return -1;
    }

    private boolean isBoolOperator(MyToken checkToken) {
        if (checkToken.getType() == TokenType.KEYWORD) {
            return checkToken.getValue().equals("AND") || checkToken.getValue().equals("OR");
        }
        return false;
    }

    private boolean isComparator(MyToken checkToken) {
        String str = checkToken.getValue();
        return (str.equals("==") || str.equals("!=") ||
                str.equals(">") || str.equals("<") ||
                str.equals(">=") || str.equals("<=") ||
                str.equals("LIKE")
        );
    }

    /* <Update>  ::=  "UPDATE " [TableName] " SET " <NameValueList> " WHERE " <Condition>
       <NameValueList>   ::=  <NameValuePair> | <NameValuePair> "," <NameValueList>
       <NameValuePair>   ::=  [AttributeName] "=" [Value]
    */
    private DbCmd parseUpdateCommand() {
        // [TableName]
        getNextToken();
        String tableName = handleName();
        if (tableName == null) {
            throw new MyError("[ERROR] Table name not found!");
        }

        // SET
        if (!currentToken.getValue().equals("SET")) {
            throw new MyError("[ERROR] Invalid update command!");
        }

        // <NameValueList>
        getNextToken();

        ArrayList<String> attributesNames = new ArrayList<>();
        ArrayList<String> updateValues = new ArrayList<>();

        boolean isNameValuePair = true;
        while (isNameValuePair) {
            // <NameValuePair>
                // [AttributeName]
            String attributeName = handleName();
            // not allowed to update id column manually
            if (attributeName == null || attributeName.equalsIgnoreCase("id")) {
                throw new MyError("[ERROR] Invalid attribute name!");
            }
            attributesNames.add(attributeName);

                // =
            if (!currentToken.getValue().equals("=")) {
                throw new MyError("[ERROR] Invalid update command!");
            }

                // [Value]
            getNextToken();
            if (currentToken.getType() == TokenType.STRING_LITERAL ||
                currentToken.getType() == TokenType.BOOLEAN_LITERAL ||
                currentToken.getType() == TokenType.FLOAT_LITERAL ||
                currentToken.getType() == TokenType.INT_LITERAL ||
                currentToken.getValue().equals("NULL")
            ) {
                updateValues.add(currentToken.getValue());
            } else {
                throw new MyError("[ERROR] Invalid update command! Value type couldn't be " + currentToken.getType() + "!");
            }

                // , or WHERE
            getNextToken();
            if (currentToken.getValue().equals(",")) {
                getNextToken();
            }
        // WHERE
            else if (currentToken.getValue().equals("WHERE")) {
                isNameValuePair = false;
            }
            else {
                throw new MyError("[ERROR] Invalid update command! Expected ',' or 'WHERE'");
            }
        }

        // check that <NameValueList> is not empty and that <NameValuePair> matches
        if (attributesNames.isEmpty() || attributesNames.size() != updateValues.size()) {
            throw new MyError("[ERROR] Invalid update command! AttributeName and Value are both required!");
        }

        // <Condition>
        getNextToken();
        ArrayList<MyToken> conditionTokens = new ArrayList<>();
        while (currentToken.getType() != TokenType.EOF && !currentToken.getValue().equals(";")) {
            conditionTokens.add(currentToken);
            getNextToken();
        }
        if (conditionTokens.isEmpty()) {
            throw new MyError("[ERROR] Invalid update command! Input at least one condition after WHERE!");
        }

        // check query ends with semicolon
        if (!currentToken.getValue().equals(";")) {
            throw new MyError("[ERROR] Query must ends with semicolon!");
        }

        MyCondition updtCondition = parseCondition(conditionTokens, 0);
        return new UpdateCommand(tableName.toLowerCase(), attributesNames, updateValues, updtCondition);
    }

    // <Delete>  ::=  "DELETE " "FROM " [TableName] " WHERE " <Condition>
    private DbCmd parseDeleteCommand() {
        getNextToken();
        if (!currentToken.getValue().equals("FROM")) {
            throw new MyError("[ERROR] Invalid delete command!");
        }

        // [TableName]
        getNextToken();
        String tableName = handleName();
        if (tableName == null) {
            throw new MyError("[ERROR] Table name not found!");
        }

        if (!currentToken.getValue().equals("WHERE")) {
            throw new MyError("[ERROR] Invalid delete command!");
        }

        // <Condition>
        if (getNextToken()) {
            ArrayList<MyToken> conditionTokens = new ArrayList<>();
            while (currentToken.getType() != TokenType.EOF && !currentToken.getValue().equals(";")) {
                conditionTokens.add(currentToken);
                getNextToken();
            }
            if (conditionTokens.isEmpty()) {
                throw new MyError("[ERROR] Invalid update command! Input at least one condition after WHERE!");
            }

            // check query ends with semicolon
            if (!currentToken.getValue().equals(";")) {
                throw new MyError("[ERROR] Query must ends with semicolon!");
            }

            MyCondition dltCondition = parseCondition(conditionTokens, 0);
            return new DeleteCommand(tableName, dltCondition);
        } else {
            throw new MyError("[ERROR] Invalid delete command! Must have a condition!");
        }
    }

    // <Join>  ::=  "JOIN " [TableName] " AND " [TableName] " ON " [AttributeName] " AND " [AttributeName]
    private DbCmd parseJoinCommand() {
        // first [TableName]
        getNextToken();
        String tableName1 = handleName();
        if (tableName1 == null) {
            throw new MyError("[ERROR] Table name not found!");
        }

        if (!currentToken.getValue().equals("AND")) {
            throw new MyError("[ERROR] Invalid join command!");
        }

        // second [TableName]
        getNextToken();
        String tableName2 = handleName();
        if (tableName2 == null) {
            throw new MyError("[ERROR] Table name not found!");
        }

        if (!currentToken.getValue().equals("ON")) {
            throw new MyError("[ERROR] Invalid join command!");
        }

        // first [AttributeName]
        getNextToken();
        String attributeName1 = handleName();
        if (attributeName1 == null) {
            throw new MyError("[ERROR] Attribute name not found!");
        }

        if (!currentToken.getValue().equals("AND")) {
            throw new MyError("[ERROR] Invalid join command!");
        }

        // second [AttributeName]
        getNextToken();
        String attributeName2 = handleName();
        if (attributeName2 == null) {
            throw new MyError("[ERROR] Attribute name not found!");
        }

        // check query ends with semicolon
        if (!currentToken.getValue().equals(";")) {
            throw new MyError("[ERROR] Query must ends with semicolon!");
        }
//        // check there is no more tokens in this command
//        if (currentToken.getType() != TokenType.EOF && !currentToken.getValue().equals(";")) {
//            throw new MyError("[ERROR] Invalid join command!");
//        }

        // check if join two same table
        if (tableName1.equalsIgnoreCase(attributeName2)) {
            throw new MyError("[ERROR] Invalid join command!");
        }

        return new JoinCommand(tableName1.toLowerCase(), tableName2.toLowerCase(), attributeName1, attributeName2);
    }

    /**  check type of currentToken is IDENTIFIER or INT_LITERAL<br>
      *  if so, return a string, could be uppercase or lowercase; or null<br>
      *  keep checking when currentToken is IDENTIFIER or INT_LITERAL<br>
      *  after calling this method, currentToken would be an unchecked token (except not IDENTIFIER or INT_LITERAL)
      */
    private String handleName() {
        StringBuilder nameBuilder = new StringBuilder();
        if (currentToken.getType() == TokenType.IDENTIFIER) {
            nameBuilder.append(currentToken.getValue());
            getNextToken();
        } else if (currentToken.getType() == TokenType.INT_LITERAL) {
            while (currentToken.getType() == TokenType.IDENTIFIER || currentToken.getType() == TokenType.INT_LITERAL) {
                nameBuilder.append(currentToken.getValue());
                getNextToken();
            }
        } else {
            return null;
        }
        return nameBuilder.toString();
    }
}
