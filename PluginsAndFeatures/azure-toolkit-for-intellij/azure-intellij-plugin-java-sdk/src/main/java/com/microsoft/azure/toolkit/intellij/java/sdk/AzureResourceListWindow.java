package com.microsoft.azure.toolkit.intellij.java.sdk;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBFont;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.RandomUtils;
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
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalToolWindowManager;
import com.intellij.openapi.ui.Messages;
import org.reflections.vfs.Vfs;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Vector;
import java.util.regex.Matcher;

import static com.sun.java.accessibility.util.AWTEventMonitor.addActionListener;

public class AzureResourceListWindow {
    private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
    private static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);

    private static final Vector<String> COLUMNS = new Vector<>(Arrays.asList("Resource name", "Resource Type", "Resource Group", "Portal"));

    private static final String STORAGE_ACCOUNT_BICEP = """
         param location string = resourceGroup().location
            
         resource sa 'Microsoft.Storage/storageAccounts@2023-01-01' = {
           name: 'test'
           location: location
           sku: {
             name: 'Standard_LRS'
           }
           kind: 'StorageV2'
           properties: {
             accessTier: 'Hot'
             allowSharedKeyAccess: false
           }
         }
        
         resource blobServices 'Microsoft.Storage/storageAccounts/blobServices@2023-01-01' = {
           parent: sa
           name: 'default'
         }
        
         resource container 'Microsoft.Storage/storageAccounts/blobServices/containers@2023-01-01' = {
           parent: blobServices
           name: 'testcontainer'
         }""";

    private static final String STORAGE_MODULE = """
        module storage 'modules/storage/storage.bicep' = {
            name: '${deployment().name}--storage'
            scope: resourceGroup(rg.name)
            params: {
                location: location
            }
        }
        """;

    private static final String EXISTING_STORAGE_MODULE = """
        module storage 'modules/storage/storage.bicep' = {
            name: '${deployment().name}--storage'
            scope: resourceGroup(rg.name)
        }
        """;

    public static void showPopup(Project project, String title, Component parent) {
        // Sample list of text items
        final Vector<Vector<String>> items = new Vector<>();
        // Main panel combining table and input area
        JPanel mainPanel = new JPanel(new BorderLayout());


        AzureResources az = Azure.az(AzureResources.class);
        String namespaceType;
        if (title.contains("Configuration")) {
            namespaceType = "AppConfiguration";
        } else if (title.contains("Storage") || title.contains("Blob") || title.contains("Container")) {
            namespaceType = "Storage";
        } else if (title.contains("OpenAI")) {
            namespaceType = "CognitiveService";
        } else if (title.contains("Search")) {
            namespaceType = "Search";
        } else if (title.contains("ServiceBus")) {
            namespaceType = "ServiceBus";
        } else if (title.contains("EventHub")) {
            namespaceType = "EventHub";
        } else {
            namespaceType = "KeyVault";
        }

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


        // JTable/JBTable consumes all events and making individual cell clickable is easier with JXTable
        // Create table model
        DefaultTableModel tableModel = new DefaultTableModel(items, COLUMNS) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

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

        JXTable table = new JXTable(tableModel);
        table.setPreferredScrollableViewportSize(new Dimension(1000, 800));
        table.setFillsViewportHeight(true);

        HyperlinkTextProvider hyperlinkProvider = new HyperlinkTextProvider(simpleAction);
        TableCellRenderer renderer = new DefaultTableRenderer(hyperlinkProvider);

        // Set hyperlink renderer for last column
        table.getColumnModel().getColumn(3).setCellRenderer(renderer);

        // Create a scroll pane for the table
        JScrollPane scrollPane = new JBScrollPane(table);

        // Create and show popup
        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(mainPanel, table)
                .setTitle("Select an Item")
                .setMovable(true)
                .setResizable(true)
                .setRequestFocus(true)
                .setCancelOnClickOutside(false)
                .setCancelOnWindowDeactivation(false)
                .setCancelButton(new IconButton("Close", AllIcons.Actions.Close, AllIcons.Actions.CloseHovered))
                .createPopup();

        // Create text field and send button
        JPanel inputPanel = new JPanel(new BorderLayout());
        if (isAzdInitialized(project)) {
            JBTextArea jbTextArea = new JBTextArea("This project is initialized with azd. Add an existing resource or create new resource.");
            jbTextArea.setEditable(false);
            jbTextArea.setLineWrap(true);
            jbTextArea.setWrapStyleWord(true);
            jbTextArea.setCaretPosition(0);
            jbTextArea.setOpaque(false);
            jbTextArea.setPreferredSize(new Dimension(100, 50));
            jbTextArea.setFont(JBFont.h2());
            jbTextArea.setAlignmentX(Component.RIGHT_ALIGNMENT);
            jbTextArea.setAlignmentY(Component.BOTTOM_ALIGNMENT);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JPanel addSelectedPanel = new JPanel(new BorderLayout());
            JPanel addNewPanel = new JPanel(new BorderLayout());

            JButton addSelected = new JButton("Add Selected");
            addSelected.setEnabled(false);

            JButton addNew = new JButton("Add New");

            addNewPanel.add(addNew, BorderLayout.CENTER);
            addSelectedPanel.add(addSelected, BorderLayout.CENTER);

            buttonPanel.add(addSelectedPanel, BorderLayout.EAST);
            buttonPanel.add(addNewPanel, BorderLayout.WEST);

            inputPanel.add(jbTextArea, BorderLayout.CENTER);
            inputPanel.add(buttonPanel, BorderLayout.SOUTH);

            addNew.addActionListener((ActionEvent e) -> {
                ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        VirtualFile infra = project.getBaseDir().findChild("infra");
                        VirtualFile mainBicep = infra.findChild("main.bicep");
                        VirtualFile modules = infra.findChild("modules");
                        if (modules != null) {
                            VirtualFile storageModule = modules.createChildDirectory(project, "storage");
                            VirtualFile storageBicep = storageModule.findOrCreateChildData(project, "storage.bicep");
                            VfsUtil.saveText(storageBicep, STORAGE_ACCOUNT_BICEP.replace("test", "test" + RandomUtils.nextInt()));
                        }
                        String s = new String(mainBicep.contentsToByteArray());
                        if (!mainBicep.isWritable()) {
                            mainBicep.setWritable(true);
                        }
                        VfsUtil.saveText(mainBicep, s.replaceFirst("output",  Matcher.quoteReplacement(STORAGE_MODULE) + "\n\noutput"));
                        ApplicationManager.getApplication().invokeLater(() -> {
                            popup.cancel();
                            Messages.showInfoMessage("Resource added successfully!", "Success");
                            FileEditorManager.getInstance(project).openFile(mainBicep, true);
                        });
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            });

            addSelected.addActionListener((ActionEvent e) -> {
                ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        VirtualFile infra = project.getBaseDir().findChild("infra");
                        VirtualFile mainBicep = infra.findChild("main.bicep");

                        VirtualFile modules = infra.findChild("modules");

                        String existingResource = "resource storage 'Microsoft.Storage/storageAccounts@2023-04-01' existing = {\n" +
                                "  name: '" + table.getStringAt(table.getSelectedRow(), 0) + "'\n" +
                                "}";

                        if (modules != null) {
                            VirtualFile storageModule = modules.createChildDirectory(project, "storage");
                            VirtualFile storageBicep = storageModule.findOrCreateChildData(project, "storage.bicep");
                            VfsUtil.saveText(storageBicep, existingResource);
                        }

                        String s = new String(mainBicep.contentsToByteArray());
                        if (!mainBicep.isWritable()) {
                            mainBicep.setWritable(true);
                        }

                        VfsUtil.saveText(mainBicep, s.replaceFirst("output", Matcher.quoteReplacement(EXISTING_STORAGE_MODULE)
                                .replace("rg.name", "'" + table.getStringAt(table.getSelectedRow(), 2)+ "'") + "\n\noutput"));

                        ApplicationManager.getApplication().invokeLater(() -> {
                            popup.cancel();
                            Messages.showInfoMessage("Resource added successfully!", "Success");
                            FileEditorManager.getInstance(project).openFile(mainBicep, true);
                        });
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            });

            // Add table selection listener
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        int selectedRow = table.getSelectedRow();
                        if (selectedRow >= 0) {
                            addSelected.setEnabled(true);
                        }
                    }
                }
            });
        } else {
            JBTextArea jbTextArea = new JBTextArea("This project is NOT initialized with azd.");
            jbTextArea.setEditable(false);
            jbTextArea.setLineWrap(true);
            jbTextArea.setWrapStyleWord(true);
            jbTextArea.setCaretPosition(0);
            jbTextArea.setOpaque(false);
            jbTextArea.setPreferredSize(new Dimension(100, 100));
            jbTextArea.setFont(JBFont.h2());
            jbTextArea.setAlignmentX(Component.CENTER_ALIGNMENT);
            jbTextArea.setAlignmentY(Component.CENTER_ALIGNMENT);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JPanel addSelectedPanel = new JPanel(new BorderLayout());
            JPanel addNewPanel = new JPanel(new BorderLayout());

            JButton initialize = new JButton("Initialize with azd ");

            addNewPanel.add(initialize, BorderLayout.CENTER);
            buttonPanel.add(addNewPanel, BorderLayout.WEST);

            inputPanel.add(jbTextArea, BorderLayout.CENTER);
            inputPanel.add(buttonPanel, BorderLayout.SOUTH);

            initialize.addActionListener((ActionEvent e) -> {
                try {
                    ShellTerminalWidget myAzdConsole = TerminalToolWindowManager.getInstance(project).createLocalShellWidget(project.getBasePath(), "my azd console");
                    mainPanel.add(myAzdConsole, BorderLayout.SOUTH);
                    myAzdConsole.executeCommand("azd init --from-code");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        popup.showInCenterOf(parent);
    }

    private static boolean isAzdInitialized(Project project) {
        VirtualFile child = project.getBaseDir().findChild("azure.yaml");
        if (child != null) {
            return true;
        }
        return false;
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
