package org.example.medflow.controller;

import org.example.medflow.dto.*;
import org.example.medflow.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    // 患者注册接口
    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody RegisterRequest request) {
        return patientService.register(request);
    }

    // 患者登录接口
    @PostMapping("/login")
    public Result<?> login(@Validated @RequestBody LoginRequest request) {
        return patientService.login(request);
    }

    // 完善个人信息接口 - 从请求体中获取patientId
    @PutMapping("/profile")
    public Result<?> updateProfile(@Validated @RequestBody UpdateProfileRequest request) {
        return patientService.updateProfile(request);
    }

    // 获取个人信息接口 - 使用查询参数
    @GetMapping("/profile")
    public Result<?> getProfile(@RequestParam("patientId") Integer patientId) {
        return patientService.getProfile(patientId);
    }

    // 修改密码接口
    @PutMapping("/changePassword/{patientId}")
    public Result<?> changePassword(
            @PathVariable("patientId") Integer patientId,
            @Validated @RequestBody ChangePasswordRequest request) {
        return patientService.changePassword(patientId, request);
    }
}