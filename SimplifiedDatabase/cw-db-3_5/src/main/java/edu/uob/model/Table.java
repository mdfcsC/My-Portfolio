package edu.uob.model;

import edu.uob.exception.MyError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class Table {
    private String tableName;
    /** column: columns is an attribute's name<br>
     *  column's name is case-sensitive<br>
     *  only when create/load a table that saved as file, must id be at first column
     */
    private ArrayList<String> columns;
    /** id <-> Row object<br>
     *  id is an auto-generated primary key, do not allow user input<br>
     *  one Row object's id could be different in different table?<br>
     *  in each table, sorted by id's numeric size
     */
    private TreeMap<Integer, Row> idRowMap;

    /** Self-incrementing ID counter, for auto-generating id as primary key */
    private int idIncreaser;

    /** construct Table with id column adding and idIncreaser set to 0  */
    public Table(String tableName) {
        this.tableName = tableName.toLowerCase();
        this.columns = new ArrayList<>();
        this.idRowMap = new TreeMap<>();
        this.idIncreaser = 0; // initialise to 0
    }

    public String getTableName() {
        return tableName;
    }

    public Integer generateId() {
        idIncreaser++;
        return idIncreaser;
    }

    /** load current table's id increaser, for file loading situation
     *  or used for reset id increaser to 0
    */
    public void setIdIncreaser(int id) {
        this.idIncreaser = id;
    }

    /** add a record<br>
     *  map its auto-generated id and Row
     */
    public void addRow(Row row) {
        Integer recordId = this.generateId();
        row.setRecordId(recordId);
        this.idRowMap.put(recordId, row);
    }

    /** add an attribute */
    public void addColumn(String column) {
        // ensure no duplicate column names
        if (getColumnIndex(column) != -1) {
            throw new MyError("[ERROR] Column '" + column + "' already exists!");
        }

        this.columns.add(column);

        // add default value for current existing rows
        for (Row row : this.idRowMap.values()) {
            row.addValue(column, "NULL");
        }
    }

    /** delete an attribute */
    public void removeColumn(String column) {
        int index = getColumnIndex(column);
        switch(index) {
            case -1 :
                throw new MyError("[ERROR] Column '" + column + "' does not exist!");
            case 0 :
                throw new MyError("[ERROR] Cannot remove ID column!");
            default :
                this.columns.remove(column);
                for (Row row : this.idRowMap.values()) {
                    row.removeValue(column);
                }
        }
    }

    /** get attribute's index */
    private int getColumnIndex(String column) {
        for (int i = 0; i < this.columns.size(); i++) {
            // find this column's name
            if (this.columns.get(i).equalsIgnoreCase(column)) {
                return i;
            }
        }
        // cannot find this column's name
        return -1;
    }

    /** fetch copy of all attributes' names including id if there is one */
    public ArrayList<String> getColumnsNames() {
        return new ArrayList<>(this.columns);
    }

    /** set table's headers, overwrite all old names including id if there is one */
    public void setColumnsNames(ArrayList<String> columns) {
        this.columns = columns;
    }

    /** fetch the deep copy of all records of this table */
    public TreeMap<Integer, Row> getIdRowMap() {
        TreeMap<Integer, Row> copyIdRowMap = new TreeMap<>();

        for (Integer id : this.idRowMap.keySet()) {
            Row originalRow = this.idRowMap.get(id);
            Row copyRow = new Row();

            // copy originalRow to new copyRow
            HashMap<String, String> copyRowDataMap = originalRow.getRowDataMap();
            for (String key : copyRowDataMap.keySet()) {
                copyRow.setRecordId(id);
                copyRow.addValue(key, copyRowDataMap.get(key));
            }

            // map: copy of original id <-> new copyRow
            copyIdRowMap.put(id, copyRow);
        }
        return copyIdRowMap;
    }

    /** can manipulate this object's field directly */
    public TreeMap<Integer, Row> getThisIdRowMap() {
        return this.idRowMap;
    }
}
