package org.example.medflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import org.example.medflow.entity.Appointment;
import org.example.medflow.dto.AppointmentDetailDTO;
import org.example.medflow.entity.Doctor;
import org.example.medflow.entity.DoctorSchedule;
import org.example.medflow.entity.Patient;
import org.example.medflow.mapper.AppointmentMapper;
import org.example.medflow.mapper.DoctorMapper;
import org.example.medflow.mapper.DoctorScheduleMapper;
import org.example.medflow.mapper.PatientMapper;
import org.example.medflow.service.AppointmentService;
import org.example.medflow.vo.CurrentPatientVO;
import org.example.medflow.vo.PatientAppointmentRecordVO;
import org.example.medflow.vo.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Override
    public Appointment getByAppointmentNumber(String appointmentNumber) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("appointment_number", appointmentNumber);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean hasDuplicateAppointment(Integer patientId, Integer doctorId, Date appointmentDate, String timeSlot) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", patientId)
                .eq("doctor_id", doctorId)
                .eq("appointment_date", appointmentDate)
                .eq("time_slot", timeSlot);
        return this.count(queryWrapper) > 0;
    }

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private DoctorScheduleMapper doctorScheduleMapper;

    private static final String[] TEST_SLOTS = {"上午", "下午"};
    private static final String[] TEST_PAST_STATUSES = {"completed", "cancelled", "no_show"};
    private static final String[] TEST_SYMPTOMS = {
            "测试数据：感冒发热，请求就诊",
            "测试数据：常规复查",
            "测试数据：头疼脑热，需开药",
            "测试数据：体检报告咨询"
    };

    @Override
    public int generateTestAppointments(int count) {
        List<Doctor> doctors = doctorMapper.selectList(null);
        List<Patient> patients = patientMapper.selectList(null);
        if (doctors == null || doctors.isEmpty() || patients == null || patients.isEmpty()) {
            throw new RuntimeException("请先创建医生和患者基础数据");
        }
        Random random = new Random();
        List<Appointment> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Doctor d = doctors.get(random.nextInt(doctors.size()));
            Patient p = patients.get(random.nextInt(patients.size()));
            // 随机日期：最近一周内（今天 ~ 6天前），纯随机分布、不均匀
            int daysAgo = random.nextInt(7);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -daysAgo);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date date = cal.getTime();
            String slot = TEST_SLOTS[random.nextInt(TEST_SLOTS.length)];
            // 确保对应排班存在（预约需挂排班），不存在则补一条
            QueryWrapper<DoctorSchedule> scheduleWrapper = new QueryWrapper<>();
            scheduleWrapper.eq("doctor_id", d.getDoctorId())
                    .eq("work_date", date)
                    .eq("time_slot", slot);
            DoctorSchedule s = doctorScheduleMapper.selectOne(scheduleWrapper);
            if (s == null) {
                s = new DoctorSchedule();
                s.setDoctorId(d.getDoctorId());
                s.setWorkDate(date);
                s.setTimeSlot(slot);
                s.setTotalSlots(10 + random.nextInt(11));
                s.setBookedSlots(0);
                s.setIsAvailable(1);
                doctorScheduleMapper.insert(s);
            }
            Appointment a = new Appointment();
            a.setPatientId(p.getPatientId());
            a.setDoctorId(d.getDoctorId());
            a.setScheduleId(s.getScheduleId());
            a.setDeptId(d.getDeptId());
            a.setAppointmentDate(date);
            a.setTimeSlot(slot);
            a.setAppointmentNumber(new SimpleDateFormat("yyyyMMdd").format(date)
                    + String.format("%04d", random.nextInt(10000)));
            a.setSymptomsDescription(TEST_SYMPTOMS[random.nextInt(TEST_SYMPTOMS.length)]);
            // 状态与日期匹配：过去的日子不会有"已确认"，只有当天才可能是已确认
            if (daysAgo == 0) {
                a.setStatus("confirmed");
            } else {
                a.setStatus(TEST_PAST_STATUSES[random.nextInt(TEST_PAST_STATUSES.length)]);
            }
            list.add(a);
        }
        this.saveBatch(list);
        return list.size();
    }

    @Override
    public List<AppointmentDetailDTO> getAppointmentDetailsByPatientId(Integer patientId) {
        return this.baseMapper.getAppointmentDetailsByPatientId(patientId);
    }

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public R<?> getAppointmentList(Map<String, String> params) {
        String keyword = params.getOrDefault("keyword", "");
        String status = params.getOrDefault("status", "");
        // 连表查询预约列表
        List<Appointment> list = appointmentMapper.selectAppointmentList(keyword, status);
        return R.success(list);
    }

    @Override
    @Transactional
    public R<?> updateAppointmentStatus(Integer appointmentId, String status) {
        // 校验状态合法性
        if (!"confirmed".equals(status) && !"completed".equals(status)
                && !"cancelled".equals(status) && !"no_show".equals(status)) {
            return R.error("状态不合法！");
        }
        int result = appointmentMapper.updateAppointmentStatus(appointmentId, status);
        if (result > 0) {
            return R.success("状态修改成功！");
        }
        return R.error("状态修改失败！");
    }

    @Override
    public R<?> getAppointmentDetail(Integer appointmentId) {
        Appointment appointment = appointmentMapper.selectAppointmentDetail(appointmentId);
        if (appointment == null) {
            return R.error("预约记录不存在！");
        }
        return R.success(appointment);
    }

    @Override
    public List<CurrentPatientVO> getCurrentReceptionPatients(Integer doctorId) {
        // 1. 查询该医生当前接诊的所有预约记录（关联患者名）
        List<PatientAppointmentRecordVO> currentRecords = appointmentMapper.selectCurrentReceptionByDoctorId(doctorId);
        if (CollectionUtils.isEmpty(currentRecords)) {
            return new ArrayList<>();
        }

        // 2. 按患者ID分组，避免同一患者多条当前预约重复展示
        Map<Integer, List<PatientAppointmentRecordVO>> patId2CurrentRecord = currentRecords.stream()
                .collect(Collectors.groupingBy(PatientAppointmentRecordVO::getPatientId));

        // 3. 封装当前接诊患者VO，同时查询每个患者的所有就诊记录
        List<CurrentPatientVO> result = new ArrayList<>();
        for (Map.Entry<Integer, List<PatientAppointmentRecordVO>> entry : patId2CurrentRecord.entrySet()) {
            Integer patientId = entry.getKey();
            PatientAppointmentRecordVO firstRecord = entry.getValue().get(0);

            CurrentPatientVO currentPatient = new CurrentPatientVO();
            currentPatient.setPatientId(patientId);
            currentPatient.setPatientName(firstRecord.getPatientName());
            // 此处可从patient表查询患者手机号等信息，补充到VO
            // currentPatient.setPatientPhone(patientService.getById(patientId).getPhone());
            // 4. 查询该患者与当前医生的所有就诊记录
            currentPatient.setAppointmentRecords(appointmentMapper.selectPatientAllRecordsByDocIdAndPatId(doctorId, patientId));
            result.add(currentPatient);
        }
        return result;
    }

    @Override
    public List<PatientAppointmentRecordVO> getPatientAllRecords(Integer doctorId, Integer patientId) {
        return appointmentMapper.selectPatientAllRecordsByDocIdAndPatId(doctorId, patientId);
    }

    @Override
    public List<AppointmentDetailDTO> getAppointmentsByScheduleId(Integer scheduleId) {
        if (scheduleId == null || scheduleId <= 0) {
            return new ArrayList<>(); // 入参校验，返回空列表
        }
        return this.baseMapper.selectAppointmentsByScheduleId(scheduleId);
    }
}