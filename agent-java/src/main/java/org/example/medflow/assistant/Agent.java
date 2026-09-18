package org.example.medflow.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(
        wiringMode = EXPLICIT,
//        chatModel = "qwenChatModel",
        streamingChatModel = "qwenStreamingChatModel",
        tools = "appointmentTool", //tools配置
        chatMemoryProvider = "chatMemoryProviderAgent",
//        contentRetriever = "contentRetrieverAgent"
        contentRetriever = "contentRetrieverAgentPincone"
)


public interface Agent {
    @SystemMessage(fromResource = "agent-prompt-template.txt")
//    String chat(@MemoryId Long memoryId, @UserMessage String userMessage);
    // current_date 由调用方传入，用于替换系统提示词中的 {{current_date}} 占位符
    Flux<String> chat(@MemoryId Long memoryId, @UserMessage String userMessage, @V("current_date") String currentDate); //流式输出
}
