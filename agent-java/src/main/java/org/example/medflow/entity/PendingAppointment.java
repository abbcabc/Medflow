package org.example.medflow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 预约待确认单（MongoDB pending_appointments 集合）
 * 流程：「预约挂号」工具校验号源后生成 PENDING 单 → 用户确认后凭 pendingId 完成挂号
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("pending_appointments")
public class PendingAppointment {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    @Id
    private ObjectId id;

    /** 待确认单ID（UUID），确认预约时凭此ID取单 */
    private String pendingId;
    /** 归属患者ID，确认时校验归属 */
    private Integer patientId;
    /** 排班ID，确认时凭此原子扣减号源 */
    private Integer scheduleId;
    /** 医生ID */
    private Integer doctorId;
    /** 科室ID（可能为空，确认时兜底取医生科室） */
    private Integer deptId;
    /** 医生姓名（冗余存，便于确认时展示） */
    private String doctorName;
    /** 就诊日期 yyyy-MM-dd（冗余存） */
    private String appointmentDate;
    /** 就诊时段：上午/下午（冗余存） */
    private String timeSlot;
    /** 症状描述 */
    private String symptomsDescription;
    /** 状态：PENDING/CONFIRMED/CANCELLED/EXPIRED */
    private String status;
    /** 创建时间 */
    private Date createdAt;
    /** 过期时间（创建时间+10分钟） */
    private Date expireAt;
}
