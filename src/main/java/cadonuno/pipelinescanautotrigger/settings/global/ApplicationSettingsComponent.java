package cadonuno.pipelinescanautotrigger.settings.global;

import cadonuno.pipelinescanautotrigger.settings.credentials.CredentialsTypeEnum;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.JComponent;
import javax.swing.JPanel;

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

    // Proxy Settings
    private final JBTextField proxyHostField = new JBTextField();
    private final JBTextField proxyPortField = new JBTextField();
    private final JBTextField proxyUsernameField = new JBTextField();
    private final JBPasswordField proxyPasswordField = new JBPasswordField();

    // Advanced Settings
    private final JBTextField optArgsField = new JBTextField();

    public ApplicationSettingsComponent() {
        JPanel credentialProfilePanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Credentials profile name:", credentialsProfileNameField)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        JPanel credentialLiteralsPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("API ID:", apiIdField)
                .addLabeledComponent("API key:", apiKeyField)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        JPanel credentialsTypePanel = FormBuilder.createFormBuilder()
                .addComponent(credentialsFileCheckBox)
                .addComponent(credentialsInIdeCheckBox)
                .addComponent(credentialLiteralsPanel)
                .addComponent(credentialProfilePanel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        JPanel severitiesToFailPanel = FormBuilder.createFormBuilder()
                .addComponent(veryHighSeverityCheckBox)
                .addComponent(highSeverityCheckBox)
                .addComponent(mediumSeverityCheckBox)
                .addComponent(lowSeverityCheckBox)
                .addComponent(informationalSeverityCheckBox)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        JPanel proxySettingsPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Proxy host:", proxyHostField)
                .addLabeledComponent("Proxy port:", proxyPortField)
                .addLabeledComponent("Proxy username:", proxyUsernameField)
                .addLabeledComponent("Proxy password:", proxyPasswordField)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        JPanel advancedSettingsPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Optional arguments:", optArgsField)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("API credentials: "), credentialsTypePanel, 0, true)
                .addComponent(new JBSplitter())
                .addLabeledComponent(new JBLabel("Severities to fail scan: "), severitiesToFailPanel, 1, true)
                .addComponent(new JBSplitter())
                .addLabeledComponent(new JBLabel("Proxy settings: "), proxySettingsPanel, 0, true)
                .addComponent(new JBSplitter())
                .addLabeledComponent(new JBLabel("Advanced settings: "), advancedSettingsPanel, 0, true)
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
