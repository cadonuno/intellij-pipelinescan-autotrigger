package cadonuno.pipelinescanautotrigger.pipelinescan;


import cadonuno.pipelinescanautotrigger.util.ZipHandler;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;

public class PipelineScanWrapper implements Closeable {
    private static final String FILE_URL = "https://downloads.veracode.com/securityscan/pipeline-scan-LATEST.zip";
    private static final String VERACODE_PIPELINESCAN_DIRECTORY = "./Veracode-pipelinescan/";
    private static final String ZIP_FILE = VERACODE_PIPELINESCAN_DIRECTORY + "pipeline-scan-LATEST.zip";
    private static final int CONNECT_TIMEOUT = 1000;
    private static final int READ_TIMEOUT = 1000;

    public boolean didDownload = false;

    private PipelineScanWrapper() {
        cleanupDirectory();
        downloadZip();
        ZipHandler.unzipFile(ZIP_FILE, VERACODE_PIPELINESCAN_DIRECTORY);
    }

    private void downloadZip() {
        try {
            FileUtils.copyURLToFile(
                    new URL(FILE_URL),
                    new File(ZIP_FILE),
                    CONNECT_TIMEOUT,
                    READ_TIMEOUT);
        } catch (IOException e) {
            return;
        }
        didDownload = true;
    }

    public static PipelineScanWrapper acquire() {
        return new PipelineScanWrapper();
    }

    @Override
    public void close() {
        /*if (!didDownload) {
            return;
        }
        cleanupDirectory();*/
    }

    private void cleanupDirectory() {
        File pipelineScanDirectory = new File(VERACODE_PIPELINESCAN_DIRECTORY);
        if (pipelineScanDirectory.exists()) {
            try {
                FileUtils.deleteDirectory(pipelineScanDirectory);
            } catch (IOException e) {
                // TODO: note that it was unable to delete
            }
        }
    }
}
