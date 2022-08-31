package cadonuno.pipelinescanautotrigger.settings;

import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class ApplicationSettingsComponent {
    private final JPanel myMainPanel;

    //TODO: add support for credentials file

    //Credentials settings:
    private final JBTextField apiIdField = new JBTextField();
    private final JBPasswordField apiKeyField = new JBPasswordField();

    //Scan Settings
    private final JBTextField fileToScanField = new JBTextField();
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
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("API credentials: "), credentialPanel, 1, true)
                .addComponent(new JBSplitter())
                .addLabeledComponent(new JBLabel("File to scan: "), fileToScanField, 1, false)
                .addComponent(new JBSplitter())
                .addLabeledComponent(new JBLabel("Severities to fail scan: "), severitiesToFailPanel, 1, true)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return fileToScanField;
    }

    @NotNull
    public String getFileToScanText() {
        return fileToScanField.getText();
    }

    public void setFileToScanText(@NotNull String newText) {
        fileToScanField.setText(newText);
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
