package edu.uob.executor;

import edu.uob.model.Table;
import edu.uob.storage.DbContext;

public class AltTbDropColumnCmd extends DbCmd{
    private String columnName;
    private String tableName;

    public AltTbDropColumnCmd(String columnName, String tableName) {
        this.cmdType = "DROP ATTRIBUTE";
        this.columnName = columnName;
        this.tableName = tableName;
    }

    @Override
    public String execute(DbContext dbContext) {
        this.dbName = dbContext.getCurrentDatabase();
        Table table = dbContext.getFileManager().loadTableFile(this.tableName, this.dbName);
        table.removeColumn(this.columnName);
        if (dbContext.getFileManager().saveTableFile(table, this.dbName)) {
            return "[OK] Successfully dropped attribute " + this.columnName + " to table " + this.tableName;
        } else {
            return "[ERROR] Failed to drop attribute " + this.columnName + " to table " + this.tableName;
        }
    }
}
