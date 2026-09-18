package org.example.medflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.medflow.dto.DashboardStat;
import org.example.medflow.dto.TrendData;
import org.example.medflow.entity.Appointment;
import org.example.medflow.entity.Department;
import org.example.medflow.entity.Doctor;
import org.example.medflow.entity.Patient;
import org.example.medflow.mapper.AppointmentMapper;
import org.example.medflow.mapper.DepartmentMapper;
import org.example.medflow.mapper.DoctorMapper;
import org.example.medflow.mapper.PatientMapper;
import org.example.medflow.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private PatientMapper patientMapper;
    @Autowired
    private DepartmentMapper deptMapper;
    @Autowired
    private DoctorMapper doctorMapper;
    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public Map<String, Object> getDashboardStat() { // 修改返回值为Map
        DashboardStat stat = new DashboardStat();

        // 1. 查询总患者数
        LambdaQueryWrapper<Patient> patientWrapper = Wrappers.lambdaQuery();
        stat.setPatientCount(Math.toIntExact(patientMapper.selectCount(patientWrapper)));

        // 2. 查询总科室数
        LambdaQueryWrapper<Department> deptWrapper = Wrappers.lambdaQuery();
        stat.setDeptCount(Math.toIntExact(deptMapper.selectCount(deptWrapper)));

        // 3. 查询总医生数
        LambdaQueryWrapper<Doctor> doctorWrapper = Wrappers.lambdaQuery();
        stat.setDoctorCount(Math.toIntExact(doctorMapper.selectCount(doctorWrapper)));

        // 4. 查询今日预约数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        LambdaQueryWrapper<Appointment> appointWrapper = Wrappers.lambdaQuery();
        appointWrapper.between(Appointment::getCreatedAt, todayStart, todayEnd);
        stat.setTodayAppointCount(Math.toIntExact(appointmentMapper.selectCount(appointWrapper)));

        // 核心：新增code=200，把原数据放入map
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("code", 200); // 加这一行code
        resultMap.put("data", stat); // 原返回的stat作为data字段
        return resultMap;
    }

    @Override
    public Map<String, Object> getAppointTrend() { // 修改返回值为Map
        TrendData trendData = new TrendData();
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        // 生成近7天日期，按就诊日期（appointment_date）统计，排除已取消/爽约（未实际就诊）
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(DateTimeFormatter.ofPattern("MM月dd日")));

            LambdaQueryWrapper<Appointment> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(Appointment::getAppointmentDate, java.sql.Date.valueOf(date));
            wrapper.notIn(Appointment::getStatus, "cancelled", "no_show");
            counts.add(Math.toIntExact(appointmentMapper.selectCount(wrapper)));
        }

        trendData.setDates(dates);
        trendData.setCounts(counts);

        // 核心：新增code=200
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("code", 200); // 加这一行code
        resultMap.put("data", trendData);
        return resultMap;
    }

    @Override
    public Map<String, Object> getLatestAppointments(Integer limit) { // 修改返回值为Map
        LambdaQueryWrapper<Appointment> wrapper = Wrappers.lambdaQuery();
        wrapper.orderByDesc(Appointment::getAppointmentDate);
        wrapper.last("LIMIT " + limit);
        List<Appointment> appointmentList = appointmentMapper.selectList(wrapper);

        // 核心：新增code=200
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("code", 200); // 加这一行code
        resultMap.put("data", appointmentList);
        return resultMap;
    }
}