package org.example.medflow.service.impl;

import org.example.medflow.entity.PendingAppointment;
import org.example.medflow.service.PendingAppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class PendingAppointmentServiceImpl implements PendingAppointmentService {

    private static final Logger log = LoggerFactory.getLogger(PendingAppointmentServiceImpl.class);

    /** 待确认单有效期：10分钟 */
    private static final long EXPIRE_MILLIS = 10 * 60 * 1000L;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public PendingAppointment createPending(Integer patientId, Integer scheduleId, Integer doctorId, Integer deptId,
                                            String doctorName, String appointmentDate, String timeSlot,
                                            String symptomsDescription) {
        Date now = new Date();
        PendingAppointment pending = new PendingAppointment();
        pending.setPendingId(UUID.randomUUID().toString().replace("-", ""));
        pending.setPatientId(patientId);
        pending.setScheduleId(scheduleId);
        pending.setDoctorId(doctorId);
        pending.setDeptId(deptId);
        pending.setDoctorName(doctorName);
        pending.setAppointmentDate(appointmentDate);
        pending.setTimeSlot(timeSlot);
        pending.setSymptomsDescription(symptomsDescription);
        pending.setStatus(PendingAppointment.STATUS_PENDING);
        pending.setCreatedAt(now);
        pending.setExpireAt(new Date(now.getTime() + EXPIRE_MILLIS));
        mongoTemplate.insert(pending);
        log.info("创建待确认单: pendingId={}, patientId={}, scheduleId={}, 就诊={} {}, 过期时间={}",
                pending.getPendingId(), patientId, scheduleId, appointmentDate, timeSlot, pending.getExpireAt());
        return pending;
    }

    @Override
    public PendingAppointment getValidByPendingIdAndPatient(String pendingId, Integer patientId) {
        if (pendingId == null || pendingId.trim().isEmpty() || patientId == null) {
            return null;
        }
        Query query = Query.query(Criteria.where("pendingId").is(pendingId.trim())
                .and("patientId").is(patientId));
        PendingAppointment pending = mongoTemplate.findOne(query, PendingAppointment.class);
        if (pending == null) {
            log.debug("待确认单不存在或不属于当前患者: pendingId={}, patientId={}", pendingId, patientId);
            return null;
        }
        // 过期策略：查询时惰性判过期（不依赖 TTL 索引），命中过期单则落库标记 EXPIRED
        if (PendingAppointment.STATUS_PENDING.equals(pending.getStatus())
                && pending.getExpireAt() != null && pending.getExpireAt().before(new Date())) {
            markExpired(pending.getPendingId());
            log.info("待确认单已过期，标记EXPIRED: pendingId={}", pending.getPendingId());
            return null;
        }
        if (!PendingAppointment.STATUS_PENDING.equals(pending.getStatus())) {
            log.debug("待确认单状态非PENDING，不可用: pendingId={}, status={}", pending.getPendingId(), pending.getStatus());
            return null;
        }
        return pending;
    }

    @Override
    public boolean markConfirmed(String pendingId) {
        return updateStatusIfPending(pendingId, PendingAppointment.STATUS_CONFIRMED);
    }

    @Override
    public boolean markCancelled(String pendingId) {
        return updateStatusIfPending(pendingId, PendingAppointment.STATUS_CANCELLED);
    }

    private void markExpired(String pendingId) {
        Query query = Query.query(Criteria.where("pendingId").is(pendingId));
        Update update = new Update().set("status", PendingAppointment.STATUS_EXPIRED);
        mongoTemplate.updateFirst(query, update, PendingAppointment.class);
    }

    /**
     * 条件更新状态：仅当当前状态为 PENDING 时才生效（原子操作，防并发重复确认/取消）
     */
    private boolean updateStatusIfPending(String pendingId, String targetStatus) {
        if (pendingId == null || pendingId.trim().isEmpty()) {
            return false;
        }
        Query query = Query.query(Criteria.where("pendingId").is(pendingId.trim())
                .and("status").is(PendingAppointment.STATUS_PENDING));
        Update update = new Update().set("status", targetStatus);
        boolean updated = mongoTemplate.updateFirst(query, update, PendingAppointment.class).getModifiedCount() > 0;
        if (updated) {
            log.info("待确认单状态更新: pendingId={}, status={}", pendingId, targetStatus);
        }
        return updated;
    }
}
