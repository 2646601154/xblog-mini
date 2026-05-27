package com.xblog.service;

import com.xblog.entity.PageResult;
import com.xblog.vo.MediaImageVo;

public interface MediaService {
    PageResult<MediaImageVo> getMediaList(int page, int size);
    void deleteImage(String url);
}
