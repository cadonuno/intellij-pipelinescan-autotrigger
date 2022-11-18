package cadonuno.pipelinescanautotrigger;

import cadonuno.pipelinescanautotrigger.pipelinescan.OsCommandRunner;
import cadonuno.pipelinescanautotrigger.pipelinescan.PipelineScanFinding;
import cadonuno.pipelinescanautotrigger.pipelinescan.PipelineScanWrapper;
import cadonuno.pipelinescanautotrigger.settings.global.ApplicationSettingsState;
import cadonuno.pipelinescanautotrigger.settings.project.ProjectSettingsState;
import cadonuno.pipelinescanautotrigger.ui.PipelineScanResultsBarToolWindowFactory;
import com.intellij.dvcs.push.PrePushHandler;
import com.intellij.dvcs.push.PushInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.SmoothProgressAdapter;
import com.intellij.openapi.project.Project;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.io.*;
import java.util.*;

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
    public static final String PIPELINE_RESULTS_JSON = "pipeline_results.json";
    public static final String FILTERED_PIPELINE_RESULTS_JSON = "filtered_pipeline_results.json";
    private static final Map<Integer, String> SEVERITY_MAP = new HashMap<>() {{
        put(0, "Informational");
        put(1, "Very low");
        put(2, "Low");
        put(3, "Medium");
        put(4, "High");
        put(5, "Very High");
    }};
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

    private static final Logger LOG = Logger.getInstance(PipelineScanAutoPrePushHandler.class);

    private static final Map<String, PipelineScanAutoPrePushHandler> projectToHandlerMap = new HashMap<>();
    private double progressMultiplier;

    public PipelineScanAutoPrePushHandler(Project project) {
        LOG.info("Creating handler for project: " + project.getProjectFilePath());
        this.project = project;
        this.projectSettingsState = project.getService(ProjectSettingsState.class);
        projectToHandlerMap.put(project.getProjectFilePath(), this);
    }

    public static Optional<PipelineScanAutoPrePushHandler> getProjectHandler(Project project) {
        return Optional.ofNullable(projectToHandlerMap.get(project.getProjectFilePath()));
    }

    @Override
    public @NotNull
    @Nls(capitalization = Nls.Capitalization.Title) String getPresentableName() {
        return runningMessage;
    }

    @Override
    public @NotNull Result handle(@NotNull List<PushInfo> list, @NotNull ProgressIndicator progressIndicator) {
        return startScan(progressIndicator);
    }

    @NotNull
    public Result startScan(@NotNull ProgressIndicator progressIndicator) {
        if (project == null || !isScanEnabled()) {
            return Result.OK;
        }
        progressMultiplier = progressIndicator instanceof SmoothProgressAdapter
                ? 1 : 2;
        setupScan(progressIndicator);
        try {
            return buildApplicationIfNecessary()
                    .map(this::runScan)
                    .orElse(Result.ABORT);
        } finally {
            timer.stop();
        }
    }

    public boolean isScanEnabled() {
        return projectSettingsState.isEnabled();
    }

    private void setupScan(@NotNull ProgressIndicator progressIndicator) {
        PipelineScanWrapper.cleanupDirectory(baseDirectory);
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
        readResults();
        if (timer.isRunning()) {
            timer.stop();
        }
        if (scanReturnCode != 0) {
            boolean shouldContinue = !getConfirmationDialogOutput(getScanFinishedConfirmationMessage(scanReturnCode) + "!" +
                    "\nAbort push?");
            if (shouldContinue) {
                return Result.OK;
            }
            return Result.ABORT;
        }
        return Result.OK;
    }

    private void readResults() {
        setMessage("Reading results");
        PipelineScanResultsBarToolWindowFactory.getInstance()
                .updateResultsForProject(project,
                        getAllFindings(PIPELINE_RESULTS_JSON),
                        getAllFindings(FILTERED_PIPELINE_RESULTS_JSON));
    }

    @NotNull
    private List<PipelineScanFinding> getAllFindings(String resultsFileName) {
        List<PipelineScanFinding> allFindings = new ArrayList<>();
        File resultsFile = new File(resultsFileName);
        if (!resultsFile.exists()) {
            return allFindings;
        }
        try (FileReader fileReader = new FileReader(resultsFile.getAbsolutePath())) {
            JSONParser parser = new JSONParser(4032);
            JSONObject outerJson = (JSONObject) parser.parse(fileReader);
            JSONArray findingsArray = (JSONArray) outerJson.get("findings");
            for (Object arrayElement : findingsArray) {
                JSONObject currentFinding = (JSONObject) arrayElement;
                JSONObject sourceFileElement = (JSONObject) ((JSONObject) currentFinding.get("files")).get("source_file");
                PipelineScanFinding finding = new PipelineScanFinding(
                        getElementAsLong(currentFinding, "issue_id"),
                        SEVERITY_MAP.get((int) getElementAsLong(currentFinding, "severity")),
                        getElementAsString(currentFinding, "cwe_id"),
                        getElementAsString(currentFinding, "issue_type"),
                        getElementAsString(currentFinding, "display_text"),
                        getElementAsString(sourceFileElement, "file"),
                        getElementAsLong(sourceFileElement, "line"));
                allFindings.add(finding);
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        } finally {
            resultsFile.delete();
        }
        return allFindings;
    }

    private String getElementAsString(JSONObject currentFinding, String elementName) {
        return (String) Optional.ofNullable(currentFinding.get(elementName))
                .filter(elementValue -> elementValue instanceof String)
                .orElse(null);
    }

    private long getElementAsLong(JSONObject currentFinding, String elementName) {
        LOG.info(currentFinding.get(elementName).getClass().getCanonicalName());
        return (long) Optional.ofNullable(currentFinding.get(elementName))
                .filter(elementValue -> elementValue instanceof Long)
                .orElse(0);
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
                ? getScanIssuesFoundMessage(scanReturnCode)
                : "Pipeline scan failed with return code of " + scanReturnCode;
    }

    private String getScanIssuesFoundMessage(int scanReturnCode) {
        return "Pipeline scan detected " + scanReturnCode +
                (isNullOrEmpty(projectSettingsState.getBaselineFile()) ? "" : " new") + " issues" +
                getScanFailCriteriaForErrorMessage();
    }

    private boolean isNullOrEmpty(String baselineFile) {
        return baselineFile != null && !"".equals(baselineFile.trim());
    }

    private String getScanFailCriteriaForErrorMessage() {
        StringBuilder scanFailCriteriaForErrorMessage = new StringBuilder("\n\nScan fail criteria:\n");
        boolean hasAddedASeverity = false;
        ApplicationSettingsState applicationSettingsState = ApplicationSettingsState.getInstance();
        if (applicationSettingsState.isShouldFailOnInformational()) {
            scanFailCriteriaForErrorMessage.append("Informational");
            hasAddedASeverity = true;
        }
        hasAddedASeverity = addForSeverityIfNecessary(applicationSettingsState.isShouldFailOnLow(),
                hasAddedASeverity, scanFailCriteriaForErrorMessage, "Low");
        hasAddedASeverity = addForSeverityIfNecessary(applicationSettingsState.isShouldFailOnMedium(),
                hasAddedASeverity, scanFailCriteriaForErrorMessage, "Medium");
        hasAddedASeverity = addForSeverityIfNecessary(applicationSettingsState.isShouldFailOnHigh(),
                hasAddedASeverity, scanFailCriteriaForErrorMessage, "High");
        hasAddedASeverity = addForSeverityIfNecessary(applicationSettingsState.isShouldFailOnVeryHigh(),
                hasAddedASeverity, scanFailCriteriaForErrorMessage, "Very High");
        return hasAddedASeverity ? scanFailCriteriaForErrorMessage.toString() : "";
    }

    private boolean addForSeverityIfNecessary(boolean isEvaluatingSeverity, boolean hasAddedASeverity,
                                              StringBuilder scanFailCriteriaForErrorMessage, String Low) {
        if (isEvaluatingSeverity) {
            if (hasAddedASeverity) {
                scanFailCriteriaForErrorMessage.append(", ");
            }
            scanFailCriteriaForErrorMessage.append(Low);
            hasAddedASeverity = true;
        }
        return hasAddedASeverity;
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

    private boolean getConfirmationDialogOutput(String aMessage) {
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
        progressIndicator.setFraction(currentFraction * progressMultiplier);
        updateProgressIndicator();
    }

    private void updateProgressIndicator() {
        progressIndicator.pushState();
        progressIndicator.popState();
    }

    public Project getProject() {
        return this.project;
    }
}
