package cadonuno.pipelinescanautotrigger.ui;

import javax.swing.table.DefaultTableModel;

public class FindingResultsTableModel extends DefaultTableModel {

    public static final String DETAILS_COLUMN_IDENTIFIER = "Details";

    public FindingResultsTableModel() {
        String[] columnNames = new String[]{
                "Issue ID",
                "Severity",
                "CWE ID",
                "CWE Name",
                DETAILS_COLUMN_IDENTIFIER,
                "File Name",
                "Line"};
        setDataVector(new Object[][]{}, columnNames);
    }

    private final Class[] types = new Class[]{
            Long.class,
            String.class,
            String.class,
            String.class,
            Object.class,
            String.class,
            Long.class};

    @Override
    public Class getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

}

