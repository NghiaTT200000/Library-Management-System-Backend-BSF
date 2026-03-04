package com.library_management.library_management_artifact.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String message;
    private boolean isOk;
    private T data;

    public static <T> ApiResponse<T> success(String message, T data){
        return ApiResponse.<T>builder()
                .message(message)
                .isOk(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message){
        return success(message,null);
    }

    public static <T> ApiResponse<T> error(String message){
        return ApiResponse.<T>builder()
                .message(message)
                .isOk(false)
                .data(null)
                .build();
    }
}
