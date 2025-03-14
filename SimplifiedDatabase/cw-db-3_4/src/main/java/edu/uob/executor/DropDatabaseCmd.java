package edu.uob.executor;

import edu.uob.storage.DbContext;

public class DropDatabaseCmd extends DbCmd{
    public DropDatabaseCmd(String databaseName) {
        this.cmdType = "DROP DATABASE";
        this.dbName = databaseName.toLowerCase();
    }

    @Override
    public String execute(DbContext dbContext) {
        return dbContext.getFileManager().dropDatabaseDirectory(this.dbName);
    }
}
