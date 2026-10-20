package com.shangda.o2o.controller.admin;

import com.shangda.o2o.common.ApiResponse;
import com.shangda.o2o.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 管理端图片上传接口：校验扩展名与大小，UUID 重命名后保存到 o2o.upload-dir
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class UploadController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_SIZE = 10 * 1024 * 1024L;

    @Value("${o2o.upload-dir:./upload/}")
    private String uploadDir;

    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("文件大小不能超过 10MB");
        }
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException("仅支持 jpg/jpeg/png/gif/webp 格式图片");
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension.toLowerCase();
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename).toFile());
        } catch (IOException e) {
            log.error("图片上传失败", e);
            throw new BusinessException("图片上传失败，请稍后重试");
        }
        return ApiResponse.success("上传成功", Map.of("url", "/upload/" + filename));
    }
}
