package com.microsoft.azure.toolkit.intellij.java.sdk.azd;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class AzdConsolePopupPanel extends JPanel {

    private final Project project;
    private JBPopup popup;
    private ConsoleView consoleView;

    public AzdConsolePopupPanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));

        consoleView = new ConsoleViewImpl(project, false);
        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.add(consoleView.getComponent(), BorderLayout.CENTER);
        consolePanel.setPreferredSize(new Dimension(-1, 780));
        consolePanel.setBorder(JBUI.Borders.empty(10, 0, 0, 0));
        add(consolePanel, BorderLayout.SOUTH);
    }

    /**
     * Run a command and show the output
     */
    public void runCommand(String command) {
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
}
