package org.example.medflow.dto;

import lombok.Data;
import java.util.List;

@Data
public class TrendData {
    private List<String> dates;  // 近7天日期
    private List<Integer> counts; // 对应日期预约数
}
