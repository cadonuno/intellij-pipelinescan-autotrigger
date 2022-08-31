package cadonuno.pipelinescanautotrigger.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipHandler {
    public static void unzipFile(String baseDirectory, String fileName, String targetDirectory) {
        try (InputStream fileInputStream = new FileInputStream(new File(baseDirectory, fileName))) {
            ZipHandler.unzipFile(fileInputStream, new File(baseDirectory, targetDirectory).toPath());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unzipFile(InputStream inputStream, Path targetDirectory) throws IOException {
        targetDirectory = targetDirectory.toAbsolutePath();
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            for (ZipEntry zipEntry; (zipEntry = zipInputStream.getNextEntry()) != null; ) {
                Path resolvedPath = targetDirectory.resolve(zipEntry.getName()).normalize();
                if (!resolvedPath.startsWith(targetDirectory)) {
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
