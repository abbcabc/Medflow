package org.example.medflow.dto;

import lombok.Data;

@Data
public class DoctorLoginResponse {
    private String token;
    private Integer doctorId;
    private String doctorName;
    private String title;
    private String specialty;
    private Integer deptId;
    private String avatarUrl;
}