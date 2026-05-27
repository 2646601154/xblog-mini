package com.xblog.service.impl;

import com.xblog.common.enums.ResultCode;
import com.xblog.common.exception.BusinessException;
import com.xblog.common.properties.OssProperties;
import com.xblog.common.util.OssUtil;
import com.xblog.entity.PageResult;
import com.xblog.mapper.ArticleMapper;
import com.xblog.mapper.UserMapper;
import com.xblog.service.MediaService;
import com.xblog.vo.MediaImageVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MediaServiceImpl implements MediaService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final OssUtil ossUtil;
    private final OssProperties ossProperties;

    public MediaServiceImpl(ArticleMapper articleMapper,
                            UserMapper userMapper,
                            OssUtil ossUtil,
                            OssProperties ossProperties) {
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
        this.ossUtil = ossUtil;
        this.ossProperties = ossProperties;
    }

    @Override
    public PageResult<MediaImageVo> getMediaList(int page, int size) {
        List<MediaImageVo> allImages = new ArrayList<>();

        // 封面图
        List<MediaImageVo> covers = articleMapper.getCoverImages();
        if (covers != null) {
            for (MediaImageVo vo : covers) {
                vo.setType("cover");
                vo.setOss(isOssUrl(vo.getUrl()));
            }
            allImages.addAll(covers);
        }

        // 头像
        List<MediaImageVo> avatars = userMapper.getAvatars();
        if (avatars != null) {
            for (MediaImageVo vo : avatars) {
                vo.setType("avatar");
                vo.setOss(isOssUrl(vo.getUrl()));
            }
            allImages.addAll(avatars);
        }

        // 按 sourceId 降序排列
        allImages.sort(Comparator.comparing(MediaImageVo::getSourceId, Comparator.nullsLast(Comparator.reverseOrder())));

        // 分页
        int total = allImages.size();
        int fromIndex = (page - 1) * size;
        if (fromIndex >= total) {
            return new PageResult<>();
        }
        int toIndex = Math.min(fromIndex + size, total);
        List<MediaImageVo> pageRecords = allImages.subList(fromIndex, toIndex);

        PageResult<MediaImageVo> result = new PageResult<>();
        result.setRecords(pageRecords);
        result.setTotal((long) total);
        result.setPage(page);
        result.setSize(size);
        return result;
    }

    @Override
    public void deleteImage(String url) {
        if (!isOssUrl(url)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅支持删除 OSS 图片");
        }
        int coverCount = articleMapper.countByCoverImage(url);
        int avatarCount = userMapper.countByAvatar(url);
        if (coverCount + avatarCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "图片仍被引用，无法删除");
        }
        ossUtil.deleteFile(url);
    }

    private boolean isOssUrl(String url) {
        return url != null && ossProperties.getUrlPrefix() != null
                && url.startsWith(ossProperties.getUrlPrefix());
    }
}
