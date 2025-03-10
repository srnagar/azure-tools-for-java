package com.microsoft.azure.toolkit.intellij.java.sdk.azd;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class RunToolAction extends AnAction {
    private AzdToolWindowContent toolWindowContent;

    // Constructor for action registration via plugin.xml
    public RunToolAction() {
        super("Run Custom Tool", "Run the selected custom CLI tool", null);
    }

    // Constructor for programmatic action creation
    public RunToolAction(AzdToolWindowContent toolWindowContent) {
        super("Run Custom Tool", "Run the selected custom CLI tool", null);
        this.toolWindowContent = toolWindowContent;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (toolWindowContent != null) {
            toolWindowContent.runCommand();
        }
    }
}
