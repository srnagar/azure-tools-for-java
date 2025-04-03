package com.microsoft.azure.toolkit.intellij.java.sdk.azd;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ToolItem {

    private static final Logger LOG = Logger.getInstance(ToolItem.class);
    private static final int COMMAND_TIMEOUT = 30000; // 30 seconds

    private static final Map<String, String> AZD_COMMAND_LOOKUP = new HashMap<>() {{
        put("Hello world Java Application on Azure Spring Apps", "azd init -t spring-guides/gs-spring-boot-for-azure");
        put("Azure Cosmos DB for Table Quickstart - Java", "azd init -t cosmos-db-table-java-quickstart");
        put("Azure Cosmos DB for NoSQL Quickstart - Java", "azd init -t cosmos-db-nosql-java-quickstart");
        put("Containerized React Web App with Java API and MongoDB", "azd init -t todo-java-mongo-aca");
        put("React Web App with Java API and MongoDB", "azd init -t todo-java-mongo");
        put("Spring PetClinic - Java Spring MySQL", "azd init -t spring-petclinic-java-mysql");
        put("Sprint Petclinic AI application on Azure Container Apps", "azd init -t spring-petclinic-ai");
        put("Spring ChatGPT Application using Azure OpenAI on Azure Spring Apps", "azd init -t spring-chatgpt-sample");
        put("Microservices App - Dapr PubSub Java AKS", "azd init -t pubsub-dapr-aks-java");
        put("Java Quarkus Apps on Azure Container Apps", "azd init -t java-on-aca-quarkus");
        put("Spring Petclinic Microservices with AI on Azure Container Apps", "azd init -t java-on-aca");
        put("Jakarta Java EE Cargo Tracker application with Open Liberty running on Azure Kubernetes Service (AKS)", "azd init -t cargotracker-liberty-aks-azd");
        put("Java - ChatGPT + Enterprise data with Azure OpenAI and AI Search", "azd init -t azure-search-openai-demo-java");
        put("Azure OpenAI RAG with Java, LangChain4j and Quarkus", "azd init -t azure-openai-rag-workshop-java");
        put("Azure Functions Java HTTP Trigger using Azure Developer CLI", "azd init -t azure-functions-java-flex-consumption-azd");
        put("Java Spring Apps with Azure OpenAI", "azd init -t app-templates-java-openai-springapps");
        put("SpringBoot OpenAI Chat on Azure App Service", "azd init -t springboot-petclinic-ai-chat-on-app-service");
        put("Static React Web App with Java API and PostgreSQL", "azd init -t asa-samples-web-application");
        put("Event Driven Java Application with Azure Service Bus on Azure Spring Apps", "azd init -t asa-samples-event-driven-application");
    }};

    private String name;
    private String description;
    private String repoLink;
    private String command;

    public ToolItem(String name, String repoLink, String description, String command) {
        this.name = name;
        this.description = description == null ? "For more details, see the link below" : description;
        this.repoLink = repoLink == null ? "https://azure.github.io/awesome-azd/" : repoLink;
        this.command = command;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRepoLink() {
        return repoLink;
    }

    public ToolItem setRepoLink(String repoLink) {
        this.repoLink = repoLink;
        return this;
    }

    public String getCommand() {
        return command;
    }

    public ToolItem setCommand(String command) {
        this.command = command;
        return this;
    }

    @Override
    public String toString() {
        return name;
    }

    public static List<ToolItem> createDataFromTemplatesJson() {
        List<AzdTemplate> azdTemplates = AzdToolWindowFactory
                .readFromGitHub("https://raw.githubusercontent.com/Azure/awesome-azd/refs/heads/main/website/static/templates.json");

        return azdTemplates.stream()
                .filter(template -> template.getTags().contains("java"))
                .map(template -> new ToolItem(template.getTitle(), template.getWebsite(), template.getDescription(), "azd init -t " + template.getSource()))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Creates data by executing a command and parsing its output
     * The command output should have a header line followed by data lines
     * Each line should contain columns, with the first column as the name
     * and the second column as the repository path (description)
     */
    public static List<ToolItem> createDataFromCommand(Project project, String command) {
        List<ToolItem> items = new ArrayList<>();

        try {
            // Create command line
            GeneralCommandLine commandLine = new GeneralCommandLine();
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                commandLine.setExePath("powershell.exe");
                commandLine.addParameter("-Command");
            } else {
                commandLine.setExePath("/bin/sh");
                commandLine.addParameter("-c");
            }
            commandLine.addParameter(command);
            commandLine.setWorkDirectory(project.getBasePath());

            // Execute command and capture output
            CapturingProcessHandler processHandler = new CapturingProcessHandler(commandLine);
            ProcessOutput output = processHandler.runProcess(COMMAND_TIMEOUT, true);

            if (output.getExitCode() == 0) {
                List<String> lines = output.getStdoutLines();

                // Skip if no output
                if (lines.isEmpty()) {
                    LOG.warn("Command execution returned no output");
                    return createFallbackData();
                }

                // Skip the header line
                boolean headerFound = false;
                for (String line : lines) {
                    if (line.contains("Repository Path")) {
                        headerFound = true;
                        continue;
                    } else if (!headerFound) {
                        continue;
                    }

                    // Split the line by tabs
                    String[] parts = line.trim().split("   ");
                    if (parts.length >= 2) {
                        String name = parts[0].trim();
                        String azdCommand = AZD_COMMAND_LOOKUP.get(name);
                        items.add(new ToolItem(name, null, null, azdCommand));
                    }
                }
            } else {
                LOG.warn("Command execution failed: " + output.getStderr());
                return createFallbackData();
            }
        } catch (Exception e) {
            LOG.error("Error executing command: " + e.getMessage(), e);
            return createFallbackData();
        }

        // If no items were created, return fallback data
        if (items.isEmpty()) {
            return createFallbackData();
        }

        return items;
    }

    /**
     * Fallback method for creating sample data when command execution fails
     */
    private static List<ToolItem> createFallbackData() {
        List<ToolItem> items = new ArrayList<>();
        items.add(new ToolItem("fallback-item-1", null, null,"/path/to/repository/1"));
        items.add(new ToolItem("fallback-item-2", null, null,"/path/to/repository/2"));
        items.add(new ToolItem("fallback-item-3", null, null,"/path/to/repository/3"));
        return items;
    }
}