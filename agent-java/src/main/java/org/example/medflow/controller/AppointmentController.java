package org.example.medflow.controller;

// 在控制器中添加接口
import org.example.medflow.dto.AppointmentDetailDTO;
import org.example.medflow.service.AppointmentService;
import org.example.medflow.vo.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/patient")
    public List<AppointmentDetailDTO> getAppointmentsByPatientId(@RequestParam("userId") String userId) {
        return appointmentService.getAppointmentDetailsByPatientId(parseInt(userId));
    }

    /**
     * 查询预约列表（支持筛选）
     * @param params 筛选参数：keyword、status
     * @return 预约列表
     */
    @GetMapping("/list")
    public R<?> getAppointmentList(@RequestParam Map<String, String> params) {
        System.out.println(appointmentService.getAppointmentList(params));
        return appointmentService.getAppointmentList(params);
    }

    /**
     * 生成测试预约数据（随机患者/医生/日期/时段/状态）
     * @param count 生成条数，默认10，上限20
     * @return 生成结果
     */
    @PostMapping("/generate-test")
    public R<?> generateTestAppointments(@RequestParam(value = "count", defaultValue = "10") Integer count) {
        int n = Math.min(Math.max(count, 1), 20);
        try {
            int created = appointmentService.generateTestAppointments(n);
            return R.success("成功生成 " + created + " 条测试预约记录");
        } catch (RuntimeException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 修改预约状态
     * @param appointmentId 预约ID
     * @param status 新状态
     * @return 操作结果
     */
    @PutMapping("/status/{appointmentId}")
    public R<?> updateStatus(
            @PathVariable("appointmentId") Integer appointmentId, // 加上名称
            @RequestParam("status") String status
    ) {
        return appointmentService.updateAppointmentStatus(appointmentId, status);
    }

    /**
     * 查询预约详情
     * @param appointmentId 预约ID
     * @return 预约详情
     */
    @GetMapping("/{appointmentId}")
    public R<?> getDetail(@PathVariable("appointmentId") Integer appointmentId) {
        return appointmentService.getAppointmentDetail(appointmentId);
    }
}