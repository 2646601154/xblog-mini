-- Xblog-mini 测试数据
-- 注意：密码均为 123456 (BCrypt 加密后的值)

USE `xblog`;

-- ============================================================
-- 测试用户
-- 密码: 123456 (BCrypt 加密后)
-- 管理员: admin / 123456
-- 普通用户: testuser / 123456, zhangsan / 123456
-- ============================================================

INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `email`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 'admin@example.com', 'admin', 'normal'),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户', 'https://api.dicebear.com/7.x/avataaars/svg?seed=testuser', 'testuser@example.com', 'user', 'normal'),
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan', 'zhangsan@example.com', 'user', 'normal');

-- ============================================================
-- 测试分类
-- ============================================================

INSERT INTO `category` (`name`, `slug`, `description`, `sort_order`) VALUES
('技术', 'tech', '技术分享与教程', 1),
('生活', 'life', '生活感悟与随笔', 2),
('随笔', 'essay', '杂谈与感悟', 3);

-- ============================================================
-- 测试标签
-- ============================================================

INSERT INTO `tag` (`name`, `slug`) VALUES
('Java', 'java'),
('Spring Boot', 'spring-boot'),
('Vue', 'vue'),
('Docker', 'docker'),
('MySQL', 'mysql'),
('Redis', 'redis'),
('随想', 'thoughts'),
('旅行', 'travel'),
('读书', 'reading');

-- ============================================================
-- 测试文章
-- ============================================================

INSERT INTO `article` (`title`, `summary`, `content`, `cover_image`, `category_id`, `author_id`, `status`, `view_count`, `published_at`, `deleted`) VALUES
(
    'Spring Boot 3.x 快速入门指南',
    '本文介绍了 Spring Boot 3.x 的基本概念和快速搭建方法，帮助读者快速上手。',
    '<h2>什么是 Spring Boot</h2><p>Spring Boot 是一个基于 Spring 框架的快速应用开发框架，它简化了 Spring 应用的配置和部署。</p><h2>创建第一个项目</h2><p>使用 Spring Initializr 可以快速创建一个 Spring Boot 项目。</p><pre><code class="language-java">@SpringBootApplication\npublic class Application {\n    public static void main(String[] args) {\n        SpringApplication.run(Application.class, args);\n    }\n}</code></pre><p>更多内容待续...</p>',
    'https://picsum.photos/seed/spring/800/400',
    1, 1, 'published', 100, '2026-04-01 10:00:00', 0
),
(
    'Vue 3 Composition API 实战技巧',
    '深入讲解 Vue 3 Composition API 的使用方法和最佳实践。',
    '<h2>setup 函数</h2><p>setup 是 Vue 3 新增的入口函数，在组件创建之前执行。</p><h2>响应式系统</h2><p>Vue 3 提供了 ref 和 reactive 来创建响应式数据。</p><pre><code class="language-javascript">import { ref, reactive } from ''vue''<br>const count = ref(0)<br>const state = reactive({ name: ''Vue 3'' })</code></pre>',
    'https://picsum.photos/seed/vue/800/400',
    1, 1, 'published', 80, '2026-04-10 14:30:00', 0
),
(
    'Docker 容器化部署最佳实践',
    '本文总结了 Docker 在生产环境中的使用经验和最佳实践。',
    '<h2>Dockerfile 编写技巧</h2><p>一个好的 Dockerfile 应该遵循以下原则：</p><ul><li>使用多阶段构建</li><li>减少镜像层数</li><li>利用构建缓存</li></ul>',
    'https://picsum.photos/seed/docker/800/400',
    1, 2, 'published', 60, '2026-04-15 09:00:00', 0
),
(
    '我的 2026 年旅行计划',
    '新的一年，计划去几个一直想去的地方走走看看。',
    '<h2>目的地清单</h2><p>今年想去几个地方：</p><ul><li>云南大理 - 期待苍山洱海的美景</li><li>西藏拉萨 - 向往已久的圣地</li><li>日本京都 - 体验古都风情</li></ul><p>希望都能成行吧！</p>',
    'https://picsum.photos/seed/travel/800/400',
    2, 2, 'published', 45, '2026-04-20 20:00:00', 0
),
(
    '读《百年孤独》有感',
    '魔幻现实主义的经典之作，读完后久久不能平静。',
    '<h2>马孔多的兴衰</h2><p>布恩迪亚家族七代人的故事，折射出一个国家的历史变迁。</p><p>最让我印象深刻的是那句开场白：</p><blockquote>多年以后，面对行刑队，奥雷里亚诺·布恩迪亚上校将会回想起父亲带他去见识冰块的那个遥远的下午。</blockquote>',
    'https://picsum.photos/seed/book/800/400',
    3, 3, 'published', 30, '2026-04-22 18:00:00', 0
),
(
    'Redis 缓存设计与优化',
    '详细介绍 Redis 在实际项目中的缓存使用策略和性能优化。',
    '<h2>缓存策略</h2><p>常见的缓存策略包括：</p><ul><li>Cache Aside</li><li>Read Through</li><li>Write Through</li><li>Write Behind</li></ul>',
    'https://picsum.photos/seed/redis/800/400',
    1, 1, 'published', 55, '2026-04-25 11:00:00', 0
),
(
    '草稿文章 - 待发布',
    '这是一篇草稿文章，尚未完成。',
    '<p>草稿内容...</p>',
    NULL, 1, 1, 'draft', 0, NULL, 0
);

-- ============================================================
-- 测试文章标签关联
-- ============================================================

INSERT INTO `article_tag` (`article_id`, `tag_id`) VALUES
(1, 1),  -- Spring Boot 入门指南 -> Java
(1, 2),  -- Spring Boot 入门指南 -> Spring Boot
(2, 3),  -- Vue 实战技巧 -> Vue
(3, 4),  -- Docker 最佳实践 -> Docker
(3, 7),  -- Docker 最佳实践 -> 随想
(4, 8),  -- 旅行计划 -> 旅行
(5, 9),  -- 读书有感 -> 读书
(5, 7),  -- 读书有感 -> 随想
(6, 6),  -- Redis 缓存 -> Redis
(6, 7);  -- Redis 缓存 -> 随想

-- ============================================================
-- 测试评论
-- ============================================================

INSERT INTO `comment` (`article_id`, `user_id`, `content`, `status`, `created_at`, `updated_at`) VALUES
(1, 2, '写得很好，通俗易懂！', 'approved', '2026-04-02 10:30:00', '2026-04-02 10:30:00'),
(1, 3, '期待后续的深入讲解', 'approved', '2026-04-03 14:20:00', '2026-04-03 14:20:00'),
(2, 2, 'Composition API 确实比 Options API 更灵活', 'approved', '2026-04-11 09:15:00', '2026-04-11 09:15:00'),
(2, 3, 'ref 和 reactive 的区别讲得很清楚', 'approved', '2026-04-12 16:45:00', '2026-04-12 16:45:00'),
(3, 2, '多阶段构建真的很实用', 'pending', '2026-04-16 11:00:00', '2026-04-16 11:00:00'),
(4, 1, '大理真的很美，值得一去', 'approved', '2026-04-21 08:30:00', '2026-04-21 08:30:00'),
(5, 2, '这本书确实经典，需要细细品读', 'approved', '2026-04-23 19:00:00', '2026-04-23 19:00:00'),
(6, 3, 'Cache Aside 是最常用的策略', 'approved', '2026-04-26 10:00:00', '2026-04-26 10:00:00');

-- ============================================================
-- 系统配置
-- ============================================================

INSERT INTO `config` (`config_key`, `config_value`, `description`) VALUES
('icp_number', '京ICP备XXXXXXXX号', '备案号'),
('copyright', '© 2026 Xblog. All rights reserved.', '版权信息');
