package com.item.util;

import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.error.BusinessException;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

@Component
@Slf4j
public class MailUtils {

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private TemplateEngine templateEngine;

    // sender和from一定要一致 和IT沟通后
    @Value("${spring.mail.username}")
    private String mailFrom;

    /**
     * 发送简单文本邮件（同步）
     */
    public void sendSimpleMail(String to, String subject, String content) {
        sendSimpleMail(null, new String[]{to}, null, null, subject, content);
    }

    /**
     * 发送简单文本邮件（支持抄送、密送、批量、发件人）
     */
    public void sendSimpleMail(String from, String[] to, String[] cc, String[] bcc, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (StringUtils.hasText(from)) {
                message.setFrom(from);
            }
            message.setTo(to);
            if (cc != null && cc.length > 0) message.setCc(cc);
            if (bcc != null && bcc.length > 0) message.setBcc(bcc);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("发送简单邮件失败: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.EMAIL_SEND_ERROR,"Sending the email failed");
        }
    }

    /**
     * 发送HTML邮件（同步）
     */
    public void sendHtmlMail(String to, String subject, String htmlContent) {
        sendHtmlMail(null, new String[]{to}, null, null, subject, htmlContent, null);
    }

    /**
     * 发送HTML邮件（支持抄送、密送、批量、发件人、附件）
     * from 参数未使用 需要保持和配置一致
     */
    public void sendHtmlMail(String from, String[] to, String[] cc, String[] bcc, String subject, String htmlContent, File[] attachments) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, attachments != null && attachments.length > 0);
//            if (StringUtils.hasText(from)) {
//                helper.setFrom(from);
//            }
            helper.setFrom(mailFrom);
            helper.setTo(to);
            if (cc != null && cc.length > 0) helper.setCc(cc);
            if (bcc != null && bcc.length > 0) helper.setBcc(bcc);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            // 添加附件
            if (attachments != null) {
                for (File file : attachments) {
                    if (file != null && file.exists()) {
                        helper.addAttachment(file.getName(), new FileSystemResource(file));
                    }
                }
            }
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("发送HTML邮件失败: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.EMAIL_SEND_ERROR,"Sending the email failed");
        } catch (Exception e) {
            log.error("send fail : {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.EMAIL_SEND_ERROR,"Sending the email failed");
        }
    }

    /**
     * 异步发送HTML邮件
     */
    @Async
    public void sendHtmlMailAsync(String from, String[] to, String[] cc, String[] bcc, String subject, String htmlContent, File[] attachments) {
        sendHtmlMail(from, to, cc, bcc, subject, htmlContent, attachments);
    }

    /**
     * 异步发送简单文本邮件
     */
    @Async
    public void sendSimpleMailAsync(String from, String[] to, String[] cc, String[] bcc, String subject, String content) {
        sendSimpleMail(from, to, cc, bcc, subject, content);
    }

    /**
     * 发送HTML模板邮件
     */
    public void sendHtmlTemplateMail(String to, String subject, String templateName, Map<String, Object> variables) {
        sendHtmlTemplateMail(null, new String[]{to}, null, null, subject, templateName, variables, null);
    }

    /**
     * 发送HTML模板邮件
     */
    public void sendHtmlTemplateMail(String from,String to, String subject, String templateName, Map<String, Object> variables) {
        sendHtmlTemplateMail(from, new String[]{to}, null, null, subject, templateName, variables, null);
    }

    public void sendHtmlTemplateMail(String from, String[] to, String[] cc, String[] bcc, String subject,
                                     String templateName, Map<String, Object> variables, File[] attachments) {
        Context context = new Context();
        if (variables != null) {
            context.setVariables(variables);
        }
        String htmlContent = templateEngine.process(templateName, context);
        sendHtmlMail(from, to, cc, bcc, subject, htmlContent, attachments);
        log.info("The html email has been sent successfully. email:{},templateName:{},context:{},variables:{}", Arrays.toString(to),templateName,htmlContent,variables);
    }

    /**
     * 发送文本模板邮件
     */
    public void sendTextTemplateMail(String to, String subject, String templateName, Map<String, Object> variables) {
        sendTextTemplateMail(null, new String[]{to}, null, null, subject, templateName, variables);
    }

    public void sendTextTemplateMail(String from, String[] to, String[] cc, String[] bcc, String subject,
                                     String templateName, Map<String, Object> variables) {
        Context context = new Context();
        if (variables != null) {
            context.setVariables(variables);
        }
        String textContent = templateEngine.process(templateName, context);
        sendSimpleMail(from, to, cc, bcc, subject, textContent);
        log.info("The text email has been sent successfully. email:{},templateName:{}", Arrays.toString(to),templateName);
    }

    public void sendHtmlTemplateMail(String[] to, String subject, String templateName, Map<String, Object> variables) {
        sendHtmlTemplateMail(null, to, null, null, subject, templateName, variables, null);
    }
}

