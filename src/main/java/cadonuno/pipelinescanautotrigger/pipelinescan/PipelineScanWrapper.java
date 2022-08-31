package cadonuno.pipelinescanautotrigger.pipelinescan;


import cadonuno.pipelinescanautotrigger.settings.ApplicationSettingsState;
import cadonuno.pipelinescanautotrigger.util.ZipHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;

public class PipelineScanWrapper implements Closeable {
    private static final Logger logger = Logger.getInstance(PipelineScanWrapper.class);
    private static final String FILE_URL = "https://downloads.veracode.com/securityscan/pipeline-scan-LATEST.zip";
    private static final String VERACODE_PIPELINE_SCAN_DIRECTORY = "Veracode-pipelinescan";
    private static final String ZIP_FILE = VERACODE_PIPELINE_SCAN_DIRECTORY + "/pipeline-scan-LATEST.zip";
    private static final int CONNECT_TIMEOUT = 1000;
    private static final int READ_TIMEOUT = 1000;
    private final String baseDirectory;

    private PipelineScanWrapper(Project project) {
        baseDirectory = project.getBasePath();
        cleanupDirectory();
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

    public static PipelineScanWrapper acquire(Project project) {
        return new PipelineScanWrapper(project);
    }

    @Override
    public void close() {
        //cleanupDirectory();
    }

    private void cleanupDirectory() {
        File pipelineScanDirectory = new File(baseDirectory, VERACODE_PIPELINE_SCAN_DIRECTORY);
        if (pipelineScanDirectory.exists()) {
            try {
                FileUtils.deleteDirectory(pipelineScanDirectory);
            } catch (IOException e) {
                // TODO: note that it was unable to delete
            }
        }
    }

    public int startScan(ApplicationSettingsState applicationSettingsState) {
        try {
            Process process = Runtime.getRuntime().exec(buildCommand(
                    new File(new File(baseDirectory, VERACODE_PIPELINE_SCAN_DIRECTORY), "pipeline-scan.jar"),
                    applicationSettingsState));
            int returnCode = process.waitFor();
            logProcessOutput(process);
            return returnCode;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void logProcessOutput(Process process) throws IOException {
        try (InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
             BufferedReader input = new BufferedReader(inputStreamReader)) {

            String line;

            StringBuilder outputReader = new StringBuilder();
            while ((line = input.readLine()) != null) {
                outputReader.append(line).append('\n');
            }
            logger.info("PipelineScan output: ");
            logger.info(outputReader.toString());
        }
    }

    @NotNull
    private String buildCommand(File pipelineScanner, ApplicationSettingsState applicationSettingsState) {
        StringBuilder commandBuilder = new StringBuilder("java -jar \"" + pipelineScanner.getAbsolutePath() + "\" ");
        appendParameter(commandBuilder, "veracode_api_id", applicationSettingsState.getApiId());
        appendParameter(commandBuilder, "veracode_api_key", applicationSettingsState.getApiKey());
        appendParameter(commandBuilder, "fail_on_severity", applicationSettingsState.getFailOnSeverity());
        appendParameter(commandBuilder, "file",
                new File(baseDirectory, applicationSettingsState.getFileToScan()).toString());
        appendParameter(commandBuilder, "json_output_file",
                new File(baseDirectory, applicationSettingsState.getFileToScan()).toString());
        appendParameter(commandBuilder, "filtered_json_output_file",
                new File(baseDirectory, applicationSettingsState.getFileToScan()).toString());
        logger.info(commandBuilder.toString());
        return commandBuilder.toString();
    }

    private void appendParameter(StringBuilder commandBuilder, String parameterName, String parameterValue) {
        commandBuilder.append("--").append(parameterName).append(' ').append('"').append(parameterValue).append('"').append(' ');
    }
}
