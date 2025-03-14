package edu.uob.storage;

import edu.uob.exception.MyError;

public class DbContext {
    private final FileManager fileManager;
    private String currentDatabase; // manipulating database (just a name, not a path)

    public DbContext(String storageFolderPath) {
        this.fileManager = new FileManager(storageFolderPath);
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    /** fetch manipulating database name */
    public String getCurrentDatabase() {
        if (!fileManager.doesDatabaseExist(currentDatabase)) {
            throw new MyError("[ERROR] Database does not exist: " + currentDatabase);
        }
        return currentDatabase;
    }

    /** switch to a database */
    public void setCurrentDatabase(String databaseName) {
        if (!fileManager.doesDatabaseExist(databaseName)) {
            throw new MyError("[ERROR] Database does not exist: " + databaseName);
        }
        this.currentDatabase = databaseName.toLowerCase().trim();
    }

    /** check if a database is currently selected */
    public boolean isDatabaseSelected() {
        return currentDatabase != null;
    }
}
