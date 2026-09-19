package org.example.medflow.bean;

import lombok.Data;

@Data
public class ChatForm {
    private Long sessionId;// 会话ID（服务端校验归属后作为聊天记忆 memoryId）
    private String message;// 用户问题
}
