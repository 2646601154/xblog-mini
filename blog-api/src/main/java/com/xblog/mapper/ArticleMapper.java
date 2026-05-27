package com.xblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xblog.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    @Select("""
        SELECT DATE(created_at) AS date, COUNT(*) AS count
        FROM article
        WHERE deleted = 0
          AND created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
        GROUP BY DATE(created_at)
        ORDER BY date
    """)
    java.util.List<java.util.Map<String, Object>> getArticleTrend();

    @Select("""
        SELECT id AS sourceId, title AS sourceName, cover_image AS url
        FROM article
        WHERE cover_image IS NOT NULL AND cover_image != ''
        ORDER BY id DESC
    """)
    java.util.List<com.xblog.vo.MediaImageVo> getCoverImages();

    @Select("""
        SELECT COUNT(*)
        FROM article
        WHERE cover_image = #{url}
    """)
    int countByCoverImage(String url);
}
