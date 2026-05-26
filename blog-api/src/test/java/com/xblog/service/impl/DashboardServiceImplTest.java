package com.xblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xblog.entity.Article;
import com.xblog.mapper.ArticleMapper;
import com.xblog.mapper.CategoryMapper;
import com.xblog.mapper.CommentMapper;
import com.xblog.mapper.TagMapper;
import com.xblog.mapper.UserMapper;
import com.xblog.vo.CategoryAdminVo;
import com.xblog.vo.DashboardVo;
import com.xblog.vo.DashboardVo.ArticleTrendVo;
import com.xblog.vo.DashboardVo.DashboardStatsVo;
import com.xblog.vo.DashboardVo.DistributionItemVo;
import com.xblog.vo.TagAdminVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DashboardServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("仪表盘服务测试")
class DashboardServiceImplTest {

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private UserMapper userMapper;

    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(
                articleMapper,
                categoryMapper,
                commentMapper,
                tagMapper,
                userMapper
        );
    }

    @Test
    @DisplayName("获取仪表盘数据 - 完整流程")
    void testGetDashboard() {
        // 准备测试数据
        when(articleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(100L);
        when(userMapper.selectCount(null)).thenReturn(50L);
        when(commentMapper.selectCount(null)).thenReturn(200L);
        when(categoryMapper.selectCount(null)).thenReturn(10L);
        when(tagMapper.selectCount(null)).thenReturn(25L);

        // 模拟文章趋势数据
        List<Map<String, Object>> trendData = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("date", Date.valueOf(LocalDate.now().minusDays(1)));
        row1.put("count", 5L);
        trendData.add(row1);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("date", Date.valueOf(LocalDate.now().minusDays(2)));
        row2.put("count", 3L);
        trendData.add(row2);
        when(articleMapper.getArticleTrend()).thenReturn(trendData);

        // 模拟分类数据
        List<CategoryAdminVo> categories = new ArrayList<>();
        CategoryAdminVo cat1 = new CategoryAdminVo();
        cat1.setName("技术");
        cat1.setArticleCount(50L);
        categories.add(cat1);

        CategoryAdminVo cat2 = new CategoryAdminVo();
        cat2.setName("生活");
        cat2.setArticleCount(30L);
        categories.add(cat2);
        when(categoryMapper.getAdminCategoryList()).thenReturn(categories);

        // 模拟标签数据
        List<TagAdminVo> tags = new ArrayList<>();
        TagAdminVo tag1 = new TagAdminVo();
        tag1.setName("Java");
        tag1.setArticleCount(40L);
        tags.add(tag1);

        TagAdminVo tag2 = new TagAdminVo();
        tag2.setName("Spring");
        tag2.setArticleCount(35L);
        tags.add(tag2);
        when(tagMapper.getAdminTagList()).thenReturn(tags);

        // 执行测试
        DashboardVo result = dashboardService.getDashboard();

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getStats());
        assertEquals(100L, result.getStats().getArticleCount());
        assertEquals(50L, result.getStats().getUserCount());
        assertEquals(200L, result.getStats().getCommentCount());
        assertEquals(10L, result.getStats().getCategoryCount());
        assertEquals(25L, result.getStats().getTagCount());

        // 验证文章趋势
        assertNotNull(result.getArticleTrend());
        assertNotNull(result.getArticleTrend().getDates());
        assertNotNull(result.getArticleTrend().getCounts());
        assertEquals(7, result.getArticleTrend().getDates().size());
        assertEquals(7, result.getArticleTrend().getCounts().size());

        // 验证分类分布
        assertNotNull(result.getCategoryDistribution());
        assertEquals(2, result.getCategoryDistribution().size());
        assertEquals("技术", result.getCategoryDistribution().get(0).getName());
        assertEquals(50L, result.getCategoryDistribution().get(0).getValue());

        // 验证标签使用
        assertNotNull(result.getTagUsage());
        assertEquals(2, result.getTagUsage().size());
        assertEquals("Java", result.getTagUsage().get(0).getName());
        assertEquals(40L, result.getTagUsage().get(0).getValue());

        // 验证调用次数
        verify(articleMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(userMapper, times(1)).selectCount(null);
        verify(commentMapper, times(1)).selectCount(null);
        verify(categoryMapper, times(1)).selectCount(null);
        verify(tagMapper, times(1)).selectCount(null);
        verify(articleMapper, times(1)).getArticleTrend();
        verify(categoryMapper, times(1)).getAdminCategoryList();
        verify(tagMapper, times(1)).getAdminTagList();
    }

    @Test
    @DisplayName("文章趋势 - 处理java.sql.Date类型")
    void testBuildArticleTrendWithSqlDate() {
        // 准备测试数据 - 使用java.sql.Date
        List<Map<String, Object>> trendData = new ArrayList<>();
        
        Map<String, Object> row1 = new HashMap<>();
        row1.put("date", Date.valueOf(LocalDate.now()));
        row1.put("count", 10L);
        trendData.add(row1);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("date", Date.valueOf(LocalDate.now().minusDays(3)));
        row2.put("count", 7L);
        trendData.add(row2);

        when(articleMapper.getArticleTrend()).thenReturn(trendData);

        // 执行测试 - 通过反射调用私有方法
        ArticleTrendVo result = invokeBuildArticleTrend();

        // 验证结果
        assertNotNull(result);
        assertEquals(7, result.getDates().size());
        assertEquals(7, result.getCounts().size());
        
        // 验证今天的计数
        String todayStr = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("M/d"));
        int todayIndex = result.getDates().indexOf(todayStr);
        assertTrue(todayIndex >= 0);
        assertEquals(10L, result.getCounts().get(todayIndex));
    }

    @Test
    @DisplayName("文章趋势 - 处理LocalDate类型")
    void testBuildArticleTrendWithLocalDate() {
        // 准备测试数据 - 使用LocalDate
        List<Map<String, Object>> trendData = new ArrayList<>();
        
        Map<String, Object> row1 = new HashMap<>();
        row1.put("date", LocalDate.now().minusDays(1));
        row1.put("count", 5L);
        trendData.add(row1);

        when(articleMapper.getArticleTrend()).thenReturn(trendData);

        // 执行测试
        ArticleTrendVo result = invokeBuildArticleTrend();

        // 验证结果
        assertNotNull(result);
        assertEquals(7, result.getDates().size());
        
        // 验证昨天的计数
        String yesterdayStr = LocalDate.now().minusDays(1)
                .format(java.time.format.DateTimeFormatter.ofPattern("M/d"));
        int yesterdayIndex = result.getDates().indexOf(yesterdayStr);
        assertTrue(yesterdayIndex >= 0);
        assertEquals(5L, result.getCounts().get(yesterdayIndex));
    }

    @Test
    @DisplayName("文章趋势 - 空数据处理")
    void testBuildArticleTrendEmpty() {
        // 准备测试数据 - 空列表
        when(articleMapper.getArticleTrend()).thenReturn(new ArrayList<>());

        // 执行测试
        ArticleTrendVo result = invokeBuildArticleTrend();

        // 验证结果
        assertNotNull(result);
        assertEquals(7, result.getDates().size());
        assertEquals(7, result.getCounts().size());
        
        // 所有计数应该都是0
        for (Long count : result.getCounts()) {
            assertEquals(0L, count);
        }
    }

    @Test
    @DisplayName("文章趋势 - 补全7天日期")
    void testBuildArticleTrendDateCompletion() {
        // 准备测试数据 - 只有1天的数据
        List<Map<String, Object>> trendData = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("date", Date.valueOf(LocalDate.now()));
        row.put("count", 15L);
        trendData.add(row);
        when(articleMapper.getArticleTrend()).thenReturn(trendData);

        // 执行测试
        ArticleTrendVo result = invokeBuildArticleTrend();

        // 验证结果
        assertNotNull(result);
        assertEquals(7, result.getDates().size());
        assertEquals(7, result.getCounts().size());
        
        // 验证日期是连续的7天
        LocalDate today = LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("M/d");
        for (int i = 0; i < 7; i++) {
            LocalDate expectedDate = today.minusDays(6 - i);
            assertEquals(expectedDate.format(formatter), result.getDates().get(i));
        }
    }

    @Test
    @DisplayName("分类分布 - 正常数据")
    void testBuildCategoryDistribution() {
        // 准备测试数据
        List<CategoryAdminVo> categories = new ArrayList<>();
        
        CategoryAdminVo cat1 = new CategoryAdminVo();
        cat1.setName("技术");
        cat1.setArticleCount(100L);
        categories.add(cat1);

        CategoryAdminVo cat2 = new CategoryAdminVo();
        cat2.setName("生活");
        cat2.setArticleCount(null); // 测试null值处理
        categories.add(cat2);

        when(categoryMapper.getAdminCategoryList()).thenReturn(categories);

        // 执行测试 - 通过反射调用私有方法
        List<DistributionItemVo> result = invokeBuildCategoryDistribution();

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("技术", result.get(0).getName());
        assertEquals(100L, result.get(0).getValue());
        assertEquals("生活", result.get(1).getName());
        assertEquals(0L, result.get(1).getValue()); // null应该转换为0
    }

    @Test
    @DisplayName("分类分布 - 空列表")
    void testBuildCategoryDistributionEmpty() {
        // 准备测试数据
        when(categoryMapper.getAdminCategoryList()).thenReturn(new ArrayList<>());

        // 执行测试
        List<DistributionItemVo> result = invokeBuildCategoryDistribution();

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("标签使用 - Top 10限制")
    void testBuildTagUsageLimit() {
        // 准备测试数据 - 超过10个标签
        List<TagAdminVo> tags = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            TagAdminVo tag = new TagAdminVo();
            tag.setName("Tag" + i);
            tag.setArticleCount((long) (100 - i * 5));
            tags.add(tag);
        }
        when(tagMapper.getAdminTagList()).thenReturn(tags);

        // 执行测试 - 通过反射调用私有方法
        List<DistributionItemVo> result = invokeBuildTagUsage();

        // 验证结果 - 应该只返回前10个
        assertNotNull(result);
        assertEquals(10, result.size());
        assertEquals("Tag1", result.get(0).getName());
        assertEquals("Tag10", result.get(9).getName());
    }

    @Test
    @DisplayName("标签使用 - 空数据处理")
    void testBuildTagUsageEmpty() {
        // 准备测试数据
        when(tagMapper.getAdminTagList()).thenReturn(new ArrayList<>());

        // 执行测试
        List<DistributionItemVo> result = invokeBuildTagUsage();

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("标签使用 - null计数处理")
    void testBuildTagUsageNullCount() {
        // 准备测试数据
        List<TagAdminVo> tags = new ArrayList<>();
        TagAdminVo tag = new TagAdminVo();
        tag.setName("TestTag");
        tag.setArticleCount(null);
        tags.add(tag);
        when(tagMapper.getAdminTagList()).thenReturn(tags);

        // 执行测试
        List<DistributionItemVo> result = invokeBuildTagUsage();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TestTag", result.get(0).getName());
        assertEquals(0L, result.get(0).getValue());
    }

    /**
     * 通过反射调用私有的buildArticleTrend方法
     */
    private ArticleTrendVo invokeBuildArticleTrend() {
        try {
            var method = DashboardServiceImpl.class.getDeclaredMethod("buildArticleTrend");
            method.setAccessible(true);
            return (ArticleTrendVo) method.invoke(dashboardService);
        } catch (Exception e) {
            throw new RuntimeException("调用buildArticleTrend方法失败", e);
        }
    }

    /**
     * 通过反射调用私有的buildCategoryDistribution方法
     */
    private List<DistributionItemVo> invokeBuildCategoryDistribution() {
        try {
            var method = DashboardServiceImpl.class.getDeclaredMethod("buildCategoryDistribution");
            method.setAccessible(true);
            return (List<DistributionItemVo>) method.invoke(dashboardService);
        } catch (Exception e) {
            throw new RuntimeException("调用buildCategoryDistribution方法失败", e);
        }
    }

    /**
     * 通过反射调用私有的buildTagUsage方法
     */
    private List<DistributionItemVo> invokeBuildTagUsage() {
        try {
            var method = DashboardServiceImpl.class.getDeclaredMethod("buildTagUsage");
            method.setAccessible(true);
            return (List<DistributionItemVo>) method.invoke(dashboardService);
        } catch (Exception e) {
            throw new RuntimeException("调用buildTagUsage方法失败", e);
        }
    }
}
