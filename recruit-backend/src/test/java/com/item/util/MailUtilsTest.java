package com.item.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class MailUtilsTest {

    @Autowired
    private MailUtils mailUtils;

    @Test
    public void testSendHtmlTemplateMail() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("username", "张三");
        vars.put("code", "123456");
        mailUtils.sendHtmlTemplateMail("yarong.guo@item.com", "HTML模板测试", "email-html.html", vars);
    }

    @Test
    public void testSendTextTemplateMail() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("username", "李四");
        vars.put("code", "654321");
        mailUtils.sendTextTemplateMail("yarong.guo@item.com", "文本模板测试", "email-text.txt", vars);
    }

    @Test
    public void sendSimpleMail() {
        // 发送简单邮件
        mailUtils.sendSimpleMail("to@xxx.com", "主题", "内容");
    }

    @Test
    public void sendQuestionMail() {
        String to = "hua.liu@item.com";
        String sub = "zhuti";
        String template = "Interview-invitation-video-en.html";
        String templateP = "Interview-invitation-phone-en.html";
        Map<String, Object> vars = new HashMap<>();
        vars.put("companyName", "公司名称0001");
        vars.put("candidateName", "候选人名称");
        vars.put("jobTitle", "job名称");
        vars.put("writtenTestUrl", "https://recruit-dev.item.pub/5s-test?dateTime=1768816606&candidateJobId=4daa4c5igh2w&signature=1b32caecb73232272b23a327da6a0656");
        vars.put("interviewUrl", "https://www.google.com");
        mailUtils.sendHtmlTemplateMail(to, sub, template, vars);
//        mailUtils.sendHtmlTemplateMail(to, sub, templateP, vars);
//        mailUtils.sendHtmlTemplateMail(to, sub, "Interview-invitation.html", vars);
    }
} 