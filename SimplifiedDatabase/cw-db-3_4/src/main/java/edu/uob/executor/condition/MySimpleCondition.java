package edu.uob.executor.condition;

import edu.uob.exception.MyError;
import edu.uob.model.Row;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MySimpleCondition implements MyCondition {
    private String attributeName;
    private String comparator;
    private String inputValue;
    private Pattern inputPattern; // only used for LIKE
    private String inputType;

    public MySimpleCondition(String attributeName, String comparator, String inputValue, String inputType) {
        this.attributeName = attributeName;
        this.comparator = comparator;
        this.inputValue = inputValue;
        this.inputType = inputType;
    }

    /** only used for LIKE */
    public MySimpleCondition(String attributeName, String comparator, Pattern inputPattern, String inputType) {
        this.attributeName = attributeName;
        this.comparator = comparator;
        this.inputPattern = inputPattern;
        this.inputType = inputType;
    }

//    @Override
//    public ArrayList<String> getAttributename() {
//        ArrayList<String> attributesNames = new ArrayList<>();
//        attributesNames.add(this.attributeName);
//        return attributesNames;
//    }

    @Override
    public boolean evaluate(Row row) {
        if (row == null) {
            return false;
        }

        String rowData;
        // this variable stores the value of an attribute in a row(record)
        if (attributeName.equalsIgnoreCase("id")) {
            rowData = row.getRecordId().toString();
        } else {
            rowData = row.getValue(this.attributeName);
        }

        switch (inputType) {
            case "NULL":
                return compareNull(rowData, comparator, inputValue);
            case "BOOLEAN":
                return compareBoolean(rowData, comparator, inputValue);
//            case "INTEGER":
//                return compareInteger(rowData, comparator, inputValue);
//            case "FLOAT":
            case "INTEGER", "FLOAT":
                return compareFloat(rowData, comparator, inputValue);
            case "STRING":
                if (comparator.equals("LIKE")) {
                    return compareLikeString(rowData, comparator, inputPattern);
                } else {
                    return compareString(rowData, comparator, inputValue);
                }
            default:
                throw new MyError("[ERROR] Unsupported input type: " + inputType);
        }
    }

    private boolean compareNull(String rowData, String comparator, String inputValue) {
        boolean literalEqual = rowData.equalsIgnoreCase("NULL") && inputValue.equals("NULL");
        if (comparator.equals("==")) {
            return literalEqual;
        } else if (comparator.equals("!=")) {
            return !literalEqual;
        } else {
            throw new MyError("[ERROR] Unsupported comparator: " + comparator + " for NULL value!");
        }
    }

    private boolean compareBoolean(String rowData, String comparator, String inputValue) {
        boolean literalEqual = rowData.equalsIgnoreCase(inputValue);
        if (comparator.equals("==")) {
            return literalEqual;
        } else if (comparator.equals("!=")) {
            return !literalEqual;
        } else {
            throw new MyError("[ERROR] Unsupported comparator: " + comparator + " for BOOLEAN value!");
        }
    }

//    private boolean compareInteger(String rowData, String comparator, String inputValue) {
//        return switch (comparator) {
//            case "==" -> Integer.parseInt(rowData) == Integer.parseInt(inputValue);
//            case "!=" -> Integer.parseInt(rowData) != Integer.parseInt(inputValue);
//            case ">" -> Integer.parseInt(rowData) > Integer.parseInt(inputValue);
//            case "<" -> Integer.parseInt(rowData) < Integer.parseInt(inputValue);
//            case ">=" -> Integer.parseInt(rowData) >= Integer.parseInt(inputValue);
//            case "<=" -> Integer.parseInt(rowData) <= Integer.parseInt(inputValue);
//            default -> throw new MyError("[ERROR] Unsupported comparator: " + comparator + " for INTEGER value!");
//        };
//    }

    private boolean compareFloat(String rowData, String comparator, String inputValue) {
        return switch (comparator) {
            case "==" -> Float.parseFloat(rowData) == Float.parseFloat(inputValue);
            case "!=" -> Float.parseFloat(rowData) != Float.parseFloat(inputValue);
            case ">" -> Float.parseFloat(rowData) > Float.parseFloat(inputValue);
            case "<" -> Float.parseFloat(rowData) < Float.parseFloat(inputValue);
            case ">=" -> Float.parseFloat(rowData) >= Float.parseFloat(inputValue);
            case "<=" -> Float.parseFloat(rowData) <= Float.parseFloat(inputValue);
            default -> throw new MyError("[ERROR] Unsupported comparator: " + comparator + " for FLOAT value!");
        };
    }

    private boolean compareLikeString(String rowData, String comparator, Pattern inputPattern) {
        Matcher matcher = inputPattern.matcher(rowData);
        return matcher.find();
    }

    private boolean compareString(String rowData, String comparator, String inputValue) {
        boolean literalEqual = rowData.equals(inputValue);
        if (comparator.equals("==")) {
            return literalEqual;
        } else if (comparator.equals("!=")) {
            return !literalEqual;
        } else {
            throw new MyError("[ERROR] Unsupported comparator: " + comparator + " for STRING value!");
        }
    }
}
