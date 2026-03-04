package com.item.framework.http.validator;

import com.item.framework.annotation.Xss;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * XSS验证器实现
 */
@Slf4j
public class XssValidator implements ConstraintValidator<Xss, String> {
    private static final Safelist SAFE_CONTEXT = Safelist.none();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 允许为空，非空验证由@NotBlank等注解处理
        if (value == null) {
            return true;
        }
        // 是否有html tag attributes
        boolean valid = Jsoup.isValid(value, SAFE_CONTEXT);

        if (!valid) {
            log.info("value exist html tag or attributes {}", value);
        }
        return valid;
    }
}
