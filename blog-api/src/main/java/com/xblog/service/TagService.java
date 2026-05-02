package com.xblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xblog.entity.Tag;
import com.xblog.dto.TagCreateParam;
import com.xblog.dto.TagUpdateParam;
import com.xblog.vo.TagAdminVo;
import com.xblog.vo.TagVo;

import java.util.List;

public interface TagService extends IService<Tag> {

    List<TagVo> getPublicTagList();

    List<TagAdminVo> getAdminTagList();

    TagVo createTag(TagCreateParam param);

    TagVo updateTag(Long id, TagUpdateParam param);

    void deleteTag(Long id);
}