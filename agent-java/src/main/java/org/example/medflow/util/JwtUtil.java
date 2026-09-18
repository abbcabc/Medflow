package org.example.medflow.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "MedicalFlowPatientAuthJWTSecretKey2024SecurityForHospitalSystem";
    private final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000; // 7天

    public String generateToken(Integer patientId) {
        return Jwts.builder()
                .claim("patientId", patientId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    /**
     * 签发带角色标识的 JWT（claims 中包含 userId 与 role）
     */
    public String generateToken(Integer userId, String role) {
        return Jwts.builder()
                .claim("patientId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    /**
     * 从 token 中解析角色标识（无 role claim 时返回 null）
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getPatientIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("patientId", Integer.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
