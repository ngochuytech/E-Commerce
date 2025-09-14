package com.example.e_commerce_techshop.responses;

public record ApiResponse<T>(
        boolean success,
        T data,
        String message) {
    
    public static <T> ApiResponse<T> ok(T data){
        return new ApiResponse<>(true, data, null);
    }
    
    // Overloaded method với message
    public static <T> ApiResponse<T> ok(String message, T data){
        return new ApiResponse<>(true, data, message);
    }
    
    // Method chỉ với message (không có data)
    public static <T> ApiResponse<T> ok(String message){
        return new ApiResponse<>(true, null, message);
    }

    public static <T> ApiResponse<T> error(String error){
        return new ApiResponse<>(false, null, error);
    }
}
