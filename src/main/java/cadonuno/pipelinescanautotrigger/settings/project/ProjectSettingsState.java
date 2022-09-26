package cadonuno.pipelinescanautotrigger.settings.project;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "cadonuno.pipelinescanautotrigger.settings.ProjectSettingsState",
        storages = {@Storage("PipelineAutoTrigger-project.xml")}
)
public class ProjectSettingsState implements PersistentStateComponent<ProjectSettingsState> {

    //TODO: add more generic commands here
    //TODO: switch most thrown exceptions to popups
    private String fileToScan = "./target/*";
    private String buildCommand = "mvn clean package";
    private String baselineFile = "results.json";

    private boolean isEnabled = false;

    public ProjectSettingsState() {
    }


    @Override
    public @Nullable ProjectSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ProjectSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static ProjectSettingsState getInstance(Project project) {
        return project.getService(ProjectSettingsState.class);
    }

    public String getFileToScan() {
        return fileToScan;
    }

    public void setFileToScan(String fileToScan) {
        this.fileToScan = fileToScan;
    }

    public String getBaselineFile() {
        return baselineFile;
    }

    public void setBaselineFile(String baselineFile) {
        this.baselineFile = baselineFile;
    }

    public String getBuildCommand() {
        return buildCommand;
    }

    public void setBuildCommand(String buildCommand) {
        this.buildCommand = buildCommand;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
