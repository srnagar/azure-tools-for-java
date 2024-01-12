/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.webapp.runner.webappconfig.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeTooltipManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.HideableDecorator;
import com.intellij.ui.HyperlinkLabel;
import com.microsoft.azure.toolkit.intellij.common.AzureArtifact;
import com.microsoft.azure.toolkit.intellij.common.AzureArtifactComboBox;
import com.microsoft.azure.toolkit.intellij.common.AzureArtifactManager;
import com.microsoft.azure.toolkit.intellij.common.AzureFormPanel;
import com.microsoft.azure.toolkit.intellij.common.component.UIUtils;
import com.microsoft.azure.toolkit.intellij.legacy.appservice.AppServiceComboBox;
import com.microsoft.azure.toolkit.intellij.legacy.appservice.table.AppSettingsTable;
import com.microsoft.azure.toolkit.intellij.legacy.appservice.table.AppSettingsTableUtils;
import com.microsoft.azure.toolkit.intellij.legacy.common.AzureSettingPanel;
import com.microsoft.azure.toolkit.intellij.legacy.webapp.runner.webappconfig.IntelliJWebAppSettingModel;
import com.microsoft.azure.toolkit.intellij.legacy.webapp.runner.webappconfig.WebAppConfiguration;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.config.AppServiceConfig;
import com.microsoft.azure.toolkit.lib.appservice.config.DeploymentSlotConfig;
import com.microsoft.azure.toolkit.lib.appservice.config.RuntimeConfig;
import com.microsoft.azure.toolkit.lib.appservice.webapp.AzureWebApp;
import com.microsoft.azure.toolkit.lib.appservice.webapp.WebApp;
import com.microsoft.azure.toolkit.lib.appservice.webapp.WebAppDeploymentSlot;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azuretools.core.mvp.model.webapp.AzureWebAppMvpModel;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.idea.maven.model.MavenConstants;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

import static com.microsoft.azure.toolkit.intellij.common.AzureBundle.message;

public class WebAppDeployConfigurationPanel extends AzureSettingPanel<WebAppConfiguration> implements AzureFormPanel<IntelliJWebAppSettingModel> {
    private static final String[] FILE_NAME_EXT = {"war", "jar", "ear"};
    private static final String DEPLOYMENT_SLOT = "&Deployment Slot";
    private static final String DEFAULT_SLOT_NAME = "slot-%s";

    private JPanel pnlSlotCheckBox;
    private JTextField txtNewSlotName;
    private JComboBox<Object> cbxSlotConfigurationSource;
    private JCheckBox chkDeployToSlot;
    private JCheckBox chkToRoot;
    private JPanel pnlRoot;
    private JPanel pnlSlotDetails;
    private JRadioButton rbtNewSlot;
    private JRadioButton rbtExistingSlot;
    private JComboBox<Object> cbxSlotName;
    private JPanel pnlSlot;
    private JPanel pnlSlotHolder;
    private JPanel pnlCheckBox;
    private JPanel pnlSlotRadio;
    private JLabel lblSlotName;
    private JLabel lblSlotConfiguration;
    private JCheckBox chkOpenBrowser;
    private HyperlinkLabel lblNewSlot;
    private JPanel pnlExistingSlot;
    private JButton btnSlotHover;
    private AzureArtifactComboBox comboBoxArtifact;
    private JLabel lblArtifact;
    private JLabel lblWebApp;
    private WebAppComboBox comboBoxWebApp;
    private JPanel pnlAppSettings;
    private JLabel lblAppSettings;
    private AppSettingsTable appSettingsTable;

    private final HideableDecorator slotDecorator;
    private final Project project;
    private String appSettingsKey;

    public WebAppDeployConfigurationPanel(@Nonnull Project project, @Nonnull WebAppConfiguration webAppConfiguration) {
        super(project);
        this.project = project;
        this.appSettingsKey = webAppConfiguration.getAppSettingsKey();

        $$$setupUI$$$();
        comboBoxWebApp.addValueChangedListener((AzureValueChangeBiListener<AppServiceConfig>) this::onWebAppChanged);
        comboBoxArtifact.addItemListener(e -> chkToRoot.setVisible(isAbleToDeployToRoot(comboBoxArtifact.getValue())));

        final ButtonGroup slotButtonGroup = new ButtonGroup();
        slotButtonGroup.add(rbtNewSlot);
        slotButtonGroup.add(rbtExistingSlot);
        rbtExistingSlot.addItemListener(e -> toggleSlotType(true));
        rbtNewSlot.addItemListener(e -> toggleSlotType(false));
        chkDeployToSlot.addItemListener(e -> toggleSlotPanel(chkDeployToSlot.isSelected()));

        final Icon informationIcon = AllIcons.General.ContextHelp;
        btnSlotHover.setIcon(informationIcon);
        btnSlotHover.setHorizontalAlignment(SwingConstants.CENTER);
        btnSlotHover.setPreferredSize(new Dimension(informationIcon.getIconWidth(), informationIcon.getIconHeight()));
        btnSlotHover.setToolTipText(message("webapp.deploy.hint.deploymentSlot"));
        btnSlotHover.addFocusListener(new FocusListener() {
            private final IdeTooltipManager instance = IdeTooltipManager.getInstance();

            @Override
            public void focusGained(FocusEvent focusEvent) {
                btnSlotHover.setBorderPainted(true);
                final MouseEvent phantom = new MouseEvent(btnSlotHover, MouseEvent.MOUSE_ENTERED,
                        System.currentTimeMillis(), 0, 10, 10, 0, false);
                AzureTaskManager.getInstance().runLater(() -> instance.show(instance.getCustomTooltip(btnSlotHover), true));
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                btnSlotHover.setBorderPainted(false);
                instance.dispose();
            }
        });

        final JLabel labelForNewSlotName = new JLabel("Slot Name");
        labelForNewSlotName.setLabelFor(txtNewSlotName);
        final JLabel labelForExistingSlotName = new JLabel("Slot Name");
        labelForExistingSlotName.setLabelFor(cbxSlotName);

        lblArtifact.setLabelFor(comboBoxArtifact);
        lblWebApp.setLabelFor(comboBoxWebApp);

        final DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
        txtNewSlotName.setText(String.format(DEFAULT_SLOT_NAME, df.format(new Date())));

        slotDecorator = new HideableDecorator(pnlSlotHolder, DEPLOYMENT_SLOT, true);
        slotDecorator.setContentComponent(pnlSlot);
    }

    @Override
    protected void resetFromConfig(@Nonnull final WebAppConfiguration configuration) {
        final IntelliJWebAppSettingModel model = configuration.getModel();
        this.appSettingsKey = configuration.getAppSettingsKey();
        setValue(model);
    }

    @Override
    protected void apply(@Nonnull final WebAppConfiguration configuration) {
        final IntelliJWebAppSettingModel model = this.getValue();
        Optional.ofNullable(model).ifPresent(configuration::setWebAppSettingModel);
    }

    @Nonnull
    @Override
    public String getPanelName() {
        return "Deploy to Azure";
    }

    @Nonnull
    @Override
    public JPanel getMainPanel() {
        return pnlRoot;
    }

    private void onWebAppChanged(final AppServiceConfig value, final AppServiceConfig before) {
        if (value == null) {
            return;
        }
        this.loadDeploymentSlot(value);
        this.loadAppSettings(value, before);
    }

    private synchronized void loadAppSettings(@Nonnull AppServiceConfig value, @Nullable AppServiceConfig before) {
        final AppServiceConfig rawValue = comboBoxWebApp.getRawValue() instanceof AppServiceConfig ? (AppServiceConfig) comboBoxWebApp.getRawValue() : value;
        if (Objects.isNull(before) && value != rawValue) {
            // when reset from configuration, leverage app settings from configuration
            if (isDraftWebApp(value)) {
                // if draft has been created, merge local configuration with remote
                appSettingsTable.loadAppSettings(() -> loadDraftAppSettings(rawValue));
            }
        } else if (!Objects.equals(value, before)) {
            appSettingsTable.loadAppSettings(() -> isDraftWebApp(value) ? value.appSettings() :
                                                   Optional.ofNullable(getWebApp(value)).map(WebApp::getAppSettings).orElse(Collections.emptyMap()));
        }
    }

    // merge local app settings with remote if draft web app has been created
    private Map<String, String> loadDraftAppSettings(AppServiceConfig value) {
        final WebApp webApp = Azure.az(AzureWebApp.class).webApps(value.subscriptionId()).get(value.appName(), value.resourceGroup());
        return webApp != null && webApp.exists() ? MapUtils.putAll(ObjectUtils.firstNonNull(webApp.getAppSettings(), new HashMap<>()), value.appSettings().entrySet().toArray()) : value.appSettings();
    }

    private void setComboBoxDefaultValue(JComboBox<?> comboBox, Object value) {
        //noinspection unchecked
        UIUtils.listComboBoxItems(comboBox).stream().filter(item -> item.equals(value)).findFirst().ifPresent(defaultItem -> comboBox.setSelectedItem(value));
    }

    private AppServiceConfig getSelectedWebApp() {
        return comboBoxWebApp.getValue();
    }

    private boolean isAbleToDeployToRoot(final AzureArtifact azureArtifact) {
        final AppServiceConfig selectedWebApp = getSelectedWebApp();
        if (selectedWebApp == null || azureArtifact == null) {
            return false;
        }
        final String containerName = Optional.ofNullable(selectedWebApp.getRuntime())
                                             .map(RuntimeConfig::getWebContainer).orElse(StringUtils.EMPTY);
        final String packaging = azureArtifact.getPackaging();
        final boolean isDeployingWar = StringUtils.equalsAnyIgnoreCase(packaging, MavenConstants.TYPE_WAR, "ear");
        return isDeployingWar && StringUtils.containsAnyIgnoreCase(containerName, "tomcat", "jboss");
    }

    private void toggleSlotPanel(boolean slot) {
        final boolean isDeployToSlot = slot && (getSelectedWebApp() != null);
        rbtNewSlot.setEnabled(isDeployToSlot);
        rbtExistingSlot.setEnabled(isDeployToSlot);
        lblSlotName.setEnabled(isDeployToSlot);
        lblSlotConfiguration.setEnabled(isDeployToSlot);
        cbxSlotName.setEnabled(isDeployToSlot);
        txtNewSlotName.setEnabled(isDeployToSlot);
        cbxSlotConfigurationSource.setEnabled(isDeployToSlot);
    }

    private void toggleSlotType(final boolean isExistingSlot) {
        pnlExistingSlot.setVisible(isExistingSlot);
        pnlExistingSlot.setEnabled(isExistingSlot);
        txtNewSlotName.setVisible(!isExistingSlot);
        txtNewSlotName.setEnabled(!isExistingSlot);
        lblSlotConfiguration.setVisible(!isExistingSlot);
        cbxSlotConfigurationSource.setVisible(!isExistingSlot);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        lblNewSlot = new HyperlinkLabel(message("webapp.deploy.noDeploymentSlot"));
        lblNewSlot.addHyperlinkListener(e -> rbtNewSlot.doClick());

        comboBoxWebApp = new WebAppComboBox(project);
        comboBoxWebApp.reloadItems();

        comboBoxArtifact = new AzureArtifactComboBox(this.project);
        comboBoxArtifact.setFileFilter(virtualFile -> {
            final String ext = FileNameUtils.getExtension(virtualFile.getPath());
            return ArrayUtils.contains(FILE_NAME_EXT, ext);
        });
        comboBoxArtifact.reloadItems();

        appSettingsTable = new AppSettingsTable();
        pnlAppSettings = AppSettingsTableUtils.createAppSettingPanel(appSettingsTable);
    }

    private void loadDeploymentSlot(@Nonnull AppServiceConfig selectedWebApp) {
        if (isDraftWebApp(selectedWebApp)) {
            chkDeployToSlot.setEnabled(false);
            chkDeployToSlot.setSelected(false);
        } else {
            chkDeployToSlot.setEnabled(true);
            Mono.fromCallable(() -> getWebApp(selectedWebApp))
                    .map(webapp -> webapp.slots().list())
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(slots -> AzureTaskManager.getInstance().runLater(() -> fillDeploymentSlots(slots, selectedWebApp), AzureTask.Modality.ANY));
        }
    }

    private synchronized void fillDeploymentSlots(List<WebAppDeploymentSlot> slotList, @Nonnull final AppServiceConfig selectedWebApp) {
        final String defaultSlot = (String) cbxSlotName.getSelectedItem();
        final String defaultConfigurationSource = (String) cbxSlotConfigurationSource.getSelectedItem();
        cbxSlotName.removeAllItems();
        cbxSlotConfigurationSource.removeAllItems();
        cbxSlotConfigurationSource.addItem(AzureWebAppMvpModel.DO_NOT_CLONE_SLOT_CONFIGURATION);
        cbxSlotConfigurationSource.addItem(selectedWebApp.appName());
        slotList.stream().filter(Objects::nonNull).forEach(slot -> {
            cbxSlotName.addItem(slot.getName());
            cbxSlotConfigurationSource.addItem(slot.getName());
        });
        setComboBoxDefaultValue(cbxSlotName, defaultSlot);
        setComboBoxDefaultValue(cbxSlotConfigurationSource, defaultConfigurationSource);
        final boolean existDeploymentSlot = slotList.size() > 0;
        lblNewSlot.setVisible(!existDeploymentSlot);
        cbxSlotName.setVisible(existDeploymentSlot);
    }

    @Override
    public void setValue(IntelliJWebAppSettingModel data) {
        // artifact
        Optional.ofNullable(AzureArtifactManager.getInstance(this.project).getAzureArtifactById(data.getAzureArtifactType(), data.getArtifactIdentifier()))
                .ifPresent(artifact -> comboBoxArtifact.setArtifact(artifact));
        // web app
        Optional.of(data.getConfig()).filter(app -> StringUtils.isNotBlank(app.getAppName())).ifPresent(webApp -> {
            if (Azure.az(AzureAccount.class).account().getSubscriptions().stream().noneMatch(s -> s.getId().equals(webApp.subscriptionId()))) {
                comboBoxWebApp.setValue((AppServiceConfig) null);
                return;
            }
            comboBoxWebApp.setConfigModel(webApp);
            comboBoxWebApp.setValue(c -> AppServiceComboBox.isSameApp(c, webApp));
            chkDeployToSlot.setSelected(Objects.nonNull(webApp.getSlotConfig()));
            Optional.ofNullable(webApp.getSlotConfig()).ifPresent(c -> {
                final WebApp app = getWebApp(webApp);
                final WebAppDeploymentSlot slot = Optional.ofNullable(app)
                        .map(a -> a.slots().get(c.getName(), a.getResourceGroupName())).orElse(null);
                final boolean exists = Optional.ofNullable(slot).map(WebAppDeploymentSlot::exists).orElse(false);
                if (exists) {
                    rbtExistingSlot.setSelected(true);
                    cbxSlotName.setSelectedItem(c.getName());
                } else {
                    rbtNewSlot.setSelected(true);
                    txtNewSlotName.setText(c.getName());
                    cbxSlotConfigurationSource.setSelectedItem(c.getConfigurationSource());
                }
            });
            appSettingsTable.setAppSettings(webApp.appSettings());
        });
        // configuration
        chkToRoot.setSelected(data.isDeployToRoot());
        chkOpenBrowser.setSelected(data.isOpenBrowserAfterDeployment());
        slotDecorator.setOn(data.isSlotPanelVisible());
    }

    @Override
    public IntelliJWebAppSettingModel getValue() {
        final IntelliJWebAppSettingModel model = new IntelliJWebAppSettingModel();
        Optional.ofNullable(comboBoxWebApp.getValue()).ifPresent(model::setConfig);
        Optional.ofNullable(comboBoxArtifact.getValue()).ifPresent(a -> {
            model.setAzureArtifactType(a.getType());
            model.setArtifactIdentifier(a.getIdentifier());
            model.setPackaging(a.getPackaging());
        });
        final DeploymentSlotConfig slotConfig = chkDeployToSlot.isSelected() ? new DeploymentSlotConfig() : null;
        if (Objects.nonNull(slotConfig)) {
            final boolean useExistingSlot = rbtExistingSlot.isSelected();
            slotConfig.setName(useExistingSlot ? Objects.toString(cbxSlotName.getSelectedItem(), null) : txtNewSlotName.getText());
            slotConfig.setConfigurationSource(useExistingSlot ? null : Objects.toString(cbxSlotConfigurationSource.getSelectedItem(), null));
        }
        model.getConfig().setSlotConfig(slotConfig);
        model.getConfig().setAppSettings(appSettingsTable.getAppSettings());
        model.setOpenBrowserAfterDeployment(chkOpenBrowser.isSelected());
        model.setSlotPanelVisible(slotDecorator.isExpanded());
        return model;
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Arrays.asList(comboBoxWebApp, comboBoxArtifact);
    }

    private WebApp getWebApp(@Nonnull final AppServiceConfig config) {
        return Azure.az(AzureWebApp.class).webApps(config.subscriptionId()).get(config.appName(), config.resourceGroup());
    }

    private boolean isDraftWebApp(@Nonnull final AppServiceConfig config) {
        return !Azure.az(AzureWebApp.class).webApps(config.subscriptionId()).exists(config.appName(), config.resourceGroup());
    }
}
