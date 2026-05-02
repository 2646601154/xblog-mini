package com.xblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xblog.common.enums.ResultCode;
import com.xblog.common.exception.BusinessException;
import com.xblog.dto.TagCreateParam;
import com.xblog.dto.TagUpdateParam;
import com.xblog.entity.Tag;
import com.xblog.mapper.ArticleTagMapper;
import com.xblog.mapper.TagMapper;
import com.xblog.service.TagService;
import com.xblog.vo.TagAdminVo;
import com.xblog.vo.TagVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Resource
    private ArticleTagMapper articleTagMapper;

    @Override
    public List<TagVo> getPublicTagList() {
        return list().stream()
                .map(tag -> {
                    TagVo vo = new TagVo();
                    vo.setId(tag.getId());
                    vo.setName(tag.getName());
                    vo.setSlug(tag.getSlug());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TagAdminVo> getAdminTagList() {
        return baseMapper.getAdminTagList();
    }

    @Override
    public TagVo createTag(TagCreateParam param) {
        // 校验标签名称是否已存在
        if (lambdaQuery().eq(Tag::getName, param.getName()).exists()) {
            throw new BusinessException(ResultCode.TAG_NAME_EXISTS);
        }

        // 校验标签slug是否已存在
        if (lambdaQuery().eq(Tag::getSlug, param.getSlug()).exists()) {
            throw new BusinessException(ResultCode.TAG_SLUG_EXISTS);
        }

        Tag tag = new Tag();
        tag.setName(param.getName());
        tag.setSlug(param.getSlug());
        save(tag);

        TagVo vo = new TagVo();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setSlug(tag.getSlug());
        return vo;
    }

    @Override
    public TagVo updateTag(Long id, TagUpdateParam param) {
        Tag tag = getById(id);
        if (tag == null) {
            throw new BusinessException(ResultCode.TAG_NOT_FOUND);
        }

        // 排除自身后校验 name 是否被占用
        if (lambdaQuery().eq(Tag::getName, param.getName()).ne(Tag::getId, id).exists()) {
            throw new BusinessException(ResultCode.TAG_NAME_EXISTS);
        }

        // 排除自身后校验 slug 是否被占用
        if (lambdaQuery().eq(Tag::getSlug, param.getSlug()).ne(Tag::getId, id).exists()) {
            throw new BusinessException(ResultCode.TAG_SLUG_EXISTS);
        }

        tag.setName(param.getName());
        tag.setSlug(param.getSlug());
        updateById(tag);

        TagVo updateVo = new TagVo();
        updateVo.setId(tag.getId());
        updateVo.setName(tag.getName());
        updateVo.setSlug(tag.getSlug());
        return updateVo;
    }

    @Override
    public void deleteTag(Long id) {
        Tag tag = getById(id);
        if (tag == null) {
            throw new BusinessException(ResultCode.TAG_NOT_FOUND);
        }

        // 删除标签前，先清理 article_tag 关联记录
        QueryWrapper<com.xblog.entity.ArticleTag> wrapper = new QueryWrapper<>();
        wrapper.eq("tag_id", id);
        articleTagMapper.delete(wrapper);

        removeById(id);
    }
}