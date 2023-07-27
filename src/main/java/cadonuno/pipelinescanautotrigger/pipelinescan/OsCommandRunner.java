package cadonuno.pipelinescanautotrigger.pipelinescan;

import cadonuno.pipelinescanautotrigger.PipelineScanAutoPrePushHandler;
import cadonuno.pipelinescanautotrigger.exceptions.VeracodePipelineScanException;
import com.google.common.base.Strings;
import com.intellij.openapi.diagnostic.Logger;
import io.netty.util.internal.PlatformDependent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OsCommandRunner {
    private static final Logger LOG = Logger.getInstance(OsCommandRunner.class);

    public static Process runCommand(String title, String commandToRun, File workDirectory,
                                     PipelineScanAutoPrePushHandler pipelineScanAutoPrePushHandler) throws VeracodePipelineScanException {
        Process process;
        ProcessBuilder processBuilder = new ProcessBuilder(convertCommandToArray(commandToRun))
                .directory(workDirectory);
        try {
            LOG.info("Running " + title);
            LOG.debug(commandToRun);
            process = processBuilder.start();
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                do {
                    line = bufferedReader.readLine();
                    logLine(pipelineScanAutoPrePushHandler, line);
                } while (line != null);
            }
        } catch (IOException e) {
            throw new VeracodePipelineScanException(title, e);
        }
        return process;
    }

    private static void logLine(PipelineScanAutoPrePushHandler pipelineScanAutoPrePushHandler, String line) {
        if (line != null) {
            LOG.info(line);
            pipelineScanAutoPrePushHandler.updateProgressIndicatorSecondaryMessage(line);
        }
    }

    private static String[] convertCommandToArray(String commandToRun) {
        String[] splitBySpaces = commandToRun.split(" ");
        LOG.info("Building command: " + commandToRun);
        List<String> parametersAsList = new ArrayList<>(
                PlatformDependent.isWindows()
                        ? Arrays.asList("cmd.exe", "/C")
                        : Arrays.asList("sh", "-c"));
        String buffer = "";
        for (String element : splitBySpaces) {
            if ("".equals(buffer)) {
                if (element.startsWith("\"") && !element.endsWith("\"")) {
                    buffer = element;
                } else {
                    addToList(parametersAsList, element);
                }
            } else {
                buffer += " " + element;
                if (element.endsWith("\"")) {
                    addToList(parametersAsList, buffer);
                    buffer = "";
                }
            }
        }
        return parametersAsList.toArray(new String[0]);
    }

    private static void addToList(List<String> parametersAsList, String element) {
        LOG.info("Adding to list: " + element);
        parametersAsList.add(element);
    }

    public static String readErrorLog(Process process) throws IOException {
        String errors = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        return Strings.isNullOrEmpty(errors)
                ? null
                : errors;
    }
}
