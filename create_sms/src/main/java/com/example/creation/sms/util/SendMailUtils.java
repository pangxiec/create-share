package com.example.creation.sms.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

/**
 * 邮件发送工具类
 *
 */
@Slf4j
@Component
public class SendMailUtils {

    @Value(value = "${spring.mail.username}")
    public String SENDER;

    @Resource
    private JavaMailSenderImpl mailSender;

    /**
     * 发送邮件
     *
     * @param receiver
     * @param text
     */
    public void sendEmail(String receiver, String text) {
        try{
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setSubject("文章");
            helper.setText(text, true);
            helper.setTo(receiver);
            helper.setFrom(SENDER);
            mailSender.send(mimeMessage);
            log.info("邮件发送成功");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
} 