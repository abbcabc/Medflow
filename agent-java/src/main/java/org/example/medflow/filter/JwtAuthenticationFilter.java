package org.example.medflow.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.medflow.context.ThreadLocalContext;
import org.example.medflow.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ROLE_PATIENT = "patient";

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        try {
            if (token != null && jwtUtil.validateToken(token)) {
                Integer patientId = jwtUtil.getPatientIdFromToken(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(patientId, null, new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 仅当 JWT 为患者身份时，将 patientId 写入线程上下文（工具层唯一信任来源）
                if (patientId != null && ROLE_PATIENT.equals(jwtUtil.getRoleFromToken(token))) {
                    ThreadLocalContext.setPatientId(patientId);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            // 请求结束必须清理线程上下文，避免线程复用导致串号
            ThreadLocalContext.removePatientId();
            SecurityContextHolder.clearContext();
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
//        System.out.println("bearerToken:" + bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
