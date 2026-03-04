package com.item.vo.iam;

import com.item.framework.annotation.Xss;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * IAM ticket exchange request VO for cross-system session sync
 * Used in Controller layer for API input validation
 * 
 * @author : lh
 */
@Data
public class IamTicketExchangeReqVO {
    /**
     * The ticket to be exchanged for access tokens
     */
    @Xss(message = "Auth ticket some special characters, such as \"<\", \">\", \"<a>\", etc.")
    @NotBlank(message = "Auth ticket cannot be blank")
    private String authTicket;
}
