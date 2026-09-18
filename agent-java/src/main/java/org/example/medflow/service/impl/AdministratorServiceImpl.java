package org.example.medflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.medflow.entity.Administrator;
import org.example.medflow.mapper.AdministratorMapper;
import org.example.medflow.service.AdministratorService;
import org.example.medflow.util.JwtUtil;
import org.example.medflow.util.PasswordEncoder;
import org.example.medflow.vo.R;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Service
public class AdministratorServiceImpl extends ServiceImpl<AdministratorMapper, Administrator>
        implements AdministratorService {

    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private JwtUtil jwtUtil;

    @Override
    public R<String> login(String username, String password) {
        // 根据用户名查询管理员
        LambdaQueryWrapper<Administrator> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Administrator::getUsername, username);
        Administrator admin = this.getOne(queryWrapper);
        if (admin == null) {
            return R.error("用户名不存在");
        }
        // 验证密码
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            return R.error("密码错误");
        }
        // 生成JWT令牌
        String token = jwtUtil.generateToken(admin.getAdminId());
        return R.success(token);
    }

    @Override
    public R<Administrator> getAdminInfo(Integer adminId) {
        Administrator admin = this.getById(adminId);
        if (admin == null) {
            return R.error("管理员不存在");
        }
        // 隐藏密码
        admin.setPassword(null);
        return R.success(admin);
    }
}