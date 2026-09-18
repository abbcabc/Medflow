package org.example.medflow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.medflow.dto.Result;
import org.example.medflow.entity.Department;
import org.example.medflow.service.DepartmentService;
import org.example.medflow.vo.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dept")
@Tag(name = "科室管理")
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    @GetMapping("/list")
    public R<IPage<Department>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "deptName", required = false) String deptName
    ) {
        return R.success(departmentService.getPage(pageNum, pageSize, deptName));
    }
    // 分页查询
    @GetMapping("/page")
    @Operation(summary = "科室分页列表")
    public R<IPage<Department>> page(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "deptName", required = false) String deptName
    ) {
        return R.success(departmentService.getPage(pageNum, pageSize, deptName));
    }

    // 根据ID查询
    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID查询科室")
    public R<Department> getById(@PathVariable(value = "id") Integer id) {
        return R.success(departmentService.getById(id));
    }

    // 新增
    @PostMapping("/add")
    @Operation(summary = "新增科室")
    public R<String> add(@RequestBody Department department) {
        departmentService.addDept(department);
        return R.success("添加成功");
    }

    // 修改
    @PostMapping("/update")
    @Operation(summary = "修改科室")
    public R<String> update(@RequestBody Department department) {
        departmentService.updateDept(department);
        return R.success("修改成功");
    }

    // 删除
    @GetMapping("/delete/{id}")
    @Operation(summary = "删除科室")
    public R<String> delete(@PathVariable(value = "id") Integer id) {
        departmentService.deleteDept(id);
        return R.success("删除成功");
    }
}
