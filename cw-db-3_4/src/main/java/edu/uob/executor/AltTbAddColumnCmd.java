package edu.uob.executor;

import edu.uob.model.Table;
import edu.uob.storage.DbContext;

public class AltTbAddColumnCmd extends DbCmd{
    private String columnName;
    private String tableName;

    public AltTbAddColumnCmd(String columnName, String tableName) {
        this.cmdType = "ADD ATTRIBUTE";
        this.columnName = columnName;
        this.tableName = tableName;
    }

    @Override
    public String execute(DbContext dbContext) {
        this.dbName = dbContext.getCurrentDatabase();
        Table table = dbContext.getFileManager().loadTableFile(this.tableName, this.dbName);
        table.addColumn(this.columnName);
        if (dbContext.getFileManager().saveTableFile(table, this.dbName)) {
            return "[OK] Successfully added attribute " + this.columnName + " to table " + this.tableName;
        } else {
            return "[ERROR] Failed to add attribute " + this.columnName + " to table " + this.tableName;
        }
    }
}
