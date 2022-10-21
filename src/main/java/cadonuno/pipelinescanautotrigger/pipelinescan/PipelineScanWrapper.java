package cadonuno.pipelinescanautotrigger.pipelinescan;


import cadonuno.pipelinescanautotrigger.PipelineScanAutoPrePushHandler;
import cadonuno.pipelinescanautotrigger.settings.credentials.VeracodeCredentials;
import cadonuno.pipelinescanautotrigger.settings.global.ApplicationSettingsState;
import cadonuno.pipelinescanautotrigger.settings.credentials.CredentialsTypeEnum;
import cadonuno.pipelinescanautotrigger.settings.project.ProjectSettingsState;
import cadonuno.pipelinescanautotrigger.util.ZipHandler;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;

public class PipelineScanWrapper implements Closeable {
    private static final Logger LOG = Logger.getInstance(PipelineScanWrapper.class);
    private static final String FILE_URL = "https://downloads.veracode.com/securityscan/pipeline-scan-LATEST.zip";
    private static final String VERACODE_PIPELINE_SCAN_DIRECTORY = "Veracode-pipelinescan";
    private static final String ZIP_FILE = VERACODE_PIPELINE_SCAN_DIRECTORY + "/pipeline-scan-LATEST.zip";
    private static final int CONNECT_TIMEOUT = 1000;
    private static final int READ_TIMEOUT = 1000;
    private final String baseDirectory;
    private final ProjectSettingsState projectSettingsState;

    private PipelineScanWrapper(String baseDirectory, ProjectSettingsState projectSettingsState) {
        this.baseDirectory = baseDirectory;
        this.projectSettingsState = projectSettingsState;
        cleanupDirectory(baseDirectory);
        File pipelineScannerZipLocation = new File(baseDirectory, ZIP_FILE);
        downloadZip(pipelineScannerZipLocation);
        ZipHandler.unzipFile(pipelineScannerZipLocation);
    }

    private void downloadZip(File pipelineScannerZipLocation) {
        try {
            FileUtils.copyURLToFile(
                    new URL(FILE_URL),
                    pipelineScannerZipLocation,
                    CONNECT_TIMEOUT,
                    READ_TIMEOUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static PipelineScanWrapper acquire(String baseDirectory,
                                              ProjectSettingsState projectSettingsState) {
        return new PipelineScanWrapper(baseDirectory, projectSettingsState);
    }

    @Override
    public void close() {
        cleanupDirectory(baseDirectory);
    }

    public static void cleanupDirectory(String baseDirectory) {
        File pipelineScanDirectory = new File(baseDirectory, VERACODE_PIPELINE_SCAN_DIRECTORY);
        if (pipelineScanDirectory.exists()) {
            try {
                FileUtils.deleteDirectory(pipelineScanDirectory);
            } catch (IOException e) {
                // TODO: note that it was unable to delete
            }
        }
    }

    public int startScan(ApplicationSettingsState applicationSettingsState,
                         PipelineScanAutoPrePushHandler pipelineScanAutoPrePushHandler) {
        return OsCommandRunner.runCommand("pipeline scan",
                buildCommand(new File(new File(baseDirectory, VERACODE_PIPELINE_SCAN_DIRECTORY), "pipeline-scan.jar"),
                        applicationSettingsState), pipelineScanAutoPrePushHandler);
    }

    @NotNull
    private String buildCommand(File pipelineScanner, ApplicationSettingsState applicationSettingsState) {
        StringBuilder commandBuilder = new StringBuilder("java -jar \"" + pipelineScanner.getAbsolutePath() + "\" ");
        addCredentialsParameters(applicationSettingsState, commandBuilder);
        appendParameter(commandBuilder, "fail_on_severity", applicationSettingsState.getFailOnSeverity());
        appendParameter(commandBuilder, "file",
                new File(baseDirectory, projectSettingsState.getFileToScan()).getAbsolutePath());
        appendParameter(commandBuilder, "json_output_file", PipelineScanAutoPrePushHandler.PIPELINE_RESULTS_JSON);
        appendParameter(commandBuilder, "filtered_json_output_file", PipelineScanAutoPrePushHandler.FILTERED_PIPELINE_RESULTS_JSON);
        if (projectSettingsState.getBaselineFile() != null) {
            String trimmedBaselineFile = projectSettingsState.getBaselineFile().trim();
            if (!trimmedBaselineFile.equals("")) {
                appendParameter(commandBuilder, "baseline_file",
                        new File(baseDirectory, trimmedBaselineFile).toString());
            }
        }
        LOG.info(commandBuilder.toString());
        return commandBuilder.toString();
    }

    private void addCredentialsParameters(ApplicationSettingsState applicationSettingsState,
                                          StringBuilder commandBuilder) {
        if (applicationSettingsState.getCredentialsType() == CredentialsTypeEnum.CredentialsFile) {
            VeracodeCredentials veracodeCredentials = getCredentialsFromFile(applicationSettingsState);
            appendParameter(commandBuilder, "veracode_api_id", veracodeCredentials.getApiId());
            appendParameter(commandBuilder, "veracode_api_key", veracodeCredentials.getApiKey());
        } else {
            appendParameter(commandBuilder, "veracode_api_id", applicationSettingsState.getApiId());
            appendParameter(commandBuilder, "veracode_api_key", applicationSettingsState.getApiKey());
        }
    }

    private VeracodeCredentials getCredentialsFromFile(ApplicationSettingsState applicationSettingsState) {
        String userHome = System.getProperty("user.home");
        File credentialsFile = new File(new File(userHome, ".veracode"), "credentials");
        if (!credentialsFile.exists()) {
            throw new RuntimeException("Credentials file not found at " +
                    credentialsFile.getAbsolutePath());
        }
        try (FileReader fileReader = new FileReader(credentialsFile);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            return getCredentialsFromFile(credentialsFile, bufferedReader, applicationSettingsState);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private VeracodeCredentials getCredentialsFromFile(File credentialsFile,
                                                       BufferedReader bufferedReader,
                                                       ApplicationSettingsState applicationSettingsState) throws IOException {
        String apiId = null;
        String apiKey = null;
        boolean foundProfile = false;
        for (String currentLine; (currentLine = bufferedReader.readLine()) != null; ) {
            currentLine = currentLine.trim();
            if (!currentLine.isEmpty()) {
                if (!foundProfile && currentLine.startsWith(
                        wrapProfileName(applicationSettingsState))) {
                    foundProfile = true;
                } else if (foundProfile) {
                    if (currentLine.charAt(0) == '[') {
                        break;
                    } else if (currentLine.startsWith("veracode_api_key_id")) {
                        apiId = getCredentialValue(currentLine);
                    } else if (currentLine.startsWith("veracode_api_key_secret")) {
                        apiKey = getCredentialValue(currentLine);
                    }
                }
            }
        }
        if (apiId == null || apiKey == null) {
            throw new RuntimeException("Unable to find profile " + applicationSettingsState.getCredentialsProfileName() +
                    " in the credentials file at: " + credentialsFile.getAbsolutePath());
        }
        return new VeracodeCredentials(apiId, apiKey);
    }

    private String wrapProfileName(ApplicationSettingsState applicationSettingsState) {
        return "[" + applicationSettingsState.getCredentialsProfileName() + "]";
    }

    private String getCredentialValue(String value) {
        return value == null ? null : StringUtils.substringAfter(value, "=").trim();
    }

    private void appendParameter(StringBuilder commandBuilder, String parameterName, String parameterValue) {
        commandBuilder.append("--").append(parameterName).append(' ').append('"').append(parameterValue).append('"').append(' ');
    }
}
