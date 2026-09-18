package org.example.medflow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.medflow.dto.DoctorLoginRequest;
import org.example.medflow.dto.Result;
import org.example.medflow.dto.UpdateAvatarRequest;
import org.example.medflow.entity.Doctor;
import org.example.medflow.mapper.DoctorMapper;
import org.example.medflow.service.DoctorService;
import org.example.medflow.vo.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "doctor-controller")
@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DoctorMapper doctorMapper;

    // 医生登录接口
    @PostMapping("/login")
    public Result<?> login(@Validated @RequestBody DoctorLoginRequest request) {
        return doctorService.login(request);
    }

    // 获取医生个人信息接口
    @GetMapping("/profile")
    public Result<?> getProfile(@RequestParam("doctorId") Integer doctorId) {
        return doctorService.getProfile(doctorId);
    }

    // 修改密码接口
    @PutMapping("/changePassword/{doctorId}")
    public Result<?> changePassword(
            @PathVariable("doctorId") Integer doctorId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        return doctorService.changePassword(doctorId, oldPassword, newPassword);
    }
    /**
     * 上传医生头像
     */
    @PostMapping("/upload-avatar")
    public Result<?> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("doctorId") Integer doctorId) {
        try {
            if (file.isEmpty()) {
                return Result.error(400, "请选择要上传的文件");
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (!contentType.startsWith("image/")) {
                return Result.error(400, "只能上传图片文件");
            }

            // 检查文件大小（限制为2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return Result.error(400, "文件大小不能超过2MB");
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "avatar_" + doctorId + "_" + System.currentTimeMillis() + fileExtension;

            // 保存文件到服务器（这里保存到static/avatars目录）
            String uploadDir = "src/main/resources/static/avatars/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filePath = uploadDir + fileName;
            file.transferTo(new File(filePath));

            // 更新数据库中的头像URL
            String avatarUrl = "/avatars/" + fileName;
            int result = doctorMapper.updateAvatar(doctorId, avatarUrl);

            if (result > 0) {
                Map<String, String> response = new HashMap<>();
                response.put("avatarUrl", avatarUrl);
                return Result.success("头像上传成功", response);
            } else {
                return Result.error(500, "头像更新失败");
            }

        } catch (IOException e) {
            return Result.error(500, "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }

    /**
     * 更新医生头像（通过URL）
     */
    @PutMapping("/avatar")
    public Result<?> updateAvatar(@RequestBody UpdateAvatarRequest request) {
        try {
            int result = doctorMapper.updateAvatar(request.getDoctorId(), request.getAvatarUrl());
            if (result > 0) {
                return Result.success("头像更新成功", null);
            } else {
                return Result.error(500, "头像更新失败");
            }
        } catch (Exception e) {
            return Result.error(500, "更新失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public R<IPage<Doctor>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize,
            @RequestParam(value = "doctorName", required = false) String doctorName
    ) {
        IPage<Doctor> page = doctorService.getPage(pageNum, pageSize, doctorName);
        return R.success(page);
    }

    @GetMapping("/get/{id}")
    public R<Doctor> getById(@PathVariable(value = "id") Integer id) {
        return R.success(doctorService.getById(id));
    }

    @PostMapping("/add")
    public R<String> add(@RequestBody Doctor doctor) {
        doctorService.save(doctor);
        return R.success("添加成功");
    }

    @PostMapping("/update")
    public R<String> update(@RequestBody Doctor doctor) {
        doctorService.updateById(doctor);
        return R.success("修改成功");
    }

    @GetMapping("/delete/{id}")
    public R<String> delete(@PathVariable(value = "id") Integer id) {
        doctorService.removeById(id);
        return R.success("删除成功");
    }
}