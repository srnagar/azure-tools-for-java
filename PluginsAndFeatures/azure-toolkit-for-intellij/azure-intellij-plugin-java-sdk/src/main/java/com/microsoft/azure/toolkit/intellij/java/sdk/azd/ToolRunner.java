package com.microsoft.azure.toolkit.intellij.java.sdk.azd;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class to run CLI tools and capture output
 */
public class ToolRunner {

    public static void runTool(Project project, String command, ConsoleView consoleView) {

        String updatedCommand = command + " -e test -s faa080af-c1d8-40ad-9cce-e1a450ca5b57";
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                // Create command line
                GeneralCommandLine commandLine = new GeneralCommandLine();
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    commandLine.setExePath("cmd.exe");
                    commandLine.addParameter("/c");
                } else {
                    commandLine.setExePath("/bin/sh");
                    commandLine.addParameter("-c");
                }
                commandLine.addParameter(updatedCommand);
                commandLine.setWorkDirectory(project.getBasePath());

                // Create process handler
                OSProcessHandler processHandler = new OSProcessHandler(commandLine);
                processHandler.addProcessListener(new ProcessAdapter() {
                    @Override
                    public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                        if (outputType == ProcessOutputTypes.STDOUT) {
                            consoleView.print(event.getText(), ConsoleViewContentType.NORMAL_OUTPUT);
                        } else if (outputType == ProcessOutputTypes.STDERR) {
                            consoleView.print(event.getText(), ConsoleViewContentType.ERROR_OUTPUT);
                        }
                    }

                    @Override
                    public void processTerminated(@NotNull ProcessEvent event) {
                        consoleView.print("\nProcess completed with exit code " +
                                        event.getExitCode() + "\n",
                                ConsoleViewContentType.SYSTEM_OUTPUT);
                    }
                });

                // Attach console to process
                ApplicationManager.getApplication().invokeLater(() -> {
                    consoleView.attachToProcess(processHandler);
                    processHandler.startNotify();
                });

            } catch (Exception e) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    consoleView.print("Error running command: " + e.getMessage() + "\n",
                            ConsoleViewContentType.ERROR_OUTPUT);
                });
            }
        });
    }
}