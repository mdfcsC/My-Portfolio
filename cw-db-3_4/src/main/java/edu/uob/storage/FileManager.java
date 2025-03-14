package edu.uob.storage;

import edu.uob.exception.MyError;
import edu.uob.model.Row;
import edu.uob.model.Table;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

public class FileManager {
    private String storageRootPath; // "*/databases/"

    public FileManager(String storageRootPath) {
        this.storageRootPath = storageRootPath;
    }

    /** check if database directory exists */
    public boolean doesDatabaseExist(String databaseName) {
        if (databaseName == null) {
            return false;
        }
        File dbDirectory = new File(storageRootPath + File.separator + databaseName.toLowerCase().trim());
        return dbDirectory.exists() && dbDirectory.isDirectory();
    }

    /** check if table file exists */
    public boolean doesTableExist(String tableName, String databaseName) {
        if (tableName == null || databaseName == null) {
            return false;
        }
        File tbFile = new File(storageRootPath + File.separator + databaseName.toLowerCase().trim() + File.separator + tableName.toLowerCase().trim() + ".tab");
        return tbFile.exists() && tbFile.isFile();
    }

    /** create a database directory */
    public String crtDbDirectory(String databaseName) {
        File dbDirectory = new File(storageRootPath + File.separator + databaseName.toLowerCase().trim());

        if (dbDirectory.exists() && dbDirectory.isDirectory()) {
            throw new MyError("[ERROR] " + databaseName + " already exists!");
        }

        if (dbDirectory.mkdirs()) {
            return "[OK] Database directory " + databaseName + " created successfully!";
        } else {
            throw new MyError("[ERROR] Failed to create database directory " + databaseName + ", create directory failed!");
        }
    }

    /** create a table file, no writing to the file */
    public String createTableFile(String tableName, String databaseName) {
        File tbFile = new File(storageRootPath + File.separator + databaseName.toLowerCase().trim() + File.separator + tableName.toLowerCase().trim() + ".tab");

        if (tbFile.exists()) {
            throw new MyError("[ERROR] Table " + tableName + " already exists!");
        }

        try {
            if (tbFile.createNewFile()) {
                return "[OK] Table file " + tableName + " created successfully!";
            } else {
                throw new MyError("[ERROR] Failed to create table file " + tableName + "!");
            }
        } catch (Exception e){
            throw new MyError("[ERROR] Failed to create table file " + tableName + ":\n" + e.getMessage());
        }
    }

    /** delete a database directory and all its files */
    public String dropDatabaseDirectory(String databaseName) {
        File dbDirectory = new File(storageRootPath + File.separator + databaseName.toLowerCase().trim());
        if (!dbDirectory.exists() || !dbDirectory.isDirectory()) {
            throw new MyError("[ERROR] Database directory " + databaseName + " does not exists!");
        }
        deleteDirOrFile(dbDirectory);
        return "[OK] Database directory " + databaseName + " deleted successfully!";
    }

    /** delete a table file */
    public String dropTableFile(String tableName, String databaseName) {
        File tbFile = new File(storageRootPath + File.separator + databaseName.toLowerCase().trim() + File.separator + tableName.toLowerCase().trim() + ".tab");
        if (!tbFile.exists()) {
            throw new MyError("[ERROR] Table file " + tableName + " does not exists!");
        }
        deleteDirOrFile(tbFile);
        return "[OK] Table file " + tableName + " deleted successfully!";
    }

    /** delete a directory and all its files, or delete a file */
    private void deleteDirOrFile(File entry) {
        if (entry.exists()) {

            // delete a directory and all its files
            if (entry.isDirectory()) {
                File[] files = entry.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            deleteDirOrFile(file);
                        } else {
                            if (!file.delete()) {
                                throw new MyError("[ERROR] Failed to delete file: " + file.getAbsolutePath());
                            }
                        }
                    }
                }
                if (!entry.delete()) {
                    throw new MyError("[ERROR] Failed to delete directory: " + entry.getAbsolutePath());
                }
            }

            // delete a file
            else if (entry.isFile()) {
                if (!entry.delete()) {
                    throw new MyError("[ERROR] Failed to delete file: " + entry.getAbsolutePath());
                }
            }

            else {
                throw new MyError("[ERROR] Only can delete a directory or file: " + entry.getAbsolutePath());
            }
        } else {
            throw new MyError("[ERROR] Cannot find the directory or file: " + entry.getAbsolutePath());
        }
    }

    /** write all its content into the file to store table file */
    public boolean saveTableFile(Table table, String databaseName) {

        if (!doesDatabaseExist(databaseName)) {
            throw new MyError("[ERROR] Cannot save table to a non-existent database: " + databaseName + "!");
        }

        String tbFilePath = storageRootPath + File.separator + databaseName.toLowerCase().trim() + File.separator + table.getTableName() + ".tab";

        try {
            // Create a BufferedWriter for the specified file path. If the file does not exist, it will be created; if it exists, it will be overwritten.
            BufferedWriter writer = new BufferedWriter(new FileWriter(tbFilePath));

            // write table's headers
            writer.write(String.join("\t", table.getColumnsNames()));
            // line break
            writer.newLine();

            // get table's idRowMap
            TreeMap<Integer, Row> tempMap = table.getIdRowMap();

            // write record line by line
            for (Integer writeId : tempMap.keySet()) {
                StringBuilder lineStr = new StringBuilder();

                // write id number first
                lineStr.append(writeId).append("\t");

                // then write data of each Row of idRowMap
                Row writeRow = tempMap.get(writeId);
                ArrayList<String> colsNamesWithoutId = table.getColumnsNames();
                colsNamesWithoutId.remove(0);
                for (String col : colsNamesWithoutId) {
                    String value = writeRow.getValue(col);
                    // ensure no tab inside data
                    if (value.contains("\t")) {
                        value = value.replace("\t", " ");
                    }
                    lineStr.append(value).append("\t");
                }

                // remove tab in the string end
                if (!lineStr.isEmpty()) {
                    lineStr.deleteCharAt(lineStr.length() - 1);
                }
                writer.write(lineStr.toString());
                writer.newLine();
            }
            writer.flush();
            writer.close();
            return true;

        } catch (Exception e) {
            throw new MyError("[ERROR] Failed to save table file: " + e.getMessage());
        }
    }

    /** read .tab file and load to Table object */
    public Table loadTableFile(String tableName, String databaseName) {
        if (!doesDatabaseExist(databaseName)) {
            throw new MyError("[ERROR] Cannot find the database directory!");
        }

        File tbFile = new File(storageRootPath + File.separator + databaseName.toLowerCase().trim() + File.separator + tableName.toLowerCase().trim() + ".tab");
        if (!tbFile.exists()) {
            throw new MyError("[ERROR] Table file" + tableName + " does not exists!");
        }

        try {
            // Use UTF-8 to ensure that files are transferred between different OSs without garbling.
            BufferedReader reader = new BufferedReader(new FileReader(tbFile.getAbsolutePath(), StandardCharsets.UTF_8));

            // read the first line
            String line = reader.readLine();

            // case that if table file exists but nothing in it
            if (line == null) {
                Table blankTable = new Table(tableName);
                blankTable.addColumn("id");
                reader.close();
                return blankTable;
            }

            // normal case
            // remove BOM and enter character
            line = removeControlChars(line);

            // use tab to split
            String[] lineParts = line.split("\t");

            // first line of .tab file should be the column headers
            ArrayList<String> columnHeaders = new ArrayList<>(Arrays.asList(lineParts));
            if (!columnHeaders.get(0).equalsIgnoreCase(("id"))) {
                throw new MyError("[ERROR] Invalid table! No id exists!");
            }

            // create Table object for existing table file
            Table table = new Table(tableName);

            // add columns' names to Table
            table.setColumnsNames(columnHeaders);

            // read lines left
            line = reader.readLine();
            int maxId = 0;

            while (line != null) {

                // skip the blank line
                if (line.trim().isEmpty()) {
                    line = reader.readLine();
                    continue;
                }

                // remove possible BOM and enter character
                line = removeControlChars(line);

                lineParts = line.split("\t");
                if (lineParts.length != columnHeaders.size()) {
                    throw new MyError("[ERROR] Invalid table! Headers and columns do not match!");
                }

                int readId;
                try {
                    readId = Integer.parseInt(lineParts[0]);
                } catch (NumberFormatException e) {
                    throw new MyError("[ERROR] Invalid table! ID is not an integer!" + e.getMessage());
                }

                // mark current table's id increaser
                maxId = Math.max(maxId, readId);

                Row readRow = new Row();
                // skip the id part of the read line
                for (int i = 1; i < lineParts.length; i++) {
                    readRow.addValue(columnHeaders.get(i), lineParts[i]);
                }
                readRow.setRecordId(readId);
                table.getThisIdRowMap().put(readId, readRow);
                line = reader.readLine();
            }

            // set this table's id increaser
            table.setIdIncreaser(maxId);

            reader.close();
            return table;

        } catch (Exception e) {
            throw new MyError("[ERROR] Failed to load table file: " + e.getMessage());
        }
    }

    /** remove control chars in a line, like BOM and enter character */
    private String removeControlChars(String line) {
        if (line.startsWith("\uFEFF")) {
            line = line.substring(1);
        }
        if (line.endsWith("\r")) {
            line = line.substring(0, line.length() - 1);
        }
        return line;
    }
}
