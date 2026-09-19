package org.example.medflow.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.medflow.assistant.Agent;
import org.example.medflow.bean.ChatForm;
import org.example.medflow.dto.ChatMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import org.example.medflow.store.MongoChatMemoryStore;
import org.example.medflow.context.ThreadLocalContext;
import org.example.medflow.util.MedicalSafetyGuard;
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
    @Autowired
    private MedicalSafetyGuard medicalSafetyGuard;
    @Autowired
    private org.example.medflow.service.ChatSessionService chatSessionService;

    @Operation(summary = "对话")
    // 注意：必须是标准 SSE 媒体类型 text/event-stream。此前自造的 "text/stream" 不被 Spring MVC 的
    // ReactiveTypeHandler 支持：元素虽能写出，但 chunked 终结符（0\r\n\r\n）永远不会发送，
    // 导致客户端流式读取永远等不到 done（前端"停止"按钮无法复位）
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatForm chatForm)  {
        long startTime = System.currentTimeMillis();
        String message = chatForm.getMessage();
        // 会话身份以 JWT 登录态为准：患者身份由服务端派生，不信任前端传入
        Integer patientId = ThreadLocalContext.getPatientId();
        if (patientId == null) {
            log.warn("/agent/chat 未登录请求被拒绝: sessionId={}", chatForm.getSessionId());
            return Flux.just("请先登录后再使用小智助手");
        }
        // 会话归属校验：sessionId 必须属于当前登录患者（防水平越权读写他人会话）
        Long sessionId = chatForm.getSessionId();
        if (!chatSessionService.isOwnedBy(sessionId, patientId)) {
            log.warn("/agent/chat 会话无效或无权访问: patientId={}, sessionId={}", patientId, sessionId);
            return Flux.just("会话不存在或无权访问，请重新选择或新建会话");
        }
        // 维护会话活跃时间，并在首条消息时生成标题
        chatSessionService.touchAndMaybeTitle(sessionId, patientId, message);
        log.info("/agent/chat 收到请求: patientId={}, sessionId={}, 消息长度={}",
                patientId, sessionId, message == null ? 0 : message.length());
        // 输入安全护栏：疑似急危重症信号时注入安全指令（确定性规则先于大模型执行，提示词规则兜底模型行为）
        if (medicalSafetyGuard.isEmergency(message)) {
            log.warn("/agent/chat 检测到疑似急症信号: patientId={}, sessionId={}", patientId, sessionId);
            message = medicalSafetyGuard.wrapWithEmergencyDirective(message);
        }
        // memoryId = sessionId：每个会话独立记忆
        return agent.chat(sessionId, message, LocalDate.now().format(CURRENT_DATE_FORMATTER))
                .doFinally(signal -> log.info("/agent/chat 流结束: patientId={}, sessionId={}, signal={}, 耗时={}ms",
                        patientId, sessionId, signal, System.currentTimeMillis() - startTime));
    }
    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;
    @GetMapping("/chat/history")
    public List<ChatMessageDTO> getChatHistory(@RequestParam("sessionId") Long sessionId) {
        // 会话归属以 JWT 登录态为准，不信任前端传入的 userId（防越权读取他人聊天记录）
        Integer patientId = ThreadLocalContext.getPatientId();
        if (patientId == null) {
            log.warn("/agent/chat/history 未登录请求被拒绝");
            return List.of();
        }
        if (!chatSessionService.isOwnedBy(sessionId, patientId)) {
            log.warn("/agent/chat/history 会话无效或无权访问: patientId={}, sessionId={}", patientId, sessionId);
            return List.of();
        }
        try {
            List<ChatMessageDTO> chatMessages = mongoChatMemoryStore.getChatMessageDTOs(sessionId);
            return chatMessages;
        } catch (Exception e) {
            log.error("获取聊天历史记录失败: sessionId={}", sessionId, e);
            return List.of();
        }
    }
}
