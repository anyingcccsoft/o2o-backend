package com.shangda.o2o.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应封装
 * code: 1 成功 / 0 失败 / 401 未登录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    public static final int CODE_SUCCESS = 1;
    public static final int CODE_ERROR = 0;
    public static final int CODE_UNAUTHORIZED = 401;

    private Integer code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(CODE_SUCCESS, "success", null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(CODE_SUCCESS, "success", data);
    }

    public static <T> ApiResponse<T> success(String msg, T data) {
        return new ApiResponse<>(CODE_SUCCESS, msg, data);
    }

    public static <T> ApiResponse<T> error(String msg) {
        return new ApiResponse<>(CODE_ERROR, msg, null);
    }

    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }

    public static <T> ApiResponse<T> unauthorized() {
        return new ApiResponse<>(CODE_UNAUTHORIZED, "未登录", null);
    }
}
