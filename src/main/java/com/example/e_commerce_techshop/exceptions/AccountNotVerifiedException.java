package com.example.e_commerce_techshop.exceptions;

public class AccountNotVerifiedException extends RuntimeException {
    private String userId;
    private String email;

    public AccountNotVerifiedException(String message) {
        super(message);
    }

    public AccountNotVerifiedException(String userId, String email) {
        super("Tài khoản chưa được xác thực. Vui lòng kiểm tra email " + email + " để xác thực tài khoản.");
        this.userId = userId;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
