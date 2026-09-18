package org.example.medflow.dto;

import lombok.Data;

@Data
public class DashboardStat {
    private Integer patientCount;        // 总患者数
    private Integer deptCount;         // 总科室数
    private Integer doctorCount;       // 总医生数
    private Integer todayAppointCount; // 今日预约数
}