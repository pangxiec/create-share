package com.example.creation.admin.annotion.OperationLogger;


import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.example.creation.admin.global.RedisConf;
import com.example.creation.admin.global.SysConf;
import com.example.creation.commons.config.security.SecurityUser;
import com.example.creation.commons.entity.SysLog;
import com.example.creation.utils.IpUtils;
import com.example.creation.utils.RedisUtil;
import com.example.creation.utils.StringUtils;
import com.example.creation.base.global.Constants;
import com.example.creation.base.holder.AbstractRequestAwareRunnable;
import com.example.creation.base.holder.RequestHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 异步记录日志
 *
 */
@Component
public class SysLogHandle extends AbstractRequestAwareRunnable {

    @Resource
    RedisUtil redisUtil;

    /**
     * 参数列表
     */
    private String paramsJson;

    /**
     * 类路径
     */
    private String classPath;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 方法请求时间
     */
    private Date startTime;

    /**
     * 操作名称
     */
    private String operationName;

    /**
     * set方法
     *
     * @param paramsJson
     * @param classPath
     * @param methodName
     * @param operationName
     * @param startTime
     */
    public void setSysLogHandle(String paramsJson, String classPath, String methodName, String operationName, Date startTime) {
        this.paramsJson = paramsJson;
        this.classPath = classPath;
        this.methodName = methodName;
        this.operationName = operationName;
        this.startTime = startTime;
    }

    @Override
    protected void onRun() {
        SysLog sysLog = new SysLog();
        HttpServletRequest request = RequestHolder.getRequest();
        String ip = IpUtils.getIpAddr(request);
        sysLog.setIp(ip);

        //从Redis中获取IP来源
        String jsonResult = redisUtil.get(RedisConf.IP_SOURCE + Constants.SYMBOL_COLON + ip);
        if (StringUtils.isEmpty(jsonResult)) {
            String addresses = IpUtils.getAddresses(SysConf.IP + SysConf.EQUAL_TO + ip, SysConf.UTF_8);
            if (StringUtils.isNotEmpty(addresses)) {
                sysLog.setIpSource(addresses);
                redisUtil.setEx(RedisConf.IP_SOURCE + Constants.SYMBOL_COLON + ip, addresses, 24, TimeUnit.HOURS);
            }
        } else {
            sysLog.setIpSource(jsonResult);
        }

        //设置请求信息
        sysLog.setIp(ip);

        //设置调用的类
        sysLog.setClassPath(classPath);

        //设置调用的方法
        sysLog.setMethod(methodName);

        //设置Request的请求方式 GET POST
        sysLog.setType(request.getMethod());
        sysLog.setUrl(request.getRequestURI());

        sysLog.setOperation(operationName);
        sysLog.setCreateTime(new Date());
        sysLog.setUpdateTime(new Date());
        SecurityUser securityUser = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        sysLog.setUserName(securityUser.getUsername());
        sysLog.setAdminUid(securityUser.getUid());
        sysLog.setParams(paramsJson);
        Date endTime = new Date();
        Long spendTime = DateUtil.between(startTime, endTime, DateUnit.MS);
        // 计算请求接口花费的时间，单位毫秒
        sysLog.setSpendTime(spendTime);
        sysLog.insert();
    }
}
