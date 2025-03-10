package com.microsoft.azure.toolkit.intellij.java.sdk.ai;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AskAzureChatAction extends AnAction {
    private static final Icon ICON = IconLoader.getIcon("/icons/Common/AskAzure.svg", AskAzureChatAction.class);

    public AskAzureChatAction() {
        super("Open Chat", "Open chat window", ICON);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        // Get the tool window
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow("Ask Azure");

        if (toolWindow != null) {
            // Show and activate the tool window
            toolWindow.show(null);
        }
    }
}
