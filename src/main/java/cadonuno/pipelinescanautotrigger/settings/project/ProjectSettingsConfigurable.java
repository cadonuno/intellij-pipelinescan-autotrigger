package cadonuno.pipelinescanautotrigger.settings.project;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ProjectSettingsConfigurable implements Configurable {
    private final Project project;
    private ProjectSettingsComponent projectSettingsComponent;

    public ProjectSettingsConfigurable(Project project) {
        if (project == null) {
            throw new IllegalStateException("Project is null");
        }
        this.project = project;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Project Settings";
    }

    @Override
    public @Nullable JComponent createComponent() {
        projectSettingsComponent = new ProjectSettingsComponent(project);
        return projectSettingsComponent.getPanel();
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return projectSettingsComponent.getPreferredFocusedComponent();
    }

    @Override
    public boolean isModified() {
        ProjectSettingsState settings = ProjectSettingsState.getInstance(project);
        boolean modified = !projectSettingsComponent.getFileToScanText().equals(settings.getFileToScan());
        modified |= projectSettingsComponent.getBuildCommandText().equals(settings.getBuildCommand());
        modified |= projectSettingsComponent.getBaselineFileText().equals(settings.getBaselineFile());
        modified |= projectSettingsComponent.isEnabled() != settings.isEnabled();
        modified |= projectSettingsComponent.isShouldScanOnPush() != settings.isShouldScanOnPush();
        return modified;
    }

    @Override
    public void apply() {
        ProjectSettingsState settings = ProjectSettingsState.getInstance(project);
        settings.setFileToScan(projectSettingsComponent.getFileToScanText());
        settings.setBuildCommand(projectSettingsComponent.getBuildCommandText());
        settings.setBaselineFile(projectSettingsComponent.getBaselineFileText());
        settings.setEnabled(projectSettingsComponent.isEnabled());
        settings.setShouldScanOnPush(projectSettingsComponent.isShouldScanOnPush());
    }

    @Override
    public void reset() {
        ProjectSettingsState settings = ProjectSettingsState.getInstance(project);
        projectSettingsComponent.setFileToScanText(settings.getFileToScan());
        projectSettingsComponent.setBuildCommandText(settings.getBuildCommand());
        projectSettingsComponent.setBaselineFileText(settings.getBaselineFile());
        projectSettingsComponent.setEnabled(settings.isEnabled());
        projectSettingsComponent.setShouldScanOnPush(settings.isShouldScanOnPush());
    }

    @Override
    public void disposeUIResources() {
        projectSettingsComponent = null;
    }
}
