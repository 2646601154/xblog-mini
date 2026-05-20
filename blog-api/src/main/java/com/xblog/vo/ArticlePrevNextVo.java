package com.xblog.vo;

import lombok.Data;

/**
 * 文章上一篇/下一篇导航 VO
 */
@Data
public class ArticlePrevNextVo {

    /**
     * 上一篇文章（按发布时间更早的）
     */
    private ArticleBriefVo previous;

    /**
     * 下一篇文章（按发布时间更晚的）
     */
    private ArticleBriefVo next;

    /**
     * 文章简要信息
     */
    @Data
    public static class ArticleBriefVo {
        private Long id;
        private String title;
    }
}
