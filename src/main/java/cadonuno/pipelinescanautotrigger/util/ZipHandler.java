package cadonuno.pipelinescanautotrigger.util;

import cadonuno.pipelinescanautotrigger.pipelinescan.PipelineScanWrapper;
import com.intellij.ide.plugins.PluginManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipHandler {
    public static void unzipFile(File fileToUnzip, String targetDirectory) {
        PipelineScanWrapper.log("Unzipping file: " + fileToUnzip.getAbsolutePath());
        try (InputStream fileInputStream = new FileInputStream(fileToUnzip)) {
            ZipHandler.unzipFile(fileInputStream, new File(fileToUnzip.getParent(), targetDirectory));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unzipFile(InputStream inputStream, File targetDirectory) throws IOException {
        PipelineScanWrapper.log("Unzipping to: " + targetDirectory.getAbsolutePath());
        Path targetDirectoryPath = targetDirectory.toPath().toAbsolutePath();
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            for (ZipEntry zipEntry; (zipEntry = zipInputStream.getNextEntry()) != null; ) {
                Path resolvedPath = targetDirectoryPath.resolve(zipEntry.getName()).normalize();
                if (!resolvedPath.startsWith(targetDirectoryPath)) {
                    throw new RuntimeException("Entry with an illegal path: "
                            + zipEntry.getName());
                }
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(resolvedPath);
                } else {
                    Files.createDirectories(resolvedPath.getParent());
                    Files.copy(zipInputStream, resolvedPath);
                }
            }
        }
    }
}
