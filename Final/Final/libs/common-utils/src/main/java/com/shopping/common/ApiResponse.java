package com.shopping.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;

    // 成功响应 - 带数据
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, null);
    }

    // 成功响应 - 带自定义消息和数据
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    // 成功响应 - 只有消息
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, null);
    }

    // 错误响应
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null);
    }

    // 错误响应 - 带错误码
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, errorCode);
    }

    // 新增：错误响应 - 带数据
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data, null);
    }

    // 新增：错误响应 - 带错误码和数据
    public static <T> ApiResponse<T> error(String message, String errorCode, T data) {
        return new ApiResponse<>(false, message, data, errorCode);
    }

    // 判断是否成功
    public boolean isSuccess() {
        return success;
    }
}