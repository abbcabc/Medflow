package org.example.medflow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class PatientAppointmentRecordVO {
    // 预约ID
    private Integer appointmentId;
    // 患者ID
    private Integer patientId;
    // 患者姓名（从patient表关联）
    private String patientName;
    // 预约号
    private String appointmentNumber;
    // 预约日期
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date appointmentDate;
    // 时段（上午/下午）
    private String timeSlot;
    // 症状描述
    private String symptomsDescription;
    // 预约状态（confirmed/completed/cancelled/no_show）
    private String status;
    // 预约创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
    // 取消原因（仅取消状态有值）
    private String cancelReason;
}
