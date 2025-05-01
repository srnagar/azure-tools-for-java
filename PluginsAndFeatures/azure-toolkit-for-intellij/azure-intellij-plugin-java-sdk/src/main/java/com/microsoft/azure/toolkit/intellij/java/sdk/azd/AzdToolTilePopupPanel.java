package com.microsoft.azure.toolkit.intellij.java.sdk.azd;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.BrowserUtil;
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
import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AzdToolTilePopupPanel extends JPanel {
    private Color activeColor = new Color(100, 150, 255);
    private Color inactiveColor = Color.GRAY;
    private final Project project;
    private JBPopup popup;
    private ConsoleView consoleView;
    private List<AzdTemplate> templates = new ArrayList<>();
    private List<JToggleButton> tagButtons = new ArrayList<>();

    public AzdToolTilePopupPanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));
        this.templates = AzdToolWindowFactory.readFromGitHub("https://raw.githubusercontent.com/Azure/awesome-azd/refs/heads/main/website/static/templates.json");

        List<String> allTags = topKTags(templates, 10);
        templates.stream()
                .filter(template -> template.getTags().contains("java"))
                .flatMap(template -> template.getTags().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        JPanel tilesPanel = new JPanel();
        JPanel filterTagsPanel = new JPanel(new GridLayout(0, 10, JBUI.scale(10), JBUI.scale(10)));

        // Create scroll pane
        addFilters(filterTagsPanel, tilesPanel, allTags);

        JScrollPane tagsScrollPane = ScrollPaneFactory.createScrollPane(filterTagsPanel);
        add(tagsScrollPane, BorderLayout.NORTH);

        // Create a scroll pane for the tiles
        tilesPanel.setLayout(new GridLayout(0, 3, JBUI.scale(10), JBUI.scale(10)));

//        tilesPanel.setLayout(new RowGridLayout(3, 0, JBUI.scale(10), CENTER));
        tilesPanel.setBorder(JBUI.Borders.empty(10));

        // Add a label explaining the panel
//        JBLabel instructionLabel = new JBLabel("Click on a tile to select and run a tool");
//        instructionLabel.setBorder(JBUI.Borders.empty(0, 0, 10, 0));
//        add(instructionLabel, BorderLayout.NORTH);

        // Add scroll pane with tiles panel
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(tilesPanel);
        scrollPane.setBorder(JBUI.Borders.empty());
        add(scrollPane, BorderLayout.CENTER);

        // Initialize console view for output
        consoleView = new ConsoleViewImpl(project, false);
        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.add(consoleView.getComponent(), BorderLayout.CENTER);
        consolePanel.setPreferredSize(new Dimension(-1, 250));
        consolePanel.setBorder(JBUI.Borders.empty(10, 0, 0, 0));
        add(consolePanel, BorderLayout.SOUTH);

        // Load data
        loadData(tilesPanel, Collections.emptyList());
    }

    private void addFilters(JPanel filterTagsPanel, JPanel tilesPanel, List<String> allTags) {
        allTags.forEach(tag -> {
            JToggleButton toggleButton = new JToggleButton(tag, tag.equals("java")) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

                    // Set background based on selection
                    Color bgColor = isSelected() ? activeColor : inactiveColor;
                    g2.setColor(bgColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                    // Set text color
                    g2.setColor(isSelected() ? Color.WHITE : Color.BLACK);

                    // Draw text
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(getText())) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(getText(), x, y);

                    g2.dispose();
                }
            };

            // Customize button appearance
            toggleButton.setMargin(new Insets(2, 5, 2, 5)); // Compact margins
            toggleButton.setFont(toggleButton.getFont().deriveFont(14f)); // Small font
            toggleButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<String> tags = tagButtons.stream()
                            .filter(toggleButton -> toggleButton.isSelected())
                            .map(toggleButton -> toggleButton.getText())
                            .collect(Collectors.toUnmodifiableList());
                    loadData(tilesPanel, tags);
                }
            });
            filterTagsPanel.add(toggleButton);
            tagButtons.add(toggleButton);
        });

    }

    /**
     * Load data from command execution and create tiles
     */
    private void loadData(JPanel tilesPanel, List<String> tags) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Loading Tool Data", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText("Executing command to load tool data...");

//                final List<ToolItem> items = ToolItem.createDataFromCommand(project, DATA_COMMAND);
//                final List<ToolItem> items = templates.stream()
//                        .filter(template -> template.getLanguages() != null && template.getLanguages().contains("java"))
//                        .filter(template -> tags == null || tags.isEmpty() || template.getTags().containsAll(tags))
//                        .map(template -> new ToolItem(template.getTitle(), template.getAuthorUrl(), template.getDescription(), "azd init -t " + template.getSource()))
//                        .collect(Collectors.toUnmodifiableList());

                List<AzdTemplate> javaTemplates = templates.stream()
                        .filter(template -> template.getLanguage() != null && template.getLanguage().contains("java"))
                        .collect(Collectors.toUnmodifiableList());

                List<AzdTemplate> tagTemplates = javaTemplates.stream()
                        .filter(template -> tags == null || tags.isEmpty() || template.getTags().containsAll(tags))
                        .collect(Collectors.toUnmodifiableList());

                List<ToolItem> items = tagTemplates
                        .stream()
                        .map(template -> new ToolItem(template.getTitle(), template.getAuthorUrl(), template.getDescription(), "azd init -t " + template.getSource()))
                        .collect(Collectors.toUnmodifiableList());

                ApplicationManager.getApplication().invokeLater(() -> {
                    tilesPanel.removeAll();

                    for (ToolItem item : items) {
                        tilesPanel.add(createToolTile(item));
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
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(descLabel);
        descriptionPanel.add(scrollPane, BorderLayout.CENTER);

        // Repo link below description
        HyperlinkLabel link = new HyperlinkLabel(item.getRepoLink());
        descLabel.setBorder(JBUI.Borders.empty(5, 0));
        link.addHyperlinkListener(e -> BrowserUtil.browse(((HyperlinkLabel) ((HyperlinkEvent) e).getSource()).getText()));
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
        ToolRunner.runTool(project, command  + " -s faa080af-c1d8-40ad-9cce-e1a450ca5b57 -e test" + RandomUtils.nextInt() , consoleView);
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

    public static List<String> topKTags(List<AzdTemplate> input, int k) {
        // Count occurrences
        Map<String, Integer> frequencyMap = new HashMap<>();
        List<String> tags = input.stream()
                .filter(template -> template.getLanguage() != null && template.getLanguage().contains("java"))
                .flatMap(template -> template.getTags().stream())
                .collect(Collectors.toUnmodifiableList());

        for (String str : tags) {
            frequencyMap.put(str, frequencyMap.getOrDefault(str, 0) + 1);
        }

        // Sort by frequency in descending order
        return frequencyMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
