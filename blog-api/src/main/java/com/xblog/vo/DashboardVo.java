package com.xblog.vo;

import lombok.Data;

import java.util.List;

@Data
public class DashboardVo {
    private DashboardStatsVo stats;
    private ArticleTrendVo articleTrend;
    private List<DistributionItemVo> categoryDistribution;
    private List<DistributionItemVo> tagUsage;

    @Data
    public static class DashboardStatsVo {
        private Long articleCount;
        private Long userCount;
        private Long commentCount;
        private Long categoryCount;
        private Long tagCount;
    }

    @Data
    public static class ArticleTrendVo {
        private List<String> dates;
        private List<Long> counts;
    }

    @Data
    public static class DistributionItemVo {
        private String name;
        private Long value;
    }
}
