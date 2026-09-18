package org.example.medflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.medflow.dto.DoctorLoginRequest;
import org.example.medflow.dto.Result;
import org.example.medflow.entity.Doctor;
import java.util.Date;
import java.util.List;

public interface DoctorService extends IService<Doctor> {

    IPage<Doctor> getPage(Integer pageNum, Integer pageSize, String doctorName);

    /**
     * 根据医生姓名查询医生信息
     */
    List<Doctor> findDoctorsByName(String doctorName);

    /**
     * 根据科室ID查询可用医生
     */
    List<Doctor> findAvailableDoctors(Integer deptId, Date workDate, String timeSlot);

    /**
     * 根据医生ID获取所属科室ID
     */
    Integer getDoctorDepartment(Integer doctorId);

    /**
     * 根据科室名称查询医生
     */
    List<Doctor> findDoctorsByDepartmentName(String departmentName);

    /**
     * 查询所有可用医生
     */
    List<Doctor> findAllAvailableDoctors();

    /**
     * 根据专长查询医生
     */
    List<Doctor> findDoctorsBySpecialty(String specialty);

    /**
     * 医生登录
     */
    Result<?> login(DoctorLoginRequest request);

    /**
     * 获取医生个人信息
     */
    Result<?> getProfile(Integer doctorId);

    /**
     * 修改医生密码
     */
    Result<?> changePassword(Integer doctorId, String oldPassword, String newPassword);
}