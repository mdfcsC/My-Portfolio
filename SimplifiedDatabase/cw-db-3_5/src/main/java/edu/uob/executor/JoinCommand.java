package edu.uob.executor;

import edu.uob.model.Row;
import edu.uob.model.Table;
import edu.uob.storage.DbContext;
import edu.uob.view.MyView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class JoinCommand extends DbCmd{
    private String tableName1;
    private String tableName2;
    private String matchAttributeName1;
    private String matchAttributeName2;

    public JoinCommand(String tableName1, String tableName2, String matchAttributeName1, String matchAttributeName2) {
        this.tableName1 = tableName1;
        this.tableName2 = tableName2;
        this.matchAttributeName1 = matchAttributeName1;
        this.matchAttributeName2 = matchAttributeName2;
    }

    /** For JOINs: discard the ids from the original tables<br>
        discard the columns that the tables were matched on<br>
        create a new unique id for each of row of the table produced<br>
        attribute names are prepended with name of table from which they originated
     */
    @Override
    public String execute(DbContext dbContext) {
        this.dbName = dbContext.getCurrentDatabase();
        Table table1 = dbContext.getFileManager().loadTableFile(this.tableName1, this.dbName);
        Table table2 = dbContext.getFileManager().loadTableFile(this.tableName2, this.dbName);
        Table joinTable = new Table("temp" + this.tableName1 + "join" + this.tableName2);

        // build join table's headers
        ArrayList<String> joinTableHeaders = new ArrayList<>();
        joinTableHeaders.add("id");

        ArrayList<String> cleanTable1Headers = table1.getColumnsNames();
        ArrayList<String> cleanTable2Headers = table2.getColumnsNames();

        String idToRemove = "id";
        cleanTable1Headers.removeIf(element -> element.equalsIgnoreCase(idToRemove));
        cleanTable2Headers.removeIf(element -> element.equalsIgnoreCase(idToRemove));
        String table1AttributeToRemove = matchAttributeName1;
        cleanTable1Headers.removeIf(element -> element.equalsIgnoreCase((table1AttributeToRemove)));
        String table2AttributeToRemove = matchAttributeName2;
        cleanTable2Headers.removeIf(element -> element.equalsIgnoreCase((table2AttributeToRemove)));

        joinTableHeaders.addAll(addTableName2Headers(cleanTable1Headers, this.tableName1));
        joinTableHeaders.addAll(addTableName2Headers(cleanTable2Headers, this.tableName2));
        joinTable.setColumnsNames(joinTableHeaders);

        // fetch two copies of two tables' row records
        Collection<Row> rows1 = table1.getIdRowMap().values();
        // for each row1, find the matched data of row2
        for (Row row1 : rows1) {
            Collection<Row> rows2 = table2.getIdRowMap().values();
            for (Row row2 : rows2) {
                String row1Data, row2Data;

                // fetch the data to match
                if (matchAttributeName1.equalsIgnoreCase("id")) {
                    row1Data = row1.getRecordId().toString();
                } else {
                    row1Data = row1.getValue(matchAttributeName1);
                }
                if (matchAttributeName2.equalsIgnoreCase("id")) {
                    row2Data = row2.getRecordId().toString();
                } else {
                    row2Data = row2.getValue(matchAttributeName2);
                }

                // if find the match
                if (Objects.equals(row1Data, row2Data)) {
                    if (!matchAttributeName1.equalsIgnoreCase("id")) {
                        row1.removeValue(matchAttributeName1);
                    }
                    for (String atrbtName : row1.getRowDataMap().keySet()) {
                        row1.modifyAttributeName(atrbtName, tableName1 + "." + atrbtName);

                    }
                    if (!matchAttributeName2.equalsIgnoreCase("id")) {
                        row2.removeValue(matchAttributeName2);
                    }
                    for (String atrbtName : row2.getRowDataMap().keySet()) {
                        row2.modifyAttributeName(atrbtName, tableName2 + "." + atrbtName);
                    }

                    // concatenate two rows then add it to join table
                    row1.getThisRowDataMap().putAll(row2.getRowDataMap());
                    joinTable.addRow(row1);

                    break;
                }
            }
        }

        // send to view
        String outputViewTable = new MyView(joinTable).render();
        return "[OK]\n" + outputViewTable;
    }

    /** old table name: students<br>
     *  old headers: age, score --> new headers: students.age, students.score<br>
     *  will discard id column
     */
    private ArrayList<String> addTableName2Headers(ArrayList<String> headers, String tableName) {
        headers.remove("id");
        for (int i = 0; i < headers.size(); i++) {
            headers.set(i, tableName + "." + headers.get(i));
        }
        return headers;
    }
}
