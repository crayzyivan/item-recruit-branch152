package com.item.dto.ayrshare;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Ayrshare Token保存DTO
 *
 * @author lh
 * @since 2025-08-28
 */
@Data
public class AyrshareTokenSaveDTO {

    /**
     * Ayrshare API Key
     */
    @NotBlank(message = "Ayrshare API Key cannot be blank")
    @Size(min = 1, max = 64, message = "Ayrshare api key size must be between {min} and {max}")
    private String ayrshareApiKey;

//    /**
//     * Ayrshare Profile Key
//     */
//    private String ayrshareProfileKey;
//
//    /**
//     * Ayrshare Subreddit
//     */
//    private String ayrshareSubreddit;
//
//    /**
//     * 是否启用
//     */
//    private Boolean isEnabled;
}
