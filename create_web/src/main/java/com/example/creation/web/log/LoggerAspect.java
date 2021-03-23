package com.example.creation.web.log;

import com.example.creation.utils.AopUtils;
import com.example.creation.utils.AspectUtil;
import com.example.creation.utils.IpUtils;
import com.example.creation.web.global.SysConf;
import com.example.creation.base.enums.EBehavior;
import com.example.creation.base.holder.RequestHolder;
import com.example.creation.base.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 日志切面
 */
@Aspect
@Component("WebLoggerAspect")
@Slf4j
public class LoggerAspect {

    @Resource
    SysLogHandle sysLogHandle;

    @Pointcut(value = "@annotation(bussinessLog)")
    public void pointcut(BussinessLog bussinessLog) {

    }

    @Around(value = "pointcut(bussinessLog)")
    public Object doAround(ProceedingJoinPoint joinPoint, BussinessLog bussinessLog) throws Throwable {

        //先执行业务
        Object result = joinPoint.proceed();

        try {
            // 日志收集
            handle(joinPoint);

        } catch (Exception e) {
            log.error("日志记录出错!", e);
        }

        return result;
    }

    private void handle(ProceedingJoinPoint point) throws Exception {

        HttpServletRequest request = RequestHolder.getRequest();

        Method currentMethod = AspectUtil.INSTANCE.getMethod(point);
        //获取操作名称
        BussinessLog annotation = currentMethod.getAnnotation(BussinessLog.class);

        boolean save = annotation.save();

        EBehavior behavior = annotation.behavior();

        String bussinessName = AspectUtil.INSTANCE.parseParams(point.getArgs(), annotation.value());

        String ua = RequestUtil.getUa();

        log.info("{} | {} - {} {} - {}", bussinessName, IpUtils.getIpAddr(request), RequestUtil.getMethod(), RequestUtil.getRequestUrl(), ua);
        if (!save) {
            return;
        }

        // 获取参数名称和值
        Map<String, Object> nameAndArgsMap = AopUtils.getFieldsName(point);

        Map<String, String> result = EBehavior.getModuleAndOtherData(behavior, nameAndArgsMap, bussinessName);

        if (result != null) {
            String userUid = "";
            if (request.getAttribute(SysConf.USER_UID) != null) {
                userUid = request.getAttribute(SysConf.USER_UID).toString();
            }
            // 异步存储日志
            sysLogHandle.setSysLogHandle(userUid, behavior.getBehavior(), result.get(SysConf.MODULE_UID), result.get(SysConf.OTHER_DATA));
            sysLogHandle.onRun();
        }
    }
}
