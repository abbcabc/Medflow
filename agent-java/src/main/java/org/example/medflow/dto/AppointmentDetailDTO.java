package org.example.medflow.dto;

// 首先创建一个DTO类来返回包含部门名称和医生名称的预约信息
import lombok.Data;
import java.util.Date;

@Data
public class AppointmentDetailDTO {
    private Long appointmentId;
    private int patientId;
    private int doctorId;
    private int scheduleId;
    private int deptId;
    private String appointmentNumber;
    private Date appointmentDate;
    private String timeSlot;
    private String symptomsDescription;
    private String status;
    private Date cancelledAt;
    private String cancelReason;
    private Date completedAt;

    // 关联字段 - 根据查询结果调整字段名
    // 使用驼峰命名
    private String deptName;    // 部门名称
    private String doctorName;  // 医生名称
}
