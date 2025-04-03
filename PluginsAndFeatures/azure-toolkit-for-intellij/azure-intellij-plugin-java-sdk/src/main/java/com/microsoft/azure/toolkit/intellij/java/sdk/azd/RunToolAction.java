package com.microsoft.azure.toolkit.intellij.java.sdk.azd;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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
                .setMinSize(new Dimension(1400, 800))
                .setCancelOnClickOutside(false)
                .setCancelOnWindowDeactivation(false)
                .setCancelButton(new IconButton("Close", AllIcons.Actions.Close, AllIcons.Actions.CloseHovered))
                .setCancelCallback(() -> new CloseConfirmationDialog(project).showAndGet())
                .createPopup();

        popupPanel.setPopup(popup);

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor != null) {
            popup.showInBestPositionFor(editor);
        } else {
//            popup.showInFocusCenter();
            popup.showCenteredInCurrentWindow(project);
        }
    }

    public static void showConsolePopup(Project project, Component component, String command) {
        AzdConsolePopupPanel popupPanel = new AzdConsolePopupPanel(project);
        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(popupPanel, null)
                .setTitle("Running " + command)
                .setResizable(true)
                .setMovable(true)
                .setRequestFocus(true)
                .setCancelOnClickOutside(false)
                .setCancelOnWindowDeactivation(false)
                .setCancelButton(new IconButton("Close", AllIcons.Actions.Close, AllIcons.Actions.CloseHovered))
                .setCancelCallback(() -> new CloseConfirmationDialog(project).showAndGet())
                .setMinSize(new Dimension(1400, 800))
                .createPopup();

        popupPanel.setPopup(popup);

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor != null) {
            popup.showInBestPositionFor(editor);
        } else {
            popup.showInFocusCenter();
        }

        popupPanel.runCommand(command);
    }

    /**
     * Dialog to show confirmation for running a command
     */
    private static class CloseConfirmationDialog extends DialogWrapper {

        public CloseConfirmationDialog(Project project) {
            super(project, false);
            setTitle("Exit AZD Window");
            init();
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("Are you sure?"), BorderLayout.CENTER);
            return panel;
        }

        @Override
        protected void doOKAction() {
            super.doOKAction();
        }
    }
}
