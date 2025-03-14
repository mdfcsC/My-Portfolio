package edu.uob.executor;

import edu.uob.storage.DbContext;

public class DropTableCmd extends DbCmd{
    private String tableName;

    public DropTableCmd(String tableName) {
        this.cmdType = "DROP TABLE";
        this.tableName = tableName.toLowerCase();
    }

    @Override
    public String execute(DbContext dbContext) {
        this.dbName = dbContext.getCurrentDatabase();
        return dbContext.getFileManager().dropTableFile(this.tableName, this.dbName);
    }
}
