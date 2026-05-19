package com.xblog.controller.admin;

import com.xblog.common.util.OssUtil;
import com.xblog.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/v1/admin")
@Tag(name = "管理-文件上传接口", description = "管理员文件上传接口")
public class UploadController {

    @Resource
    private OssUtil ossUtil;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        log.info("上传文件, fileName={}", file.getOriginalFilename());
        String url = ossUtil.uploadFile(file);
        return Result.success(url);
    }
}
