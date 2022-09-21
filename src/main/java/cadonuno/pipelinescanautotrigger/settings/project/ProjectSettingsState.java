package cadonuno.pipelinescanautotrigger.settings.project;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;

@State(
        name = "cadonuno.pipelinescanautotrigger.settings.ProjectSettingsState",
        storages = {@Storage("project-label.xml")}
)
public class ProjectSettingsState {
    private String fileToScan = "./target/*";
    private String buildCommand = "mvn clean package";
    private String baselineFile = "results.json";

    public ProjectSettingsState() {
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
}
