package com.example.e_commerce_techshop.exceptions;

import java.time.LocalDateTime;

public class UserBannedException extends RuntimeException {
    private String userId;
    private String banReason;
    private LocalDateTime bannedUntil;

    public UserBannedException(String message) {
        super(message);
    }

    public UserBannedException(String userId, String banReason, LocalDateTime bannedUntil) {
        super(buildMessage(banReason, bannedUntil));
        this.userId = userId;
        this.banReason = banReason;
        this.bannedUntil = bannedUntil;
    }

    private static String buildMessage(String banReason, LocalDateTime bannedUntil) {
        if (bannedUntil == null) {
            return "Tài khoản của bạn đã bị chặn vĩnh viễn. Lý do: " + banReason;
        } else {
            return "Tài khoản của bạn đã bị chặn đến " + bannedUntil + ". Lý do: " + banReason;
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getBanReason() {
        return banReason;
    }

    public LocalDateTime getBannedUntil() {
        return bannedUntil;
    }
}
