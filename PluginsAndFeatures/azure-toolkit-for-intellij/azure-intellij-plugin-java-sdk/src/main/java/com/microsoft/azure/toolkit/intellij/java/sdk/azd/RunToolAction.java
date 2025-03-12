package com.microsoft.azure.toolkit.intellij.java.sdk.azd;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class RunToolAction extends AnAction {
    private Project project;

    public RunToolAction() {
        super("Show Tools", "Show available tools", null);
    }

    public RunToolAction(Project project) {
        super("Show Tools", "Show available tools", null);
        this.project = project;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        showToolPopup(project, e.getInputEvent().getComponent());
    }

    /**
     * Shows the tool popup
     */
    public static void showToolPopup(Project project, Component component) {
        AzdToolTilePopupPanel popupPanel = new AzdToolTilePopupPanel(project);

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(popupPanel, null)
                .setTitle("Available AZD templates for Java")
                .setResizable(true)
                .setMovable(true)
                .setRequestFocus(true)
                .setMinSize(new java.awt.Dimension(1400, 800))
                .createPopup();

        popupPanel.setPopup(popup);

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor != null) {
            popup.showInBestPositionFor(editor);
        } else {
            popup.showInFocusCenter();
        }
    }
}
