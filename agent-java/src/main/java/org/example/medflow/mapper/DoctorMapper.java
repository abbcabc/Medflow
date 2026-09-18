package org.example.medflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.example.medflow.entity.Doctor;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

    /**
     * 根据科室ID查询医生
     */
    @Select("SELECT * FROM doctor WHERE dept_id = #{deptId} AND is_available = 1")
    List<Doctor> selectByDepartmentId(@Param("deptId") Integer deptId);

    /**
     * 根据专长查询医生
     */
    @Select("SELECT * FROM doctor WHERE specialty LIKE CONCAT('%', #{specialty}, '%') AND is_available = 1")
    List<Doctor> selectBySpecialty(@Param("specialty") String specialty);

    /**
     * 查询经验丰富的医生（经验大于指定年限）
     */
    @Select("SELECT * FROM doctor WHERE years_experience >= #{minExperience} AND is_available = 1 ORDER BY years_experience DESC")
    List<Doctor> selectExperiencedDoctors(@Param("minExperience") Integer minExperience);

    /**
     * 根据多个ID查询医生
     */
    @Select("<script>" +
            "SELECT * FROM doctor WHERE doctor_id IN " +
            "<foreach collection='doctorIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND is_available = 1" +
            "</script>")
    List<Doctor> selectDoctorsByIds(@Param("doctorIds") List<Integer> doctorIds);

    /**
     * 根据医生姓名查询医生
     */
    @Select("SELECT * FROM doctor WHERE doctor_name LIKE CONCAT('%', #{doctorName}, '%') AND is_available = 1")
    List<Doctor> selectByDoctorName(@Param("doctorName") String doctorName);

    /**
     * 根据职称查询医生
     */
    @Select("SELECT * FROM doctor WHERE title LIKE CONCAT('%', #{title}, '%') AND is_available = 1")
    List<Doctor> selectByTitle(@Param("title") String title);

    /**
     * 查询所有可用医生（新增方法）
     */
    @Select("SELECT * FROM doctor WHERE is_available = 1")
    List<Doctor> selectAllAvailableDoctors();

    /**
     * 根据咨询费用范围查询医生（新增方法）
     */
    @Select("SELECT * FROM doctor WHERE consultation_fee BETWEEN #{minFee} AND #{maxFee} AND is_available = 1")
    List<Doctor> selectByFeeRange(@Param("minFee") Float minFee, @Param("maxFee") Float maxFee);

    /**
     * 根据工作经验范围查询医生（新增方法）
     */
    @Select("SELECT * FROM doctor WHERE years_experience BETWEEN #{minExp} AND #{maxExp} AND is_available = 1")
    List<Doctor> selectByExperienceRange(@Param("minExp") Integer minExp, @Param("maxExp") Integer maxExp);

    /**
     * 根据部门ID和医生姓名查找医生（用于登录）
     */
    @Select("SELECT * FROM doctor WHERE dept_id = #{deptId} AND doctor_name = #{doctorName}")
    Doctor findByDeptIdAndName(@Param("deptId") Integer deptId, @Param("doctorName") String doctorName);

    /**
     * 根据医生ID查找医生
     */
    @Select("SELECT * FROM doctor WHERE doctor_id = #{doctorId}")
    Doctor findByDoctorId(@Param("doctorId") Integer doctorId);

    /**
     * 更新医生密码
     */
    @Update("UPDATE doctor SET password = #{password} WHERE doctor_id = #{doctorId}")
    int updatePassword(@Param("doctorId") Integer doctorId, @Param("password") String password);

    /**
     * 更新医生头像
     */
    @Update("UPDATE doctor SET avatar_url = #{avatarUrl} WHERE doctor_id = #{doctorId}")
    int updateAvatar(@Param("doctorId") Integer doctorId, @Param("avatarUrl") String avatarUrl);
}