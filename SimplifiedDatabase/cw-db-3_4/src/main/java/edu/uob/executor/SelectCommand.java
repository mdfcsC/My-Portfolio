package edu.uob.executor;

import edu.uob.executor.condition.MyCondition;
import edu.uob.model.Table;
import edu.uob.storage.DbContext;
import edu.uob.view.MyView;

import java.util.ArrayList;

public class SelectCommand extends DbCmd{
    private ArrayList<String> selectedAttributes;
    private String tableName;
    private MyCondition condition;

    public SelectCommand(ArrayList<String> selectAttributes, String tableName, MyCondition condition) {
        this.selectedAttributes = selectAttributes;
        this.tableName = tableName;
        this.condition = condition;
    }

    @Override
    public String execute(DbContext dbContext) {
        this.dbName = dbContext.getCurrentDatabase();
        Table selectedTable = dbContext.getFileManager().loadTableFile(this.tableName, this.dbName);

        // build output table
        Table outputTable = new Table("tempselect" + this.tableName);

        // set output table's headers
        if (selectedAttributes.get(0).equals("*")) {
            // all columns
            outputTable.setColumnsNames(selectedTable.getColumnsNames());
        } else {
            // selected attributes only
            outputTable.setColumnsNames(selectedAttributes);
        }

        for (Integer id : selectedTable.getIdRowMap().keySet()) {
            if (condition == null || condition.evaluate(selectedTable.getIdRowMap().get(id))) {
                outputTable.getThisIdRowMap().put(id, selectedTable.getIdRowMap().get(id));
            }
        }

//        // check if no records in outputTable
//        if (outputTable.getThisIdRowMap().isEmpty()) {
//            return "[ERROR] No record matches the condition!";
//        }

        // output message and table view
        String outputTableView = new MyView(outputTable).render();
        return "[OK]\n" + outputTableView;
    }
}
