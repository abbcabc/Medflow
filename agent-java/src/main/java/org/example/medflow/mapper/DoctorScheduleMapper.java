package org.example.medflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.medflow.entity.DoctorSchedule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.Date;
import java.util.List;

@Mapper
public interface DoctorScheduleMapper extends BaseMapper<DoctorSchedule> {

    /**
     * 根据医生ID、日期和时间段查询排班信息
     */
    @Select("SELECT * FROM doctor_schedule WHERE doctor_id = #{doctorId} AND work_date = #{workDate} AND time_slot = #{timeSlot}")
    List<DoctorSchedule> selectByDoctorAndDateAndTime(
            @Param("doctorId") Integer doctorId,
            @Param("workDate") Date workDate,
            @Param("timeSlot") String timeSlot);

    /**
     * 根据日期和时间段查询所有有号源的医生排班
     */
    @Select("SELECT * FROM doctor_schedule WHERE work_date = #{workDate} AND time_slot = #{timeSlot} AND total_slots > booked_slots")
    List<DoctorSchedule> selectAvailableSchedules(
            @Param("workDate") Date workDate,
            @Param("timeSlot") String timeSlot);

    /**
     * 根据日期查询所有有号源的医生排班（不区分时间段）
     */
    @Select("SELECT * FROM doctor_schedule WHERE work_date = #{workDate} AND total_slots > booked_slots")
    List<DoctorSchedule> selectAvailableSchedulesByDate(@Param("workDate") Date workDate);

    /**
     * 根据医生ID查询排班记录
     */
    @Select("SELECT * FROM doctor_schedule WHERE doctor_id = #{doctorId} ORDER BY work_date DESC, time_slot")
    List<DoctorSchedule> selectByDoctorId(@Param("doctorId") Integer doctorId);

    /**
     * 根据医生ID和日期范围查询排班记录
     */
    @Select("SELECT * FROM doctor_schedule WHERE doctor_id = #{doctorId} AND work_date BETWEEN #{startDate} AND #{endDate} ORDER BY work_date, time_slot")
    List<DoctorSchedule> selectByDoctorIdAndDateRange(@Param("doctorId") Integer doctorId,
                                                      @Param("startDate") Date startDate,
                                                      @Param("endDate") Date endDate);

    /**
     * 根据医生ID查询未来日期的排班记录
     */
    @Select("SELECT * FROM doctor_schedule WHERE doctor_id = #{doctorId} AND work_date >= CURDATE() ORDER BY work_date, time_slot")
    List<DoctorSchedule> selectFutureSchedulesByDoctorId(@Param("doctorId") Integer doctorId);

    /**
     * 根据医生ID和具体日期查询排班记录
     */
    @Select("SELECT * FROM doctor_schedule WHERE doctor_id = #{doctorId} AND work_date = #{workDate} ORDER BY time_slot")
    List<DoctorSchedule> selectByDoctorIdAndDate(@Param("doctorId") Integer doctorId,
                                                 @Param("workDate") Date workDate);
}