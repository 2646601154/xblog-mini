package com.xblog.controller.admin;

import com.xblog.dto.TagCreateParam;
import com.xblog.dto.TagUpdateParam;
import com.xblog.entity.Result;
import com.xblog.service.TagService;
import com.xblog.vo.TagAdminVo;
import com.xblog.vo.TagVo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("adminTagController")
@RequestMapping("/v1/admin/tags")
public class TagController {

    @Resource
    private TagService tagService;

    @GetMapping
    public Result<List<TagAdminVo>> getAdminTagList() {
        return Result.success(tagService.getAdminTagList());
    }

    @PostMapping
    public Result<TagVo> createTag(@Valid @RequestBody TagCreateParam param) {
        return Result.success(tagService.createTag(param));
    }

    @PutMapping("/{id}")
    public Result<TagVo> updateTag(@PathVariable Long id, @Valid @RequestBody TagUpdateParam param) {
        return Result.success(tagService.updateTag(id, param));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success(null);
    }
}