package com.example.creation.base.exception.exceptionType;

import com.example.creation.base.global.BaseMessageConf;
import com.example.creation.base.global.ErrorCode;

import java.io.Serializable;

/**
 * 自定义更新操作相关的异常
 *
 */
public class UpdateException extends RuntimeException implements Serializable {
    /**
     * 异常状态码
     */
    private String code;

    public UpdateException() {
        super(BaseMessageConf.UPDATE_DEFAULT_ERROR);
        this.code = ErrorCode.UPDATE_DEFAULT_ERROR;
    }

    public UpdateException(String message, Throwable cause) {
        super(message, cause);
        this.code = ErrorCode.UPDATE_DEFAULT_ERROR;
    }

    public UpdateException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public UpdateException(String message) {
        super(message);
        this.code = ErrorCode.UPDATE_DEFAULT_ERROR;
    }

    public UpdateException(String code, String message) {
        super(message);
        this.code = code;
    }

    public UpdateException(Throwable cause) {
        super(cause);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}