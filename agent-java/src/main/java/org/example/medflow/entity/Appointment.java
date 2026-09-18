package org.example.medflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("appointment")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Integer appointmentId;    // 预约ID
    private Integer patientId;        // 患者ID
    private Integer doctorId;         // 医生ID
    private Integer scheduleId;       // 排班ID
    private Integer deptId;           // 科室ID
    private String appointmentNumber; // 预约编号
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date appointmentDate;// 预约日期（就诊日期，只精确到天）
    private String timeSlot;          // 时段（上午/下午）
    private String symptomsDescription;// 症状描述
    private String status;            // 状态：confirmed/completed/cancelled/no_show
    private Date createdAt;  // 创建时间
    private Date completedAt;// 完成时间
    private Date cancelledAt;// 取消时间
    private String cancelReason;      // 取消原因

    // 连表查询新增字段 - 非数据库字段，用于返回中文名称
    @TableField(exist = false)
    private String realName;          // 患者真实姓名
    @TableField(exist = false)
    private String doctorName;        // 医生姓名
    @TableField(exist = false)
    private String deptName;          // 科室名称
}
