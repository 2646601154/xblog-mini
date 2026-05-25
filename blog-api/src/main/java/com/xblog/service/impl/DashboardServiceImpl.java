package com.xblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xblog.entity.Article;
import com.xblog.entity.Category;
import com.xblog.entity.Comment;
import com.xblog.entity.Tag;
import com.xblog.entity.User;
import com.xblog.mapper.ArticleMapper;
import com.xblog.mapper.CategoryMapper;
import com.xblog.mapper.CommentMapper;
import com.xblog.mapper.TagMapper;
import com.xblog.mapper.UserMapper;
import com.xblog.service.DashboardService;
import com.xblog.vo.CategoryAdminVo;
import com.xblog.vo.DashboardVo;
import com.xblog.vo.DashboardVo.ArticleTrendVo;
import com.xblog.vo.DashboardVo.DashboardStatsVo;
import com.xblog.vo.DashboardVo.DistributionItemVo;
import com.xblog.vo.TagAdminVo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final CommentMapper commentMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;

    public DashboardServiceImpl(ArticleMapper articleMapper,
                                 CategoryMapper categoryMapper,
                                 CommentMapper commentMapper,
                                 TagMapper tagMapper,
                                 UserMapper userMapper) {
        this.articleMapper = articleMapper;
        this.categoryMapper = categoryMapper;
        this.commentMapper = commentMapper;
        this.tagMapper = tagMapper;
        this.userMapper = userMapper;
    }

    @Override
    public DashboardVo getDashboard() {
        DashboardVo vo = new DashboardVo();

        // 1. 统计数量
        DashboardStatsVo stats = new DashboardStatsVo();
        stats.setArticleCount(articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getDeleted, false)));
        stats.setUserCount(userMapper.selectCount(null));
        stats.setCommentCount(commentMapper.selectCount(null));
        stats.setCategoryCount(categoryMapper.selectCount(null));
        stats.setTagCount(tagMapper.selectCount(null));
        vo.setStats(stats);

        // 2. 文章发布趋势（最近7天）
        vo.setArticleTrend(buildArticleTrend());

        // 3. 分类分布
        vo.setCategoryDistribution(buildCategoryDistribution());

        // 4. 标签用量（Top 10）
        vo.setTagUsage(buildTagUsage());

        return vo;
    }

    private ArticleTrendVo buildArticleTrend() {
        List<Map<String, Object>> rows = articleMapper.getArticleTrend();
        Map<String, Long> dateCountMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String date = (String) row.get("date");
            Long count = ((Number) row.get("count")).longValue();
            dateCountMap.put(date, count);
        }

        // 补全最近7天的所有日期
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            dates.add(day.format(formatter));
            counts.add(dateCountMap.getOrDefault(day.toString(), 0L));
        }

        ArticleTrendVo trend = new ArticleTrendVo();
        trend.setDates(dates);
        trend.setCounts(counts);
        return trend;
    }

    private List<DistributionItemVo> buildCategoryDistribution() {
        List<CategoryAdminVo> categories = categoryMapper.getAdminCategoryList();
        return categories.stream()
                .map(cat -> {
                    DistributionItemVo item = new DistributionItemVo();
                    item.setName(cat.getName());
                    item.setValue(cat.getArticleCount() != null ? cat.getArticleCount() : 0L);
                    return item;
                })
                .collect(Collectors.toList());
    }

    private List<DistributionItemVo> buildTagUsage() {
        List<TagAdminVo> tags = tagMapper.getAdminTagList();
        return tags.stream()
                .limit(10)
                .map(tag -> {
                    DistributionItemVo item = new DistributionItemVo();
                    item.setName(tag.getName());
                    item.setValue(tag.getArticleCount() != null ? tag.getArticleCount() : 0L);
                    return item;
                })
                .collect(Collectors.toList());
    }
}
