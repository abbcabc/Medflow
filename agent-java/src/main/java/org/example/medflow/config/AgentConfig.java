package org.example.medflow.config;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import org.example.medflow.rag.DashScopeRerankScoringModel;
import org.example.medflow.store.MongoChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;

    /**
     * 辅助任务专用低配模型（会话标题生成等低频小任务），
     * 与主对话模型分级路由：简单任务用便宜模型，控制成本
     */
    @Bean(name = "titleChatModel")
    public QwenChatModel titleChatModel(@Value("${DASH_SCOPE_API_KEY}") String apiKey) {
        return QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName("qwen-turbo")
                .build();
    }

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

    /**
     * 候选池检索器：为重排提供候选项。
     * 与最终 maxResults(4) 的区别：这里放宽到 20 条、minScore 0.3——
     * 宁可多召回低分候选交给重排精挑，也不在向量相似度这一层就砍掉相关内容
     * （向量相似度只看字面语义重合，重排模型对"意图相关"判断更准）
     */
    @Bean
    ContentRetriever contentRetrieverAgentPincone() {

        // 创建一个 EmbeddingStoreContentRetriever 对象，用于从嵌入存储中检索内容
        return EmbeddingStoreContentRetriever
                .builder()
                // 设置用于生成嵌入向量的嵌入模型
                .embeddingModel(embeddingModel)
                // 指定要使用的嵌入存储
                .embeddingStore(embeddingStore)
                // 设置最大检索结果数量，作为重排的候选池
                .maxResults(20)
                // 设置最小得分阈值，放宽以保留更多候选
                .minScore(0.3)
                // 构建最终的 EmbeddingStoreContentRetriever 实例
                .build();
    }

    /**
     * 重排模型（gte-rerank-v2）：对候选池按"与问题的意图相关度"重新打分排序，
     * 用一次便宜调用（约百毫秒）换掉向量相似度带来的噪声候选
     */
    @Bean
    ScoringModel scoringModel(@Value("${DASH_SCOPE_API_KEY}") String apiKey) {
        return new DashScopeRerankScoringModel(apiKey, "gte-rerank-v2");
    }

    /**
     * 高级 RAG 检索增强器：查询改写 → 候选检索 → 重排。
     * CompressingQueryTransformer 用低配模型（qwen-turbo）结合聊天记忆把多轮指代查询
     * （如"他呢？"）改写成自包含的独立查询（如"徐作军医生擅长什么？"）；
     * ReRankingContentAggregator 用 gte-rerank 对候选池重排出最终 top-4——
     * 多轮导诊场景下检索命中率的关键一环。
     */
    @Bean
    RetrievalAugmentor retrievalAugmentor(@Qualifier("titleChatModel") QwenChatModel titleChatModel,
                                           ScoringModel scoringModel,
                                           ContentRetriever contentRetrieverAgentPincone) {
        return DefaultRetrievalAugmentor.builder()
                .queryTransformer(new CompressingQueryTransformer(titleChatModel))
                .contentRetriever(contentRetrieverAgentPincone)
                .contentAggregator(new ReRankingContentAggregator(
                        scoringModel, ReRankingContentAggregator.DEFAULT_QUERY_SELECTOR, null, 4))
                .build();
    }
}
