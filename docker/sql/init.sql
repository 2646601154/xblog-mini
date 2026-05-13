-- Xblog-mini 数据库初始化脚本
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `xblog`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `xblog`;

-- ============================================================
-- 用户表
-- ============================================================
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `article_tag`;
DROP TABLE IF EXISTS `article`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `tag`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `config`;

CREATE TABLE `user` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`      VARCHAR(20) NOT NULL COMMENT '用户名，3-20 字符',
    `password`      VARCHAR(255) NOT NULL COMMENT '密码 (BCrypt)，最少 6 字符',
    `nickname`       VARCHAR(50) NOT NULL COMMENT '昵称，2-50 字符',
    `avatar`         VARCHAR(500) DEFAULT NULL COMMENT '头像 URL',
    `email`          VARCHAR(100) DEFAULT NULL COMMENT '邮箱，唯一',
    `role`           VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色: admin/user',
    `status`         VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '状态: normal/disabled',
    `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE INDEX `idx_username` ON `user` (`username`);

-- ============================================================
-- 分类表
-- ============================================================
CREATE TABLE `category` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`          VARCHAR(20) NOT NULL COMMENT '分类名称',
    `slug`          VARCHAR(50) NOT NULL COMMENT 'URL 标识',
    `description`   VARCHAR(200) DEFAULT NULL COMMENT '分类描述',
    `sort_order`    INT NOT NULL DEFAULT 0 COMMENT '排序 (越小越靠前)',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    UNIQUE KEY `uk_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

CREATE INDEX `idx_sort_order` ON `category` (`sort_order`);

-- ============================================================
-- 标签表
-- ============================================================
CREATE TABLE `tag` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`          VARCHAR(20) NOT NULL COMMENT '标签名称',
    `slug`          VARCHAR(50) NOT NULL COMMENT 'URL 标识',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    UNIQUE KEY `uk_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ============================================================
-- 文章表
-- ============================================================
CREATE TABLE `article` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`         VARCHAR(200) NOT NULL COMMENT '文章标题',
    `summary`       VARCHAR(500) DEFAULT NULL COMMENT '文章摘要',
    `content`       LONGTEXT NOT NULL COMMENT '文章正文 (HTML)',
    `cover_image`   VARCHAR(500) DEFAULT NULL COMMENT '封面图 URL',
    `category_id`   BIGINT NOT NULL COMMENT '分类 ID (逻辑外键)',
    `author_id`     BIGINT NOT NULL COMMENT '作者 ID (逻辑外键)',
    `status`        VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态: draft/published/recycled',
    `view_count`    INT NOT NULL DEFAULT 0 COMMENT '浏览量',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `published_at`  DATETIME DEFAULT NULL COMMENT '发布时间',
    `deleted`       TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

CREATE INDEX `idx_status_published_at` ON `article` (`status`, `published_at`);
CREATE INDEX `idx_category_id` ON `article` (`category_id`);
CREATE INDEX `idx_author_id` ON `article` (`author_id`);

-- ============================================================
-- 文章标签关联表
-- ============================================================
CREATE TABLE `article_tag` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`    BIGINT NOT NULL COMMENT '文章 ID',
    `tag_id`        BIGINT NOT NULL COMMENT '标签 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

CREATE INDEX `idx_tag_id` ON `article_tag` (`tag_id`);

-- ============================================================
-- 评论表
-- ============================================================
CREATE TABLE `comment` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`    BIGINT NOT NULL COMMENT '文章 ID (逻辑外键)',
    `user_id`       BIGINT NOT NULL COMMENT '用户 ID (逻辑外键)',
    `content`       VARCHAR(1000) NOT NULL COMMENT '评论内容',
    `status`        VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/approved/rejected',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

CREATE INDEX `idx_article_id_status` ON `comment` (`article_id`, `status`);
CREATE INDEX `idx_user_id` ON `comment` (`user_id`);

-- ============================================================
-- 系统配置表
-- ============================================================
CREATE TABLE `config` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key`    VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value`  TEXT DEFAULT NULL COMMENT '配置值',
    `description`   VARCHAR(200) DEFAULT NULL COMMENT '配置描述',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';
