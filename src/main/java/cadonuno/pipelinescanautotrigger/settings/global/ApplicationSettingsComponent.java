package cadonuno.pipelinescanautotrigger.settings.global;

import cadonuno.pipelinescanautotrigger.settings.credentials.CredentialsTypeEnum;
import cadonuno.pipelinescanautotrigger.settings.util.InterfaceBuilder;
import cadonuno.pipelinescanautotrigger.settings.util.JLinkLabel;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ApplicationSettingsComponent {
    private final JPanel mainPanel;

    //Credentials settings:
    private final JBCheckBox credentialsFileCheckBox = new JBCheckBox("Credentials file");
    private final JBTextField credentialsProfileNameField = new JBTextField();
    private final JBCheckBox credentialsInIdeCheckBox = new JBCheckBox("Credentials in IDE");
    private final JBPasswordField apiIdField = new JBPasswordField();
    private final JBPasswordField apiKeyField = new JBPasswordField();

    //Scan Settings
    private final JBCheckBox veryHighSeverityCheckBox = new JBCheckBox("Very high");
    private final JBCheckBox highSeverityCheckBox = new JBCheckBox("High");
    private final JBCheckBox mediumSeverityCheckBox = new JBCheckBox("Medium");
    private final JBCheckBox lowSeverityCheckBox = new JBCheckBox("Low");
    private final JBCheckBox informationalSeverityCheckBox = new JBCheckBox("Informational");

    private final JBTextField policyToEvaluateField = new JBTextField();

    // Proxy Settings
    private final JBTextField proxyHostField = new JBTextField();
    private final JBTextField proxyPortField = new JBTextField();
    private final JBTextField proxyUsernameField = new JBTextField();
    private final JBPasswordField proxyPasswordField = new JBPasswordField();

    // Advanced Settings
    private final JBTextField optArgsField = new JBTextField();

    public ApplicationSettingsComponent() {
        JPanel credentialProfilePanel = FormBuilder.createFormBuilder()
                .addComponent(InterfaceBuilder.makeLabelWithSupportingLink("Credentials profile name: ",
                        "Configuring an API credentials file",
                        "https://docs.veracode.com/r/c_configure_api_cred_file"))
                .addComponent(credentialsProfileNameField)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        JPanel credentialLiteralsPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("API ID:", apiIdField, true)
                .addLabeledComponent("API key:", apiKeyField, true)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        JPanel credentialsTypePanel = FormBuilder.createFormBuilder()
                .addComponent(InterfaceBuilder.makeLabelWithSupportingLink("API credentials: ",
                        "How to generate API credentials",
                        "https://docs.veracode.com/r/t_create_api_creds"))
                .addComponent(credentialsFileCheckBox)
                .addComponent(credentialsInIdeCheckBox)
                .addComponent(credentialLiteralsPanel)
                .addComponent(credentialProfilePanel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();


        JPanel proxySettingsPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Proxy host:", proxyHostField, true)
                .addLabeledComponent("Proxy port:", proxyPortField, true)
                .addLabeledComponent("Proxy username:", proxyUsernameField, true)
                .addLabeledComponent("Proxy password:", proxyPasswordField, true)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        JPanel advancedSettingsPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Optional arguments:", optArgsField, true)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(InterfaceBuilder.addBorderToPanel(credentialsTypePanel), 1)
                .addComponent(new JBSplitter())
                .addLabeledComponent(new JBLabel("Failure criteria: "),
                        InterfaceBuilder.addBorderToPanel(getFailureCriteriaComponent()), 1, true)
                .addComponent(new JBSplitter())
                .addLabeledComponent(new JBLabel("Proxy settings: "),
                        InterfaceBuilder.addBorderToPanel(proxySettingsPanel), 1, true)
                .addComponent(new JBSplitter())
                .addLabeledComponent(new JBLabel("Advanced settings: "),
                        InterfaceBuilder.addBorderToPanel(advancedSettingsPanel), 1, true)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        credentialsFileCheckBox.addChangeListener(e -> {
            if (credentialsFileCheckBox.isSelected()) {
                credentialsInIdeCheckBox.setSelected(false);
                credentialLiteralsPanel.setVisible(false);
                credentialProfilePanel.setVisible(true);
            } else {
                credentialLiteralsPanel.setVisible(true);
                credentialProfilePanel.setVisible(false);
            }
        });
        credentialsInIdeCheckBox.addChangeListener(e -> {
            if (credentialsInIdeCheckBox.isSelected()) {
                credentialsFileCheckBox.setSelected(false);
            }
        });
    }

    private JPanel getFailureCriteriaComponent() {
        return buildIndentedPanel(Arrays.asList(new JLabel("Severities: "),
                getSeveritiesPanel(),
                new JLabel("Policy to evaluate: "),
                policyToEvaluateField));
    }

    private JPanel buildIndentedPanel(List<JComponent> internalElements) {
        JPanel externalPanel = new JPanel();
        externalPanel.setLayout(new BoxLayout(externalPanel, BoxLayout.X_AXIS));
        externalPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        externalPanel.add(makeInternalPanel(internalElements));
        return externalPanel;
    }

    private Component makeInternalPanel(List<JComponent> internalElements) {
        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new VerticalLayout());

        for (Component component : internalElements) {
            internalPanel.add(component);
        }

        return internalPanel;
    }

    private JComponent getSeveritiesPanel() {
        return buildIndentedPanel(Arrays.asList(veryHighSeverityCheckBox,
                highSeverityCheckBox,
                mediumSeverityCheckBox,
                lowSeverityCheckBox,
                informationalSeverityCheckBox));
    }


    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return apiIdField;
    }

    public boolean isShouldFailOnVeryHigh() {
        return veryHighSeverityCheckBox.isSelected();
    }

    public boolean isShouldFailOnHigh() {
        return highSeverityCheckBox.isSelected();
    }

    public boolean isShouldFailOnMedium() {
        return mediumSeverityCheckBox.isSelected();
    }

    public boolean isShouldFailOnLow() {
        return lowSeverityCheckBox.isSelected();
    }

    public boolean isShouldFailOnInformational() {
        return informationalSeverityCheckBox.isSelected();
    }

    public String getPolicyToEvaluate() {
        return policyToEvaluateField.getText();
    }

    public void setShouldFailOnVeryHigh(boolean shouldFailOnVeryHigh) {
        veryHighSeverityCheckBox.setSelected(shouldFailOnVeryHigh);
    }

    public void setShouldFailOnHigh(boolean shouldFailOnHigh) {
        highSeverityCheckBox.setSelected(shouldFailOnHigh);
    }

    public void setShouldFailOnMedium(boolean shouldFailOnMedium) {
        mediumSeverityCheckBox.setSelected(shouldFailOnMedium);
    }

    public void setShouldFailOnLow(boolean shouldFailOnLow) {
        lowSeverityCheckBox.setSelected(shouldFailOnLow);
    }

    public void setShouldFailOnInformational(boolean shouldFailOnInformational) {
        informationalSeverityCheckBox.setSelected(shouldFailOnInformational);
    }

    public void setPolicyToEvaluate(String policyToEvaluate) {
        policyToEvaluateField.setText(policyToEvaluate);
    }

    public String getApiIdText() {
        return apiIdField.getText();
    }

    public void setApiIdText(String apiId) {
        apiIdField.setText(apiId);
    }

    public String getApiKeyText() {
        return apiKeyField.getText();
    }

    public void setApiKeyText(String apiKey) {
        apiKeyField.setText(apiKey);
    }

    public CredentialsTypeEnum getCredentialsType() {
        return credentialsFileCheckBox.isSelected()
                ? CredentialsTypeEnum.CredentialsFile
                : CredentialsTypeEnum.LiteralCredentials;
    }

    public void setCredentialsType(CredentialsTypeEnum credentialsType) {
        if (credentialsType == CredentialsTypeEnum.CredentialsFile) {
            credentialsInIdeCheckBox.setSelected(false);
            credentialsFileCheckBox.setSelected(true);
        } else {
            credentialsFileCheckBox.setSelected(false);
            credentialsInIdeCheckBox.setSelected(true);
        }
    }

    public String getCredentialsProfileName() {
        return credentialsProfileNameField.getText();
    }

    public void setCredentialsProfileName(String credentialsProfileName) {
        credentialsProfileNameField.setText(credentialsProfileName);
    }

    public String getOptArgs() {
        return optArgsField.getText();
    }

    public String getProxyHost() {
        return proxyHostField.getText();
    }

    public String getProxyPort() {
        return proxyPortField.getText();
    }

    public String getProxyUsername() {
        return proxyUsernameField.getText();
    }

    public String getProxyPassword() {
        return proxyPasswordField.getText();
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHostField.setText(proxyHost);
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPortField.setText(proxyPort);
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsernameField.setText(proxyUsername);
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPasswordField.setText(proxyPassword);
    }

    public void setOptArgs(String optArgs) {
        this.optArgsField.setText(optArgs);
    }
}
