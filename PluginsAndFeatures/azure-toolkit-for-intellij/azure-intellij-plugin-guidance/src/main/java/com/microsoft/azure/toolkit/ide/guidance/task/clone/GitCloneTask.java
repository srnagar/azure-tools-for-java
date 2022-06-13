package com.microsoft.azure.toolkit.ide.guidance.task.clone;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.ide.impl.ProjectUtil;
import com.microsoft.azure.toolkit.ide.guidance.Guidance;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceConfigManager;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceTask;
import com.microsoft.azure.toolkit.ide.guidance.task.TaskContext;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import static com.microsoft.azure.toolkit.ide.guidance.GuidanceConfigManager.GETTING_START_CONFIGURATION_NAME;

public class GitCloneTask implements GuidanceTask {
    public static final String DIRECTORY = "directory";
    private final Guidance guidance;
    private final TaskContext context;

    public GitCloneTask(@Nonnull TaskContext context) {
        this.context = context;
        this.guidance = context.getGuidance();
    }


    @Override
    public boolean isDone() {
        // Check whether project was clone to local
        final File file = new File(guidance.getProject().getBasePath(), GETTING_START_CONFIGURATION_NAME);
        return file.exists();
    }

    @Override
    public void execute() throws Exception {
        final String projectPath = (String) context.getParameter(DIRECTORY);
        final String gitUrl = "https://github.com/spring-guides/gs-spring-boot.git";
        try {
            Git.cloneRepository()
                    .setURI(gitUrl)
                    .setDirectory(Paths.get(projectPath).toFile())
                    .call();
            // Copy get start file to path
            copyConfigurationToWorkspace(projectPath);
            ProjectUtil.openOrImport(Paths.get(projectPath, "complete"), OpenProjectTask.newProject());
        } catch (final Exception ex) {
            AzureMessager.getMessager().error(ex);
            throw new AzureToolkitRuntimeException(ex);
        }
    }

    private void copyConfigurationToWorkspace(final String projectPath) throws IOException {
        try (final InputStream inputStream = GuidanceConfigManager.class.getResourceAsStream(guidance.getUri())) {
            final File complete = new File(projectPath, "complete");
            FileUtils.copyInputStreamToFile(inputStream, new File(complete, GuidanceConfigManager.GETTING_START_CONFIGURATION_NAME));
        } catch (final IOException e) {
            throw e;
        }
    }
}
