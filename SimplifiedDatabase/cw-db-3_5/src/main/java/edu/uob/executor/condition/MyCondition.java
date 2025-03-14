package edu.uob.executor.condition;

import edu.uob.model.Row;

import java.util.ArrayList;

public interface MyCondition {
    public boolean evaluate(Row row);

//    public ArrayList<String> getAttributename();
}
/*
<condition>
(<condition>)
(<FirstCondition><BoolOperator><SecondCondition>)
(<Condition> AND(<Condition>))
(a>3 AND(<FirstCondition><BoolOperator><SecondCondition>))
(a>3 AND((<Condition>)OR <Condition>))
(a>3 AND((b>4)OR c>5))
 */
/*
base logic is: attributeName + comparator + value
attributeName -> (identifier / int_literal) String
comparator -> TokenType.OPERATOR / "LIKE"(TokenType.KEYWORD)
value -> (literal:) String / Integer / Double / Boolean / NULL

priority issue: round brackets
*/
