package org.example.medflow;

import org.example.medflow.entity.Doctor;
import org.example.medflow.mapper.DoctorMapper;
import org.example.medflow.service.DoctorScheduleService;
import org.example.medflow.service.DoctorService;
import org.example.medflow.util.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class DoctorTest {
    @Autowired
    private DoctorService doctorService;

    @Autowired
    DoctorScheduleService doctorScheduleService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DoctorMapper doctorMapper;
    @Test
    public void getDeptId(){
        System.out.println(doctorService.getDoctorDepartment(1));
    }


    @Test
    public void a(){
        System.out.println(doctorScheduleService.decreaseBookedSlots(1));
    }

    // 可以创建一个临时的方法来批量设置密码
    @Test
    public void batchSetDefaultPassword() {
        List<Doctor> doctors = doctorService.findAllAvailableDoctors();
        String defaultEncodedPassword = passwordEncoder.encode("doctor123");

        for (Doctor doctor : doctors) {
            doctorMapper.updatePassword(doctor.getDoctorId(), defaultEncodedPassword);
        }
    }
}
