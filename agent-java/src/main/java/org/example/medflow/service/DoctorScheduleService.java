package org.example.medflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.medflow.entity.DoctorSchedule;
import org.example.medflow.vo.ScheduleBatchVO;
import org.example.medflow.vo.ScheduleOneKeyVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface DoctorScheduleService extends IService<DoctorSchedule> {

    /**
     * 查询指定医生在某个日期和时间段是否有号源
     */
    boolean hasAvailableSlots(Integer doctorId, Date workDate, String timeSlot);

    /**
     * 查询某个日期和时间段是否有任何医生有空闲号源
     */
    boolean hasAnyAvailableDoctor(Date workDate, String timeSlot);

    /**
     * 查询某个日期是否有任何医生有空闲号源
     */
    boolean hasAnyAvailableDoctor(Date workDate);

    /**
     * 增加已预约数量
     */
    boolean increaseBookedSlots(Integer scheduleId);

    /**
     * 减少已预约数量
     */
    boolean decreaseBookedSlots(Integer scheduleId);

    /**
     * 根据医生ID、日期和时间段获取排班ID
     */
    Integer getScheduleId(Integer doctorId, Date workDate, String timeSlot);

    /**
     * 根据医生ID获取排班记录
     */
    List<DoctorSchedule> getSchedulesByDoctorId(Integer doctorId);

    /**
     * 根据医生ID和日期范围获取排班记录
     */
    List<DoctorSchedule> getSchedulesByDoctorIdAndDateRange(Integer doctorId, Date startDate, Date endDate);

    /**
     * 获取医生未来的排班记录
     */
    List<DoctorSchedule> getFutureSchedulesByDoctorId(Integer doctorId);

    /**
     * 根据医生ID和具体日期获取排班记录
     */
    List<DoctorSchedule> getSchedulesByDoctorIdAndDate(Integer doctorId, Date workDate);


    /**
     * 新增排班
     * @param doctorSchedule 排班信息
     * @return 操作结果
     */
    boolean addSchedule(DoctorSchedule doctorSchedule);

    /**
     * 删除排班（单条）
     * @param scheduleId 排班ID
     * @return 操作结果
     */
    boolean deleteSchedule(Integer scheduleId);

    /**
     * 批量删除排班
     * @param scheduleIds 排班ID集合
     * @return 操作结果
     */
    boolean batchDeleteSchedule(List<Integer> scheduleIds);

    /**
     * 修改排班
     * @param doctorSchedule 排班信息
     * @return 操作结果
     */
    boolean updateSchedule(DoctorSchedule doctorSchedule);

    /**
     * 按ID查询排班
     * @param scheduleId 排班ID
     * @return 排班信息
     */
    DoctorSchedule getScheduleById(Integer scheduleId);

    /**
     * 条件查询排班（医生ID/日期/时间段）
     * @param doctorSchedule 查询条件
     * @return 排班列表
     */
    List<DoctorSchedule> listScheduleByCondition(DoctorSchedule doctorSchedule);

    /**
     * 一键排班
     * @param vo 一键排班参数（医生ID/日期范围/时间段/总号源）
     * @return 成功排班数量
     */
    int oneKeySchedule(ScheduleOneKeyVO vo);

    /**
     * 批量修改排班状态/号源
     * @param vo 批量操作参数
     * @return 操作结果
     */
    boolean batchUpdateSchedule(ScheduleBatchVO vo);

    /**
     * 分页查询排班列表（查看全部/条件筛选）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param doctorSchedule 查询条件
     * @return 分页数据
     */
    IPage<DoctorSchedule> pageScheduleList(Integer pageNum, Integer pageSize, DoctorSchedule doctorSchedule);
}