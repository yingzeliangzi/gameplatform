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
@Component
@Slf4j
public class FileUtil {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.max-size}")
    private long maxSize;

    @Value("${upload.allowed-types}")
    private String[] allowedTypes;

    public Resource loadFileAsResource(String fileName) throws IOException {
        try {
            Path filePath = Paths.get(uploadPath).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if(resource.exists()) {
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
}