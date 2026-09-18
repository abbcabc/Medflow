package org.example.medflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessageDTO {
    private String type;
    private String text;
    private List<ContentItem> contents;
    private String name;
    private List<ToolExecutionRequest> toolExecutionRequests;
    private List<ToolExecutionResult> toolExecutionResults;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentItem {
        private String text;
        private String type;
        private Map<String, Object> imageUrl;
        private Map<String, Object> document;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ToolExecutionRequest {
        private String id;
        private String name;
        private String arguments; // 存储为字符串

        private static final ObjectMapper mapper = new ObjectMapper();

        // 将 Map 转换为 JSON 字符串
        public void setArgumentsMap(Map<String, Object> argumentsMap) {
            try {
                this.arguments = mapper.writeValueAsString(argumentsMap);
            } catch (JsonProcessingException e) {
                this.arguments = "{}";
            }
        }

        // 将 JSON 字符串转换为 Map
        public Map<String, Object> getArgumentsMap() {
            try {
                if (arguments != null && !arguments.trim().isEmpty()) {
                    return mapper.readValue(arguments, new TypeReference<Map<String, Object>>() {});
                }
            } catch (Exception e) {
                // 如果解析失败，尝试处理特殊情况
                try {
                    // 处理已经是 Map 的情况（在反序列化时可能发生）
                } catch (Exception ex) {
                    // 忽略异常
                }
            }
            return Map.of();
        }

        // 获取特定参数的值
        public Object getArgument(String key) {
            return getArgumentsMap().get(key);
        }
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ToolExecutionResult {
        private String toolExecutionId;
        private String toolName;
        private String result; // 存储为字符

        // 设置对象结果
        public void setResultObject(Object resultObj) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.result = mapper.writeValueAsString(resultObj);
            } catch (Exception e) {
                this.result = String.valueOf(resultObj);
            }
        }

        // 获取结果对象
        public Object getResultObject() {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(result, Object.class);
            } catch (Exception e) {
                return result;
            }
        }
    }

    public String getDisplayText() {
        if ("USER".equals(type) && contents != null && !contents.isEmpty()) {
            return contents.get(0).getText();
        }
        return text;
    }
}