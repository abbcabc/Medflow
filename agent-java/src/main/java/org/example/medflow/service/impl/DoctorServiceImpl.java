package org.example.medflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import org.example.medflow.dto.DoctorLoginRequest;
import org.example.medflow.dto.DoctorLoginResponse;
import org.example.medflow.dto.Result;
import org.example.medflow.entity.Doctor;
import org.example.medflow.mapper.DoctorMapper;
import org.example.medflow.service.DoctorScheduleService;
import org.example.medflow.service.DoctorService;
import org.example.medflow.util.JwtUtil;
import org.example.medflow.util.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    @Override
    public IPage<Doctor> getPage(Integer pageNum, Integer pageSize, String doctorName) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(doctorName)) {
            wrapper.like(Doctor::getDoctorName, doctorName);
        }
        // ✅ 倒序（最新的在第一页）
        wrapper.orderByAsc(Doctor::getDoctorId);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Autowired
    private DoctorScheduleService doctorScheduleService;

    @Override
    public List<Doctor> findDoctorsByName(String doctorName) {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("doctor_name", doctorName)
                .eq("is_available", 1); // 只查询可用的医生
        return this.list(queryWrapper);
    }

    @Override
    public List<Doctor> findAvailableDoctors(Integer deptId, Date workDate, String timeSlot) {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();

        // 基础查询条件：可用的医生
        queryWrapper.eq("is_available", 1);

        // 如果指定了科室，添加科室条件
        if (deptId != null && deptId > 0) {
            queryWrapper.eq("dept_id", deptId);
        }

        // 获取所有符合条件的医生
        List<Doctor> doctors = this.list(queryWrapper);

        // 如果提供了日期和时间段，进一步筛选有号源的医生
        if (workDate != null && timeSlot != null) {
            doctors.removeIf(doctor ->
                    !doctorScheduleService.hasAvailableSlots(doctor.getDoctorId(), workDate, timeSlot)
            );
        }

        return doctors;
    }

    @Override
    public Integer getDoctorDepartment(Integer doctorId) {
        Doctor doctor = this.getById(doctorId);
        return doctor != null ? doctor.getDeptId() : null;
    }

    @Override
    public List<Doctor> findDoctorsByDepartmentName(String departmentName) {
        // 这里需要根据部门名称查询，可能需要关联部门表
        // 假设部门名称存储在doctor表的某个字段中，或者需要关联查询
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_available", 1);
        // 这里需要根据实际数据库结构调整查询条件
        return this.list(queryWrapper);
    }

    @Override
    public List<Doctor> findAllAvailableDoctors() {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_available", 1);
        return this.list(queryWrapper);
    }

    @Override
    public List<Doctor> findDoctorsBySpecialty(String specialty) {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("specialty", specialty)
                .eq("is_available", 1);
        return this.list(queryWrapper);
    }

    /**
     * 根据症状推荐医生（简化版）
     */
    public List<Doctor> recommendDoctorsBySymptoms(String symptoms) {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_available", 1);

        // 根据症状关键词匹配专长
        if (symptoms != null && !symptoms.trim().isEmpty()) {
            String[] symptomKeywords = symptoms.split("[，, ]");
            for (String keyword : symptomKeywords) {
                if (keyword.length() > 1) { // 忽略单字
                    queryWrapper.or().like("specialty", keyword.trim());
                }
            }
        }

        return this.list(queryWrapper);
    }

    /**
     * 获取医生详细信息
     */
    public Doctor getDoctorDetail(Integer doctorId) {
        return this.getById(doctorId);
    }

    /**
     * 根据经验和费用筛选医生
     */
    public List<Doctor> findDoctorsByCriteria(Integer minExperience, Integer maxExperience,
                                              Float minFee, Float maxFee) {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_available", 1);

        if (minExperience != null) {
            queryWrapper.ge("years_experience", minExperience);
        }
        if (maxExperience != null) {
            queryWrapper.le("years_experience", maxExperience);
        }
        if (minFee != null) {
            queryWrapper.ge("consultation_fee", minFee);
        }
        if (maxFee != null) {
            queryWrapper.le("consultation_fee", maxFee);
        }

        return this.list(queryWrapper);
    }

    // 在类中添加依赖
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // 添加登录方法实现
    @Override
    public Result<?> login(DoctorLoginRequest request) {
        // 根据部门ID和医生姓名查找医生
        Doctor doctor = baseMapper.findByDeptIdAndName(request.getDeptId(), request.getDoctorName());
        if (doctor == null) {
            return Result.error(400, "部门ID或医生姓名错误");
        }

        // 检查密码是否已设置
        if (doctor.getPassword() == null || doctor.getPassword().trim().isEmpty()) {
            return Result.error(400, "请联系管理员设置密码");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), doctor.getPassword())) {
            return Result.error(400, "密码错误");
        }

        // 检查医生是否可用
        if (doctor.getIsAvailable() != null && doctor.getIsAvailable() == 0) {
            return Result.error(400, "该医生账号暂不可用");
        }

        // 生成token
        String token = jwtUtil.generateToken(doctor.getDoctorId());

        // 构建响应
        DoctorLoginResponse response = new DoctorLoginResponse();
        response.setToken("Bearer " + token);
        response.setDoctorId(doctor.getDoctorId());
        response.setDoctorName(doctor.getDoctorName());
        response.setTitle(doctor.getTitle());
        response.setSpecialty(doctor.getSpecialty());
        response.setDeptId(doctor.getDeptId());
        response.setAvatarUrl(doctor.getAvatarUrl());

        return Result.success("登录成功", response);
    }

    @Override
    public Result<?> getProfile(Integer doctorId) {
        Doctor doctor = baseMapper.findByDoctorId(doctorId);
        if (doctor == null) {
            return Result.error(404, "医生不存在");
        }
//        System.out.println(doctor);
        // 返回医生信息（不包含密码）
        doctor.setPassword("保密");
        return Result.success("获取成功", doctor);
    }

    @Override
    public Result<?> changePassword(Integer doctorId, String oldPassword, String newPassword) {
        Doctor doctor = baseMapper.findByDoctorId(doctorId);
        if (doctor == null) {
            return Result.error(404, "医生不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, doctor.getPassword())) {
            return Result.error(400, "原密码错误");
        }

        // 加密新密码
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        int result = baseMapper.updatePassword(doctorId, encodedNewPassword);

        if (result > 0) {
            return Result.success("密码修改成功", null);
        } else {
            return Result.error(500, "密码修改失败");
        }
    }

}