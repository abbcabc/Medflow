package org.example.medflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.medflow.entity.Doctor;
import org.example.medflow.entity.DoctorSchedule;
import org.example.medflow.mapper.DoctorMapper;
import org.example.medflow.mapper.DoctorScheduleMapper;
import org.example.medflow.service.DoctorScheduleService;
import org.example.medflow.vo.ScheduleBatchVO;
import org.example.medflow.vo.ScheduleOneKeyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorScheduleServiceImpl extends ServiceImpl<DoctorScheduleMapper, DoctorSchedule> implements DoctorScheduleService {

    @Autowired
    private DoctorMapper doctorMapper;

    @Override
    public boolean hasAvailableSlots(Integer doctorId, Date workDate, String timeSlot) {
        // 参数校验
        validateParams(doctorId, workDate, timeSlot);
//        System.out.println("doctorId:"+doctorId+",workDate:"+workDate+",time:"+timeSlot);
        // 查询指定医生的排班信息
        List<DoctorSchedule> schedules = this.baseMapper.selectByDoctorAndDateAndTime(doctorId, workDate, timeSlot);

        // 如果没有排班记录，返回false
        if (schedules == null || schedules.isEmpty()) {
            return false;
        }

        // 检查是否有空闲号源
        return schedules.stream()
                .anyMatch(schedule -> schedule.getTotalSlots() > schedule.getBookedSlots());
    }

    @Override
    public boolean hasAnyAvailableDoctor(Date workDate, String timeSlot) {
        validateParams(null, workDate, timeSlot);

        List<DoctorSchedule> availableSchedules = this.baseMapper.selectAvailableSchedules(workDate, timeSlot);
        return availableSchedules != null && !availableSchedules.isEmpty();
    }

    @Override
    public boolean hasAnyAvailableDoctor(Date workDate) {
        if (workDate == null) {
            throw new IllegalArgumentException("工作日期不能为空");
        }

        List<DoctorSchedule> availableSchedules = this.baseMapper.selectAvailableSchedulesByDate(workDate);
        return availableSchedules != null && !availableSchedules.isEmpty();
    }

    private void validateParams(Integer doctorId, Date workDate, String timeSlot) {
        if (doctorId != null && doctorId <= 0) {
            throw new IllegalArgumentException("医生ID必须大于0");
        }

        if (workDate == null) {
            throw new IllegalArgumentException("工作日期不能为空");
        }

        if (timeSlot == null || (!timeSlot.equals("上午") && !timeSlot.equals("下午"))) {
            throw new IllegalArgumentException("时间必须是'上午'或'下午'");
        }
    }

    @Override
    public Integer getScheduleId(Integer doctorId, Date workDate, String timeSlot) {
        validateParams(doctorId, workDate, timeSlot);

        QueryWrapper<DoctorSchedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("doctor_id", doctorId)
                .eq("work_date", workDate)
                .eq("time_slot", timeSlot)
                .select("schedule_id"); // 只查询schedule_id字段

        DoctorSchedule schedule = this.getOne(queryWrapper);
        return schedule != null ? schedule.getScheduleId() : null;
    }

    /**
     * 原子扣减号源：仅当 booked_slots < total_slots 时才 +1（单条 UPDATE 条件更新，防超卖）
     * 更新影响行数为0即表示号源已满或排班不存在，返回 false
     */
    @Override
    @Transactional
    public boolean increaseBookedSlots(Integer scheduleId) {
        if (scheduleId == null || scheduleId <= 0) {
            throw new IllegalArgumentException("排班ID不能为空");
        }

        UpdateWrapper<DoctorSchedule> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("schedule_id", scheduleId)
                // 条件更新：只有还有剩余号源时才允许扣减
                .apply("booked_slots < total_slots")
                .setSql("booked_slots = booked_slots + 1");

        return this.update(updateWrapper);
    }

    @Override
    @Transactional
    public boolean decreaseBookedSlots(Integer scheduleId) {
        if (scheduleId == null || scheduleId <= 0) {
            throw new IllegalArgumentException("排班ID不能为空");
        }

        // 先检查当前已预约数量
        DoctorSchedule schedule = this.getById(scheduleId);
        if (schedule == null) {
            throw new RuntimeException("未找到对应的排班记录");
        }

        if (schedule.getBookedSlots() <= 0) {
            // 已经是0，不需要减少
            return true;
        }

        // 使用UpdateWrapper进行原子操作
        UpdateWrapper<DoctorSchedule> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("schedule_id", scheduleId)
                .setSql("booked_slots = booked_slots - 1")
                .ge("booked_slots", 1); // 确保不会减到负数

        return this.update(updateWrapper);
    }
    @Override
    public List<DoctorSchedule> getSchedulesByDoctorId(Integer doctorId) {
        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("医生ID不能为空");
        }
        return this.baseMapper.selectByDoctorId(doctorId);
    }

    @Override
    public List<DoctorSchedule> getSchedulesByDoctorIdAndDateRange(Integer doctorId, Date startDate, Date endDate) {
        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("医生ID不能为空");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("开始日期和结束日期不能为空");
        }
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
        return this.baseMapper.selectByDoctorIdAndDateRange(doctorId, startDate, endDate);
    }

    @Override
    public List<DoctorSchedule> getFutureSchedulesByDoctorId(Integer doctorId) {
        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("医生ID不能为空");
        }
        return this.baseMapper.selectFutureSchedulesByDoctorId(doctorId);
    }

    @Override
    public List<DoctorSchedule> getSchedulesByDoctorIdAndDate(Integer doctorId, Date workDate) {
        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("医生ID不能为空");
        }
        if (workDate == null) {
            throw new IllegalArgumentException("工作日期不能为空");
        }
        return this.baseMapper.selectByDoctorIdAndDate(doctorId, workDate);
    }

    //管理员
    /**
     * 新增排班 - 校验：同一医生同一日期同一时间段只能有一条排班
     */
    @Override
    public boolean addSchedule(DoctorSchedule doctorSchedule) {
        // 唯一索引校验
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorSchedule::getDoctorId, doctorSchedule.getDoctorId())
                .eq(DoctorSchedule::getWorkDate, doctorSchedule.getWorkDate())
                .eq(DoctorSchedule::getTimeSlot, doctorSchedule.getTimeSlot());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("该医生在该日期该时间段已存在排班，请勿重复添加");
        }
        // 号源数校验：已预约不能大于总号源
        if (doctorSchedule.getBookedSlots() > doctorSchedule.getTotalSlots()) {
            throw new RuntimeException("已预约号源数不能大于总号源数");
        }
        return this.save(doctorSchedule);
    }

    /**
     * 单条删除排班
     */
    @Override
    public boolean deleteSchedule(Integer scheduleId) {
//        if (!this.existsById(scheduleId)) {
//            throw new RuntimeException("该排班记录不存在");
//        }
        return this.removeById(scheduleId);
    }

    /**
     * 批量删除排班
     */
    @Override
    public boolean batchDeleteSchedule(List<Integer> scheduleIds) {
        // 校验排班是否存在
        List<DoctorSchedule> list = this.listByIds(scheduleIds);
        if (list.size() != scheduleIds.size()) {
            throw new RuntimeException("部分排班记录不存在，删除失败");
        }
        return this.removeByIds(scheduleIds);
    }

    /**
     * 修改排班 - 排除自身的唯一索引校验
     */
    @Override
    public boolean updateSchedule(DoctorSchedule doctorSchedule) {
        Integer scheduleId = doctorSchedule.getScheduleId();
//        if (!this.existsById(scheduleId)) {
//            throw new RuntimeException("该排班记录不存在");
//        }
        // 唯一索引校验（排除当前记录）
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorSchedule::getDoctorId, doctorSchedule.getDoctorId())
                .eq(DoctorSchedule::getWorkDate, doctorSchedule.getWorkDate())
                .eq(DoctorSchedule::getTimeSlot, doctorSchedule.getTimeSlot())
                .ne(DoctorSchedule::getScheduleId, scheduleId);
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("该医生在该日期该时间段已存在排班，修改失败");
        }
        // 号源数校验
        if (doctorSchedule.getBookedSlots() > doctorSchedule.getTotalSlots()) {
            throw new RuntimeException("已预约号源数不能大于总号源数");
        }
        return this.updateById(doctorSchedule);
    }

    /**
     * 按ID查询排班
     */
    @Override
    public DoctorSchedule getScheduleById(Integer scheduleId) {
        return this.getById(scheduleId);
    }

    /**
     * 条件查询排班（支持医生ID/工作日期/时间段任意组合）
     */
    @Override
    public List<DoctorSchedule> listScheduleByCondition(DoctorSchedule doctorSchedule) {
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
        if (doctorSchedule.getDoctorId() != null) {
            wrapper.eq(DoctorSchedule::getDoctorId, doctorSchedule.getDoctorId());
        }
        if (doctorSchedule.getWorkDate() != null) {
            wrapper.eq(DoctorSchedule::getWorkDate, doctorSchedule.getWorkDate());
        }
        if (StringUtils.hasText(doctorSchedule.getTimeSlot())) {
            wrapper.eq(DoctorSchedule::getTimeSlot, doctorSchedule.getTimeSlot());
        }
        // 按日期倒序、时间段正序排序
        wrapper.orderByDesc(DoctorSchedule::getScheduleId);
        return this.list(wrapper);
    }

    /**
     * 一键排班 - 支持指定医生、日期范围、多个时间段批量生成排班
     * 开启事务，保证批量插入的原子性
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int oneKeySchedule(ScheduleOneKeyVO vo) {
        Date startDate = vo.getStartDate();
        Date endDate = vo.getEndDate();
        // 日期合法性校验：开始日期不能晚于结束日期
        if (startDate.after(endDate)) {
            throw new RuntimeException("开始日期不能晚于结束日期");
        }
        // 拆分时间段
        List<String> timeSlotList = List.of(vo.getTimeSlots().split(","));
        // 确定排班医生：未指定 doctorId 时对全部医生一键排班
        List<Integer> doctorIds;
        if (vo.getDoctorId() != null) {
            doctorIds = List.of(vo.getDoctorId());
        } else {
            List<Doctor> doctors = doctorMapper.selectList(null);
            if (doctors == null || doctors.isEmpty()) {
                throw new RuntimeException("系统中暂无医生，无法生成排班");
            }
            doctorIds = doctors.stream().map(Doctor::getDoctorId).collect(Collectors.toList());
        }
        // 构造排班列表
        List<DoctorSchedule> scheduleList = new ArrayList<>();
        // 遍历日期范围
        Date currentDate = startDate;
        while (!currentDate.after(endDate)) {
            for (Integer doctorId : doctorIds) {
                for (String timeSlot : timeSlotList) {
                    DoctorSchedule schedule = new DoctorSchedule();
                    schedule.setDoctorId(doctorId);
                    schedule.setWorkDate(currentDate);
                    schedule.setTimeSlot(timeSlot);
                    schedule.setTotalSlots(vo.getTotalSlots());
                    schedule.setBookedSlots(0);
                    schedule.setIsAvailable(1);
                    // 提前校验唯一索引，避免批量插入失败
                    if (checkUnique(schedule.getDoctorId(), schedule.getWorkDate(), schedule.getTimeSlot())) {
                        scheduleList.add(schedule);
                    } else {
                        log.warn("医生" + doctorId + "在" + currentDate + " " + timeSlot + " 已存在排班，跳过");
                    }
                }
            }
            // 日期+1天
            currentDate = new Date(currentDate.getTime() + 24 * 60 * 60 * 1000L);
        }
        // 批量插入
        if (scheduleList.isEmpty()) {
            throw new RuntimeException("所选日期范围内排班已存在，无可生成的排班记录");
        }
        this.saveBatch(scheduleList);
        return scheduleList.size();
    }

    /**
     * 批量修改排班 - 支持批量修改状态/总号源/已预约号源
     * 开启事务，保证批量修改的原子性
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateSchedule(ScheduleBatchVO vo) {
        List<Integer> scheduleIds = vo.getScheduleIds();
        // 校验排班是否存在
        List<DoctorSchedule> scheduleList = this.listByIds(scheduleIds);
        if (scheduleList.size() != scheduleIds.size()) {
            throw new RuntimeException("部分排班记录不存在，批量操作失败");
        }
        // 构造修改后的排班列表
        List<DoctorSchedule> updateList = scheduleList.stream().map(schedule -> {
            if (vo.getIsAvailable() != null) {
                schedule.setIsAvailable(vo.getIsAvailable());
            }
            if (vo.getTotalSlots() != null) {
                schedule.setTotalSlots(vo.getTotalSlots());
            }
            if (vo.getBookedSlots() != null) {
                // 号源数校验
                if (vo.getBookedSlots() > (vo.getTotalSlots() == null ? schedule.getTotalSlots() : vo.getTotalSlots())) {
                    throw new RuntimeException("已预约号源数不能大于总号源数");
                }
                schedule.setBookedSlots(vo.getBookedSlots());
            }
            return schedule;
        }).collect(Collectors.toList());
        // 批量更新
        return this.updateBatchById(updateList);
    }

    /**
     * 私有方法：校验医生-日期-时间段的唯一性
     */
    private boolean checkUnique(Integer doctorId, Date workDate, String timeSlot) {
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorSchedule::getDoctorId, doctorId)
                .eq(DoctorSchedule::getWorkDate, workDate)
                .eq(DoctorSchedule::getTimeSlot, timeSlot);
        return this.count(wrapper) == 0;
    }

    @Override
    public IPage<DoctorSchedule> pageScheduleList(Integer pageNum, Integer pageSize, DoctorSchedule doctorSchedule) {
        // 分页对象
        IPage<DoctorSchedule> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
        // 条件拼接（不传就是查看全部）
        if (doctorSchedule.getDoctorId() != null) {
            wrapper.eq(DoctorSchedule::getDoctorId, doctorSchedule.getDoctorId());
        }
        if (doctorSchedule.getWorkDate() != null) {
            wrapper.eq(DoctorSchedule::getWorkDate, doctorSchedule.getWorkDate());
        }
        if (StringUtils.hasText(doctorSchedule.getTimeSlot())) {
            wrapper.eq(DoctorSchedule::getTimeSlot, doctorSchedule.getTimeSlot());
        }
        if (doctorSchedule.getIsAvailable() != null) {
            wrapper.eq(DoctorSchedule::getIsAvailable, doctorSchedule.getIsAvailable());
        }

        // 排序：日期倒序 > 时间段
        wrapper.orderByDesc(DoctorSchedule::getWorkDate)
                .orderByAsc(DoctorSchedule::getTimeSlot);

        return this.page(page, wrapper);
    }
}