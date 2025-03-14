package edu.uob.executor;

import edu.uob.exception.MyError;
import edu.uob.storage.DbContext;

public class UseCommand extends DbCmd{
    public UseCommand(String databaseName) {
        this.cmdType = "USE";
        this.dbName = databaseName.toLowerCase();
    }

    @Override
    public String execute(DbContext dbContext) {
        try {
            if (!dbContext.getFileManager().doesDatabaseExist(this.dbName)) {
                throw new MyError("[ERROR] Database does not exist: " + this.dbName);
            }
            dbContext.setCurrentDatabase(this.dbName);
            return "[OK] Now using database: " + this.dbName;
        } catch (Exception e) {
            throw new MyError("[ERROR] Database exception happened!");
        }
    }
}
