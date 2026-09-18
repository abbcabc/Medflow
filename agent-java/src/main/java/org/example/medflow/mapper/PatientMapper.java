package org.example.medflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.medflow.entity.Patient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PatientMapper  extends BaseMapper<Patient>{

    // 根据手机号查询患者
    Patient findByPhone(@Param("phone") String phone);

    // 根据患者ID查询患者
    Patient findByPatientId(@Param("patientId") Integer patientId);

    // 插入患者
    int insert(Patient patient);

    // 更新患者信息
    int update(Patient patient);

    // 检查手机号是否存在
    int countByPhone(@Param("phone") String phone);

    // 更新密码
    int updatePassword(Patient patient);
}