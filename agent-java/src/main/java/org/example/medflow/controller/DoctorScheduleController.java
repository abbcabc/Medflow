package org.example.medflow.controller;

import org.example.medflow.dto.Result;
import org.example.medflow.entity.DoctorSchedule;
import org.example.medflow.service.DoctorScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/doctor-schedule")
public class DoctorScheduleController {

    @Autowired
    private DoctorScheduleService doctorScheduleService;

    /**
     * 根据医生ID获取所有排班记录
     */
    @GetMapping("/doctor/{doctorId}")
    public Result<?> getSchedulesByDoctorId(@PathVariable("doctorId") Integer doctorId) {
        try {
            List<DoctorSchedule> schedules = doctorScheduleService.getSchedulesByDoctorId(doctorId);
            return Result.success("获取排班记录成功", schedules);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "获取排班记录失败");
        }
    }

    /**
     * 根据医生ID获取未来排班记录
     */
    @GetMapping("/doctor/{doctorId}/future")
    public Result<?> getFutureSchedulesByDoctorId(@PathVariable("doctorId") Integer doctorId) {
        try {
            List<DoctorSchedule> schedules = doctorScheduleService.getFutureSchedulesByDoctorId(doctorId);
            return Result.success("获取未来排班记录成功", schedules);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "获取未来排班记录失败");
        }
    }

    /**
     * 根据医生ID和日期获取排班记录
     */
    @GetMapping("/doctor/{doctorId}/date/{workDate}")
    public Result<?> getSchedulesByDoctorIdAndDate(
            @PathVariable("doctorId") Integer doctorId,
            @PathVariable("workDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date workDate) {
        try {
            List<DoctorSchedule> schedules = doctorScheduleService.getSchedulesByDoctorIdAndDate(doctorId, workDate);
            return Result.success("获取排班记录成功", schedules);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "获取排班记录失败");
        }
    }

    /**
     * 根据医生ID和日期范围获取排班记录
     */
    @GetMapping("/doctor/{doctorId}/date-range")
    public Result<?> getSchedulesByDoctorIdAndDateRange(
            @PathVariable("doctorId") Integer doctorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            List<DoctorSchedule> schedules = doctorScheduleService.getSchedulesByDoctorIdAndDateRange(doctorId, startDate, endDate);
            return Result.success("获取排班记录成功", schedules);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "获取排班记录失败");
        }
    }

    /**
     * 获取医生排班统计信息
     */
    @GetMapping("/doctor/{doctorId}/stats")
    public Result<?> getScheduleStats(@PathVariable("doctorId") Integer doctorId) {
        try {
            List<DoctorSchedule> futureSchedules = doctorScheduleService.getFutureSchedulesByDoctorId(doctorId);

            long totalSlots = futureSchedules.stream().mapToInt(DoctorSchedule::getTotalSlots).sum();
            long bookedSlots = futureSchedules.stream().mapToInt(DoctorSchedule::getBookedSlots).sum();
            long availableSlots = totalSlots - bookedSlots;

            // 构建统计信息
            ScheduleStats stats = new ScheduleStats();
            stats.setTotalSchedules(futureSchedules.size());
            stats.setTotalSlots(totalSlots);
            stats.setBookedSlots(bookedSlots);
            stats.setAvailableSlots(availableSlots);
            stats.setFutureSchedules(futureSchedules);

            return Result.success("获取排班统计成功", stats);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "获取排班统计失败");
        }
    }

    // 内部统计类
    public static class ScheduleStats {
        private int totalSchedules;
        private long totalSlots;
        private long bookedSlots;
        private long availableSlots;
        private List<DoctorSchedule> futureSchedules;

        // getter和setter方法
        public int getTotalSchedules() { return totalSchedules; }
        public void setTotalSchedules(int totalSchedules) { this.totalSchedules = totalSchedules; }

        public long getTotalSlots() { return totalSlots; }
        public void setTotalSlots(long totalSlots) { this.totalSlots = totalSlots; }

        public long getBookedSlots() { return bookedSlots; }
        public void setBookedSlots(long bookedSlots) { this.bookedSlots = bookedSlots; }

        public long getAvailableSlots() { return availableSlots; }
        public void setAvailableSlots(long availableSlots) { this.availableSlots = availableSlots; }

        public List<DoctorSchedule> getFutureSchedules() { return futureSchedules; }
        public void setFutureSchedules(List<DoctorSchedule> futureSchedules) { this.futureSchedules = futureSchedules; }
    }
}