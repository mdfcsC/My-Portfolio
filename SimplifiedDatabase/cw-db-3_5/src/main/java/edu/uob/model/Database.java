package edu.uob.model;

import java.util.HashMap;

public class Database {
    private String databaseName;
    private HashMap<String, Table> tablesMap; // table's name <-> Table object

    public Database(String databaseName) {
        this.databaseName = databaseName.toLowerCase();
        this.tablesMap = new HashMap<>();
    }

    public String getDatabaseName() {
        return databaseName;
    }

    /** only works for Database object */
    public void addTable(Table table) {
        this.tablesMap.put(table.getTableName(), table);
    }

    public Table getTableByName(String tableName) {
        return this.tablesMap.get(tableName.toLowerCase());
    }

    /** only works for Database object */
    public void removeTable(String tableName) {
        this.tablesMap.remove(tableName.toLowerCase());
    }

    /** does a table exist in this database, searching by table name */
    public boolean containsTable(String tableName) {
        return this.tablesMap.containsKey(tableName.toLowerCase());
    }
}
