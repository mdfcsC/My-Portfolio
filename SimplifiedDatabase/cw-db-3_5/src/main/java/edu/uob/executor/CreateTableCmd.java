package edu.uob.executor;

import edu.uob.model.Table;
import edu.uob.storage.DbContext;

import java.util.ArrayList;

public class CreateTableCmd extends DbCmd{
    private String tbName;
    private ArrayList<String> colsNames;

    public CreateTableCmd(String tableName, ArrayList<String> colsNames) {
        this.cmdType = "CREATE TABLE";
        this.tbName = tableName;
        this.colsNames = new ArrayList<>();
        this.colsNames.add("id"); // should have id as the first elment of ArrayList
        this.colsNames.addAll(colsNames);
    }

    public CreateTableCmd(String tableName) {
        this.cmdType = "CREATE TABLE";
        this.tbName = tableName;
        this.colsNames = new ArrayList<>();
    }

    @Override
    public String execute(DbContext dbContext) {
        if (colsNames.isEmpty()) {
            return dbContext.getFileManager().createTableFile(this.tbName, dbContext.getCurrentDatabase());
        } else {
            this.dbName = dbContext.getCurrentDatabase();
            Table table = new Table(this.tbName);
            table.setColumnsNames(this.colsNames);

            // check if this table name file exists
            if (dbContext.getFileManager().doesTableExist(this.tbName, this.dbName)) {
                return "[ERROR] Table " + this.tbName + " already exists!";
            }

            if (dbContext.getFileManager().saveTableFile(table, this.dbName)) {
                return "[OK] Successfully created table: " + this.tbName + "!";
            } else {
                return "[ERROR] Failed to create table: " + this.tbName + "!";
            }
        }
    }
}
