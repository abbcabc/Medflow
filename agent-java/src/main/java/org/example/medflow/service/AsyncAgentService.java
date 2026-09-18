package org.example.medflow.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import org.example.medflow.store.MongoChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AsyncAgentService {

    // 替换为SLF4J日志（生产环境不建议用System.out/err）
    private static final Logger log = LoggerFactory.getLogger(AsyncAgentService.class);
    // 重试次数配置
    private static final int MAX_RETRY_TIMES = 2;
    // 重试基础等待时间（毫秒）
    private static final long RETRY_BASE_DELAY = 100;

    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;

    /**
     * 异步添加系统消息（原子追加，高并发安全）
     * @param memoryId 对话内存ID
     * @param systemMessage 系统消息内容
     */
    @Async
    public void addSystemMessageAsync(long memoryId, String systemMessage) {
        // 构建系统消息对象
        ChatMessage systemChatMessage = new SystemMessage(systemMessage);

        // 带重试的原子追加逻辑
        boolean appendSuccess = appendMessageWithRetry(memoryId, systemChatMessage);

        if (appendSuccess) {
            log.info("系统消息添加成功，memoryId: {}", memoryId);
        } else {
            log.error("系统消息添加失败（已重试{}次），memoryId: {}", MAX_RETRY_TIMES, memoryId);
            // 可扩展：接入监控告警（如钉钉/短信/普罗米修斯）
        }
    }

    /**
     * 带重试机制的原子追加消息
     * @param memoryId 对话内存ID
     * @param message 要追加的消息
     * @return 是否追加成功
     */
    private boolean appendMessageWithRetry(long memoryId, ChatMessage message) {
        for (int retryCount = 0; retryCount <= MAX_RETRY_TIMES; retryCount++) {
            try {
                // 调用原子追加方法（核心：避免先查后改的并发问题）
                mongoChatMemoryStore.appendMessage(memoryId, message);
                return true;
            } catch (Exception e) {
                log.warn("第{}次追加消息失败，memoryId: {}，异常：{}",
                        retryCount + 1, memoryId, e.getMessage());

                // 最后一次重试失败则返回false
                if (retryCount == MAX_RETRY_TIMES) {
                    log.error("追加消息最终失败，memoryId: {}，异常栈：", memoryId, e);
                    return false;
                }

                // 退避重试（指数级等待，避免高频重试）
                try {
                    long delay = RETRY_BASE_DELAY * (long) Math.pow(2, retryCount);
                    TimeUnit.MILLISECONDS.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("重试等待被中断，memoryId: {}", memoryId, ie);
                    return false;
                }
            }
        }
        return false;
    }
}