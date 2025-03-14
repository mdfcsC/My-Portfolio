package edu.uob.view;

import edu.uob.model.Row;
import edu.uob.model.Table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

public class MyView {
    private Table outputTable;

    public MyView(Table outputTable) {
        this.outputTable = outputTable;
    }

    public String render() {
        StringBuilder result = new StringBuilder();

        // output headers first
        ArrayList<String> tableHeaders = outputTable.getColumnsNames();
        for (int j = 0; j < tableHeaders.size(); j++) {
            result.append(tableHeaders.get(j));
            // no tab at the end at every line
            if (j != tableHeaders.size() - 1) {
                result.append("\t");
            }
        }
        result.append("\n");

        // iterator approach
        TreeMap<Integer, Row> tempMap = outputTable.getIdRowMap();
        Iterator<Integer> idIterator = tempMap.keySet().iterator();
        while (idIterator.hasNext()) {
            Integer idNumber = idIterator.next();
            for (int k = 0; k < tableHeaders.size(); k++) {
                if (tableHeaders.get(k).equals("id")) {
                    result.append(idNumber);
                } else {
                    result.append(tempMap.get(idNumber).getValue(tableHeaders.get(k)));
                }
                if (k != tableHeaders.size() - 1) {
                    result.append("\t");
                }
            }
            result.append("\n");
        }
// old version, for loop approach
//        // get all records' IDs, whatever they will be output
//        ArrayList<Integer> idList = new ArrayList<>(outputTable.getIdRowMap().keySet());
//
//        // output records row by row
//        for (int i = 0; i < outputTable.getRecordsCount(); i++) {
//            // only output value of attributes selected of every row
//            for (int j = 0; j < tableHeaders.size(); j++) {
//                if (tableHeaders.get(j).equals("id")) {
//                    result.append(idList.get(i));
//                    // no tab at the end at every line
//                    if (j != tableHeaders.size() - 1) {
//                        result.append("\t");
//                    }
//                } else {
//                    result.append(outputTable.getIdRowMap().get(idList.get(i)));
//                    // no tab at the end at every line
//                    if (j != tableHeaders.size() - 1) {
//                        result.append("\t");
//                    }
//                }
//            }
//            result.append("\n");
//        }

        return result.toString();
    }
}
