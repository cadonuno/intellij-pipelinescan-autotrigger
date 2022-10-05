package cadonuno.pipelinescanautotrigger;

import cadonuno.pipelinescanautotrigger.pipelinescan.OsCommandRunner;
import cadonuno.pipelinescanautotrigger.pipelinescan.PipelineScanWrapper;
import cadonuno.pipelinescanautotrigger.settings.global.ApplicationSettingsState;
import cadonuno.pipelinescanautotrigger.settings.project.ProjectSettingsState;
import com.intellij.dvcs.push.PrePushHandler;
import com.intellij.dvcs.push.PushInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.util.List;
import java.util.Optional;

public class PipelineScanAutoPrePushHandler implements PrePushHandler {
    private static final String VERACODE_PIPELINE_SCAN = "Veracode Pipeline Scan - ";
    private static final String INITIAL_MESSAGE = VERACODE_PIPELINE_SCAN + "Initializing Veracode Pipeline Scan";
    private static final String BUILDING_APPLICATION_MESSAGE = VERACODE_PIPELINE_SCAN + "Building Application";
    private static final String SETTING_UP_PIPELINE_SCANNER_MESSAGE = VERACODE_PIPELINE_SCAN + "Setting up Veracode Pipeline Scan";
    private static final String RUNNING_PIPELINE_SCAN_UPLOADING_FILES_MESSAGE = VERACODE_PIPELINE_SCAN + "Running Veracode Pipeline Scan - uploading files";
    private static final String RUNNING_PIPELINE_SCAN_WAITING_FOR_RESULTS_MESSAGE = VERACODE_PIPELINE_SCAN + "Running Veracode Pipeline Scan - waiting for scan results";
    private static final double BUILD_STEP_MAX_FRACTION = 0.35d;
    private static final double UPLOADING_FILES_MAX_FRACTION = 0.63d;
    private static final double WAITING_FOR_RESULTS_MAX_FRACTION = 0.99d;
    private static final double FULL_PROGRESS_BAR = 1.0d;
    private String runningMessage = INITIAL_MESSAGE;
    private String messageQueue = "";

    private final ProjectSettingsState projectSettingsState;

    private final Project project;
    private ProgressIndicator progressIndicator;
    private double currentFraction = 0;
    private double maxFraction = 0;
    private double currentIncrement = 0;
    private int elipsisIndex;
    private static final String[] ELIPSIS_SET = {"", ".", "..", "..."};
    private Timer timer;
    private String baseDirectory;

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
        if (project == null) {
            return Result.OK;
        }
        setupScan(progressIndicator);
        try {
            return buildApplicationIfNecessary()
                    .map(this::runScan)
                    .orElse(Result.ABORT);
        } finally {
            timer.stop();
        }
    }

    private void setupScan(@NotNull ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
        messageQueue = "";
        setCurrentFraction(.0d);
        setMessage(INITIAL_MESSAGE);
        setupTimer();
        maxFraction = BUILD_STEP_MAX_FRACTION;
        currentIncrement = 0.003;
    }

    private Optional<ApplicationSettingsState> buildApplicationIfNecessary() {
        ApplicationSettingsState applicationSettingsState = ApplicationSettingsState.getInstance();
        baseDirectory = project.getBasePath();
        if (projectSettingsState.getBuildCommand() != null) {
            setMessage(BUILDING_APPLICATION_MESSAGE);
            if (OsCommandRunner.runCommand("application build",
                    projectSettingsState.getBuildCommand(), this) != 0) {
                showMessagePopup(VERACODE_PIPELINE_SCAN + "Unable to build application!");
                return Optional.empty();
            }
        }
        return Optional.of(applicationSettingsState);
    }

    @NotNull
    private Result runScan(ApplicationSettingsState applicationSettingsState) {
        maxFraction = UPLOADING_FILES_MAX_FRACTION;
        setCurrentFraction(BUILD_STEP_MAX_FRACTION);

        int scanReturnCode = runPipelineScan(applicationSettingsState);
        maxFraction = FULL_PROGRESS_BAR;
        setCurrentFraction(FULL_PROGRESS_BAR);
        if (timer.isRunning()) {
            timer.stop();
        }
        if (scanReturnCode != 0) {
            boolean shouldContinue = showConfirmationDialog(getScanFinishedConfirmationMessage(scanReturnCode) + "!" +
                    "\nContinue pushing?");
            if (shouldContinue) {
                return Result.OK;
            }
            //TODO: load results into the UI to allow for navigation
            return Result.ABORT;
        }
        return Result.OK;
    }

    private void setupTimer() {
        timer = new Timer(400, actionEvent -> {
            if (progressIndicator.isCanceled()) {
                progressIndicator.setText2("Cancelling scan");
            }
            progressIndicator.setText(runningMessage + ELIPSIS_SET[elipsisIndex]);
            updateProgressIndicator();
            elipsisIndex++;
            if (elipsisIndex > 3) {
                elipsisIndex = 0;
            }
        });
        timer.setRepeats(true);
        timer.start();
    }

    private String getScanFinishedConfirmationMessage(int scanReturnCode) {
        return scanReturnCode > 0
                ? "Pipeline scan detected " + scanReturnCode + " issues"
                : "Pipeline scan failed with return code of " + scanReturnCode;
    }

    private int runPipelineScan(ApplicationSettingsState applicationSettingsState) {
        int scanReturnCode;
        currentIncrement = 0.1d;
        setMessage(SETTING_UP_PIPELINE_SCANNER_MESSAGE);
        try (PipelineScanWrapper pipelineScanWrapper = PipelineScanWrapper.acquire(baseDirectory, projectSettingsState)) {
            setMessage(RUNNING_PIPELINE_SCAN_UPLOADING_FILES_MESSAGE);
            scanReturnCode = pipelineScanWrapper.startScan(applicationSettingsState, this);
        }
        return scanReturnCode;
    }

    private boolean showConfirmationDialog(String aMessage) {
        return JOptionPane.showConfirmDialog(null, aMessage,
                "Veracode Pipeline Scan", JOptionPane.YES_NO_CANCEL_OPTION) == 0;
    }

    private void showMessagePopup(String aMessage) {
        JOptionPane.showMessageDialog(null, aMessage);
    }

    private void setMessage(String message) {
        progressIndicator.checkCanceled();
        runningMessage = message;
        progressIndicator.setText(message);
        updateProgressIndicator();
    }

    public void updateProgressIndicatorSecondaryMessage(String newMessage) {
        setCurrentFraction(Math.min(maxFraction, currentFraction + currentIncrement));
        progressIndicator.setText2(newMessage + " " + messageQueue);
        messageQueue = newMessage;
        if (newMessage.contains("PIPELINE-SCAN INFO: Analysis Started")) {
            setMessage(RUNNING_PIPELINE_SCAN_WAITING_FOR_RESULTS_MESSAGE);
            maxFraction = WAITING_FOR_RESULTS_MAX_FRACTION;
            currentIncrement = 2d;
        }
    }

    private void setCurrentFraction(double valueToSet) {
        progressIndicator.checkCanceled();
        currentFraction = valueToSet;
        progressIndicator.setFraction(currentFraction*2);
        updateProgressIndicator();
    }


    private void updateProgressIndicator() {
        progressIndicator.pushState();
        progressIndicator.popState();
    }
}
