package edu.uob.executor;

import edu.uob.storage.DbContext;

public abstract class DbCmd {
    public String cmdType;
    public String dbName;

    public abstract String execute(DbContext dbContext);
}
