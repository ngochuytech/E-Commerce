package com.example.e_commerce_techshop.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để đánh dấu các endpoint/method cần kiểm tra:
 * - User phải được xác thực (enable = true)
 * - User không bị chặn (isActive = true và không trong thời gian bị ban)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireActiveAccount {
    /**
     * Có yêu cầu email đã verify không (mặc định true)
     */
    boolean requireVerified() default true;
    
    /**
     * Message tùy chỉnh khi user bị chặn
     */
    String message() default "";
}
