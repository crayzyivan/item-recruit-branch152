package com.item.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.item.dto.ai.TranscriptDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 对话内容自动转换为 JSON 格式的工具类
 * 支持自定义配置、日期处理和灵活的格式转换
 */
@Component
public class TranscriptConverter {
    private final ObjectMapper objectMapper;
    private final Pattern messagePattern;

    // 构造函数（使用默认配置）
    public TranscriptConverter() {
        this.objectMapper = createDefaultObjectMapper();
        this.messagePattern = Pattern.compile("(\\w+):\\s*(.*?)(?=\\n\\w+:|$)", Pattern.DOTALL);
    }

    private ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper;
    }

    /**
     * 将对话文本转换为 JSON 字符串
     * @param conversation 对话文本
     * @return 格式化后的 JSON 字符串
     */
    public String convertToJson(String conversation) {
        if (conversation == null || conversation.trim().isEmpty()) {
            return "[]";
        }

        List<TranscriptDTO> messages = parseMessages(conversation);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messages);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 转换失败", e);
        }
    }

    /**
     * 解析对话文本为消息列表
     */
    private List<TranscriptDTO> parseMessages(String conversation) {
        List<TranscriptDTO> transcriptDTOS = new ArrayList<>();
        Matcher matcher = messagePattern.matcher(conversation);

        while (matcher.find()) {
            String spokesperson = matcher.group(1).trim();
            String content = matcher.group(2).trim()
                    .replaceAll("\\s+", " ");  // 合并连续空格

            transcriptDTOS.add(new TranscriptDTO(spokesperson, content));
        }

        return transcriptDTOS;
    }

}
