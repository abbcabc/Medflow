package org.example.medflow.dto;

import lombok.Data;

@Data
public class UpdateAvatarRequest {
    private Integer doctorId;
    private String avatarUrl;
}