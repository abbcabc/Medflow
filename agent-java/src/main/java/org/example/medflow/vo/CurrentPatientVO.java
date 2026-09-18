package org.example.medflow.vo;

import lombok.Data;
import java.util.List;

@Data
public class CurrentPatientVO {
    // 患者ID
    private Integer patientId;
    // 患者姓名
    private String patientName;
    // 患者手机号（从patient表关联，可选）
    private String patientPhone;
    // 该患者与当前医生的所有就诊记录
    private List<PatientAppointmentRecordVO> appointmentRecords;
}
