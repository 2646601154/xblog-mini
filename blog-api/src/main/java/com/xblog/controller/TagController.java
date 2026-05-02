package com.xblog.controller;

import com.xblog.entity.Result;
import com.xblog.service.TagService;
import com.xblog.vo.TagVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/tags")
public class TagController {

    @Resource
    private TagService tagService;

    @GetMapping
    public Result<List<TagVo>> getTagList() {
        return Result.success(tagService.getPublicTagList());
    }
}