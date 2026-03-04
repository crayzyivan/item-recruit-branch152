package com.item;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.apache.commons.validator.routines.CreditCardValidator;
import org.owasp.encoder.Encode;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author : lh
 */
@Slf4j
@SpringBootTest
public class ValidationTest {


    @Test
    public void validatorCard() {
        CreditCardValidator validator = new CreditCardValidator();
        boolean isValid = validator.isValid("4111111111111111");
        log.info("isValid: {}", isValid);
        boolean isValidq = validator.isValid("4111111111111112");
        log.info("isValid: {}", isValidq);
    }

    @Test
    public void validatorXss(){
        String value = """
                As a Java Developer, you will play a pivotal role in shaping the digital backbone of our organization's solutions, driving innovation and ensuring scalability in line with business objectives. Positioned within the development team, this role requires close collaboration with product managers and stakeholders to translate business needs into robust and efficient codebase. Your contributions will have a direct impact on our service offerings, influencing customer satisfaction and competitive positioning in the market. """;
        // 对输入值进行HTML转义
        String escapedValue = Encode.forHtml(value);
        log.info("escapedValue: {}", escapedValue.equals(value));
        log.info("escapedValue: {}", escapedValue);

    }

}
