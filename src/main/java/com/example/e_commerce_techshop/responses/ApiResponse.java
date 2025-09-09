package com.example.e_commerce_techshop.responses;

public record ApiResponse<T>(
        boolean success,
        T data,
        String error) {
    public static <T> ApiResponse<T> ok(T data){
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String error){
        return new ApiResponse<>(false, null, error);
    }
}
