package org.example.medflow.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.medflow.dto.Result;
import org.example.medflow.service.AppointmentService;
import org.example.medflow.vo.PatientAppointmentRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "appointment-controller", description = "预约管理-医生接诊相关接口")
@RestController
@RequestMapping("/api/doctor/appointment")
public class DoctorAppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 医生端：根据医生ID查询当前接诊的所有患者（含每个患者的所有就诊记录）
     * @param doctorId 医生ID
     * @return 接诊患者列表（含就诊记录）
     */
    @GetMapping("/current-reception")
    public Result<?> getCurrentReceptionPatients(@RequestParam("doctorId") Integer doctorId) {
        return Result.success("查询成功", appointmentService.getCurrentReceptionPatients(doctorId));
    }

    /**
     * 医生端：单独查看某患者与自己的所有就诊记录
     * @param doctorId 医生ID
     * @param patientId 患者ID
     * @return 该患者的所有就诊记录
     */
    @GetMapping("/patient-records")
    public Result<?> getPatientAllRecords(
            @RequestParam("doctorId") Integer doctorId,
            @RequestParam("patientId") Integer patientId
    ) {
        List<PatientAppointmentRecordVO> records = appointmentService.getPatientAllRecords(doctorId, patientId);
        if (records.isEmpty()) {
            return Result.success("该患者暂无就诊记录", records);
        }
        return Result.success("查询成功", records);
    }
}
