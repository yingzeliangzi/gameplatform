package com.gameplatform.util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:12
 * @description TODO
 */
@Slf4j
@Component
public class FileUtil {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.max-size}")
    private long maxSize;

    @Value("${upload.allowed-types}")
    private String[] allowedTypes;

    public String saveFile(MultipartFile file, String directory) throws IOException {
        validateFile(file);

        String fileName = generateFileName(file.getOriginalFilename());
        String filePath = directory + "/" + fileName;

        Path uploadDir = Paths.get(uploadPath + "/" + directory);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        File dest = new File(uploadPath + "/" + filePath);
        file.transferTo(dest);

        return filePath;
    }

    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadPath + "/" + filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Delete file failed: {}", filePath, e);
        }
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        if (file.getSize() > maxSize) {
            throw new IOException("File size exceeds maximum limit");
        }

        String contentType = file.getContentType();
        boolean isAllowed = false;
        for (String type : allowedTypes) {
            if (type.equals(contentType)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new IOException("File type not allowed");
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + extension;
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return filename.substring(lastDot);
    }

    public boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    public String getFileUrl(String filePath) {
        return "/uploads/" + filePath;
    }
}
