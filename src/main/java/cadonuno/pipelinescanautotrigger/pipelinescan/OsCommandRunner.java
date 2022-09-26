package cadonuno.pipelinescanautotrigger.pipelinescan;

import cadonuno.pipelinescanautotrigger.PipelineScanAutoPrePushHandler;
import com.intellij.openapi.diagnostic.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OsCommandRunner {
    private static final Logger logger = Logger.getInstance(OsCommandRunner.class);

    public static int runCommand(String title, String commandToRun,
                                 PipelineScanAutoPrePushHandler pipelineScanAutoPrePushHandler) {
        Process process;
        ProcessBuilder processBuilder = new ProcessBuilder(convertCommandToArray(commandToRun))
                .redirectErrorStream(true);
        try {
            logger.info("Running " + title);
            logger.debug(commandToRun);
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
            throw new RuntimeException(e);
        }
        return process.exitValue();
    }

    private static void logLine(PipelineScanAutoPrePushHandler pipelineScanAutoPrePushHandler, String line) {
        if (line != null) {
            logger.info(line);
            pipelineScanAutoPrePushHandler.updateProgressIndicatorSecondaryMessage(line);
        }
    }

    private static String[] convertCommandToArray(String commandToRun) {
        String[] splitBySpaces = commandToRun.split(" ");
        logger.info("Building command: " + commandToRun);
        List<String> parametersAsList = new ArrayList<>();
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
        logger.info("Adding to list: " + element);
        parametersAsList.add(element);
    }
}
