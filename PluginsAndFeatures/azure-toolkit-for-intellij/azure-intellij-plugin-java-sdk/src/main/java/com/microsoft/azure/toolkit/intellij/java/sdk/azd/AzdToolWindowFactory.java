package com.microsoft.azure.toolkit.intellij.java.sdk.azd;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class AzdToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        AzdToolWindowContent toolWindowContent = new AzdToolWindowContent(project, toolWindow);
        Content content = ContentFactory.getInstance().createContent(
                toolWindowContent.getContentPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public static List<AzdTemplate> readFromGitHub(String githubUrl) {
        try {
            // Create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper()
                    .setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
                    .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // Read JSON from URL directly into the Repository model
            return objectMapper.readValue(new URL(githubUrl), new TypeReference<List<AzdTemplate>>() {});
        } catch (IOException e) {
            System.err.println("Error reading JSON from GitHub URL: " + e.getMessage());
            return null;
        }
    }
}
