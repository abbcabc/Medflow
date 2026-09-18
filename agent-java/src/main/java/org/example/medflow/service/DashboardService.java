package org.example.medflow.service;


import org.example.medflow.dto.DashboardStat;
import org.example.medflow.dto.TrendData;
import org.example.medflow.entity.Appointment;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    Map<String, Object> getDashboardStat();
    Map<String, Object> getAppointTrend();
    Map<String, Object> getLatestAppointments(Integer limit);
}