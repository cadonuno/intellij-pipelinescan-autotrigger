package cadonuno.pipelinescanautotrigger.util;

import java.io.File;

public class ScanDirectoryUtil {
    private static final String VERACODE_PIPELINE_SCAN_DIRECTORY = "Veracode-pipelinescan";
    private static final String ZIP_FILE = "pipeline-scan-LATEST.zip";

    public static File getScanDirectory(String baseDirectory) {
        return new File(baseDirectory, VERACODE_PIPELINE_SCAN_DIRECTORY);
    }

    public static File getZipFile(String baseDirectory) {
        return new File(new File(baseDirectory, VERACODE_PIPELINE_SCAN_DIRECTORY), ZIP_FILE);
    }
}
