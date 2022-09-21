package cadonuno.pipelinescanautotrigger;

import cadonuno.pipelinescanautotrigger.pipelinescan.OsCommandRunner;
import cadonuno.pipelinescanautotrigger.pipelinescan.PipelineScanWrapper;
import cadonuno.pipelinescanautotrigger.settings.global.ApplicationSettingsState;
import cadonuno.pipelinescanautotrigger.settings.project.ProjectSettingsState;
import com.intellij.dvcs.push.PrePushHandler;
import com.intellij.dvcs.push.PushInfo;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class PipelineScanAutoPrePushHandler implements PrePushHandler {
    private static final String INITIAL_MESSAGE = "Initializing Veracode Pipeline Scan";
    private static final String BUILDING_APPLICATION_MESSAGE = "Building Application";
    private static final String PREPARING_PIPELINE_SCANNER_MESSAGE = "Preparing Veracode Pipeline Scan";
    private static final String RUNNING_PIPELINE_SCAN_MESSAGE = "Running Veracode Pipeline Scan";
    private String runningMessage = INITIAL_MESSAGE;

    private final ProjectSettingsState projectSettingsState;

    private final Project project;

    public PipelineScanAutoPrePushHandler(Project project) {
        this.project = project;
        this.projectSettingsState = project.getService(ProjectSettingsState.class);
    }

    @Override
    public @NotNull
    @Nls(capitalization = Nls.Capitalization.Title) String getPresentableName() {
        return runningMessage;
    }

    @Override
    public @NotNull Result handle(@NotNull List<PushInfo> list, @NotNull ProgressIndicator progressIndicator) {
        setMessage(INITIAL_MESSAGE);
        if (project == null) {
            return Result.OK;
        }
        ApplicationSettingsState applicationSettingsState = ApplicationSettingsState.getInstance();
        String baseDirectory = project.getBasePath();
        if (projectSettingsState.getBuildCommand() != null) {
            setMessage(BUILDING_APPLICATION_MESSAGE);
            if (OsCommandRunner.runCommand(projectSettingsState.getBuildCommand() + " " + baseDirectory) != 0) {
                showMessagePopup("Unable to build application!");
                return Result.ABORT;
            }
        }

        int scanReturnCode = runPipelineScan(applicationSettingsState, baseDirectory);
        if (scanReturnCode != 0) {
            showMessagePopup("Pipeline scan failed with return code of " + scanReturnCode + "!");
            return Result.ABORT;
        }
        return Result.OK;
    }

    private int runPipelineScan(ApplicationSettingsState applicationSettingsState, String baseDirectory) {
        int scanReturnCode;
        setMessage(PREPARING_PIPELINE_SCANNER_MESSAGE);
        try (PipelineScanWrapper pipelineScanWrapper = PipelineScanWrapper.acquire(baseDirectory, projectSettingsState)) {
            setMessage(RUNNING_PIPELINE_SCAN_MESSAGE);
            scanReturnCode = pipelineScanWrapper.startScan(applicationSettingsState);
        }
        return scanReturnCode;
    }

    private void showMessagePopup(String aMessage) {
        JOptionPane.showMessageDialog(null, aMessage);
    }

    private void setMessage(String message) {
        runningMessage = message;
    }
}
