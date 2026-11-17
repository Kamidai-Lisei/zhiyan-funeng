package com.jay.aigcoj.controller;

import com.jay.aigcoj.common.BaseResponse;
import com.jay.aigcoj.common.ErrorCode;
import com.jay.aigcoj.common.ResultUtils;
import com.jay.aigcoj.exception.BusinessException;
import com.jay.aigcoj.exception.ThrowUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${file.upload-dir:./data/uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXT = Arrays.asList(
            "pdf", "doc", "docx", "ppt", "pptx", "zip", "rar", "png", "jpg", "jpeg", "webp", "md", "txt");

    @PostMapping("/upload")
    public BaseResponse<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                    @RequestParam(value = "biz", required = false) String biz) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件为空");
        long maxSize = 30L * 1024 * 1024;
        ThrowUtils.throwIf(file.getSize() > maxSize, ErrorCode.PARAMS_ERROR, "文件不能超过 30MB");
        String originalName = StringUtils.defaultString(file.getOriginalFilename(), "file");
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            ext = originalName.substring(dot + 1).toLowerCase();
        }
        ThrowUtils.throwIf(!ALLOWED_EXT.contains(ext), ErrorCode.PARAMS_ERROR, "不支持的文件类型：" + ext);

        String folder = StringUtils.isBlank(biz) ? "common" : biz.replaceAll("[^a-zA-Z0-9_-]", "");
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File dir = new File(uploadDir, folder);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建上传目录失败");
        }
        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件保存失败");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("url", "/api/file/" + folder + "/" + fileName);
        result.put("name", originalName);
        result.put("size", file.getSize());
        result.put("type", ext);
        return ResultUtils.success(result);
    }
}
