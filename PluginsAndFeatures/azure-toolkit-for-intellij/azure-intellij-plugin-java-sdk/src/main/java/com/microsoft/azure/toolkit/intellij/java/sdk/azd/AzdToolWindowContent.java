package com.microsoft.azure.toolkit.intellij.java.sdk.azd;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class AzdToolWindowContent {
    // Command to get the data
    private static final String DATA_COMMAND = "azd template list --filter java";

    private final Project project;
    private final ToolWindow toolWindow;
    private JPanel contentPanel;
    private JTextField commandTextField;
    private JBTable toolItemsTable;
    private ToolItemTableModel tableModel;
    private ConsoleView consoleView;

    public AzdToolWindowContent(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        createUIComponents();
        loadData();
    }

    private void createUIComponents() {
        // Create main content panel
        contentPanel = new JPanel(new BorderLayout());

        // Create the splitter panel for table and console
        JBSplitter splitter = new JBSplitter(true, 0.7f);

        // Create the top panel with table and command input
        JPanel topPanel = new JPanel(new BorderLayout());

        // Create table with tool items
        tableModel = new ToolItemTableModel(new ArrayList<>());
        toolItemsTable = new JBTable(tableModel);
        toolItemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set column widths
        TableColumn nameColumn = toolItemsTable.getColumnModel().getColumn(0);
        nameColumn.setPreferredWidth(100);
        TableColumn descColumn = toolItemsTable.getColumnModel().getColumn(1);
        descColumn.setPreferredWidth(300);

        JBScrollPane tableScrollPane = new JBScrollPane(toolItemsTable);
        topPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Create command panel with text field and button
        JPanel commandPanel = new JPanel(new BorderLayout());
        commandPanel.setBorder(JBUI.Borders.empty(5));

        commandTextField = new JTextField();
        JButton runButton = new JButton("Run");
        commandPanel.add(commandTextField, BorderLayout.CENTER);
        commandPanel.add(runButton, BorderLayout.EAST);

        topPanel.add(commandPanel, BorderLayout.SOUTH);

        // Create console panel
        consoleView = new ConsoleViewImpl(project, true);
        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.add(consoleView.getComponent(), BorderLayout.CENTER);

        // Add console toolbar
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new RunToolAction(this));
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("AzdToolConsoleToolbar", actionGroup, false);
        consolePanel.add(actionToolbar.getComponent(), BorderLayout.WEST);

        // Add reload button to the top panel
        JButton reloadButton = new JButton("Reload Data");
        reloadButton.addActionListener(e -> loadData());
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topButtonPanel.add(reloadButton);
        topPanel.add(topButtonPanel, BorderLayout.NORTH);

        // Add panels to splitter
        splitter.setFirstComponent(topPanel);
        splitter.setSecondComponent(consolePanel);

        contentPanel.add(splitter, BorderLayout.CENTER);

        // Add table selection listener
        toolItemsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = toolItemsTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        ToolItem selectedItem = tableModel.getItemAt(selectedRow);
                        commandTextField.setText(selectedItem.getDescription());
                    }
                }
            }
        });

        // Add run button action
        runButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runCommand();
            }
        });
    }

    /**
     * Load data from command execution
     */
    private void loadData() {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Loading Tool Data", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText("Executing command to load tool data...");

                final List<ToolItem> items = ToolItem.createDataFromCommand(project, DATA_COMMAND);

                ApplicationManager.getApplication().invokeLater(() -> {
                    tableModel = new ToolItemTableModel(items);
                    toolItemsTable.setModel(tableModel);

                    // Reset column widths as they may be lost when model changes
                    TableColumn nameColumn = toolItemsTable.getColumnModel().getColumn(0);
                    nameColumn.setPreferredWidth(100);
                    TableColumn descColumn = toolItemsTable.getColumnModel().getColumn(1);
                    descColumn.setPreferredWidth(300);

                    // If there are items, select the first one
                    if (!items.isEmpty()) {
                        toolItemsTable.getSelectionModel().setSelectionInterval(0, 0);
                    }
                });
            }
        });
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void runCommand() {
        String command = commandTextField.getText().trim();
        if (!command.isEmpty()) {
            // Clear previous output
            consoleView.clear();

            // Print command
            consoleView.print("> " + command + "\n", ConsoleViewContentType.USER_INPUT);

            // Execute command and show result
            ToolRunner.runTool(project, command, consoleView);
        }
    }

    public String getCommand() {
        return commandTextField.getText();
    }

    public Project getProject() {
        return project;
    }

    public ConsoleView getConsoleView() {
        return consoleView;
    }
}
