package cadonuno.pipelinescanautotrigger.settings.global;

import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class ApplicationSettingsComponent {
    private final JPanel mainPanel;

    //TODO: add support for credentials file
    //TODO: find out why the first field shows up half-way down

    //Credentials settings:
    private final JBPasswordField apiIdField = new JBPasswordField();
    private final JBPasswordField apiKeyField = new JBPasswordField();

    //Scan Settings
    private final JBCheckBox veryHighSeverityCheckBox = new JBCheckBox("Very high");
    private final JBCheckBox highSeverityCheckBox = new JBCheckBox("High");
    private final JBCheckBox mediumSeverityCheckBox = new JBCheckBox("Medium");
    private final JBCheckBox lowSeverityCheckBox = new JBCheckBox("Low");
    private final JBCheckBox informationalSeverityCheckBox = new JBCheckBox("Informational");

    public ApplicationSettingsComponent() {
        JPanel credentialPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("API ID:", apiIdField)
                .addLabeledComponent("API key:", apiKeyField)
                .getPanel();
        JPanel severitiesToFailPanel = FormBuilder.createFormBuilder()
                .addComponent(veryHighSeverityCheckBox)
                .addComponent(highSeverityCheckBox)
                .addComponent(mediumSeverityCheckBox)
                .addComponent(lowSeverityCheckBox)
                .addComponent(informationalSeverityCheckBox)
                .getPanel();
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("API credentials: "), credentialPanel, 0, true)
                .addComponent(new JBSplitter())
                .addLabeledComponent(new JBLabel("Severities to fail scan: "), severitiesToFailPanel, 1, true)
                .getPanel();
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
}
