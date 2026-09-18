package org.example.medflow.store;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.bson.Document;
import org.example.medflow.bean.ChatMessages;
import org.example.medflow.dto.ChatMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MongoChatMemoryStore implements ChatMemoryStore {
    // 日志组件，替换System.out/err，便于生产环境日志管理
    private static final Logger log = LoggerFactory.getLogger(MongoChatMemoryStore.class);
    // 复用ObjectMapper实例，避免频繁创建销毁
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    // 正则表达式匹配规则：匹配"key":值 格式的内容，值为数字/字符串
    private static final Pattern PATTERN_PATIENT_ID = Pattern.compile("\"patientId\"\\s*:\\s*(\\d+)");
    private static final Pattern PATTERN_SCHEDULE_ID = Pattern.compile("\"scheduleId\"\\s*:\\s*(\\d+)");
    private static final Pattern PATTERN_APPOINTMENT_NUMBER = Pattern.compile("\"appointmentNumber\"\\s*:\\s*(\\w+)");

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 根据记忆ID查询ChatMessage列表
     * @param memoryId 记忆唯一标识
     * @return 空列表（无数据/参数异常时）或ChatMessage列表
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // 空值校验，避免null导致Mongo查询异常
        if (memoryId == null) {
            log.warn("getMessages failed: memoryId is null");
            return new LinkedList<>();
        }
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        ChatMessages chatMessages = mongoTemplate.findOne(query, ChatMessages.class);
        if (chatMessages == null) {
            log.debug("getMessages: no chat messages found for memoryId={}", memoryId);
            return new LinkedList<>();
        }
        // 反序列化异常捕获，避免接口抛出未处理异常
        try {
            return ChatMessageDeserializer.messagesFromJson(chatMessages.getContent());
        } catch (Exception e) {
            log.error("getMessages failed: deserialize content error, memoryId={}", memoryId, e);
            return new LinkedList<>();
        }
    }

    /**
     * 根据记忆ID查询ChatMessageDTO列表（业务层DTO适配）
     * @param memoryId 记忆唯一标识
     * @return 空列表（无数据/参数异常/反序列化失败时）或ChatMessageDTO列表
     */
    public List<ChatMessageDTO> getChatMessageDTOs(Object memoryId) {
        if (memoryId == null) {
            log.warn("getChatMessageDTOs failed: memoryId is null");
            return new ArrayList<>();
        }
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        ChatMessages chatMessages = mongoTemplate.findOne(query, ChatMessages.class);
        if (chatMessages == null) {
            log.debug("getChatMessageDTOs: no chat messages found for memoryId={}", memoryId);
            return new ArrayList<>();
        }
        try {
            List<ChatMessageDTO> messages = OBJECT_MAPPER.readValue(
                    chatMessages.getContent(),
                    new TypeReference<List<ChatMessageDTO>>() {}
            );
            log.info("getChatMessageDTOs success: memoryId={}, message count={}", memoryId, messages.size());
            return messages;
        } catch (Exception e) {
            log.error("getChatMessageDTOs failed: deserialize content error, memoryId={}", memoryId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 更新/新增聊天消息（存在则更新，不存在则新增）
     * @param memoryId 记忆唯一标识
     * @param messages 待存储的ChatMessage列表
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        if (memoryId == null) {
            log.warn("updateMessages failed: memoryId is null");
            return;
        }
        if (messages == null) {
            log.warn("updateMessages failed: messages list is null, memoryId={}", memoryId);
            return;
        }
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        Update update = new Update();
        try {
            String jsonContent = ChatMessageSerializer.messagesToJson(messages);
            update.set("content", jsonContent);
        } catch (Exception e) {
            log.error("updateMessages failed: serialize messages error, memoryId={}", memoryId, e);
            return;
        }
        // 将 patientId 落为文档独立字段（本项目登录流程保证 memoryId 即 patientId），便于后续按字段读取
        Integer patientId = resolvePatientIdFromMemoryId(memoryId);
        if (patientId != null) {
            update.set("patientId", patientId);
        }
        // 根据query条件更新文档；无匹配文档则新增
        mongoTemplate.upsert(query, update, ChatMessages.class);
        log.debug("updateMessages success: memoryId={}, message count={}", memoryId, messages.size());
    }

    /**
     * 根据记忆ID删除聊天消息
     * @param memoryId 记忆唯一标识
     */
    @Override
    public void deleteMessages(Object memoryId) {
        if (memoryId == null) {
            log.warn("deleteMessages failed: memoryId is null");
            return;
        }
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        mongoTemplate.remove(query, ChatMessages.class);
        log.debug("deleteMessages success: memoryId={}", memoryId);
    }

    /**
     * 原子追加消息到指定memoryId（MongoDB $push 原子操作，避免并发覆盖）
     * @param memoryId 对话内存ID
     * @param message 要追加的消息
     */
    public void appendMessage(long memoryId, ChatMessage message) {
        // 空值校验：避免无效的Mongo操作
        if (message == null) {
            log.warn("appendMessage failed: message is null, memoryId={}", memoryId);
            return;
        }
        // 1. 构建查询条件：匹配memoryId
        Query query = Query.query(Criteria.where("memoryId").is(memoryId));
        // 2. 构建更新操作：原子追加到messages数组末尾
        Update update = new Update().push("messages", convertToDocument(message));
        // 3. 执行更新（upsert=true：不存在则自动创建文档）
        // 注意：chat_memory为集合名，需根据实际业务调整；若需复用ChatMessages实体，可替换为ChatMessages.class
        mongoTemplate.upsert(query, update, "chat_messages");
//        log.debug("appendMessage success: memoryId={}, message type={}", memoryId, message.type().name());
    }

    /**
     * 将ChatMessage转换为MongoDB可存储的Document
     * @param message 聊天消息
     * @return MongoDB Document
     */
    private Document convertToDocument(ChatMessage message) {
        Document doc = new Document();
        doc.put("text", message); // 消息内容
//        doc.put("type", message.type()); // 消息类型（SYSTEM/USER/AI等）
        doc.put("timestamp", System.currentTimeMillis()); // 追加时间戳
        return doc;
    }

    // ======================== 新增方法：提取patientId、scheduleId、appointmentNumber ========================
    /**
     * 从 memoryId 派生 patientId：本项目登录流程（PatientServiceImpl）保证 memoryId 即 patientId
     * @param memoryId 对话内存ID
     * @return patientId；memoryId 非数字时返回 null
     */
    private Integer resolvePatientIdFromMemoryId(Object memoryId) {
        if (memoryId instanceof Number) {
            return ((Number) memoryId).intValue();
        }
        if (memoryId instanceof String && !((String) memoryId).isBlank()) {
            try {
                return Integer.parseInt(((String) memoryId).trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 根据memoryId提取聊天记录中的scheduleId
     * @param memoryId 对话内存ID
     * @return 匹配到的scheduleId（Long），无匹配/解析失败返回null
     */
    public Integer getScheduleId(Long memoryId) {
        String value = extractValueByPattern(memoryId, PATTERN_SCHEDULE_ID);
        if (value == null) {
            log.debug("getScheduleId: no scheduleId found for memoryId={}", memoryId);
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.error("getScheduleId failed: parse value error, memoryId={}, value={}", memoryId, value, e);
            return null;
        }
    }

    /**
     * 根据memoryId提取聊天记录中的appointmentNumber
     * @param memoryId 对话内存ID
     * @return 匹配到的appointmentNumber（String），无匹配/解析失败返回null
     */
    public String getAppointmentNumber(Long memoryId) {
        String value = extractValueByPattern(memoryId, PATTERN_APPOINTMENT_NUMBER);
        if (value == null) {
            log.debug("getAppointmentNumber: no appointmentNumber found for memoryId={}", memoryId);
        }
        return value;
    }

    /**
     * 通用提取方法：根据memoryId和正则表达式提取SystemMessage中的目标值
     * @param memoryId 对话内存ID
     * @param pattern 正则匹配规则
     * @return 匹配到的字符串值，无匹配/异常返回null
     */
    private String extractValueByPattern(Long memoryId, Pattern pattern) {
        // 空值校验
        if (memoryId == null || pattern == null) {
            log.warn("extractValueByPattern failed: memoryId or pattern is null");
            return null;
        }
        // 查询指定memoryId的所有消息
        Query query = Query.query(Criteria.where("memoryId").is(memoryId));
        Document doc = mongoTemplate.findOne(query, Document.class, "chat_messages");
        if (doc == null) {
            log.debug("extractValueByPattern: no document found for memoryId={}", memoryId);
            return null;
        }
        // 解析messages数组
        List<Document> messages = doc.getList("messages", Document.class);
        if (messages == null || messages.isEmpty()) {
            log.debug("extractValueByPattern: no messages found for memoryId={}", memoryId);
            return null;
        }
        // 遍历消息，匹配目标值
        for (Document messageDoc : messages) {
            Document textDoc = messageDoc.get("text", Document.class);
            if (textDoc == null) {
                continue;
            }
            // 只解析SystemMessage类型的消息
            String msgClass = textDoc.getString("_class");
            if (msgClass == null || !msgClass.equals("dev.langchain4j.data.message.SystemMessage")) {
                continue;
            }
            String msgText = textDoc.getString("text");
            if (msgText == null || msgText.isBlank()) {
                continue;
            }
            // 正则匹配
            Matcher matcher = pattern.matcher(msgText);
            if (matcher.find()) {
                log.debug("extractValueByPattern success: memoryId={}, match value={}", memoryId, matcher.group(1));
                return matcher.group(1);
            }
        }
        return null;
    }
}