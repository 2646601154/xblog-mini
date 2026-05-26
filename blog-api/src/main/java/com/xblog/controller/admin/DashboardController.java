package com.xblog.controller.admin;

import com.xblog.entity.Result;
import com.xblog.service.DashboardService;
import com.xblog.vo.DashboardVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("adminDashboardController")
@RequestMapping("/v1/admin")
@Tag(name = "管理-仪表盘接口", description = "管理员仪表盘统计接口")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/dashboard")
    public Result<DashboardVo> getDashboard() {
        log.info("获取仪表盘统计数据");
        return Result.success(dashboardService.getDashboard());
    }
}
