package com.microsoft.azure.toolkit.intellij.java.sdk.azd;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.JBColor;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.List;

public class AzdToolTilePopupPanel extends JPanel {

    private final Project project;
    private JBPopup popup;
    private ConsoleView consoleView;

    // Command to get the data
    private static final String DATA_COMMAND = "azd template list --filter java";

    public AzdToolTilePopupPanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));

        // Create a scroll pane for the tiles
        JPanel tilesPanel = new JPanel();
        tilesPanel.setLayout(new GridLayout(0, 3, JBUI.scale(10), JBUI.scale(10)));
        tilesPanel.setBorder(JBUI.Borders.empty(10));

        // Add a label explaining the panel
        JBLabel instructionLabel = new JBLabel("Click on a tile to select and run a tool");
        instructionLabel.setBorder(JBUI.Borders.empty(0, 0, 10, 0));
        add(instructionLabel, BorderLayout.NORTH);

        // Add scroll pane with tiles panel
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(tilesPanel);
        scrollPane.setBorder(JBUI.Borders.empty());
        add(scrollPane, BorderLayout.CENTER);

        // Initialize console view for output
        consoleView = new ConsoleViewImpl(project, true);
        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.add(consoleView.getComponent(), BorderLayout.CENTER);
        consolePanel.setPreferredSize(new Dimension(-1, 150));
        consolePanel.setBorder(JBUI.Borders.empty(10, 0, 0, 0));
        add(consolePanel, BorderLayout.SOUTH);

        // Load data
        loadData(tilesPanel);
    }

    /**
     * Load data from command execution and create tiles
     */
    private void loadData(JPanel tilesPanel) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Loading Tool Data", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText("Executing command to load tool data...");

                final List<ToolItem> items = ToolItem.createDataFromCommand(project, DATA_COMMAND);

                ApplicationManager.getApplication().invokeLater(() -> {
                    tilesPanel.removeAll();

                    for (ToolItem item : items) {
                        tilesPanel.add(createToolTile(item));
                        tilesPanel.add(Box.createVerticalStrut(10)); // Spacing between tiles
                    }

                    tilesPanel.revalidate();
                    tilesPanel.repaint();
                });
            }
        });
    }

    /**
     * Create a rectangular tile for a tool item
     */
    private JPanel createToolTile(ToolItem item) {
        JBPanel<JBPanel<?>> tilePanel = new JBPanel<>(new BorderLayout());

        // Style the tile
        Border lineBorder = BorderFactory.createLineBorder(JBColor.border(), 1);
        Border emptyBorder = JBUI.Borders.empty(10);
        tilePanel.setBorder(BorderFactory.createCompoundBorder(lineBorder, emptyBorder));
        tilePanel.setBackground(JBColor.background().brighter());

        // Title at the top
        JBLabel titleLabel = new JBLabel(item.getName());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, titleLabel.getFont().getSize() + 2));
        titleLabel.setAllowAutoWrapping(true);
        titleLabel.setBorder(JBUI.Borders.empty(0, 0, 5, 0));
        tilePanel.add(titleLabel, BorderLayout.NORTH);

        // Description in the middle
        JPanel descriptionPanel = new JPanel(new BorderLayout());
        JBLabel descLabel = new JBLabel("<html><body width='300px'>" + item.getDescription() + "</body></html>");
        descLabel.setBorder(JBUI.Borders.empty(5, 0));
        descriptionPanel.add(descLabel, BorderLayout.CENTER);

        // Repo link below description
        HyperlinkLabel link = new HyperlinkLabel(item.getRepoLink());
        descLabel.setBorder(JBUI.Borders.empty(5, 0));
        descriptionPanel.add(link, BorderLayout.SOUTH);

        tilePanel.add(descriptionPanel, BorderLayout.CENTER);

        // Command at the bottom
        JPanel commandPanel = new JPanel(new BorderLayout());
        commandPanel.setOpaque(false);
        commandPanel.setBorder(JBUI.Borders.empty(5, 0, 0, 0));

        JBLabel commandLabel = new JBLabel("Command: ");
        commandLabel.setForeground(JBColor.gray);

        JBLabel commandValueLabel = new JBLabel(item.getCommand());
        commandValueLabel.setForeground(JBColor.foreground().darker());

        commandPanel.add(commandLabel, BorderLayout.WEST);
        commandPanel.add(commandValueLabel, BorderLayout.CENTER);

        JButton runButton = new JButton("Run");
        runButton.addActionListener(e -> runCommand(item.getCommand()));
        commandPanel.add(runButton, BorderLayout.EAST);

        tilePanel.add(commandPanel, BorderLayout.SOUTH);

        // Make entire tile clickable to select
        tilePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // If double-click, run the command
                if (e.getClickCount() == 2) {
                    runCommand(item.getName());
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                tilePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                tilePanel.setBackground(JBColor.background().brighter().brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tilePanel.setCursor(Cursor.getDefaultCursor());
                tilePanel.setBackground(JBColor.background().brighter());
            }
        });

        // Set the preferred size for the tile
        tilePanel.setPreferredSize(new Dimension(350, 175));
        tilePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 175));

        return tilePanel;
    }

    /**
     * Run a command and show the output
     */
    private void runCommand(String command) {
        // Clear previous output
        consoleView.clear();

        // Print command
        consoleView.print("> " + command + "\n", ConsoleViewContentType.USER_INPUT);

        // Execute command and show result
        ToolRunner.runTool(project, command, consoleView);
    }

    public void setPopup(JBPopup popup) {
        this.popup = popup;
    }

    /**
     * Dialog to show confirmation for running a command
     */
    private class RunConfirmationDialog extends DialogWrapper {
        private final String command;

        public RunConfirmationDialog(String command) {
            super(project, false);
            this.command = command;
            setTitle("Confirm Run");
            init();
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("Run command: " + command + "?"), BorderLayout.CENTER);
            return panel;
        }

        @Override
        protected void doOKAction() {
            super.doOKAction();
            runCommand(command);
        }
    }
}
