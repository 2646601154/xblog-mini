package com.xblog.controller;

import com.xblog.entity.Result;
import com.xblog.service.TagService;
import com.xblog.vo.TagVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/tags")
@Tag(name = "标签接口", description = "公开标签相关接口")
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public Result<List<TagVo>> getTagList() {
        return Result.success(tagService.getPublicTagList());
    }
}