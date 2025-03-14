package edu.uob.storage;

import edu.uob.exception.MyError;

import java.io.File;
import java.io.IOException;

public class IdManager {
    private String tableName;
    private String databaseName;
    private String folderPath;

    public IdManager(String tableName, String databaseName, String folderPath) {
        this.tableName = tableName.toLowerCase();
        this.databaseName = databaseName.toLowerCase();
        this.folderPath = folderPath;
    }

    public boolean createIdManagerFile() {
        File idManagerFolder = new File(folderPath + File.separator + "." + databaseName);
        if (!idManagerFolder.exists() || !idManagerFolder.isDirectory()) {
            if (!idManagerFolder.mkdirs()) {
                throw new MyError("[ERROR] Failed to create id manager folder!");
            }
        }

        File idManagerFile = new File(idManagerFolder, "." + tableName);
        if (!idManagerFile.exists() || !idManagerFile.isFile()) {
            try {
                idManagerFile.createNewFile();
                return true;
            } catch (IOException e) {
                throw new MyError("[ERROR] " + e.getMessage());
            }
        }
        return false;
    }

    public boolean deleteIdManagerFile() {
        File idManagerFile = new File(folderPath + File.separator + "." + tableName);
    }
}
