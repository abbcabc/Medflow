package org.example.medflow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 一键排班参数VO
 */
@Data
public class ScheduleOneKeyVO {
    /**
     * 医生ID（为空时表示对全部医生一键排班）
     */
    private Integer doctorId;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    /**
     * 时间段 上午/下午（多个用逗号分隔，如：上午,下午）
     */
    @NotNull(message = "时间段不能为空")
    private String timeSlots;

    /**
     * 总号源数 默认为10
     */
    private Integer totalSlots = 10;
}
