package com.microsoft.azure.toolkit.intellij.azuremcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.copilot.api.mcp.ExtInstalledMcpServerConfiguration;
import com.github.copilot.api.mcp.ExtInstalledMcpServerConfigurationItem;
import com.github.copilot.api.mcp.McpServerProvider;
import kotlin.coroutines.Continuation;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.azure.toolkit.intellij.azuremcp.AzureMcpUtils.logErrorTelemetryEvent;
import static com.microsoft.azure.toolkit.intellij.azuremcp.AzureMcpUtils.logTelemetryEvent;

@Slf4j
public class GitHubCopilotMcpProvider implements McpServerProvider {
    private static final String AZURE_MCP_SERVER_NAME = "Azure MCP Server IntelliJ";
    private static final String AZURE_MCP_SERVER_DESCRIPTION =
            "Azure MCP Server provides context-aware AI tools for working with Azure resources";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Nullable
    @Override
    public Object getConfigs(@NotNull Continuation<? super ExtInstalledMcpServerConfiguration> continuation) {
        try {
            log.info("Getting Azure MCP Server configuration for GitHub Copilot");
            logTelemetryEvent("azmcp-copilot-provider-getconfigs");

            final AzureMcpPackageManager packageManager = new AzureMcpPackageManager();
            final File azMcpExe = packageManager.getAzureMcpExecutable();

            if (azMcpExe != null && azMcpExe.exists()) {
                log.info("Azure MCP executable found at: {}", azMcpExe.getAbsolutePath());
                packageManager.cleanup();

                final String configString = buildConfigString(azMcpExe);
                logTelemetryEvent("azmcp-copilot-provider-success");

                return new ExtInstalledMcpServerConfiguration() {
                    @Nullable
                    @Override
                    public List<ExtInstalledMcpServerConfigurationItem> getConfigurationItems() {
                        return List.of((ExtInstalledMcpServerConfigurationItem) new AzureMcpServerConfigurationItem(configString));
                    }
                };
            }

            log.warn("Azure MCP executable not found, returning empty configuration");
            logTelemetryEvent("azmcp-copilot-provider-no-executable");
            return emptyConfiguration();
        } catch (final Exception e) {
            log.error("Error getting Azure MCP Server configuration: {}", e.getMessage(), e);
            logErrorTelemetryEvent("azmcp-copilot-provider-failed", e);
            return emptyConfiguration();
        }
    }

    private static ExtInstalledMcpServerConfiguration emptyConfiguration() {
        return new ExtInstalledMcpServerConfiguration() {
            @Nullable
            @Override
            public List<ExtInstalledMcpServerConfigurationItem> getConfigurationItems() {
                return null;
            }
        };
    }

    private String buildConfigString(@NotNull final File azMcpExe) throws JsonProcessingException {
        final Map<String, Object> config = new LinkedHashMap<>();
        config.put("type", "stdio");
        config.put("command", azMcpExe.getAbsolutePath());
        config.put("args", Arrays.asList("server", "start"));
        return OBJECT_MAPPER.writeValueAsString(config);
    }

    private static class AzureMcpServerConfigurationItem implements ExtInstalledMcpServerConfigurationItem {
        private final String configString;

        AzureMcpServerConfigurationItem(@NotNull final String configString) {
            this.configString = configString;
        }

        @NotNull
        @Override
        public String getMcpServerName() {
            return AZURE_MCP_SERVER_NAME;
        }

        @NotNull
        @Override
        public String getDescription() {
            return AZURE_MCP_SERVER_DESCRIPTION;
        }

        @NotNull
        @Override
        public String getConfigString() {
            return configString;
        }
    }
}
