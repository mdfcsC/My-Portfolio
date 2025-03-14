package edu.uob.executor;

import edu.uob.executor.condition.MyCondition;
import edu.uob.model.Row;
import edu.uob.model.Table;
import edu.uob.storage.DbContext;

import java.util.ArrayList;

public class UpdateCommand extends DbCmd{
    private String tableName;
    private ArrayList<String> attributesNames;
    private ArrayList<String> updateValues;
    private MyCondition condition;

    public UpdateCommand(String tableName, ArrayList<String> attributesNames, ArrayList<String> updateValues, MyCondition condition) {
        this.tableName = tableName;
        this.attributesNames = attributesNames;
        this.updateValues = updateValues;
        this.condition = condition;
    }

    @Override
    public String execute(DbContext dbContext) {
        this.dbName = dbContext.getCurrentDatabase();
        Table table = dbContext.getFileManager().loadTableFile(this.tableName, this.dbName);
        for (Row row : table.getThisIdRowMap().values()) {
            if (condition.evaluate(row)) {
                for (int i = 0; i < attributesNames.size(); i++) {
                    row.setValue(attributesNames.get(i), updateValues.get(i));
                }
            }
        }
        if (dbContext.getFileManager().saveTableFile(table, this.dbName)) {
            return "[OK]";
        } else {
            return "[ERROR] Failed to update table file!";
        }
    }
}
