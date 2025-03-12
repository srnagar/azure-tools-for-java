package com.microsoft.azure.toolkit.intellij.java.sdk.azd;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TilesToolWindowPanel extends JPanel {
    private final Project project;
    private final ToolWindow toolWindow;
    private JBPopup tilesPopup;

    public TilesToolWindowPanel(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;

        setLayout(new BorderLayout());

        // Add a button or label to the tool window that will show the popup
        JLabel showTilesLabel = new JLabel("Show Tiles", JLabel.CENTER);
        showTilesLabel.setFont(JBUI.Fonts.label().asBold());
        showTilesLabel.setBorder(JBUI.Borders.empty(10));
        showTilesLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showTilesLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                RunToolAction.showToolPopup(project, toolWindow.getComponent());
            }
        });

        add(showTilesLabel, BorderLayout.CENTER);

        // Also listen for tool window activation to show the popup automatically
        toolWindow.getContentManager().addContentManagerListener(new ContentManagerAdapter() {
            @Override
            public void selectionChanged(ContentManagerEvent event) {
                if (event.getOperation() == ContentManagerEvent.ContentOperation.add) {
                    RunToolAction.showToolPopup(project, toolWindow.getComponent());
                }
            }
        });
    }
}
