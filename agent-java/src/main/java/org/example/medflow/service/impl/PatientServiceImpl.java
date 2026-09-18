package org.example.medflow.service.impl;

import org.example.medflow.assistant.Agent;
import org.example.medflow.context.ThreadLocalContext;
import org.example.medflow.dto.*;
import org.example.medflow.entity.Patient;
import org.example.medflow.mapper.PatientMapper;
import org.example.medflow.service.PatientService;
import org.example.medflow.util.JwtUtil;
import org.example.medflow.util.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private Agent agent;
    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;


    // 患者注册（仅手机号和密码）
    @Override
    public Result<?> register(RegisterRequest request) {
        // 检查手机号是否已存在
        if (patientMapper.countByPhone(request.getPhone()) > 0) {
            return Result.error(400, "手机号已注册");
        }

        // 加密密码
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 创建患者
        Patient patient = new Patient(request.getPhone(), encodedPassword);
        int result = patientMapper.insert(patient);

        if (result > 0) {
            return Result.success("注册成功", null);
        } else {
            return Result.error(500, "注册失败");
        }
    }

    @Override
    public Result<?> login(LoginRequest request) {
        Patient patient = patientMapper.findByPhone(request.getPhone());
        if (patient == null) {
            return Result.error(400, "手机号或密码错误");
        }
        if (passwordEncoder.matches(request.getPassword(), patient.getPassword())) {
            // 签发带患者角色标识的 JWT，claims 中含 patientId 与 role
            String token = jwtUtil.generateToken(patient.getPatientId(), "patient");
            patient.setPassword("保密");

            LoginResponse response = new LoginResponse();
            // 返回原始 token，"Bearer " 前缀由前端请求头统一拼接
            response.setToken(token);
            response.setPatientId(patient.getPatientId());
            return Result.success("登录成功", response);
        } else {
            return Result.error(400, "手机号或密码错误");
        }
    }

    // 完善患者信息
    @Override
    @Transactional
    public Result<?> updateProfile(UpdateProfileRequest request) {
        // 查询患者是否存在
        Patient existingPatient = patientMapper.findByPatientId(request.getPatientId());
        if (existingPatient == null) {
            return Result.error(404, "患者不存在");
        }

        // 更新患者信息
        existingPatient.setUsername(request.getUsername());
        existingPatient.setRealName(request.getRealName());
        existingPatient.setAge(request.getAge());
        existingPatient.setIdCard(request.getIdCard());
        existingPatient.setEmail(request.getEmail());
        existingPatient.setGender(request.getGender());

        int result = patientMapper.update(existingPatient);

        if (result > 0) {
            // 返回更新后的患者信息（不包含密码）
            existingPatient.setPassword("保密");
            return Result.success("个人信息更新成功", existingPatient);
        } else {
            return Result.error(500, "个人信息更新失败");
        }
    }

    // 获取患者信息
    @Override
    public Result<?> getProfile(Integer patientId) {
        Patient patient = patientMapper.findByPatientId(patientId);
        if (patient == null) {
            return Result.error(404, "患者不存在");
        }

        // 返回患者信息（不包含密码）
        patient.setPassword("保密");
        return Result.success("获取成功", patient);
    }

    // 修改密码
    @Override
    @Transactional
    public Result<?> changePassword(Integer patientId, ChangePasswordRequest request) {
        // 查询患者
        Patient patient = patientMapper.findByPatientId(patientId);
        if (patient == null) {
            return Result.error(404, "患者不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), patient.getPassword())) {
            return Result.error(400, "原密码错误");
        }

        // 加密新密码
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        patient.setPassword(encodedNewPassword);

        int result = patientMapper.updatePassword(patient);

        if (result > 0) {
            return Result.success("密码修改成功", null);
        } else {
            return Result.error(500, "密码修改失败");
        }
    }
}