package org.example.medflow.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.medflow.assistant.Agent;
import org.example.medflow.bean.ChatForm;
import org.example.medflow.dto.ChatMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import org.example.medflow.store.MongoChatMemoryStore;
import org.example.medflow.context.ThreadLocalContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Tag(name = "chat-controller")
@RestController
@RequestMapping("/agent")
public class Controller {

    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    // 当前日期格式：如 2026年09月18日 星期五
    private static final DateTimeFormatter CURRENT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEEE", Locale.CHINA);

    @Autowired
    private Agent agent;
    @Operation(summary = "对话")
    @PostMapping(value = "/chat", produces = "text/stream;charset=utf-8")
    public Flux<String> chat(@RequestBody ChatForm chatForm)  {
        long startTime = System.currentTimeMillis();
        String message = chatForm.getMessage();
        // 会话身份以 JWT 登录态为准：memoryId 由服务端派生，不信任前端传入
        Integer patientId = ThreadLocalContext.getPatientId();
        if (patientId == null) {
            log.warn("/agent/chat 未登录请求被拒绝: 前端memoryId={}", chatForm.getMemoryId());
            return Flux.just("请先登录后再使用小智助手");
        }
        log.info("/agent/chat 收到请求: patientId={}, 消息长度={}",
                patientId, message == null ? 0 : message.length());
        return agent.chat(patientId.longValue(), message, LocalDate.now().format(CURRENT_DATE_FORMATTER))
                .doFinally(signal -> log.info("/agent/chat 流结束: patientId={}, signal={}, 耗时={}ms",
                        patientId, signal, System.currentTimeMillis() - startTime));
    }
    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;
    @GetMapping("/chat/history")
    public List<ChatMessageDTO> getChatHistory() {
        // 会话归属以 JWT 登录态为准，不信任前端传入的 userId（防越权读取他人聊天记录）
        Integer patientId = ThreadLocalContext.getPatientId();
        if (patientId == null) {
            log.warn("/agent/chat/history 未登录请求被拒绝");
            return List.of();
        }
        try {
            List<ChatMessageDTO> chatMessages = mongoChatMemoryStore.getChatMessageDTOs(patientId.longValue());
            if (chatMessages.isEmpty()) {
                return null;
            }
            // 直接返回 ChatMessage 列表
            return chatMessages;
        } catch (Exception e) {
            System.err.println("获取聊天历史记录失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
