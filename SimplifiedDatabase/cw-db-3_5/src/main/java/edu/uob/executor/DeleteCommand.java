package edu.uob.executor;

import edu.uob.executor.condition.MyCondition;
import edu.uob.model.Row;
import edu.uob.model.Table;
import edu.uob.storage.DbContext;

import java.util.TreeMap;

public class DeleteCommand extends DbCmd{
    private String tableName;
    private MyCondition condition;

    public DeleteCommand(String tableName, MyCondition condition) {
        this.tableName = tableName;
        this.condition = condition;
    }

    @Override
    public String execute(DbContext dbContext) {
        this.dbName = dbContext.getCurrentDatabase();
        Table table = dbContext.getFileManager().loadTableFile(this.tableName, this.dbName);

        // create a copy treemap, so deletion in the following loop is fine
        TreeMap<Integer, Row> cpIdRowMap = table.getIdRowMap();

        for (Integer rowId : cpIdRowMap.keySet()) {
            if (condition.evaluate(cpIdRowMap.get(rowId))) {
                table.getThisIdRowMap().remove(rowId);
            }
        }

        if (dbContext.getFileManager().saveTableFile(table, this.dbName)) {
            return "[OK]";
        } else {
            return "[ERROR] Failed to save table file after record deletion!";
        }
    }
}
