package org.example.medflow.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.medflow.service.AppointmentService;
import org.example.medflow.vo.R;
import org.example.medflow.entity.DoctorSchedule;
import org.example.medflow.service.DoctorScheduleService;
import org.example.medflow.vo.ScheduleBatchVO;
import org.example.medflow.vo.ScheduleOneKeyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 医生排班控制层
 * 接口前缀：/api/schedule
 */
@RestController
@RequestMapping("/admin/doctorSchedule")
@Validated
public class DocScheduleAdminController {

    @Autowired
    private DoctorScheduleService doctorScheduleService;

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 新增排班
     * POST /api/schedule/add
     */
    @PostMapping("/add")
    public R<Boolean> addSchedule(@Valid @RequestBody DoctorSchedule doctorSchedule) {
        try {
            boolean result = doctorScheduleService.addSchedule(doctorSchedule);
            return R.success(result);
        } catch (RuntimeException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 单条删除排班
     * DELETE /api/schedule/delete/{scheduleId}
     */
    @DeleteMapping("/delete/{scheduleId}")
    public R<Boolean> deleteSchedule(@NotNull @PathVariable("scheduleId") Integer scheduleId) {
        System.out.println(appointmentService.getAppointmentsByScheduleId(scheduleId));
        if (!appointmentService.getAppointmentsByScheduleId(scheduleId).isEmpty()) {
            return R.error("该排班已被预约，无法删除！");
        }
        try {
            boolean result = doctorScheduleService.deleteSchedule(scheduleId);
            return R.success(result);
        } catch (RuntimeException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 批量删除排班
     * DELETE /api/schedule/batch/delete
     */
    @DeleteMapping("/batch/delete")
    public R<Boolean> batchDeleteSchedule(@RequestBody List<Integer> scheduleIds) {
        try {
            boolean result = doctorScheduleService.batchDeleteSchedule(scheduleIds);
            return R.success(result);
        } catch (RuntimeException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 修改排班
     * PUT /api/schedule/update
     */
    @PutMapping("/update")
    public R<Boolean> updateSchedule(@Valid @RequestBody DoctorSchedule doctorSchedule) {
        try {
            boolean result = doctorScheduleService.updateSchedule(doctorSchedule);
            return R.success(result);
        } catch (RuntimeException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 按ID查询排班
     * GET /api/schedule/get/{scheduleId}
     */
    @GetMapping("/get/{scheduleId}")
    public R<DoctorSchedule> getScheduleById(@NotNull @PathVariable("scheduleId") Integer scheduleId) {
        DoctorSchedule schedule = doctorScheduleService.getScheduleById(scheduleId);
        return R.success(schedule);
    }

    /**
     * 条件查询排班
     * POST /api/schedule/list
     */
    @PostMapping("/list")
    public R<List<DoctorSchedule>> listScheduleByCondition(@RequestBody DoctorSchedule doctorSchedule) {
        List<DoctorSchedule> list = doctorScheduleService.listScheduleByCondition(doctorSchedule);
        return R.success(list);
    }

    /**
     * 一键排班
     * POST /api/schedule/oneKey
     */
    @PostMapping("/oneKey")
    public R<Integer> oneKeySchedule(@Valid @RequestBody ScheduleOneKeyVO vo) {
        try {
            int count = doctorScheduleService.oneKeySchedule(vo);
            return R.success(count);
        } catch (RuntimeException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 批量修改排班
     * PUT /api/schedule/batch/update
     */
    @PutMapping("/batch/update")
    public R<Boolean> batchUpdateSchedule(@Valid @RequestBody ScheduleBatchVO vo) {
        try {
            boolean result = doctorScheduleService.batchUpdateSchedule(vo);
            return R.success(result);
        } catch (RuntimeException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 分页查询排班列表（查看全部/筛选）
     * POST /api/schedule/pageList
     * 不传条件 = 查看全部排班
     */
    @PostMapping("/pageList")
    public R<IPage<DoctorSchedule>> pageScheduleList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestBody DoctorSchedule doctorSchedule) {
        IPage<DoctorSchedule> page = doctorScheduleService.pageScheduleList(pageNum, pageSize, doctorSchedule);
        return R.success(page);
    }
}
