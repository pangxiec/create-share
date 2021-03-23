package com.example.creation.web.restapi;


import com.example.creation.utils.ResultUtil;
import com.example.creation.utils.wechat.SignUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 微信公众号 RestApi
 *
 */
@RestController
@RequestMapping("/wechat")
@Api(value = "关于我相关接口", tags = {"关于我相关接口"})
@Slf4j
public class WechatRestApi {

    @ApiOperation(value = "获取微信公众号状态", notes = "获取微信公众号状态")
    @GetMapping("/wechatCheck")
    public String wechatCheck(HttpServletRequest request) {
        System.out.println("我进来了");
        String msgSignature = request.getParameter("signature");
        String msgTimestamp = request.getParameter("timestamp");
        String msgNonce = request.getParameter("nonce");
        String echostr = request.getParameter("echostr");
        if(SignUtil.checkSignature(msgSignature, msgTimestamp, msgNonce)) {
            return echostr;
        }
        return null;
    }

    @ApiOperation(value = "用户扫码后触发的事件", notes = "用户扫码后触发的事件")
    @PostMapping("/wechatCheck")
    public String index(HttpServletRequest request) {
        System.out.println(request);
        return "success";
    }


    @ApiOperation(value = "获取微信公众号登录二维码", notes = "获取微信公众号登录二维码")
    @GetMapping("/getWechatOrCodeTicket")
    public String getWechatOrCodeTicket() {
        String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=TOKEN";
        return ResultUtil.successWithData("http://image.moguit.cn/cb9ac3e6c1244a6f8c2cce667bd7c4ae");
    }


}

