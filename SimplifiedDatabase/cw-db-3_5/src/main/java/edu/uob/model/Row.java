package edu.uob.model;

import edu.uob.exception.MyError;

import java.util.HashMap;

public class Row {
    /** attribute's name <-> data<br>
     *  should not include id as key<br>
     *  attribute's name is lower-case
     */
    private HashMap<String, String> rowDataMap;
    private Integer recordId;

    public Row() {
        this.rowDataMap = new HashMap<>();
    }

    public Integer getRecordId() {
        if (recordId == null) {
            throw new MyError("[ERROR] Cannot get record id!");
        }
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public void addValue(String columnName, String value) {
        if (columnName.equalsIgnoreCase("id")) {
            throw new MyError("[ERROR] Cannot add ID column manually!");
        }
        rowDataMap.put(columnName.toLowerCase(), value); // column name searching is not case-sensitive
    }

    public String getValue(String columnName) {
        if (columnName.equalsIgnoreCase("id")) {
            throw new MyError("[ERROR] Cannot find id column by Row.getValue()!");
        }
        if (rowDataMap.containsKey(columnName.toLowerCase())) {
            return rowDataMap.get(columnName.toLowerCase());
        }
        throw new MyError("[ERROR] Cannot find column: " + columnName);
    }

    public void removeValue(String columnName) {
        rowDataMap.remove(columnName.toLowerCase());
    }

    public void setValue(String columnName, String newValue) {
        if (columnName.equalsIgnoreCase("id")) {
            throw new MyError("[ERROR] Cannot add ID column manually!");
        }
        if (rowDataMap.containsKey(columnName.toLowerCase())) {
            rowDataMap.put(columnName.toLowerCase(), newValue);
        } else {
            throw new MyError("[ERROR] Cannot find column " + columnName);
        }
    }

    /** get a copy of attributeName-data Hashmap */
    public HashMap<String, String> getRowDataMap() {
        return new HashMap<>(rowDataMap);
    }

    /** can manipulate this object's field directly */
    public HashMap<String, String> getThisRowDataMap() {
        return this.rowDataMap;
    }

    /** modify attribute's name (HashMap's key)
     *  actually remove old mapping, then add a new one
    */
    public void modifyAttributeName(String oldName, String newName) {
        // error if oldName doesn't exist or newName exists
        if (!rowDataMap.containsKey(oldName.toLowerCase())) {
            throw new MyError("[ERROR] Unable to find the attribute!");
        }
        if (rowDataMap.containsKey(newName.toLowerCase())) {
            throw new MyError("[ERROR] Cannot have same attribute name!");
        }

        String data = rowDataMap.remove(oldName.toLowerCase());
        addValue(newName, data);
    }
}
