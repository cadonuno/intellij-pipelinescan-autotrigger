package cadonuno.pipelinescanautotrigger;

import cadonuno.pipelinescanautotrigger.pipelinescan.PipelineScanWrapper;
import cadonuno.pipelinescanautotrigger.settings.ApplicationSettingsState;
import com.intellij.dvcs.push.PrePushHandler;
import com.intellij.dvcs.push.PushInfo;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PipelineScanAutoPrePushHandler implements PrePushHandler {
    @Override
    public @NotNull
    @Nls(capitalization = Nls.Capitalization.Title) String getPresentableName() {
        return "Running Veracode Pipeline Scan";
    }

    @Override
    public @NotNull Result handle(@NotNull List<PushInfo> list, @NotNull ProgressIndicator progressIndicator) {
        if (ProjectManager.getInstance() == null) {
            return Result.OK;
        }
        ApplicationSettingsState applicationSettingsState = ApplicationSettingsState.getInstance();
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length == 0 || list.isEmpty() || !applicationSettingsState.isEnabled()) {
            return Result.OK;
        }
        Project project = projects[0];
        boolean hasFailedScan;
        try (PipelineScanWrapper pipelineScanWrapper = PipelineScanWrapper.acquire(project)) {
            hasFailedScan = pipelineScanWrapper.startScan(applicationSettingsState) != 0;
            //TODO display popup with number of issues found
        }
        return hasFailedScan ? Result.ABORT : Result.OK;
    }
}
