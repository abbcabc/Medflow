package org.example.medflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import com.alibaba.fastjson.JSONObject;

@Data
@TableName("administrator")
public class Administrator {
    @TableId(type = IdType.AUTO)
    private Integer adminId; // 管理员ID
    private String username; // 用户名
    private String password; // 密码（BCrypt加密）
    private String realName; // 真实姓名
    private String role; // 角色：super_admin/dept_admin/operator
    private Integer deptId; // 所属科室ID
    private JSONObject permissions; // 权限（JSON）
    private LocalDateTime createdAt; // 创建时间
}