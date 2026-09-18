package org.example.medflow;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import org.example.medflow.service.AsyncAgentService;
import org.example.medflow.store.MongoChatMemoryStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.CountDownLatch;
/**
 * AsyncAgentService 测试类
 * 测试系统消息存入和读取逻辑
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // 测试后清理上下文，避免数据污染
public class AsyncAgentServiceTest {

    // 注入待测试的服务
    @Autowired
    private AsyncAgentService asyncAgentService;

    // 注入存储层（用于读取验证）
    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;

    /**
     * 核心测试：存入chat_memory + 从chat_memory读取
     */
    @Test
    void testAddAndRetrieveSystemMessage_FromChatMemory() throws InterruptedException {
        // 1. 测试数据
        long memoryId = 9L;
        Long patientId = 9527L;
        String expectedMessage = String.format(
                "当前患者ID是为%s,在预约服务中需要用到这个id",
                patientId
        );

        // 2. 异步存入消息（用CountDownLatch精准等待异步执行）
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            asyncAgentService.addSystemMessageAsync(memoryId, expectedMessage);
            latch.countDown();
        }).start();
        // 最多等待1秒，确保异步方法执行完成
        boolean isAsyncDone = latch.await(1, TimeUnit.SECONDS);
        assertTrue(isAsyncDone, "异步存储消息超时");

        // 3. 从chat_memory集合读取消息（核心修正：用统一的读取方法）
        List<ChatMessage> messages = mongoChatMemoryStore.getMessages(memoryId);

        // 4. 断言验证
        assertNotNull(messages, "从chat_memory读取的消息列表不应为空");
        assertFalse(messages.isEmpty(), "chat_memory中未找到对应memoryId的消息");

        // 验证系统消息内容
        SystemMessage systemMsg = messages.stream()
                .filter(msg -> msg instanceof SystemMessage)
                .map(msg -> (SystemMessage) msg)
                .findFirst()
                .orElseThrow(() -> new AssertionError("chat_memory中未找到系统消息"));

        assertEquals(expectedMessage, systemMsg.text(),
                "chat_memory中存储的系统消息内容不匹配");

        // 调试打印
        System.out.println("从chat_memory读取到的消息："+systemMsg.text());
    }

    /**
     * 反向验证：chat_messages集合中无该数据（可选，确认数据未存错位置）
     */
//    @Test
//    void testChatMessagesCollection_NoData() {
//        long memoryId = 10001L;
//        // 尝试从错误的集合（chat_messages）读取
//        Query query = Query.query(Criteria.where("memoryId").is(memoryId));
//        MongoChatMemoryStore.ChatMemoryDoc doc = mongoTemplate.findOne(
//                query, MongoChatMemoryStore.ChatMemoryDoc.class, "chat_messages");
//        assertNull(doc, "chat_messages集合中不应存在该数据");
//    }
//
//    // 日志对象（替换System.out，统一日志规范）
//    private static final org.slf4j.Logger log =
//            org.slf4j.LoggerFactory.getLogger(AsyncAgentServiceTest.class);
}
