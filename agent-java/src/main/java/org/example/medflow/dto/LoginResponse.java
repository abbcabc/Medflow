package org.example.medflow.dto;

import org.example.medflow.entity.Patient;

public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private long patientId;

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setPatientId(long patient) {
        this.patientId = patient;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getPatientId() {
        return patientId;
    }

    // getter setter
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
