package com.example.creation.sms.listener;

import com.example.creation.sms.util.SmsUtil;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 短信监听器【用于发送短信】
 *
 */
@Component
public class SmsListener {

    @Resource
    private SmsUtil smsUtil;

    @RabbitListener(queues = "create.sms")
    public void sendSms(Map<String, String> map) {
        //TODO 短信发送暂时不用
//        try {
//            SendSmsResponse response = smsUtil.sendSms(
//                    map.get("mobile"),
//                    map.get("template_code"),
//                    map.get("sign_name"),
//                    map.get("param"));
//            System.out.println("code:" + response.getCode());
//            System.out.println("message:" + response.getMessage());
//
//        } catch (ClientException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

}
