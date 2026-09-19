package org.example.medflow.store;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
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

@Component
public class MongoChatMemoryStore implements ChatMemoryStore {
    // 日志组件，替换System.out/err，便于生产环境日志管理
    private static final Logger log = LoggerFactory.getLogger(MongoChatMemoryStore.class);
    // 复用ObjectMapper实例，避免频繁创建销毁
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        // memoryId 即 chat_session 表的会话ID，患者归属关系只在 MySQL chat_session 表维护，
        // 本集合不冗余存储 patientId，避免两处状态不一致
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
}