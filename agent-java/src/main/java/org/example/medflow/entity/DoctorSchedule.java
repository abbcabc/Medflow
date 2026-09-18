package org.example.medflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
@TableName("doctor_schedule") // 假设表名为 doctor_schedule
public class DoctorSchedule {
    @TableId(type = IdType.AUTO)
    private Integer scheduleId;

    private Integer doctorId;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date workDate;

    private String timeSlot;

    private Integer totalSlots = 10;

    private Integer bookedSlots = 0;

    private Integer isAvailable = 1;

    /**
     * 创建时间 自动生成
     */
    private Date createdAt;
}