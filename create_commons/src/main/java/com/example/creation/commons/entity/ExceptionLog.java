package com.example.creation.commons.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.creation.base.entity.SuperEntity;
import lombok.Data;

/**
 * <p>
 * 操作日志异常记录表
 * </p>
 *
 * @author xmy
 * @date 2021/3/19 14:21
 */
@Data
@TableName("exception_log")
public class ExceptionLog extends SuperEntity<ExceptionLog> {

    private static final long serialVersionUID = -4851055162892178225L;

    /**
     * 操作IP
     */
    private String ip;

    /**
     * ip来源
     */
    private String ipSource;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 描述
     */
    private String operation;

    /**
     * 参数
     */
    private String params;

    /**
     * 异常对象json格式
     */
    private String exceptionJson;

    /**
     * 异常简单信息,等同于e.getMessage
     */
    private String exceptionMessage;
}
