package org.example.medflow.service;

import org.example.medflow.entity.PendingAppointment;

public interface PendingAppointmentService {

    /**
     * 创建预约待确认单（expireAt = now + 10分钟）
     *
     * @param patientId           归属患者ID
     * @param scheduleId          排班ID
     * @param doctorId            医生ID
     * @param deptId              科室ID（可为空）
     * @param doctorName          医生姓名（可为空）
     * @param appointmentDate     就诊日期 yyyy-MM-dd
     * @param timeSlot            就诊时段：上午/下午
     * @param symptomsDescription 症状描述（可为空）
     * @return 已落库的待确认单（含 pendingId）
     */
    PendingAppointment createPending(Integer patientId, Integer scheduleId, Integer doctorId, Integer deptId,
                                     String doctorName, String appointmentDate, String timeSlot,
                                     String symptomsDescription);

    /**
     * 按 待确认单ID+患者ID 查询有效待确认单（状态为 PENDING 且未过期）
     * 过期策略：查询时惰性判过期，命中过期单会落库标记为 EXPIRED
     *
     * @param pendingId 待确认单ID
     * @param patientId 当前登录患者ID（归属校验）
     * @return 有效待确认单；不存在/已过期/已使用/非本人时返回 null
     */
    PendingAppointment getValidByPendingIdAndPatient(String pendingId, Integer patientId);

    /**
     * 标记为已确认（仅当当前状态为 PENDING 时生效，防重复确认）
     */
    boolean markConfirmed(String pendingId);

    /**
     * 标记为已取消（仅当当前状态为 PENDING 时生效）
     */
    boolean markCancelled(String pendingId);
}
