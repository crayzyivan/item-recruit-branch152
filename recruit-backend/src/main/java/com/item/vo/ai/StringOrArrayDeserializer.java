package com.item.vo.ai;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class StringOrArrayDeserializer extends JsonDeserializer<String> {
    
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        
        if (node.isArray()) {
            // Convert array to string, joining with newlines or other separator
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < node.size(); i++) {
                if (i > 0 && i < node.size() - 1) {
                    sb.append(", ");
                }
                sb.append(node.get(i).asText());
            }
            return sb.toString();
        } else if (node.isTextual()) {
            // Already a string, return as-is
            return node.asText();
        } else if (node.isNull()) {
            return null;
        } else {
            // For other types, convert to string
            return node.toString();
        }
    }
}
