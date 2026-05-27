package com.xblog.controller.admin;

import com.xblog.entity.PageResult;
import com.xblog.entity.Result;
import com.xblog.service.MediaService;
import com.xblog.vo.MediaImageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("adminMediaController")
@RequestMapping("/v1/admin/media")
@Tag(name = "管理-图片管理接口", description = "管理员图片管理接口")
public class MediaController {
    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @Operation(summary = "获取图片列表")
    @GetMapping
    public Result<PageResult<MediaImageVo>> getMediaList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("获取图片列表: page={}, size={}", page, size);
        return Result.success(mediaService.getMediaList(page, size));
    }

    @Operation(summary = "删除图片")
    @DeleteMapping
    public Result<Void> deleteImage(@RequestParam String url) {
        log.info("删除图片: {}", url);
        mediaService.deleteImage(url);
        return Result.success();
    }
}
