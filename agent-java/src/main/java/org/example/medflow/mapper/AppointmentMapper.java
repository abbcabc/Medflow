// 在 AppointmentMapper.java 中添加查询方法
package org.example.medflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.medflow.entity.Appointment;
import org.example.medflow.dto.AppointmentDetailDTO;
import org.example.medflow.vo.PatientAppointmentRecordVO;

import java.util.List;
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    @Select("SELECT a.*, d.doctor_name, dept.dept_name " +
            "FROM appointment a " +
            "LEFT JOIN doctor d ON a.doctor_id = d.doctor_id " +
            "LEFT JOIN department dept ON a.dept_id = dept.dept_id " +
            "WHERE a.patient_id = #{patientId} " +
            "ORDER BY a.appointment_date DESC")
    List<AppointmentDetailDTO> getAppointmentDetailsByPatientId(Integer patientId);

    /**
     * 连表查询所有预约（关联患者/医生/科室）
     * @param keyword 筛选关键词（预约编号/患者名/医生名/科室名）
     * @param status 预约状态
     * @return 预约列表（含中文名称）
     */
    List<Appointment> selectAppointmentList(
            @Param("keyword") String keyword,
            @Param("status") String status
    );

    /**
     * 根据ID修改预约状态
     * @param appointmentId 预约ID
     * @param status 新状态
     * @return 受影响行数
     */
    int updateAppointmentStatus(
            @Param("appointmentId") Integer appointmentId,
            @Param("status") String status
    );

    /**
     * 根据ID查询预约详情（连表）
     * @param appointmentId 预约ID
     * @return 预约详情
     */
    Appointment selectAppointmentDetail(@Param("appointmentId") Integer appointmentId);

    /**
     * 根据医生ID查询当前接诊的患者预约（status=confirmed），关联患者基础信息
     */
    List<PatientAppointmentRecordVO> selectCurrentReceptionByDoctorId(@Param("doctorId") Integer doctorId);

    /**
     * 根据医生ID+患者ID查询该患者的所有就诊记录，关联患者基础信息
     */
    List<PatientAppointmentRecordVO> selectPatientAllRecordsByDocIdAndPatId(
            @Param("doctorId") Integer doctorId,
            @Param("patientId") Integer patientId
    );

    /**
     * 根据排班ID查询预约记录
     * @param scheduleId 排班ID
     * @return 预约记录列表
     */
    @Select("SELECT a.*, d.doctor_name, dept.dept_name " +
            "FROM appointment a " +
            "LEFT JOIN doctor d ON a.doctor_id = d.doctor_id " +
            "LEFT JOIN department dept ON a.dept_id = dept.dept_id " +
            "WHERE a.schedule_id = #{scheduleId} ")
    List<AppointmentDetailDTO> selectAppointmentsByScheduleId(@Param("scheduleId") Integer scheduleId);
}