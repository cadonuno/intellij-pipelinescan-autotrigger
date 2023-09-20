package cadonuno.pipelinescanautotrigger;

import cadonuno.pipelinescanautotrigger.exceptions.VeracodePipelineScanException;
import cadonuno.pipelinescanautotrigger.pipelinescan.OsCommandRunner;
import cadonuno.pipelinescanautotrigger.pipelinescan.PipelineScanFinding;
import cadonuno.pipelinescanautotrigger.pipelinescan.PipelineScanWrapper;
import cadonuno.pipelinescanautotrigger.settings.global.ApplicationSettingsState;
import cadonuno.pipelinescanautotrigger.settings.project.ProjectSettingsState;
import cadonuno.pipelinescanautotrigger.ui.MessageHandler;
import cadonuno.pipelinescanautotrigger.ui.scanresults.PipelineScanResultsBarToolWindowFactory;
import cadonuno.pipelinescanautotrigger.util.Constants;
import cadonuno.pipelinescanautotrigger.util.ScanDirectoryUtil;
import com.google.common.base.Strings;
import com.intellij.dvcs.push.PrePushHandler;
import com.intellij.dvcs.push.PushInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
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
    private List<VeracodePipelineScanException> exceptions;

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
        return startScan(progressIndicator, false);
    }

    @NotNull
    public Result startScan(@NotNull ProgressIndicator progressIndicator, boolean isManualCall) {
        if (project == null
                || !projectSettingsState.isEnabled()
                || (!isManualCall && !projectSettingsState.isShouldScanOnPush())) {
            if (isManualCall) {
                MessageHandler.showMessagePopup("Scan is disabled and won't be started");
            }

            return Result.OK;
        }

        String foundErrors = projectSettingsState.getValidationErrors(ApplicationSettingsState.getInstance(), project);
        if (!Strings.isNullOrEmpty(foundErrors)) {
            MessageHandler.showMessagePopup(foundErrors);
            return Result.ABORT;
        }

        progressMultiplier = progressIndicator instanceof BackgroundableProcessIndicator
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
            try {
                Process process = OsCommandRunner.runCommand("Application Build",
                        projectSettingsState.getBuildCommand(), new File(baseDirectory), this);
                LOG.info("Left with return code of " + process.exitValue());
                if (process.exitValue() != 0) {
                    try {
                        MessageHandler.showErrorPopup(VERACODE_PIPELINE_SCAN + "Unable to build application!", OsCommandRunner.readErrorLog(process));
                    } catch (IOException e) {
                        throw new VeracodePipelineScanException("Unable to build application!", e);
                    }
                    return Optional.empty();
                }
            } catch (VeracodePipelineScanException e) {
                e.showErrorMessage();
                return Optional.empty();
            }
        }
        return Optional.of(applicationSettingsState);
    }

    @NotNull
    private Result runScan(ApplicationSettingsState applicationSettingsState) {
        maxFraction = UPLOADING_FILES_MAX_FRACTION;
        setCurrentFraction(BUILD_STEP_MAX_FRACTION);

        try (PipelineScanWrapper scanWrapper = runPipelineScan(applicationSettingsState)) {
            int scanReturnCode = scanWrapper.getScanReturnCode();
            if (scanReturnCode == Constants.UNABLE_TO_START_ERROR) {
                return Result.ABORT;
            }
            maxFraction = FULL_PROGRESS_BAR;
            setCurrentFraction(FULL_PROGRESS_BAR);
            readResults();
            if (timer.isRunning()) {
                timer.stop();
            }
            if (scanReturnCode != 0) {
                boolean shouldContinue = showScanFailedMessageAndGetStatus(scanReturnCode);
                if (shouldContinue) {
                    return Result.OK;
                }
                return Result.ABORT;
            }
            if (progressIndicator instanceof BackgroundableProcessIndicator) {
                MessageHandler.showMessagePopup("Scan finished, no" + getNewIssuesMessageSection() + " found");
            }
            return Result.OK;
        }
    }

    private boolean showScanFailedMessageAndGetStatus(int scanReturnCode) {
        if (scanReturnCode < Constants.FIRST_NEGATIVE_VALUE) {
            return false;
        }
        if (progressIndicator instanceof BackgroundableProcessIndicator) {
            MessageHandler.showMessagePopup(getScanFinishedConfirmationMessage(scanReturnCode) + "!");
            return true;
        }
        return !getConfirmationDialogOutput(getScanFinishedConfirmationMessage(scanReturnCode) + "!" +
                "\nAbort push?");
    }

    private void readResults() {
        exceptions = new ArrayList<>();
        setMessage("Reading results");
        PipelineScanResultsBarToolWindowFactory.getInstance()
                .updateResultsForProject(project,
                        getAllFindings(PIPELINE_RESULTS_JSON),
                        getAllFindings(FILTERED_PIPELINE_RESULTS_JSON));
        if (exceptions.size() == 1) {
            exceptions.get(0).showErrorMessage();
        } else if (exceptions.size() == 2) {
            exceptions.get(0).showCombinedError(exceptions.get(1));
        }
    }

    @NotNull
    private List<PipelineScanFinding> getAllFindings(String resultsFileName) {
        List<PipelineScanFinding> allFindings = new ArrayList<>();
        File resultsFile = new File(ScanDirectoryUtil.getScanDirectory(baseDirectory), resultsFileName);
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
            exceptions.add(new VeracodePipelineScanException("Unable to read json results at " + resultsFileName, e));
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
                getNewIssuesMessageSection() +
                getScanFailCriteriaForErrorMessage();
    }

    private String getNewIssuesMessageSection() {
        return (isNullOrEmpty(projectSettingsState.getBaselineFile()) ? "" : " new") + " issues";
    }

    private boolean isNullOrEmpty(String baselineFile) {
        return baselineFile == null || "".equals(baselineFile.trim());
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
        hasAddedASeverity = addForSeverityIfNecessary(
                !Strings.isNullOrEmpty(applicationSettingsState.getPolicyToEvaluate()),
                hasAddedASeverity, scanFailCriteriaForErrorMessage, "Policy: " + applicationSettingsState.getPolicyToEvaluate());
        return hasAddedASeverity ? scanFailCriteriaForErrorMessage.toString() : "";
    }

    private boolean addForSeverityIfNecessary(boolean toEvaluate, boolean hasAddedASeverity,
                                              StringBuilder scanFailCriteriaForErrorMessage, String valueToAppend) {
        if (toEvaluate) {
            if (hasAddedASeverity) {
                scanFailCriteriaForErrorMessage.append(", ");
            }
            scanFailCriteriaForErrorMessage.append(valueToAppend);
            hasAddedASeverity = true;
        }
        return hasAddedASeverity;
    }

    private PipelineScanWrapper runPipelineScan(ApplicationSettingsState applicationSettingsState) {
        currentIncrement = 0.1d;
        setMessage(SETTING_UP_PIPELINE_SCANNER_MESSAGE);
        PipelineScanWrapper pipelineScanWrapper = PipelineScanWrapper.acquire(baseDirectory, projectSettingsState);
        if (pipelineScanWrapper.isEmptyWrapper()) {
            return pipelineScanWrapper;
        }
        setMessage(RUNNING_PIPELINE_SCAN_UPLOADING_FILES_MESSAGE);
        pipelineScanWrapper.runScan(applicationSettingsState, this);
        return pipelineScanWrapper;
    }

    private boolean getConfirmationDialogOutput(String aMessage) {
        return JOptionPane.showConfirmDialog(null, aMessage,
                "Veracode Pipeline Scan", JOptionPane.YES_NO_CANCEL_OPTION) == 0;
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
