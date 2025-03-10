package com.microsoft.azure.toolkit.intellij.java.sdk;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.components.JBScrollPane;
import com.microsoft.azure.toolkit.intellij.java.sdk.azd.ToolItem;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.resource.AzureResources;
import com.microsoft.azure.toolkit.lib.resource.ResourcesServiceSubscription;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.hyperlink.AbstractHyperlinkAction;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.HyperlinkProvider;
import org.jdesktop.swingx.renderer.JXRendererHyperlink;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
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

        // JTable/JBTable consumes all events and making individual cell clickable is easier with JXTable
        JXTable table = new JXTable(tableModel);
        table.setPreferredScrollableViewportSize(new Dimension(500, 350));
        table.setFillsViewportHeight(true);

        AbstractHyperlinkAction<Object> simpleAction = new AbstractHyperlinkAction<Object>(null) {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JXRendererHyperlinkText) {
                    JXRendererHyperlinkText source = (JXRendererHyperlinkText) e.getSource();
                    BrowserUtil.browse(source.getOriginalUrl());
                } else {
                    BrowserUtil.browse(e.getActionCommand());
                }
            }
        };
        HyperlinkTextProvider hyperlinkProvider = new HyperlinkTextProvider(simpleAction);
        TableCellRenderer renderer = new DefaultTableRenderer(hyperlinkProvider);

        // Set hyperlink renderer for last column
        table.getColumnModel().getColumn(3).setCellRenderer(renderer);

        // Create a scroll pane for the table
        JScrollPane scrollPane = new JBScrollPane(table);

        // Create text field and send button
        JTextField textField = new JTextField();
        JButton sendButton = new JButton("Run");

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

        // Add table selection listener
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        textField.setText("azd add " + (String) tableModel.getValueAt(selectedRow, 0));
                    }
                }
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

    private static class HyperlinkTextProvider extends HyperlinkProvider {
        HyperlinkTextProvider(AbstractHyperlinkAction action) {
            super(action);
        }
        @Override
        protected void format(CellContext context) {
            super.format(context);
            ((JXHyperlink) this.rendererComponent).setText("More Details");
            ((JXRendererHyperlinkText) this.rendererComponent).setOriginalUrl((String) context.getValue());
        }

        @Override
        protected JXHyperlink createRendererComponent() {
            return new JXRendererHyperlinkText();
        }
    }

    private static class JXRendererHyperlinkText extends JXRendererHyperlink {
        private String originalUrl;

        public void setOriginalUrl(String originalUrl) {
            this.originalUrl = originalUrl;
        }

        public String getOriginalUrl() {
            return this.originalUrl;
        }
    }
}
