package org.example.medflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import org.example.medflow.entity.Department;
import org.example.medflow.mapper.DepartmentMapper;
import org.example.medflow.service.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Override
    public List<Map<String, Object>> getAllDeptIdAndName() {
        return baseMapper.getAllDeptIdAndName();
    }

    @Override
    public List<Department> getAllDepartments() {
        return baseMapper.getAllDepartments();
    }

    @Override
    public Integer getDeptIdByName(String deptName) {
        return baseMapper.getDeptIdByName(deptName);
    }

    @Override
    public IPage<Department> getPage(Integer pageNum, Integer pageSize, String deptName) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(deptName)) {
            wrapper.like(Department::getDeptName, deptName);
        }
        wrapper.orderByDesc(Department::getDeptId);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public boolean addDept(Department department) {
        department.setCreateTime(new Date());
        department.setStatus(1);
        return save(department);
    }

    @Override
    public boolean updateDept(Department department) {
        return updateById(department);
    }

    @Override
    public boolean deleteDept(Integer deptId) {
        return removeById(deptId);
    }
}
