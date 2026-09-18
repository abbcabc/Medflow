package org.example.medflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("department")
public class Department {

    @TableId(type = IdType.AUTO)
    private Integer deptId;      // 对应数据库 dept_id
    private String deptName;     // 对应数据库 dept_name
    private String deptDescription;     // 对应数据库 dept_desc
    private Integer parentDeptId;    // 对应数据库 parent_id
    private String contactPhone;
    private Integer status;      // 对应数据库 status
    private Date createTime;     // 对应数据库 create_time
}