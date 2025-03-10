package com.microsoft.azure.toolkit.intellij.java.sdk.azd;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ToolItemTableModel extends AbstractTableModel {
    private final List<ToolItem> toolItems;
    private final String[] columnNames = {"Name", "Description"};

    public ToolItemTableModel(List<ToolItem> toolItems) {
        this.toolItems = toolItems;
    }

    @Override
    public int getRowCount() {
        return toolItems.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ToolItem item = toolItems.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> item.getName();
            case 1 -> item.getDescription();
            default -> null;
        };
    }

    public ToolItem getItemAt(int rowIndex) {
        return toolItems.get(rowIndex);
    }
}