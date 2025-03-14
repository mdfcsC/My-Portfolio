package edu.uob.executor;

import edu.uob.exception.MyError;
import edu.uob.model.Row;
import edu.uob.model.Table;
import edu.uob.storage.DbContext;

import java.util.ArrayList;

public class InsertCommand extends DbCmd{
    private ArrayList<String> values;
    private String tableName;

    public InsertCommand(ArrayList<String> values, String tableName) {
        this.values = values;
        this.tableName = tableName;
    }

    @Override
    public String execute(DbContext dbContext) {
        this.dbName = dbContext.getCurrentDatabase();
        Table table = dbContext.getFileManager().loadTableFile(this.tableName, this.dbName);
        ArrayList<String> colsNamesWithoutId = table.getColumnsNames();
        colsNamesWithoutId.remove(0);
        if (colsNamesWithoutId.size() != values.size()) {
            throw new MyError("[ERROR] Cannot match the number of attributes of Table " + this.tableName+ "!");
        }
        Row row = new Row();
        for (int i = 0; i < colsNamesWithoutId.size(); i++) {
            row.addValue(colsNamesWithoutId.get(i), values.get(i));
        }
        table.addRow(row);
        if (dbContext.getFileManager().saveTableFile(table, this.dbName)) {
            return "[OK]";
        } else {
            return "[ERROR] Failed to insert into table file " + this.tableName + "!";
        }
    }
}
