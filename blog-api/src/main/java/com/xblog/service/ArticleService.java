package com.xblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xblog.dto.AdminQueryArticleDto;
import com.xblog.dto.ArticleCreateParam;
import com.xblog.dto.ArticleUpdateParam;
import com.xblog.dto.QueryArticleDto;
import com.xblog.entity.Article;
import com.xblog.entity.PageResult;
import com.xblog.vo.ArticleCreateVo;
import com.xblog.vo.ArticlePrevNextVo;
import com.xblog.vo.ArticlePublishVo;
import com.xblog.vo.ArticleStatusVo;
import com.xblog.vo.ArticleUpdateVo;
import com.xblog.vo.ArticleVo;
import com.xblog.vo.TagVo;

import java.util.List;

public interface ArticleService extends IService<Article> {
    PageResult<ArticleVo> getPublicArticlePage(QueryArticleDto queryDto);

    ArticleVo getArticleDetail(Long id);

    ArticlePrevNextVo getArticlePrevNext(Long id);

    List<TagVo> getArticleTags(Long id);

    PageResult<ArticleVo> getAdminArticlePage(AdminQueryArticleDto queryDto);

    ArticleCreateVo createArticle(ArticleCreateParam param);

    ArticleUpdateVo updateArticle(Long id, ArticleUpdateParam param);

    ArticlePublishVo publishArticle(Long id);

    ArticleStatusVo recycleArticle(Long id);

    ArticleStatusVo restoreArticle(Long id);

    void deleteArticle(Long id);

    List<TagVo> bindTags(Long id, List<Long> tagIds);
}
