package com.item.dto.iam;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author : lh
 */
@Data
public class InitPointsDTO {
    @NotBlank(message = "userEmail must not be blank")
    @Email(message = "userEmail must be a well-formed email address")
    private String userEmail;

    @NotBlank(message = "userEmail must not be blank")
    private String userName;

    @NotNull(message = "userId must not be null")
    @Min(value = 1, message = "userId must be greater than or equal to {value}")
    private Long userId;
}
