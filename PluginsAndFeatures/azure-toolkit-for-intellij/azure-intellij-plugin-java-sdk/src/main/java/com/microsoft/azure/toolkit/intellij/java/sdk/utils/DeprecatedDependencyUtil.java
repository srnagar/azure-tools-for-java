package com.microsoft.azure.toolkit.intellij.java.sdk.utils;

import com.microsoft.azure.toolkit.intellij.java.sdk.models.DeprecatedDependency;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contains the mapping for outdated dependencies and it's replacement.
 */
public class DeprecatedDependencyUtil {

    // TODO: we need a scalable way to track replacements for outdated dependencies but this matches the recommendations in Maven plugin
    private static final String TRACK_ONE_GROUP_ID = "com.microsoft.azure";
    private static final String TRACK_TWO_GROUP_ID = "com.azure";

    // This map is for all com.microsoft.* group IDs, mapping them into their com.azure equivalents
    private static final Map<String, List<String>> TRACK_ONE_REDIRECTS = new HashMap<>();
    static {
        // Cosmos
        TRACK_ONE_REDIRECTS.put("azure-cosmosdb", Collections.singletonList("azure-cosmos"));

        // Key Vault - Track 1 KeyVault library is split into three Track 2 libraries
        TRACK_ONE_REDIRECTS.put("azure-keyvault",
                Arrays.asList("azure-security-keyvault-keys",
                        "azure-security-keyvault-certificates",
                        "azure-security-keyvault-secrets"));

        // Blob Storage
        TRACK_ONE_REDIRECTS.put("azure-storage-blob", Collections.singletonList("azure-storage-blob"));

        // Event Hubs
        TRACK_ONE_REDIRECTS.put("azure-eventhubs", Collections.singletonList("azure-messaging-eventhubs"));
        TRACK_ONE_REDIRECTS.put("azure-eventhubs-eph", Collections.singletonList("azure-messaging-eventhubs-checkpointstore-blob"));

        // Service Bus
        TRACK_ONE_REDIRECTS.put("azure-servicebus", Collections.singletonList("azure-messaging-servicebus"));

        // Event Grid
        TRACK_ONE_REDIRECTS.put("azure-eventgrid", Collections.singletonList("azure-messaging-eventgrid"));

        // Log Analytics
        TRACK_ONE_REDIRECTS.put("azure-loganalytics", Collections.singletonList("azure-monitor-query"));
    }


    /**
     * This method will look to see if we have any recorded guidance on how to replace the given artifact with
     * something else.
     * @param groupId The groupId of the artifact
     * @param artifactId the artifactId of the artifact
     * @return the outdated dependency type with suggested replacement, if any.
     */
    public static Optional<DeprecatedDependency> lookupReplacement(String groupId, String artifactId) {

        if (TRACK_ONE_GROUP_ID.equals(groupId)) {
            if (TRACK_ONE_REDIRECTS.containsKey(artifactId)) {
                final List<String> newArtifactIds = TRACK_ONE_REDIRECTS.get(artifactId);

                List<String> newGavs = newArtifactIds.stream()
                        .map(newArtifactId -> TRACK_TWO_GROUP_ID + ":" + newArtifactId + ":" + MavenUtils.getLatestArtifactVersion(TRACK_TWO_GROUP_ID, newArtifactId)).collect(Collectors.toList());
                return Optional.of(new DeprecatedDependency(groupId + ":" + artifactId, newGavs));
            } else {
                // we've hit artifact location where we don't know know if the com.microsoft.azure artifact has newer
                // replacement...For now we will not give artifact failure
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
