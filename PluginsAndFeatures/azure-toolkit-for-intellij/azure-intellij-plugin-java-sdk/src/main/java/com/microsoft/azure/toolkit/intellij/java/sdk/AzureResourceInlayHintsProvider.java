package com.microsoft.azure.toolkit.intellij.java.sdk;

import com.intellij.codeInsight.hints.ChangeListener;
import com.intellij.codeInsight.hints.FactoryInlayHintsCollector;
import com.intellij.codeInsight.hints.ImmediateConfigurable;
import com.intellij.codeInsight.hints.InlayHintsCollector;
import com.intellij.codeInsight.hints.InlayHintsProvider;
import com.intellij.codeInsight.hints.InlayHintsSink;
import com.intellij.codeInsight.hints.InlayPresentationFactory;
import com.intellij.codeInsight.hints.NoSettings;
import com.intellij.codeInsight.hints.SettingsKey;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.OnClickPresentation;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.codeInsight.hints.presentation.WithCursorOnHoverPresentation;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiConstructorCall;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class AzureResourceInlayHintsProvider implements InlayHintsProvider<NoSettings> {

    private static final Icon ICON = IconLoader.getIcon("/icons/Common/Azure.svg", AzureResourceInlayHintsProvider.class);
    private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
    private static final InlayPresentationFactory.HoverListener HOVER_LISTENER = new InlayPresentationFactory.HoverListener() {
        @Override
        public void onHover(@Nonnull MouseEvent mouseEvent, @Nonnull Point point) {
            mouseEvent.getComponent().setCursor(HAND_CURSOR);
        }

        @Override
        public void onHoverFinished() {

        }
    };

    @Nullable
    @Override
    public InlayHintsCollector getCollectorFor(@Nonnull PsiFile psiFile, @Nonnull Editor editor, @Nonnull NoSettings noSettings, @Nonnull InlayHintsSink inlayHintsSink) {
        return new FactoryInlayHintsCollector(editor) {
            @Override
            public boolean collect(@Nonnull PsiElement psiElement, @Nonnull Editor editor, @Nonnull InlayHintsSink inlayHintsSink) {
                if(psiElement instanceof PsiConstructorCall) {
                    int endOffset = psiElement.getTextRange().getEndOffset();
                    InlayPresentation iconPresentation = new PresentationFactory(editor).icon(ICON);
                    InlayPresentationFactory.ClickListener azureClickListener = (mouseEvent, point) -> {
                        AzureResourceListWindow.showPopup(psiElement.getText(), mouseEvent.getComponent());
                    };
                    OnClickPresentation azureClickPresentation = new OnClickPresentation(iconPresentation, azureClickListener);
                    WithCursorOnHoverPresentation presentation = new WithCursorOnHoverPresentation(azureClickPresentation, HAND_CURSOR, editor);
                    inlayHintsSink.addInlineElement(endOffset, true, presentation, true);
                }
                return true;
            }
        };
    }

    @Nonnull
    @Override
    public NoSettings createSettings() {
        return new NoSettings();
    }

    @Nonnull
    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) String getName() {
        return "old";
    }

    @Nonnull
    @Override
    public SettingsKey<NoSettings> getKey() {
        return new SettingsKey<>("old.end.of.line.icon");
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return "previewtext";
    }

    @Nonnull
    @Override
    public ImmediateConfigurable createConfigurable(@Nonnull NoSettings noSettings) {
        return new ImmediateConfigurable() {
            @Nonnull
            @Override
            public JComponent createComponent(@Nonnull ChangeListener changeListener) {
                JButton jButton = new JButton();
                jButton.setIcon(AllIcons.General.ArrowDown);
                return jButton;
            }
        };
    }
}
