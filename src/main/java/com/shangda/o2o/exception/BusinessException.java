package com.shangda.o2o.exception;

import lombok.Getter;

/**
 * 业务异常，由全局异常处理器统一转为 ApiResponse
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 响应码，默认 0（失败） */
    private final int code;

    public BusinessException(String message) {
        this(0, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
