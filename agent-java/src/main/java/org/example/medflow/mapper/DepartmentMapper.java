package org.example.medflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.medflow.entity.Department;
import java.util.List;
import java.util.Map;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 获取所有部门的id和名称
     */
    @Select("SELECT dept_id, dept_name FROM department ORDER BY dept_id")
    List<Map<String, Object>> getAllDeptIdAndName();

    /**
     * 获取所有部门列表（完整信息）
     */
    @Select("SELECT * FROM department ORDER BY dept_id")
    List<Department> getAllDepartments();

    /**
     * 根据部门名称查询部门ID
     */
    @Select("SELECT dept_id FROM department WHERE dept_name = #{deptName}")
    Integer getDeptIdByName(@Param("deptName") String deptName);
}
