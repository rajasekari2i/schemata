package com.opsbeach.sharedlib.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.FileNotFoundException;
import com.opsbeach.sharedlib.exception.InvalidDataException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtil {

    public static void deleteFile(String path) {
        try {
            FileUtils.delete(new File(path));
        } catch (IOException e) {
            throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }

    public static void deleteDirectory(String path) {
        try {
            FileUtils.cleanDirectory(new File(path));
            FileUtils.deleteDirectory(new File(path));
        } catch (IOException e) {
            throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }

    public static void uncompressTarGZ(String folderPath, String filePath) {
        File dest = new File(folderPath);
        try (var buf = new BufferedInputStream(new FileInputStream(filePath));
             TarArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(buf));) {
            
            TarArchiveEntry tarEntry = tarIn.getNextTarEntry();
        // tarIn is a TarArchiveInputStream
        while (tarEntry != null) {
            // create a file with the same name as the tarEntry
            File destPath = new File(dest, tarEntry.getName());
            log.info("working: " + destPath.getCanonicalPath());
            if (tarEntry.isDirectory()) {
                destPath.mkdirs();
            } else {
                destPath.createNewFile();
                write(destPath, tarIn);
            }
            tarEntry = tarIn.getNextTarEntry();
        }
        } catch (IOException e) {
            throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }

    private static void write(File destPath, TarArchiveInputStream tarIn) {
        byte [] btoRead = new byte[1024];
        try (BufferedOutputStream bout =  new BufferedOutputStream(new FileOutputStream(destPath));) {
            int len = 0;
            while((len = tarIn.read(btoRead)) != -1)
            {
                bout.write(btoRead,0,len);
            }
        } catch (IOException e) {
            throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
        btoRead = null;
    }

    public static String getChecksum(String filePath) {
        try (InputStream is = Files.newInputStream(Paths.get(filePath))){
            return DigestUtils.md5Hex(is);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }       
    }

    public static String getChecksum(byte[] bytes) {
        return DigestUtils.md5Hex(bytes);
    }

    public static List<String> deepSearchFiles(String folderPath, String fileType) {
        try (Stream<Path> walk = Files.walk(Paths.get(folderPath))) {
            return walk.filter(p -> !Files.isDirectory(p))
                       .map(Path::toString)
                       .filter(f -> (f.endsWith(fileType)))
                       .toList();
        } catch (IOException e) {
            throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }

    public static boolean isCSVFormat(MultipartFile file) {
        if (("text/csv").equals(file.getContentType()) || ("application/vnd.ms-excel").equals(file.getContentType())) {
            return true;
        }
        return false;
    }

    public static List<String> getCsvFileHeaders(MultipartFile multipartFile) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true).build());) {
            return csvParser.getHeaderNames();
        } catch (IOException e) {
            throw new InvalidDataException(ErrorCode.INVALID_FILE, e.getMessage());
        }
    }

    public static ArrayNode readCsvFile(MultipartFile multipartFile) {
        try {
            return readCsvFile(multipartFile.getInputStream());
        } catch (IOException e) {
                throw new InvalidDataException(ErrorCode.INVALID_FILE, e.getMessage());
            }
    }

    public static ArrayNode readCsvFile(byte[] bytes) {
        return readCsvFile(new ByteArrayInputStream(bytes));
    }

    public static ArrayNode readCsvFile(InputStream file){
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true).build());) {
            var headers = csvParser.getHeaderNames();
            var size = csvParser.getHeaderNames().size();
            var records = csvParser.getRecords();
            ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode(records.size());
            records.forEach(record -> {
                var payload = JsonNodeFactory.instance.objectNode();
                for (int i=0; i<size; i++) {
                    payload.put(headers.get(i), record.get(headers.get(i)));
                }
                arrayNode.add(payload);
            });
            return arrayNode;
        } catch (IOException e) {
            throw new InvalidDataException(ErrorCode.INVALID_FILE, e.getMessage());
        }
    }

    public static String getBaseFileName(String filename) {
        if (filename == null) return null;

        String[] knownCompositeExtensions = { ".tar.gz", ".tar.bz2", ".tar.xz" };

        for (String ext : knownCompositeExtensions) {
            if (filename.endsWith(ext)) {
                return filename.substring(0, filename.length() - ext.length());
            }
        }

        // Default: remove only last extension
        int lastDot = filename.lastIndexOf('.');
        return (lastDot != -1) ? filename.substring(0, lastDot) : filename;
    }

    public static void unzip(String zipFilePath, String destDirPath) {
        File destDir = new File(destDirPath);
        if (!destDir.exists()) {
            destDir.mkdirs(); // Create target folder if it doesn't exist
        }

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                File outFile = new File(destDir, entry.getName());

                if (entry.isDirectory()) {
                    outFile.mkdirs(); // create directory
                } else {
                    // Create parent directories if needed
                    File parent = outFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }

                    // Write file content
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile))) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zipIn.read(buffer)) > 0) {
                            bos.write(buffer, 0, len);
                        }
                    }
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        } catch (IOException e) {
            throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }
}
