package org.example.medflow.tools;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.example.medflow.context.ThreadLocalContext;
import org.example.medflow.dto.Result;
import org.example.medflow.entity.Appointment;
import org.example.medflow.entity.Patient;
import org.example.medflow.entity.PendingAppointment;
import org.example.medflow.service.*;
import org.example.medflow.store.MongoChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Date;
import java.util.Objects;

@Component("appointmentTool")
public class AppointmentTool {

    private static final Logger log = LoggerFactory.getLogger(AppointmentTool.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DISPLAY_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final int MAX_APPOINTMENT_MONTH = 3;
    private static final String DATE_VALID = "valid";
    private static final String STATUS_CONFIRMED = "confirmed";
    private static final String STATUS_CANCELLED = "cancelled";
    private static final int RANDOM_PART_LENGTH = 4;
    private static final int RANDOM_PART_MIN = 1000;
    private static final int RANDOM_PART_MAX = 9999;

    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;
    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorScheduleService doctorScheduleService;

    @Autowired
    private DoctorService doctorService;

    @Lazy
    @Autowired
    private PatientService patientService;

    @Autowired
    private PendingAppointmentService pendingAppointmentService;

    static {
        SDF.setLenient(false);
    }

    @Tool(name = "查询我的预约", value = "当用户表示想查看预约/我的预约/预约记录时调用。根据会话ID定位当前用户，查询其所有预约并返回简洁列表；如无预约则提示。会话id从上下文中获取")
    public String queryMyAppointments(@P(value = "会话ID") Long memoryId) {
        log.info("[AI工具] 查询我的预约: memoryId={}", memoryId);
        // patientId 只信任登录态（ThreadLocal），不再从记忆/会话ID中推断
        Integer patientId = ThreadLocalContext.getPatientId();

        if (patientId == null || patientId <= 0) {
            return "🔐 请先登录后再查询预约记录。";
        }

        List<org.example.medflow.dto.AppointmentDetailDTO> list =
                appointmentService.getAppointmentDetailsByPatientId(patientId);
        if (list == null || list.isEmpty()) {
            return "📭 你目前没有预约记录。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📌 你的预约记录如下（共 ").append(list.size()).append(" 条）：\n");
        int idx = 1;
        for (org.example.medflow.dto.AppointmentDetailDTO a : list) {
            String dateStr = a.getAppointmentDate() == null ? "-" : DISPLAY_DATE_TIME_FORMAT.format(a.getAppointmentDate());
            sb.append(idx++).append(") ")
                    .append(dateStr).append(" ")
                    .append(a.getTimeSlot() == null ? "" : a.getTimeSlot())
                    .append(" | ")
                    .append(a.getDeptName() == null ? "未知科室" : a.getDeptName())
                    .append(" | ")
                    .append(a.getDoctorName() == null ? ("医生ID:" + a.getDoctorId()) : a.getDoctorName())
                    .append(" | 状态：").append(a.getStatus() == null ? "-" : a.getStatus())
                    .append(" | 预约号：").append(a.getAppointmentNumber() == null ? "-" : a.getAppointmentNumber())
                    .append("\n");
        }
        sb.append("如需取消预约，请告诉我“取消预约 + 原因”。");
        return sb.toString();
    }

    private String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    private String validateAppointmentDate(String userDate) {
        if (userDate == null || userDate.trim().isEmpty()) {
            return "错误：预约日期不能为空，请输入 yyyy-MM-dd 格式的日期（例如：" + getCurrentDate() + "）";
        }

        LocalDate appointmentDate;
        try {
            appointmentDate = LocalDate.parse(userDate, DATE_FORMATTER);
        } catch (Exception e) {
            return "错误：日期格式不正确，请使用 yyyy-MM-dd 格式（例如：" + getCurrentDate() + "）";
        }

        LocalDate currentDate = LocalDate.now();
        if (appointmentDate.isBefore(currentDate)) {
            return String.format("错误：预约日期不能是过去的时间。今天是 %s", getCurrentDate());
        }

        LocalDate maxDate = currentDate.plusMonths(MAX_APPOINTMENT_MONTH);
        if (appointmentDate.isAfter(maxDate)) {
            return String.format("错误：预约日期不能超过%d个月。今天是 %s，最晚可预约到 %s",
                    MAX_APPOINTMENT_MONTH, getCurrentDate(), maxDate.format(DATE_FORMATTER));
        }

        return DATE_VALID;
    }

    @Tool(name = "预约挂号", value = "根据参数完成预约前置校验，校验通过后生成一张待确认单（10分钟内有效）并返回待确认单ID（pendingId）与号源摘要；无医生姓名时从向量存储获取医生；用户确认后凭 pendingId 调用工具'确认预约'完成挂号；当前日期见系统提示，会话id从上下文中获取")
    public String bookAppointment(
            @P(value = "会话ID") Long memoryId,
            @P(value = "医生ID") Integer doctorId,
            @P(value = "医生姓名") String doctorName,
            @P(value = "科室ID") Integer deptId,
            @P(value = "预约日期，格式：yyyy-MM-dd") String appointmentDate,
            @P(value = "时间段，上午或下午") String timeSlot,
            @P(value = "症状描述") String symptomsDescription) {

        log.info("[AI工具] 预约挂号: memoryId={}, doctorId={}, 日期={}, 时间段={}", memoryId, doctorId, appointmentDate, timeSlot);
        // patientId 只信任登录态（ThreadLocal），不再从记忆/会话ID中推断
        Integer patientId = ThreadLocalContext.getPatientId();
        if (patientId == null || patientId <= 0) {
            return "🔐 请先登录后再进行预约挂号。";
        }
        if (timeSlot == null || !("上午".equals(timeSlot) || "下午".equals(timeSlot))) {
            return "错误：时间段只能是「上午」或「下午」，请确认后重新输入";
        }

        try {
            String dateValidation = validateAppointmentDate(appointmentDate);
            if (!DATE_VALID.equals(dateValidation)) {
                return dateValidation;
            }

            // 查询号源不再依赖外部传入的 patientId
            boolean hasAvailableSlots = queryDepartment(doctorId, appointmentDate, timeSlot);
            if (!hasAvailableSlots) {
                return String.format("抱歉，%s %s 该时间段没有可用号源，请选择其他时间。今天是 %s",
                        appointmentDate, timeSlot, getCurrentDate());
            }

            String patientCheckResult = checkPatientInfo();
            if (!patientCheckResult.contains("用户信息完整")) {
                return patientCheckResult;
            }

            Date parsedDate = parseDate(appointmentDate);
            Integer scheduleId = doctorScheduleService.getScheduleId(doctorId, parsedDate, timeSlot);
            if (scheduleId == null) {
                return "未找到对应的排班信息，无法预约。";
            }

            // 校验通过：创建服务端待确认单（10分钟内有效），确认预约时凭 pendingId 取单
            PendingAppointment pending = pendingAppointmentService.createPending(
                    patientId, scheduleId, doctorId, deptId, doctorName,
                    appointmentDate, timeSlot, symptomsDescription);
            return buildPendingMsg(pending);

        } catch (ParseException e) {
            return String.format("❌ 日期格式错误，请使用 yyyy-MM-dd 格式（例如：%s）", getCurrentDate());
        } catch (Exception e) {
            return String.format("❌ 预约过程中出现错误：%s", e.getMessage());
        }
    }

    private String buildPendingMsg(PendingAppointment pending) {
        return String.format("""
                📅 已为您生成预约待确认单，请核对信息：
                🆔 待确认单ID（pendingId）：%s
                🩺 医生：%s (ID:%d)
                🏥 科室ID：%d
                ⏰ 就诊时间：%s %s
                📝 症状描述：%s
                ⏳ 该待确认单10分钟内有效，回复"确认"即可完成挂号；
                ❌ 若超时未确认或号源被抢完，请重新选择号源生成新的待确认单。
                """,
                pending.getPendingId(),
                (pending.getDoctorName() == null || pending.getDoctorName().trim().isEmpty()) ? "未指定" : pending.getDoctorName(),
                pending.getDoctorId(),
                (pending.getDeptId() == null) ? 0 : pending.getDeptId(),
                pending.getAppointmentDate(),
                pending.getTimeSlot(),
                (pending.getSymptomsDescription() == null || pending.getSymptomsDescription().trim().isEmpty()) ? "无" : pending.getSymptomsDescription());
    }

    @Tool(name = "查询是否有号源", value = "先从向量存储获取对应医生ID，再按医生ID、日期、时间段查询号源；当前日期见系统提示")
    public boolean queryDepartment(
            @P(value = "医生id") int doctorId,
            @P(value = "日期，格式：yyyy-MM-dd") String workDateStr,
            @P(value = "时间，可选值：上午、下午") String time) {
        log.info("[AI工具] 查询是否有号源: doctorId={}, 日期={}, 时间={}", doctorId, workDateStr, time);
        try {
            String dateValidation = validateAppointmentDate(workDateStr);
            if (!DATE_VALID.equals(dateValidation)) {
                throw new IllegalArgumentException(dateValidation);
            }
            if (!"上午".equals(time) && !"下午".equals(time)) {
                throw new IllegalArgumentException("错误：时间段只能是「上午」或「下午」");
            }
            Date workDate = parseDate(workDateStr);
            return doctorId > 0
                    ? doctorScheduleService.hasAvailableSlots(doctorId, workDate, time)
                    : doctorScheduleService.hasAnyAvailableDoctor(workDate, time);
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("❌ 日期格式错误，请使用 yyyy-MM-dd 格式。今天是 %s", getCurrentDate()), e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("查询号源时出现错误：" + e.getMessage(), e);
        }
    }

    // 已修复：patientId 只从登录态（ThreadLocal）获取
    @Tool(name = "确认预约", value = "凭待确认单ID（pendingId，来自'预约挂号'工具返回的待确认单ID）完成最终挂号：原子扣减号源并生成预约记录；先检查用户个人信息是否完善，会话id从上下文中获取")
    public String confirmAppointment(
            @P(value = "会话ID") Long memoryId,
            @P(value = "待确认单ID，即'预约挂号'工具返回的待确认单ID（pendingId）") String pendingId) {

        log.info("[AI工具] 确认预约: memoryId={}, pendingId={}", memoryId, pendingId);
        // patientId 只信任登录态（ThreadLocal），不再从记忆/会话ID中推断
        Integer patientId = ThreadLocalContext.getPatientId();
        if (patientId == null || patientId <= 0) {
            return "🔐 请先登录后再确认预约。";
        }
        if (pendingId == null || pendingId.trim().isEmpty()) {
            return "❌ 缺少待确认单ID，请先通过「预约挂号」选择号源生成待确认单";
        }
        try {
            // 1. 取有效待确认单（校验未过期 + 归属当前登录患者）
            PendingAppointment pending = pendingAppointmentService
                    .getValidByPendingIdAndPatient(pendingId.trim(), patientId);
            if (pending == null) {
                return "❌ 待确认单不存在、已过期或已使用，请重新选择号源后再确认";
            }

            String patientCheckResult = checkPatientInfo();
            if (!patientCheckResult.contains("用户信息完整")) {
                return patientCheckResult;
            }

            // 2. 原子扣减号源（仅当 booked_slots < total_slots 时生效，防超卖）
            if (!doctorScheduleService.increaseBookedSlots(pending.getScheduleId())) {
                // 号源刚被抢完：作废待确认单，引导用户重新选择
                pendingAppointmentService.markCancelled(pending.getPendingId());
                return "😔 该号源刚被约满，请换个时间段或医生重新预约。";
            }

            // 3. 扣减成功后再创建预约单，保证号源与预约单一致
            Appointment appointment = buildAppointmentFromPending(patientId, pending);
            if (!appointmentService.save(appointment)) {
                // 插入失败：回滚号源扣减
                doctorScheduleService.decreaseBookedSlots(pending.getScheduleId());
                return "❌ 预约失败，请稍后重试";
            }

            // 4. 标记待确认单已确认（仅当仍为 PENDING 时生效，防重复确认）；失败则整体回滚
            if (!pendingAppointmentService.markConfirmed(pending.getPendingId())) {
                appointmentService.removeById(appointment.getAppointmentId());
                doctorScheduleService.decreaseBookedSlots(pending.getScheduleId());
                return "❌ 该待确认单已被使用或已失效，请重新选择号源";
            }

            return buildAppointmentSuccessMsg(appointment, pending);
        } catch (Exception e) {
            log.error("确认预约异常: memoryId={}, pendingId={}", memoryId, pendingId, e);
            return String.format("❌ 确认预约时出现错误：%s", e.getMessage());
        }
    }

    private Appointment buildAppointmentFromPending(Integer patientId, PendingAppointment pending) throws ParseException {
        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorId(pending.getDoctorId());
        appointment.setScheduleId(pending.getScheduleId());
        appointment.setAppointmentDate(parseDate(pending.getAppointmentDate()));
        appointment.setTimeSlot(pending.getTimeSlot());
        appointment.setAppointmentNumber(generateAppointmentNumber());
        appointment.setDeptId(Objects.nonNull(pending.getDeptId()) ? pending.getDeptId()
                : doctorService.getDoctorDepartment(pending.getDoctorId()));
        appointment.setSymptomsDescription(Objects.isNull(pending.getSymptomsDescription()) ? "无" : pending.getSymptomsDescription());
        appointment.setStatus(STATUS_CONFIRMED);
        return appointment;
    }

    private String buildAppointmentSuccessMsg(Appointment appointment, PendingAppointment pending) {
        return String.format("""
                ✅ 预约成功！
                📋 预约详情：
                🔢 预约号：%s
                🩺 医生：%s (ID:%d)
                ⏰ 预约时间：%s %s
                🏥 部门ID：%d
                📅 排班ID：%d
                📝 症状描述：%s
                💡 温馨提示：请按时就诊，如有变动请提前取消预约。
                """,
                appointment.getAppointmentNumber(),
                (pending.getDoctorName() == null || pending.getDoctorName().trim().isEmpty()) ? "未指定" : pending.getDoctorName(),
                appointment.getDoctorId(),
                DISPLAY_DATE_TIME_FORMAT.format(appointment.getAppointmentDate()),
                appointment.getTimeSlot(),
                appointment.getDeptId() == null ? 0 : appointment.getDeptId(),
                appointment.getScheduleId(),
                appointment.getSymptomsDescription());
    }

    @Tool(name = "取消预约", value = "根据预约号取消预约，需传入取消原因；若预约已取消，直接告知用户；会话Id从对话历史获取，无需重复询问")
    public String cancelAppointment(
//            @P(value = "预约号") String appointmentNumber,
            @P(value = "会话ID") Long memoryId,
            @P(value = "取消原因") String cancelReason) {

        log.info("[AI工具] 取消预约: memoryId={}, 原因长度={}", memoryId, cancelReason == null ? 0 : cancelReason.length());
        String appointmentNumber = mongoChatMemoryStore.getAppointmentNumber(memoryId);
        if (appointmentNumber == null || appointmentNumber.trim().isEmpty()) {
            return "❌ 预约号不能为空，请提供有效的预约号";
        }
        if (cancelReason == null || cancelReason.trim().isEmpty()) {
            return "❌ 取消原因不能为空，请说明取消预约的理由";
        }
        // patientId 只信任登录态（ThreadLocal），用于校验预约归属，防止越权取消他人预约
        Integer patientId = ThreadLocalContext.getPatientId();
        if (patientId == null || patientId <= 0) {
            return "🔐 请先登录后再取消预约。";
        }

        try {
            Appointment appointment = appointmentService.getByAppointmentNumber(appointmentNumber);
            if (appointment == null) {
                return "❌ 未找到对应的预约记录";
            }

            if (!patientId.equals(appointment.getPatientId())) {
                log.warn("取消预约被拒绝：预约不属于当前登录用户, memoryId={}", memoryId);
                return "❌ 无权操作该预约记录";
            }

            if (STATUS_CANCELLED.equals(appointment.getStatus())) {
                return "ℹ️ 该预约已经处于取消状态，无需重复操作";
            }

            // 条件更新：仅当预约状态未取消时才生效（防并发重复取消导致重复退号）
            UpdateWrapper<Appointment> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("appointment_id", appointment.getAppointmentId())
                    .and(w -> w.ne("status", STATUS_CANCELLED).or().isNull("status"))
                    .set("status", STATUS_CANCELLED)
                    .set("cancelled_at", new Date())
                    .set("cancel_reason", cancelReason);

            if (appointmentService.update(updateWrapper)) {
                // 更新成功才退号，保证防重
                doctorScheduleService.decreaseBookedSlots(appointment.getScheduleId());
                return "✅ 预约取消成功";
            } else {
                return "❌ 取消失败，请稍后重试";
            }

        } catch (Exception e) {
            return String.format("❌ 取消预约时出现错误：%s", e.getMessage());
        }
    }

    @Tool(name = "检查用户信息完整性", value = "预约前校验用户核心信息是否完善；信息不全时提醒用户点击左侧「完善信息」按钮补充")
    public String checkPatientInfo() {
        log.info("[AI工具] 检查用户信息完整性");
        // 已修复：patientId 只信任登录态（ThreadLocal），不再接受外部传入
        Integer actualPatientId = ThreadLocalContext.getPatientId();
        if (actualPatientId == null) {
            return "❌ 请先登录后再操作";
        }
        try {
            Result<?> result = patientService.getProfile(actualPatientId);
            if (result.getCode() != 200) {
                return String.format("无法获取用户信息：%s", result.getMessage());
            }

            Patient patient = (Patient) result.getData();
            if (patient == null) {
                return "❌ 未查询到该用户的信息，请确认用户ID是否正确";
            }

            StringBuilder missingFields = new StringBuilder();
            checkPatientField(patient.getRealName(), "真实姓名", missingFields);
            checkPatientField(patient.getIdCard(), "身份证号", missingFields);
            checkPatientField(patient.getAge(), "年龄", missingFields);
            checkPatientField(patient.getGender(), "性别", missingFields);

            if (missingFields.length() > 0) {
                String missingInfo = missingFields.substring(0, missingFields.length() - 1);
                return String.format("""
                        ❌ 您的个人信息不完整，无法进行预约。
                        📋 缺失的信息：%s
                        💡 请先完善个人信息后再进行预约（点击左侧「完善信息」按钮即可）。
                        """, missingInfo);
            }

            return "✅ 用户信息完整，可以继续进行预约";

        } catch (Exception e) {
            return String.format("❌ 检查用户信息时出现错误：%s", e.getMessage());
        }
    }

    private void checkPatientField(Object fieldValue, String fieldName, StringBuilder missingFields) {
        if (fieldValue == null) {
            missingFields.append(fieldName).append("、");
            return;
        }
        if (fieldValue instanceof String && ((String) fieldValue).trim().isEmpty()) {
            missingFields.append(fieldName).append("、");
        }
    }

    private Date parseDate(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new ParseException("日期字符串不能为空", 0);
        }
        return SDF.parse(dateStr);
    }

    private String generateAppointmentNumber() {
        String datePart = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int randomNum = RANDOM_PART_MIN + (int) (Math.random() * (RANDOM_PART_MAX - RANDOM_PART_MIN + 1));
        String randomPart = String.format("%0" + RANDOM_PART_LENGTH + "d", randomNum);
        return datePart + randomPart;
    }
}
