package com.item.dto.iam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author : lh
 */
@Data
public class ExistsCompanyDTO {
    @NotNull(message = "companyName must not be null")
    @NotBlank(message = "companyName must not be blank")
    private String companyName;
}
