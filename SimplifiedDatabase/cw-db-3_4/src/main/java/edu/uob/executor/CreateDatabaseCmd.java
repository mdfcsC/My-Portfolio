package edu.uob.executor;

import edu.uob.storage.DbContext;

public class CreateDatabaseCmd extends DbCmd{
    public CreateDatabaseCmd(String databaseName) {
        this.cmdType = "CREATE DATABASE";
        this.dbName = databaseName;
    }

    @Override
    public String execute(DbContext dbContext) {
        return dbContext.getFileManager().crtDbDirectory(this.dbName);
    }
}
