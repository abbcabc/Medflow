package org.example.medflow.service;

import org.example.medflow.dto.*;

public interface PatientService {

    /**
     * 患者注册（仅手机号和密码）
     * @param request 注册请求DTO
     * @return 结果
     */
    Result<?> register(RegisterRequest request);

    /**
     * 患者登录
     * @param request 登录请求DTO
     * @return 结果（包含token和患者ID）
     */
    Result<?> login(LoginRequest request);

    /**
     * 完善患者个人信息
     * @param request 信息更新请求DTO
     * @return 结果（包含更新后的患者信息）
     */
    Result<?> updateProfile(UpdateProfileRequest request);

    /**
     * 获取患者个人信息
     * @param patientId 患者ID
     * @return 结果（包含患者信息）
     */
    Result<?> getProfile(Integer patientId);

    /**
     * 修改患者密码
     * @param patientId 患者ID
     * @param request 密码修改请求DTO
     * @return 结果
     */
    Result<?> changePassword(Integer patientId, ChangePasswordRequest request);
}