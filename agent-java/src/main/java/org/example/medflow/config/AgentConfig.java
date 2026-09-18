package org.example.medflow.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import org.example.medflow.store.MongoChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;

    @Bean
    ChatMemoryProvider chatMemoryProviderAgent() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(40)
                .chatMemoryStore(mongoChatMemoryStore)
                .build();
    }

    @Autowired
    private EmbeddingStore embeddingStore;
    @Autowired
    private EmbeddingModel embeddingModel;
    @Bean
    ContentRetriever contentRetrieverAgentPincone() {

        // 创建一个 EmbeddingStoreContentRetriever 对象，用于从嵌入存储中检索内容
        return EmbeddingStoreContentRetriever
                .builder()
                // 设置用于生成嵌入向量的嵌入模型
                .embeddingModel(embeddingModel)
                // 指定要使用的嵌入存储
                .embeddingStore(embeddingStore)
                // 设置最大检索结果数量，这里表示最多返回 4 条匹配结果
                .maxResults(4)
                // 设置最小得分阈值，只有得分大于等于 0.55 的结果才会被返回
                .minScore(0.55)
                // 构建最终的 EmbeddingStoreContentRetriever 实例
                .build();
    }
}
