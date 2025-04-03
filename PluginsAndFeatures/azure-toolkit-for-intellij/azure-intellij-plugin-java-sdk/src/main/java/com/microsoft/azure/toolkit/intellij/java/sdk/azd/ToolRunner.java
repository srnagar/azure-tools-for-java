package com.microsoft.azure.toolkit.intellij.java.sdk.azd;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class to run CLI tools and capture output
 */
public class ToolRunner {
    private static final Logger LOG = Logger.getInstance(ToolRunner.class);

    public static void runTool(Project project, String command, ConsoleView consoleView) {

        String updatedCommand = command;
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

                // Flag to track if we've seen the confirmation prompt
                AtomicBoolean confirmationSent = new AtomicBoolean(false);

                processHandler.addProcessListener(new ProcessAdapter() {

                    private StringBuilder outputBuffer = new StringBuilder();

                    @Override
                    public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                        String text = event.getText();
                        if (outputType == ProcessOutputTypes.STDOUT) {
//                            consoleView.print(text, ConsoleViewContentType.NORMAL_OUTPUT);

                            // Add to buffer for confirmation detection
                            outputBuffer.append(text);

                            // Check if the text contains the confirmation prompt
                            if (!confirmationSent.get() &&
                                    outputBuffer.toString().contains("Continue initializing an app")) {

                                // Send confirmation input
                                try {
                                    sendInput(processHandler, "y\n");

                                    // Log and show the automatic confirmation
                                    String message = "[Auto-confirmed: 'y']\n";
                                    consoleView.print(message, ConsoleViewContentType.USER_INPUT);
                                    LOG.info("Auto-confirmed process with 'y'");

                                    // Set flag to avoid sending multiple times
                                    confirmationSent.set(true);

                                    // Reset buffer after handling
                                    outputBuffer = new StringBuilder();
                                } catch (IOException e) {
                                    LOG.error("Error sending automatic confirmation: " + e.getMessage(), e);
                                    consoleView.print("Error sending automatic confirmation: " +
                                                    e.getMessage() + "\n",
                                            ConsoleViewContentType.ERROR_OUTPUT);
                                }
                            } else if(!confirmationSent.get()
                                    && outputBuffer.toString().contains("host your app on Azure using Azure Container Apps")) {

                                // Send confirmation input
                                try {
                                    sendInput(processHandler, "Confirm and continue initializing my app\n");
                                    // Reset buffer after handling
                                    outputBuffer = new StringBuilder();
                                    // Set flag to avoid sending multiple times
                                    confirmationSent.set(true);
                                } catch (IOException e) {
                                    LOG.error("Error sending automatic confirmation: " + e.getMessage(), e);
                                    consoleView.print("Error sending automatic confirmation: " +
                                                    e.getMessage() + "\n",
                                            ConsoleViewContentType.ERROR_OUTPUT);
                                }
                            }
                        } else if (outputType == ProcessOutputTypes.STDERR) {
                            consoleView.print(text, ConsoleViewContentType.ERROR_OUTPUT);
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

    /**
     * Send input to the running process
     */
    private static void sendInput(OSProcessHandler processHandler, String input) throws IOException {
        OutputStream outputStream = processHandler.getProcessInput();
        if (outputStream != null) {
            outputStream.write(input.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } else {
            throw new IOException("Process does not accept input");
        }
    }
}