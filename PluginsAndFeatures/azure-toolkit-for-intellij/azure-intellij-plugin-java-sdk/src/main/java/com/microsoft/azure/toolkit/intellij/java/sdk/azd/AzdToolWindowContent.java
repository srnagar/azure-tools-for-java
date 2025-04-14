package com.microsoft.azure.toolkit.intellij.java.sdk.azd;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.RowGridLayout;
import com.intellij.util.ui.JBUI;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.RandomUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static java.awt.Font.SANS_SERIF;
import static javax.swing.SwingConstants.CENTER;

public class AzdToolWindowContent {
    // Command to get the data
//    private static final String DATA_COMMAND = "azd template list --filter java";
//
//    private final Project project;
//    private final ToolWindow toolWindow;
//    private JPanel contentPanel;
//    private JTextField commandTextField;
//    private JBTable toolItemsTable;
//    private ToolItemTableModel tableModel;
//    private ConsoleView consoleView;
//
//    public AzdToolWindowContent(Project project, ToolWindow toolWindow) {
//        this.project = project;
//        this.toolWindow = toolWindow;
//        createUIComponents();
//        loadData();
//    }
//
//    private void createUIComponents() {
//        // Create main content panel
//        contentPanel = new JPanel(new BorderLayout());
//
//        // Create the splitter panel for table and console
//        JBSplitter splitter = new JBSplitter(true, 0.7f);
//
//        // Create the top panel with table and command input
//        JPanel topPanel = new JPanel(new BorderLayout());
//
//        // Create table with tool items
//        tableModel = new ToolItemTableModel(new ArrayList<>());
//        toolItemsTable = new JBTable(tableModel);
//        toolItemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//
//        // Set column widths
//        TableColumn nameColumn = toolItemsTable.getColumnModel().getColumn(0);
//        nameColumn.setPreferredWidth(100);
//        TableColumn descColumn = toolItemsTable.getColumnModel().getColumn(1);
//        descColumn.setPreferredWidth(300);
//
//        JBScrollPane tableScrollPane = new JBScrollPane(toolItemsTable);
//        topPanel.add(tableScrollPane, BorderLayout.CENTER);
//
//        // Create command panel with text field and button
//        JPanel commandPanel = new JPanel(new BorderLayout());
//        commandPanel.setBorder(JBUI.Borders.empty(5));
//
//        commandTextField = new JTextField();
//        JButton runButton = new JButton("Run");
//        commandPanel.add(commandTextField, BorderLayout.CENTER);
//        commandPanel.add(runButton, BorderLayout.EAST);
//
//        topPanel.add(commandPanel, BorderLayout.SOUTH);
//
//        // Create console panel
//        consoleView = new ConsoleViewImpl(project, true);
//        JPanel consolePanel = new JPanel(new BorderLayout());
//        consolePanel.add(consoleView.getComponent(), BorderLayout.CENTER);
//
//        // Add console toolbar
//        DefaultActionGroup actionGroup = new DefaultActionGroup();
//        actionGroup.add(new RunToolAction(this));
//        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("AzdToolConsoleToolbar", actionGroup, false);
//        consolePanel.add(actionToolbar.getComponent(), BorderLayout.WEST);
//
//        // Add reload button to the top panel
//        JButton reloadButton = new JButton("Reload Data");
//        reloadButton.addActionListener(e -> loadData());
//        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        topButtonPanel.add(reloadButton);
//        topPanel.add(topButtonPanel, BorderLayout.NORTH);
//
//        // Add panels to splitter
//        splitter.setFirstComponent(topPanel);
//        splitter.setSecondComponent(consolePanel);
//
//        contentPanel.add(splitter, BorderLayout.CENTER);
//
//        // Add table selection listener
//        toolItemsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
//            @Override
//            public void valueChanged(ListSelectionEvent e) {
//                if (!e.getValueIsAdjusting()) {
//                    int selectedRow = toolItemsTable.getSelectedRow();
//                    if (selectedRow >= 0) {
//                        ToolItem selectedItem = tableModel.getItemAt(selectedRow);
//                        commandTextField.setText(selectedItem.getDescription());
//                    }
//                }
//            }
//        });
//
//        // Add run button action
//        runButton.addActionListener(new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                runCommand();
//            }
//        });
//    }
//
//    /**
//     * Load data from command execution
//     */
//    private void loadData() {
//        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Loading Tool Data", false) {
//            @Override
//            public void run(@NotNull ProgressIndicator indicator) {
//                indicator.setText("Executing command to load tool data...");
//
//                final List<ToolItem> items = ToolItem.createDataFromCommand(project, DATA_COMMAND);
//
//                ApplicationManager.getApplication().invokeLater(() -> {
//                    tableModel = new ToolItemTableModel(items);
//                    toolItemsTable.setModel(tableModel);
//
//                    // Reset column widths as they may be lost when model changes
//                    TableColumn nameColumn = toolItemsTable.getColumnModel().getColumn(0);
//                    nameColumn.setPreferredWidth(100);
//                    TableColumn descColumn = toolItemsTable.getColumnModel().getColumn(1);
//                    descColumn.setPreferredWidth(300);
//
//                    // If there are items, select the first one
//                    if (!items.isEmpty()) {
//                        toolItemsTable.getSelectionModel().setSelectionInterval(0, 0);
//                    }
//                });
//            }
//        });
//    }
//
//    public JPanel getContentPanel() {
//        return contentPanel;
//    }
//
//    public void runCommand() {
//        String command = commandTextField.getText().trim();
//        if (!command.isEmpty()) {
//            // Clear previous output
//            consoleView.clear();
//
//            // Print command
//            consoleView.print("> " + command + "\n", ConsoleViewContentType.USER_INPUT);
//
//            // Execute command and show result
//            ToolRunner.runTool(project, command, consoleView);
//        }
//    }
//
//    public String getCommand() {
//        return commandTextField.getText();
//    }
//
//    public Project getProject() {
//        return project;
//    }
//
//    public ConsoleView getConsoleView() {
//        return consoleView;
//    }


    private final Project project;
    private JPanel contentPanel;

    public AzdToolWindowContent(Project project, ToolWindow toolWindow) {
        this.project = project;
        createUIComponents(toolWindow);
    }

    private void createUIComponents(ToolWindow toolWindow) {
        contentPanel = new JBPanel<>(new BorderLayout());

        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new RowGridLayout(1, 0, JBUI.scale(2), CENTER));
        itemsPanel.setOpaque(false);

        JButton showPopupButton = new IconOnlyButton(AllIcons.Vcs.Clone);
        showPopupButton.addActionListener(e -> RunToolAction.showToolPopup(project, toolWindow.getComponent()));

        JButton initFromSource = new IconOnlyButton(AllIcons.Actions.Install);
        initFromSource.addActionListener(e -> RunToolAction.showConsolePopup(project, toolWindow.getComponent(), "azd init --from-code -e test" + RandomUtils.nextInt()));

        JButton provisionResources = new IconOnlyButton(AllIcons.Actions.Upload);
        provisionResources.addActionListener(e -> RunToolAction.showConsolePopup(project, toolWindow.getComponent(), "azd provision --no-prompt"));

        JButton deployResources = new IconOnlyButton(AllIcons.Actions.Execute);
        deployResources.addActionListener(e -> RunToolAction.showConsolePopup(project, toolWindow.getComponent(), "azd deploy --no-prompt"));

        JButton provisionAndDeploy = new IconOnlyButton(AllIcons.Actions.RunAll);
        provisionAndDeploy.addActionListener(e -> RunToolAction.showConsolePopup(project, toolWindow.getComponent(), "azd up --no-prompt"));

        addButtonWrapper(itemsPanel, showPopupButton, "Initialize From Templates");
        addButtonWrapper(itemsPanel, initFromSource, "Initialize From Source");
        addButtonWrapper(itemsPanel, provisionResources, "Provision Azure Resources");
        addButtonWrapper(itemsPanel, deployResources, "Deploy to Azure");
        addButtonWrapper(itemsPanel, provisionAndDeploy, "Provision & Deploy to Azure");

        JBPanel<JBPanel<?>> centerPanel = new JBPanel<>(new BorderLayout());
        centerPanel.add(itemsPanel, BorderLayout.CENTER);

        contentPanel.add(centerPanel, BorderLayout.CENTER);
    }

    private void addButtonWrapper(JPanel panel, JButton button, String label) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setOpaque(false);

        // Create a label for the text
        JLabel textLabel = new JLabel(label);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLabel.setFont(JBUI.Fonts.create(SANS_SERIF, 14));

        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        itemPanel.add(button);
        itemPanel.add(textLabel);

        itemPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(itemPanel);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
//        JSeparator jSeparator = new JSeparator(SwingConstants.HORIZONTAL);
//        panel.add(jSeparator);
//        panel.add(Box.createVerticalStrut(5)); // Small gap between icons
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }


    // Icon-only button with rounded square shape
    private static class IconOnlyButton extends JButton {
        private static final int BUTTON_SIZE = 50; // Fixed size for the button
        private static final int ARC_SIZE = 10;

        private final Icon icon;

        public IconOnlyButton(Icon icon) {
            this.icon = icon;

            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);

            // Fixed size
            setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
            setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
            setMaximumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Button background based on state
            if (getModel().isPressed()) {
                g2.setColor(JBColor.GRAY);
            } else if (getModel().isRollover()) {
                g2.setColor(JBColor.LIGHT_GRAY);
            } else {
                g2.setColor(new JBColor(new Color(240, 240, 240, 200), new Color(60, 63, 65)));
            }

            // Draw rounded square for the button
            g2.fill(new RoundRectangle2D.Double(0, 0, BUTTON_SIZE - 1, BUTTON_SIZE - 1, ARC_SIZE, ARC_SIZE));

            // Draw the icon in the center
            if (icon != null) {
                int iconWidth = icon.getIconWidth();
                int iconHeight = icon.getIconHeight();
                int iconX = (BUTTON_SIZE - iconWidth) / 2;
                int iconY = (BUTTON_SIZE - iconHeight) / 2;
                icon.paintIcon(this, g2, iconX, iconY);
            }

            g2.dispose();
        }
    }
}
