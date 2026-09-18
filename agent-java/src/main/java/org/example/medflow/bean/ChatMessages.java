package org.example.medflow.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("chat_messages")
public class ChatMessages {

    @Id
    private ObjectId id;

    private Object memoryId; // 添加这个字段

    private Object messageId;

    private String content; //存储当前聊天记录列表的json字符串
}