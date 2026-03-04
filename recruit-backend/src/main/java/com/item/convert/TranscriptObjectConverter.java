package com.item.convert;

import com.item.dto.ai.TranscriptItemDTO;
import com.item.util.JsonUtils;
import com.item.vo.ai.TranscriptObjectVO;
import com.item.vo.ai.WordVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter for TranscriptObjectVO to JSON string
 */
@Component
public class TranscriptObjectConverter {

    /**
     * Convert List of TranscriptObjectVO to JSON string
     *
     * @param transcriptObjects List of TranscriptObjectVO
     * @return JSON string in the format: [{"spokesperson": "Agent", "time": "1.612 - 24.615", "content": "..."}, ...]
     */
    public String convertToJson(List<TranscriptObjectVO> transcriptObjects) {
        if (transcriptObjects == null || transcriptObjects.isEmpty()) {
            return "[]";
        }

        List<TranscriptItemDTO> items = new ArrayList<>();
        for (TranscriptObjectVO transcriptObject : transcriptObjects) {
            TranscriptItemDTO item = convertToItem(transcriptObject);
            if (item != null) {
                items.add(item);
            }
        }

        return JsonUtils.toJson(items);
    }

    /**
     * Convert single TranscriptObjectVO to TranscriptItemDTO
     *
     * @param transcriptObject TranscriptObjectVO object
     * @return TranscriptItemDTO object
     */
    private TranscriptItemDTO convertToItem(TranscriptObjectVO transcriptObject) {
        if (transcriptObject == null) {
            return null;
        }

        TranscriptItemDTO item = new TranscriptItemDTO();

        // Convert role to spokesperson
        String role = transcriptObject.getRole();
        String spokesperson = convertRoleToSpokesperson(role);
        item.setSpokesperson(spokesperson);

        // Set content
        item.setContent(transcriptObject.getContent());

        // Calculate time from words
        String time = calculateTime(transcriptObject.getWords());
        item.setTime(time);

        return item;
    }

    /**
     * Convert role to spokesperson
     * "agent" -> "Agent", "user" -> "User"
     *
     * @param role Original role value
     * @return Converted spokesperson value
     */
    private String convertRoleToSpokesperson(String role) {
        if (role == null) {
            return null;
        }

        String lowerRole = role.toLowerCase();
        if ("agent".equals(lowerRole)) {
            return "Agent";
        } else if ("user".equals(lowerRole)) {
            return "User";
        }

        // If role is not recognized, return capitalized version
        return capitalizeFirstLetter(role);
    }

    /**
     * Calculate time range from words list
     * Format: "start - end"
     *
     * @param words List of WordVO objects
     * @return Time range string
     */
    private String calculateTime(List<WordVO> words) {
        if (words == null || words.isEmpty()) {
            return "";
        }

        WordVO firstWord = words.get(0);
        WordVO lastWord = words.get(words.size() - 1);

        double start = firstWord.getStart();
        double end = lastWord.getEnd();

        return start + "-" + end;
    }

    /**
     * Capitalize first letter of a string
     *
     * @param str Input string
     * @return String with first letter capitalized
     */
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}

