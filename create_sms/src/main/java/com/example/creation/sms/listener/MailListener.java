package com.example.creation.sms.listener;

import com.example.creation.sms.util.SendMailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 邮件监听器【用于发送邮件】
 *
 */
@Slf4j
@Component
public class MailListener {

    @Resource
    private SendMailUtils sendMailUtils;


    @RabbitListener(queues = "create.email")
    public void sendMail(Map<String, String> map) {
        if (map != null) {
            sendMailUtils.sendEmail(
                    map.get("receiver"),
                    map.get("text")
            );
        }
    }
}
