package com.xblog.common.constant;

/**
 * 缓存键常量
 * <p>
 * 定义项目中使用的 Redis 缓存键前缀和格式。
 * </p>
 */
public final class CacheKeyConstants {

    private CacheKeyConstants() {
        // 防止实例化
    }

    /**
     * 文章列表缓存前缀
     * <p>
     * 完整键格式: article:list:{queryHash}
     * </p>
     */
    public static final String ARTICLE_LIST = "article:list:";

    /**
     * 文章详情缓存前缀
     * <p>
     * 完整键格式: article:detail:{articleId}
     * </p>
     */
    public static final String ARTICLE_DETAIL = "article:detail:";

    /**
     * 文章浏览记录前缀（用于防重复计数）
     * <p>
     * 完整键格式: article:view:{articleId}:{ip}
     * </p>
     */
    public static final String ARTICLE_VIEW = "article:view:";

    /**
     * 文章列表缓存过期时间（秒）
     */
    public static final long ARTICLE_LIST_EXPIRE = 300; // 5 分钟

    /**
     * 文章详情缓存过期时间（秒）
     */
    public static final long ARTICLE_DETAIL_EXPIRE = 600; // 10 分钟

    /**
     * 文章浏览记录过期时间（秒）
     */
    public static final long ARTICLE_VIEW_EXPIRE = 86400; // 24 小时
}
