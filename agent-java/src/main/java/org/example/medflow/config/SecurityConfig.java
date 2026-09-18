package org.example.medflow.config;

import jakarta.servlet.http.HttpServletResponse;
import org.example.medflow.filter.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF
                .csrf(csrf -> csrf.disable())

                // 配置CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 会话管理设置为无状态
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 未登录/令牌无效时返回 401 JSON，而非重定向
                .exceptionHandling(exception -> exception.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"未登录或令牌无效，请先登录\"}");
                }))
                // 接口鉴权：白名单放行，其余全部需要认证
                .authorizeHttpRequests(authz -> authz
                        // OPTIONS 预检请求必须放行，否则前端跨域请求会被拦截
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 登录/注册接口 + 公开科室列表（医生登录页下拉用）
                        .requestMatchers("/api/patient/login", "/api/patient/register",
                                "/api/doctor/login", "/admin/login", "/dept/list").permitAll()
                        // Knife4j / 接口文档
                        .requestMatchers("/doc.html", "/webjars/**", "/v3/api-docs/**",
                                "/swagger-resources/**").permitAll()
                        // 静态资源（头像等）
                        .requestMatchers("/avatars/**", "/favicon.ico", "/error").permitAll()
                        // 其余接口一律需要认证
                        .anyRequest().authenticated())
                // 关键：把 JWT 过滤器挂进 Security 过滤链（否则它作为普通 Servlet 过滤器
                // 运行在 Security 链之后，鉴权时 SecurityContext 永远为空，全部 401）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 禁用 Boot 对该过滤器的自动注册，避免在 Security 链外重复执行
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    // CORS配置
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // 生产环境请指定具体域名
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}