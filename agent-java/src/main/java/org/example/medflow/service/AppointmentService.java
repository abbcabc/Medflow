package org.example.medflow.service;

// 在 AppointmentService.java 中添加接口方法

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.medflow.entity.Appointment;
import org.example.medflow.dto.AppointmentDetailDTO;
import org.example.medflow.vo.CurrentPatientVO;
import org.example.medflow.vo.PatientAppointmentRecordVO;
import org.example.medflow.vo.R;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface AppointmentService extends IService<Appointment> {

    Appointment getByAppointmentNumber(String appointmentNumber);

    boolean hasDuplicateAppointment(Integer patientId, Integer doctorId, Date appointmentDate, String timeSlot);

    /**
     * 生成测试预约数据（随机患者/医生/日期/时段/状态）
     * @param count 生成条数
     * @return 实际生成条数
     */
    int generateTestAppointments(int count);

    // 新增方法：根据患者ID获取预约详情
    List<AppointmentDetailDTO> getAppointmentDetailsByPatientId(Integer patientId);

    /**
     * 查询预约列表（连表+筛选）
     * @param params 筛选参数：keyword(关键词)、status(状态)
     * @return 预约列表
     */
    R<?> getAppointmentList(Map<String, String> params);

    /**
     * 修改预约状态
     * @param appointmentId 预约ID
     * @param status 新状态
     * @return 操作结果
     */
    R<?> updateAppointmentStatus(Integer appointmentId, String status);

    /**
     * 查询预约详情
     * @param appointmentId 预约ID
     * @return 预约详情
     */
    R<?> getAppointmentDetail(Integer appointmentId);

    /**
     * 根据医生ID查询当前接诊的所有患者（含每个患者的就诊记录）
     */
    List<CurrentPatientVO> getCurrentReceptionPatients(Integer doctorId);

    /**
     * 单独查询某患者与该医生的所有就诊记录
     */
    List<PatientAppointmentRecordVO> getPatientAllRecords(Integer doctorId, Integer patientId);

    /**
     * 根据排班ID查询预约记录
     * @param scheduleId 排班ID
     * @return 预约详情列表
     */
    List<AppointmentDetailDTO> getAppointmentsByScheduleId(Integer scheduleId);

}