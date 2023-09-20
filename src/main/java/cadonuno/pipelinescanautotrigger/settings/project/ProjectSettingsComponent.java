package cadonuno.pipelinescanautotrigger.settings.project;

import cadonuno.pipelinescanautotrigger.settings.util.InterfaceBuilder;
import cadonuno.pipelinescanautotrigger.ui.scanresults.PipelineScanResultsBarToolWindowFactory;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;

public class ProjectSettingsComponent {
    //TODO: add information popups with details on the expected input
    private final JPanel mainPanel;
    private final JPanel settingsPanel;
    private final JBCheckBox isEnabledCheckBox = new JBCheckBox("Enabled");
    private final JBCheckBox shouldScanOnPushCheckBox = new JBCheckBox("Scan before pushing new code");
    private final JBTextField fileToScanField = new JBTextField();
    private final JBTextField buildCommandField = new JBTextField();
    private final JBTextField baselineFileField = new JBTextField();
    private final JBTextField moduleSelectionField = new JBTextField();
    private final Project project;

    public ProjectSettingsComponent(Project project) {
        this.project = project;
        settingsPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("File to scan*: "), fileToScanField, 1, true)
                .addLabeledComponent(new JBLabel("Build command (including file parameter): "), buildCommandField, 1, true)
                .addLabeledComponent(new JBLabel("Baseline file: "), baselineFileField, 1, true)
                .addLabeledComponent(new JBLabel("Top level modules (--include): "), moduleSelectionField, 1, true)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(InterfaceBuilder.addBorderToPanel(
                        InterfaceBuilder.makeBasePanel(
                                Arrays.asList(isEnabledCheckBox, shouldScanOnPushCheckBox))))
                .addComponent(InterfaceBuilder.addBorderToPanel(settingsPanel))
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        isEnabledCheckBox.addChangeListener(e -> {
            settingsPanel.setVisible(isEnabledCheckBox.isSelected());
            PipelineScanResultsBarToolWindowFactory.handleIsScanEnabledChange(
                    project, isEnabledCheckBox.isSelected());
        });
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return fileToScanField;
    }


    public String getFileToScanText() {
        return fileToScanField.getText();
    }

    public void setFileToScanText(@NotNull String newText) {
        fileToScanField.setText(newText);
    }

    public String getBuildCommandText() {
        return buildCommandField.getText();
    }

    public void setBuildCommandText(String newText) {
        buildCommandField.setText(newText);
    }


    public String getModuleSelectionText() {
        return moduleSelectionField.getText();
    }

    public void setModuleSelectionText(String newText) {
        moduleSelectionField.setText(newText);
    }


    public String getBaselineFileText() {
        return baselineFileField.getText();
    }

    public void setBaselineFileText(String newText) {
        baselineFileField.setText(newText);
    }

    public boolean isEnabled() {
        return isEnabledCheckBox.isSelected();
    }

    public void setShouldScanOnPush(boolean shouldScanOnPush) {
        shouldScanOnPushCheckBox.setSelected(shouldScanOnPush);
    }

    public boolean isShouldScanOnPush() {
        return shouldScanOnPushCheckBox.isSelected();
    }

    public void setEnabled(boolean isEnabled) {
        isEnabledCheckBox.setSelected(isEnabled);
        settingsPanel.setVisible(isEnabled);
        PipelineScanResultsBarToolWindowFactory.handleIsScanEnabledChange(
                project, isEnabledCheckBox.isSelected());
    }
}
