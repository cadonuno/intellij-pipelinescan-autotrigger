package cadonuno.pipelinescanautotrigger.util;

import cadonuno.pipelinescanautotrigger.exceptions.VeracodePipelineScanException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipHandler {
    public static void unzipFile(File fileToUnzip) throws VeracodePipelineScanException {
        try (InputStream fileInputStream = new FileInputStream(fileToUnzip)) {
            ZipHandler.unzipFile(fileInputStream, new File(fileToUnzip.getParent()));
        } catch (IOException e) {
            throw new VeracodePipelineScanException("Unable to unzip pipeline scanner file", e);
        }
    }

    public static void unzipFile(InputStream inputStream, File targetDirectory) throws IOException {
        Path targetDirectoryPath = targetDirectory.toPath().toAbsolutePath();
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            for (ZipEntry zipEntry; (zipEntry = zipInputStream.getNextEntry()) != null; ) {
                Path resolvedPath = targetDirectoryPath.resolve(zipEntry.getName()).normalize();
                if (resolvedPath.endsWith("pipeline-scan.jar")) {
                    Files.copy(zipInputStream, resolvedPath);
                }
            }
        }
    }
}
