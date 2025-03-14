/** This is a class that was going to be used to record the maximum id usage for all tables in every database.<br>
 *  Due to time constraints it has not been implemented yet.<br>
 *  Without the use of this class, currently when a table is read, it is scanned for the largest id,<br>
 *  and then the id values of newly added records are incremented by that.<br>
 *  But this implementation has a potential problem, if you delete the record with the largest id value of a table, save the table,<br>
 *  and then re-load the table, then the deleted id will be used by the newly added record, <br>
 *  which violates the principle of not reusing ids in the same table. */
/*
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
*/