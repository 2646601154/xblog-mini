package com.xblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xblog.dto.QueryArticleDto;
import com.xblog.entity.Article;
import com.xblog.entity.PageResult;
import com.xblog.vo.ArticleVo;

public interface ArticleService extends IService<Article> {
    PageResult<ArticleVo> getPublicArticlePage(QueryArticleDto queryDto);

    ArticleVo getArticleDetail(Long id);
}
