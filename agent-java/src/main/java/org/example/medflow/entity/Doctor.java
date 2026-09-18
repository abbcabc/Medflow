package org.example.medflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("doctor")
public class Doctor {

    @TableId(type = IdType.AUTO)
    private Integer doctorId;
    private Integer deptId;
    private String doctorName;
    private String password;
    private String title;
    private String specialty;
    private String introduction;
    private Integer yearsExperience;
    private Integer isAvailable;
    private String avatarUrl;
}