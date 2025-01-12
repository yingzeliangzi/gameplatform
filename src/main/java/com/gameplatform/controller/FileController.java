package com.gameplatform.controller;

import com.gameplatform.annotation.RequirePermission;
import com.gameplatform.common.Result;
import com.gameplatform.exception.BusinessException;
import com.gameplatform.exception.FileOperationException;
import com.gameplatform.util.FileUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/05 12:25
 * @description TODO
 */
@Tag(name = "文件接口", description = "文件上传下载相关操作接口")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "Bearer Authentication")
public class FileController {

    private final FileUtil fileUtil;

    @Value("${upload.path}")
    private String uploadPath;

    @Operation(summary = "上传文件", description = "上传单个文件")
    @PostMapping("/upload")
    @RequirePermission("file:upload")
    public Result<Map<String, String>> uploadFile(
            @Parameter(description = "文件", required = true)
            @NotNull(message = "文件不能为空") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件目录") @RequestParam(required = false, defaultValue = "common") String directory,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String filePath = fileUtil.saveFile(file, directory);
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUtil.getFileUrl(filePath));
            response.put("path", filePath);
            return Result.success(response);
        } catch (IOException e) {
            throw new FileOperationException("文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量上传文件", description = "上传多个文件")
    @PostMapping("/upload/batch")
    @RequirePermission("file:upload")
    public Result<List<Map<String, String>>> uploadFiles(
            @Parameter(description = "文件列表", required = true)
            @NotNull(message = "文件不能为空") @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "文件目录") @RequestParam(required = false, defaultValue = "common") String directory) {
        List<Map<String, String>> results = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String filePath = fileUtil.saveFile(file, directory);
                Map<String, String> fileInfo = new HashMap<>();
                fileInfo.put("url", fileUtil.getFileUrl(filePath));
                fileInfo.put("path", filePath);
                results.add(fileInfo);
            } catch (IOException e) {
                throw new FileOperationException("文件上传失败: " + e.getMessage());
            }
        }
        return Result.success(results);
    }

    @Operation(summary = "下载文件", description = "下载指定文件")
    @GetMapping("/download/{fileName}")
    @RequirePermission("file:download")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "文件名", required = true)
            @PathVariable String fileName) {
        try {
            Resource resource = fileUtil.loadFileAsResource(fileName);
            String filename = URLEncoder.encode(resource.getFilename(), StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            throw new FileOperationException("文件下载失败: " + e.getMessage());
        }
    }

    @Operation(summary = "预览文件", description = "预览指定文件")
    @GetMapping("/preview/{fileName}")
    public ResponseEntity<Resource> previewFile(@PathVariable String fileName) {
        try {
            Resource resource = fileUtil.loadFileAsResource(fileName);
            MediaType mediaType = fileUtil.getMediaType(fileName);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);
        } catch (IOException e) {
            throw new FileOperationException("文件预览失败: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BusinessException("预览文件时发生错误: " + e.getMessage());
        }
    }

    @Operation(summary = "删除文件", description = "删除指定文件")
    @DeleteMapping("/{fileName}")
    public Result<Void> deleteFile(@PathVariable String fileName) {
        String filePath = uploadPath + "/" + fileName;
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
            return Result.success();
        } catch (IOException e) {
            throw new BusinessException("文件删除失败: " + e.getMessage());
        }
    }
}