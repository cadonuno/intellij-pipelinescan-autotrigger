package cadonuno.pipelinescanautotrigger.settings.project;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ProjectSettingsComponent {
    //TODO: add information popups with details on the expected input
    private final JPanel mainPanel;
    private final JPanel settingsPanel;
    private final JBCheckBox isEnabledCheckBox = new JBCheckBox("Enabled");
    private final JBTextField fileToScanField = new JBTextField();
    private final JBTextField buildCommandField = new JBTextField();
    private final JBTextField baselineFileField = new JBTextField();

    public ProjectSettingsComponent() {
        settingsPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("File to scan: "), fileToScanField, 1, false)
                .addLabeledComponent(new JBLabel("Build command (including file parameter): "), buildCommandField, 1, false)
                .addLabeledComponent(new JBLabel("Baseline file: "), baselineFileField, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(isEnabledCheckBox)
                .addComponent(settingsPanel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        isEnabledCheckBox.addChangeListener(e -> {
            settingsPanel.setVisible(isEnabledCheckBox.isSelected());
        });
    }

    public JPanel getPanel() {
        return mainPanel;
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
    @NotNull
    public String getBuildCommandText() {
        return buildCommandField.getText();
    }

    public void setBuildCommandText(@NotNull String newText) {
        buildCommandField.setText(newText);
    }

    @NotNull
    public String getBaselineFileText() {
        return baselineFileField.getText();
    }

    public void setBaselineFileText(@NotNull String newText) {
        baselineFileField.setText(newText);
    }

    public boolean isEnabled() {
        return isEnabledCheckBox.isSelected();
    }

    public void setEnabled(boolean isEnabled) {
        isEnabledCheckBox.setSelected(isEnabled);
        settingsPanel.setVisible(isEnabled);
    }
}
