package edu.uob.executor.condition;

import edu.uob.exception.MyError;
import edu.uob.model.Row;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MyCompoundCondition implements MyCondition {
    private MyCondition leftCondition;
    private String boolOperator;
    private MyCondition rightCondition;

    public MyCompoundCondition(MyCondition leftCondition, String boolOperator, MyCondition rightCondition) {
        this.leftCondition = leftCondition;
        this.boolOperator = boolOperator;
        this.rightCondition = rightCondition;
    }

//    @Override
//    public ArrayList<String> getAttributename() {
//        ArrayList<String> attributesNames = new ArrayList<>();
//        attributesNames.addAll(leftCondition.getAttributename());
//        attributesNames.addAll(rightCondition.getAttributename());
//        return attributesNames;
//    }

    @Override
    public boolean evaluate(Row row) {
        if (this.boolOperator.equalsIgnoreCase("AND")) {
            return leftCondition.evaluate(row) && rightCondition.evaluate(row);
        } else if (this.boolOperator.equalsIgnoreCase("OR")) {
            return leftCondition.evaluate(row) || rightCondition.evaluate(row);
        } else {
            throw new MyError("[ERROR] Unsupported bool operator: " + this.boolOperator);
        }
    }
}
