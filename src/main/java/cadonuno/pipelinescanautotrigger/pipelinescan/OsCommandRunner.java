package cadonuno.pipelinescanautotrigger.pipelinescan;

import com.intellij.openapi.diagnostic.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class OsCommandRunner {
    private static final Logger logger = Logger.getInstance(OsCommandRunner.class);


    public static int runCommand(String commandToRun) {
        try {
            Process process = Runtime.getRuntime().exec(commandToRun);
            int returnCode = process.waitFor();
            logProcessOutput(process);
            return returnCode;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void logProcessOutput(Process process) throws IOException {
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
}
