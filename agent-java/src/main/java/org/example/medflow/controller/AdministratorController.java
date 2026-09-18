package org.example.medflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.medflow.entity.Administrator;
import org.example.medflow.service.AdministratorService;
import org.example.medflow.util.JwtUtil;
import org.example.medflow.vo.R;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin")
@Tag(name = "管理员模块", description = "管理员登录、信息查询、增删改查")
public class AdministratorController {

    @Resource
    private AdministratorService administratorService;
    @Resource
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "用户名+密码登录，返回JWT令牌")
    public R<String> login(
            // 去掉 @RequestParam，用实体类接收 JSON
            @RequestBody Administrator administrator
    ) {
        return administratorService.login(administrator.getUsername(), administrator.getPassword());
    }

    @GetMapping("/info")
    @Operation(summary = "获取管理员信息", description = "从JWT令牌中解析adminId，返回管理员信息（隐藏密码）")
    public R<Administrator> getAdminInfo(HttpServletRequest request) {
        // 从请求头获取Token
        String token = request.getHeader("token");
        if (token == null || !jwtUtil.validateToken(token)) {
            return R.result(401, "令牌无效或过期", null);
        }
        Integer adminId = jwtUtil.getPatientIdFromToken(token); // 注：你原JwtUtil方法名是getPatientIdFromToken，可改名为getUserIdFromToken
        return administratorService.getAdminInfo(adminId);
    }
}