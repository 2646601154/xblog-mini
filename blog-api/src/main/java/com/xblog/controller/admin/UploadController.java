package com.xblog.controller.admin;

import com.xblog.common.util.OssUtil;
import com.xblog.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/v1/admin")
@Tag(name = "管理-文件上传接口", description = "管理员文件上传接口")
public class UploadController {
    private final OssUtil ossUtil;

    public UploadController(@Autowired(required = false) OssUtil ossUtil) {
        this.ossUtil = ossUtil;
    }

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam(required = false, defaultValue = "common") String dir) {
        if (ossUtil == null) {
            return Result.error(500, "OSS 未配置，无法上传文件");
        }
        log.info("上传文件, fileName={}, dir={}", file.getOriginalFilename(), dir);
        String url = ossUtil.uploadFile(file, dir);
        return Result.success(url);
    }
}
