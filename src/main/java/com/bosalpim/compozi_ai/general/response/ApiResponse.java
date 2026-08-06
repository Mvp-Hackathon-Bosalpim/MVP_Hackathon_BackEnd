package com.bosalpim.compozi_ai.general.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private String status;
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return new ApiResponse<>("SUCCESS", code, message, data);
    }

    public static ApiResponse<?> fail(int code, String message) {
        return new ApiResponse<>("FAIL", code, message, null);
    }
}