package com.example.creation.base.exception.exceptionType;

import com.example.creation.base.global.BaseMessageConf;
import com.example.creation.base.global.ErrorCode;

import java.io.Serializable;

/**
 * 自定义新增操作相关的异常
 *
 * @author xmy
 * @date 2021/3/1 14:33
 */
public class InsertException extends RuntimeException implements Serializable {

    /**
     * 异常状态码
     */
    private String code;

    public InsertException() {
        super(BaseMessageConf.INSERT_DEFAULT_ERROR);
        this.code = ErrorCode.INSERT_DEFAULT_ERROR;
    }

    public InsertException(String message, Throwable cause) {
        super(message, cause);
        this.code = ErrorCode.INSERT_DEFAULT_ERROR;
    }

    public InsertException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public InsertException(String message) {
        super(message);
        this.code = ErrorCode.INSERT_DEFAULT_ERROR;
    }

    public InsertException(String code, String message) {
        super(message);
        this.code = code;
    }

    public InsertException(Throwable cause) {
        super(cause);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
