/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.marker;

import com.intellij.psi.PsiElement;
import com.microsoft.azure.toolkit.intellij.connector.Connection;
import com.microsoft.azure.toolkit.intellij.connector.ResourceDefinition;
import com.microsoft.azure.toolkit.intellij.connector.Utils;
import com.microsoft.azure.toolkit.intellij.connector.spring.SpringSupported;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class YamlResourceConnectionLineMarkerProvider extends AbstractResourceConnectionLineMarkerProvider {

    @Override
    protected boolean shouldAccept(@Nonnull PsiElement element) {
        return element instanceof YAMLPlainTextImpl;
    }

    @Override
    protected Connection<? extends AzResource, ?> getConnectionForPsiElement(@Nonnull PsiElement element) {
        final Connection<? extends AzResource, ?> connection = Utils.getConnectionForYamlPlainText((YAMLPlainTextImpl) element);
        if (Objects.isNull(connection)) {
            return null;
        }
        final ResourceDefinition<? extends AzResource> definition = connection.getResource().getDefinition();
        final List<Pair<String, String>> variables = definition instanceof SpringSupported<? extends AzResource> ?
                ((SpringSupported<? extends AzResource>) definition).getSpringProperties() : Collections.emptyList();
        final String property = YAMLUtil.getConfigFullName((YAMLPlainTextImpl) element);
        return CollectionUtils.isNotEmpty(variables) && StringUtils.equalsIgnoreCase(variables.get(0).getKey(), property) ? connection : null;
    }
}
