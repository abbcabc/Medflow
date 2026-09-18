package org.example.medflow.controller;

import org.example.medflow.dto.DashboardStat;
import org.example.medflow.dto.TrendData;
import org.example.medflow.entity.Appointment;
import org.example.medflow.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("admin/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/stat")
    public Map<String, Object> getDashboardStat() {
        return dashboardService.getDashboardStat();
    }

    /**
     * 获取近7天预约趋势数据
     */
    @GetMapping("/trend")
    public Map<String, Object> getAppointTrend() {
        return dashboardService.getAppointTrend();
    }

    /**
     * 获取最新预约记录
     */
    @GetMapping("/latest-appointments")
    public Map<String, Object> getLatestAppointments(
            @RequestParam(name = "limit", defaultValue = "5") Integer limit
    ) {
        return dashboardService.getLatestAppointments(limit);
    }
}
