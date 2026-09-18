package org.example.medflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.medflow.entity.Department;
import java.util.List;
import java.util.Map;

public interface DepartmentService extends IService<Department> {

    /**
     * 获取所有部门的id和名称列表
     */
    List<Map<String, Object>> getAllDeptIdAndName();

    /**
     * 获取所有部门列表
     */
    List<Department> getAllDepartments();

    /**
     * 根据部门名称获取部门ID
     */
    Integer getDeptIdByName(String deptName);

    IPage<Department> getPage(Integer pageNum, Integer pageSize, String deptName);
    boolean addDept(Department department);
    boolean updateDept(Department department);
    boolean deleteDept(Integer deptId);
}