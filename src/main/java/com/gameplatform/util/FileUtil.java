package com.gameplatform.util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:12
 * @description TODO
 */
@Component
@Slf4j
public class FileUtil {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.max-size}")
    private long maxSize;

    @Value("${upload.allowed-types}")
    private List<String> allowedTypes;

    public boolean isValidFileType(String contentType) {
        return allowedTypes.contains(contentType);
    }

    public boolean isValidFileType(MultipartFile file) {
        return file != null && isValidFileType(file.getContentType());
    }

    public Resource loadFileAsResource(String fileName) throws IOException {
        try {
            Path filePath = Paths.get(uploadPath).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("File not found: " + fileName);
        }
    }

    public String saveFile(MultipartFile file, String directory) throws IOException {
        validateFile(file);

        String fileName = generateFileName(file.getOriginalFilename());
        String filePath = directory + "/" + fileName;

        Path uploadDir = Paths.get(uploadPath + "/" + directory);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 使用NIO保存文件
        Path targetLocation = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return filePath;
    }

    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(uploadPath + "/" + filePath);
        Files.deleteIfExists(path);
    }

    public boolean validateFileType(String contentType) {
        return Arrays.asList(allowedTypes).contains(contentType);
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制");
        }

        String contentType = file.getContentType();
        if (contentType == null || !validateFileType(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型");
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + extension;
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf("."));
    }

    public String getFileUrl(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return "";
        }
        return "/api/files/preview/" + filePath;
    }

    public MediaType getMediaType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        switch (extension) {
            case ".jpg":
            case ".jpeg":
                return MediaType.IMAGE_JPEG;
            case ".png":
                return MediaType.IMAGE_PNG;
            case ".gif":
                return MediaType.IMAGE_GIF;
            case ".pdf":
                return MediaType.APPLICATION_PDF;
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}