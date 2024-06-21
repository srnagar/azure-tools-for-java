/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerapps.component;

import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.intellij.common.AzureDialog;
import com.microsoft.azure.toolkit.intellij.common.AzureIntegerInput;
import com.microsoft.azure.toolkit.intellij.common.AzureTextInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.containerapps.model.WorkloadProfile;
import com.microsoft.azure.toolkit.lib.containerapps.model.WorkloadProfileType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.*;

public class WorkloadProfileCreationDialog extends AzureDialog<WorkloadProfile> implements AzureForm<WorkloadProfile> {
    public static final String MINIUM_COUNT_LESS_WARNING = "Setting minimum instance count to less than 3 is not recommended for production workloads due to redundancy concerns.";
    public static final String INVALID_MAXIMUM_COUNT_ERROR = "Maximum count should be greater than or equal to minimum count.";
    private JLabel lblName;
    private AzureTextInput txtName;
    private AzureComboBox<WorkloadProfileType> cbSize;
    private JPanel pnlRoot;
    private AzureIntegerInput txtMinimumCount;
    private JLabel lblMaximumCount;
    private AzureIntegerInput txtMaximumCount;
    private JLabel lblMinimumCount;

    private final String subscriptionId;
    private final Region region;

    public WorkloadProfileCreationDialog(String subscriptionId, Region region) {
        super();
        this.subscriptionId = subscriptionId;
        this.region = region;
        $$$setupUI$$$();
        this.init();
    }

    @Override
    protected void init() {
        super.init();
        this.cbSize.setUsePreferredSizeAsMinimum(false);

        this.txtMinimumCount.setMinValue(0);
        this.txtMinimumCount.setMaxValue(20);
        this.lblMinimumCount.setLabelFor(txtMinimumCount);
        this.txtMaximumCount.setMinValue(0);
        this.txtMaximumCount.setMaxValue(20);
        this.lblMaximumCount.setLabelFor(txtMaximumCount);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        this.cbSize = new WorkloadProfileTypeComboBox(subscriptionId, region);
    }

    @Override
    public List<AzureValidationInfo> validateAdditionalInfo() {
        final List<AzureValidationInfo> result = new ArrayList<>();
        final Integer min = txtMinimumCount.getValue();
        final Integer max = txtMaximumCount.getValue();
        if (Objects.nonNull(min) && min < 3) {
            result.add(AzureValidationInfo.warning(MINIUM_COUNT_LESS_WARNING, txtMinimumCount));
        }
        if (Objects.nonNull(max) && Objects.nonNull(min) && min > max) {
            result.add(AzureValidationInfo.error(INVALID_MAXIMUM_COUNT_ERROR, txtMaximumCount));
        }
        return result;
    }

    @Override
    public WorkloadProfile getValue() {
        return WorkloadProfile.builder()
                .name(txtName.getValue())
                .maximumCount(txtMaximumCount.getValue())
                .minimumCount(txtMinimumCount.getValue())
                .workloadProfileType(Objects.requireNonNull(cbSize.getValue()).getName()).build();
    }

    @Override
    public void setValue(@Nonnull final WorkloadProfile data) {
        Optional.ofNullable(data.getName()).ifPresent(txtName::setValue);
        Optional.ofNullable(data.getWorkloadProfileType()).ifPresent(type ->
                cbSize.setValue(profile -> StringUtils.equalsIgnoreCase(profile.getName(), type)));
        Optional.ofNullable(data.getMaximumCount()).ifPresent(txtMaximumCount::setValue);
        Optional.ofNullable(data.getMinimumCount()).ifPresent(txtMinimumCount::setValue);
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Arrays.asList(txtName, cbSize, txtMaximumCount, txtMinimumCount);
    }

    @Override
    public AzureForm<WorkloadProfile> getForm() {
        return this;
    }

    @Nonnull
    @Override
    protected String getDialogTitle() {
        return "New Workload Profile";
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return pnlRoot;
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    private void $$$setupUI$$$() {
    }
}