package org.example.medflow.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DoctorLoginRequest {
    @NotNull(message = "部门ID不能为空")
    private Integer deptId;

    @NotBlank(message = "医生姓名不能为空")
    private String doctorName;

    @NotBlank(message = "密码不能为空")
    private String password;
}