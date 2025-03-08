package com.microsoft.azure.toolkit.intellij.java.sdk;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.resource.AzureResources;
import com.microsoft.azure.toolkit.lib.resource.ResourcesServiceSubscription;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Vector;

public class AzureResourceListWindow {
    private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
    private static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);

    private static final Vector<String> COLUMNS = new Vector<>(Arrays.asList("Resource name", "Resource Type", "Resource Group", "Portal"));

    public static void showPopup(String title, Component parent) {
        // Sample list of text items
        final Vector<Vector<String>> items = new Vector<>();

        AzureResources az = Azure.az(AzureResources.class);
        String namespaceType = title.contains("Configuration") ? "AppConfiguration" : "Storage";

        ResourcesServiceSubscription resourcesServiceSubscription = Azure.az(AzureResources.class)
                .list()
                .get(0);
        resourcesServiceSubscription
                .getResourceManager()
                .genericResources()
                .list()
                .stream()
                .filter(rg -> rg.innerModel().type().contains(namespaceType))
                .forEach(resource -> {
                    Vector<String> columns = new Vector<>();
                    columns.add(resource.name());
                    columns.add(resource.resourceProviderNamespace());
                    columns.add(resource.resourceGroupName());
                    columns.add(resourcesServiceSubscription.getPortalUrl());
                    items.add(columns);
                });

        // Create table model
        DefaultTableModel tableModel = new DefaultTableModel(items, COLUMNS) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        // Create table
        JBTable table = new JBTable(tableModel);
        table.setPreferredScrollableViewportSize(new Dimension(500, 350));
        table.setFillsViewportHeight(true);

        // Set hyperlink renderer for last column
        table.getColumnModel().getColumn(3).setCellRenderer(new LinkRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(table.getSelectedColumn() == 3) {
                    String url = (String) table.getValueAt(table.getSelectedRow(), table.getSelectedColumn());
                    BrowserUtil.browse(url);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
                if (col == 3) {
                    e.getComponent().setCursor(HAND_CURSOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
                if (col != 3) {
                    e.getComponent().setCursor(DEFAULT_CURSOR);
                }
            }
        });


        // Create a scroll pane for the table
        JScrollPane scrollPane = new JBScrollPane(table);

        // Create text field and send button
        JTextField textField = new JTextField();
        JButton sendButton = new JButton("Send");

        // Send button action
        sendButton.addActionListener((ActionEvent e) -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String selectedText = table.getValueAt(selectedRow, 0) + ", " +
                        table.getValueAt(selectedRow, 1) + ", " +
                        table.getValueAt(selectedRow, 2);
                String enteredText = textField.getText();
                System.out.println("Selected: " + selectedText + ", Message: " + enteredText);
            }
        });

        // Panel for input field and button
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Main panel combining table and input area
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // Create and show popup
        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(mainPanel, table)
                .setTitle("Select an Item")
                .setMovable(true)
                .setResizable(true)
                .setRequestFocus(true)
                .createPopup();

        popup.showInCenterOf(parent);
    }


    // Renderer for clickable links
    static class LinkRenderer extends JPanel implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof String) {
                String url = (String) value;
                HyperlinkLabel linkLabel = new HyperlinkLabel("More details");
                linkLabel.setHyperlinkTarget(url);
                return linkLabel;
            }
            return new JLabel(value.toString());
        }


    }
}
