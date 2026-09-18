package org.example.medflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.medflow.entity.Administrator;
import org.example.medflow.vo.R;

public interface AdministratorService extends IService<Administrator> {
    // 管理员登录
    R<String> login(String username, String password);
    // 获取管理员信息
    R<Administrator> getAdminInfo(Integer adminId);
}