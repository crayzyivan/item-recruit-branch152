package com.item.dto.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author : lh
 */
@Data
public class GenerateJobDescDTO {
    @NotBlank(message = "jobTitle must not be blank")
    @Size(min = 1, max = 200, message = "size must be between {min} and {max}")
    private String jobTitle;
    private String extraAi;
    private LocationValDTO location;
    private GenerateJobDescDTO data;

    //优先获取data中的字段值 data没有在获取当前jobTitle
    public String customerGetJobTitle(){
        if (Objects.nonNull(this.getData()) && StringUtils.isNotBlank(this.getData().getJobTitle())) {
            return this.getData().getJobTitle();
        }
        return this.getJobTitle();
    }

    //优先获取data中的字段值 data没有在获取当前extraAi
    public String customerGetExtraAi(){
        if (Objects.nonNull(this.getData()) && StringUtils.isNotBlank(this.getData().getExtraAi())) {
            return this.getData().getExtraAi();
        }
        return this.getExtraAi();
    }
}
