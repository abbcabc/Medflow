package org.example.medflow.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量排班操作VO
 */
@Data
public class ScheduleBatchVO {
    /**
     * 排班ID集合
     */
    @NotNull(message = "请选择要操作的排班")
    private List<Integer> scheduleIds;

    /**
     * 可用状态 1-可用 0-不可用（可选，不传则不修改）
     */
    private Integer isAvailable;

    /**
     * 总号源数（可选，不传则不修改）
     */
    private Integer totalSlots;

    /**
     * 已预约号源数（可选，不传则不修改）
     */
    private Integer bookedSlots;
}
