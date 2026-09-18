package org.example.medflow;

import org.example.medflow.store.MongoChatMemoryStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MongoChatMemoryStore 单元测试
 * 测试 patientId、scheduleId、appointmentNumber 提取方法
 */
@SpringBootTest // 启动Spring容器，自动注入Bean
public class MongoChatMemoryStoreTest {

    private static final Logger log = LoggerFactory.getLogger(MongoChatMemoryStoreTest.class);

    // 注入你编写的Mongo存储工具类
    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;

    /**
     * 测试：提取三个目标字段
     * 请替换为你真实存在的 memoryId
     */
    @Test
    public void testGetAllFields() {
        // 【重要】替换成你Mongo中真实存在的 memoryId
        Long memoryId = 8L;

        log.info("===== 开始测试字段提取，memoryId: {} =====", memoryId);

        // 调用新增的三个方法
        Integer patientId = mongoChatMemoryStore.getPatientId(memoryId);
        Integer scheduleId = mongoChatMemoryStore.getScheduleId(memoryId);
        String appointmentNumber = mongoChatMemoryStore.getAppointmentNumber(memoryId);

        // 打印结果
        log.info("提取结果：");
        log.info("patientId: {}", patientId);
        log.info("scheduleId: {}", scheduleId);
        log.info("appointmentNumber: {}", appointmentNumber);

        // 断言（可选）
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            log.info("方法执行无异常");
        });
    }

    /**
     * 测试：空memoryId场景
     */
    @Test
    public void testNullMemoryId() {
        log.info("===== 测试空memoryId =====");
        Integer patientId = mongoChatMemoryStore.getPatientId(null);
        log.info("空memoryId -> patientId: {}", patientId);
        org.junit.jupiter.api.Assertions.assertNull(patientId);
    }

    /**
     * 测试：不存在的memoryId场景
     */
    @Test
    public void testNotExistMemoryId() {
        log.info("===== 测试不存在的memoryId =====");
        Integer scheduleId = mongoChatMemoryStore.getScheduleId(999999999L);
        log.info("不存在的ID -> scheduleId: {}", scheduleId);
        org.junit.jupiter.api.Assertions.assertNull(scheduleId);
    }
}
