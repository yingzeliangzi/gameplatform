package com.gameplatform.util;

import com.gameplatform.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;

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
    private List<String> allowedTypes;

    private static final Map<String, MediaType> MEDIA_TYPE_MAP = new HashMap<>();

    static {
        MEDIA_TYPE_MAP.put(".jpg", MediaType.IMAGE_JPEG);
        MEDIA_TYPE_MAP.put(".jpeg", MediaType.IMAGE_JPEG);
        MEDIA_TYPE_MAP.put(".png", MediaType.IMAGE_PNG);
        MEDIA_TYPE_MAP.put(".gif", MediaType.IMAGE_GIF);
        MEDIA_TYPE_MAP.put(".pdf", MediaType.APPLICATION_PDF);
        MEDIA_TYPE_MAP.put(".doc", MediaType.parseMediaType("application/msword"));
        MEDIA_TYPE_MAP.put(".docx", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
    }

    public String saveFile(MultipartFile file, String directory) throws IOException {
        validateFile(file);

        String fileName = generateFileName(file.getOriginalFilename());
        String fullDirectory = uploadPath + "/" + directory;
        Path uploadDir = Paths.get(fullDirectory);

        if (!Files.exists(uploadDir)) {
            try {
                Files.createDirectories(uploadDir);
            } catch (IOException e) {
                log.error("创建目录失败: {}", e.getMessage());
                throw new BusinessException(BusinessException.ErrorCode.SYSTEM_ERROR, "创建目录失败");
            }
        }

        Path filePath = uploadDir.resolve(fileName);
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return directory + "/" + fileName;
        } catch (IOException e) {
            log.error("保存文件失败: {}", e.getMessage());
            throw new BusinessException(BusinessException.ErrorCode.SYSTEM_ERROR, "保存文件失败");
        }
    }

    public Resource loadFileAsResource(String filePath) throws IOException {
        try {
            Path path = Paths.get(uploadPath).resolve(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IOException("文件不存在或无法读取: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new IOException("文件路径错误: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IOException("无法读取文件: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(uploadPath).resolve(filePath).normalize();
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("删除文件失败: {}", e.getMessage());
            throw new BusinessException(BusinessException.ErrorCode.SYSTEM_ERROR, "删除文件失败");
        }
    }

    public MediaType getMediaType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return MEDIA_TYPE_MAP.getOrDefault(extension, MediaType.APPLICATION_OCTET_STREAM);
    }

    public boolean isValidFileType(MultipartFile file) {
        if (file == null || file.getContentType() == null) {
            return false;
        }
        return allowedTypes.contains(file.getContentType());
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(BusinessException.ErrorCode.INVALID_PARAMETER, "文件为空");
        }

        if (file.getSize() > maxSize) {
            throw new BusinessException(BusinessException.ErrorCode.INVALID_PARAMETER,
                    String.format("文件大小超过限制: %d MB", maxSize / (1024 * 1024)));
        }

        if (!isValidFileType(file)) {
            throw new BusinessException(BusinessException.ErrorCode.INVALID_PARAMETER,
                    "不支持的文件类型，允许的类型: " + String.join(", ", allowedTypes));
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + extension;
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf(".");
        return dotIndex == -1 ? "" : filename.substring(dotIndex);
    }

    public String getFileUrl(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return "";
        }
        return "/api/files/preview/" + filePath;
    }

    public void cleanupTempFiles() {
        try {
            Path tempDir = Paths.get(uploadPath, "temp");
            if (Files.exists(tempDir)) {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.error("删除临时文件失败: {}", e.getMessage());
                            }
                        });
            }
        } catch (IOException e) {
            log.error("清理临时文件失败: {}", e.getMessage());
        }
    }
}