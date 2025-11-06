package com.example.e_commerce_techshop.aspects;

import com.example.e_commerce_techshop.annotations.RequireActiveAccount;
import com.example.e_commerce_techshop.exceptions.AccountNotVerifiedException;
import com.example.e_commerce_techshop.exceptions.UserBannedException;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AccountValidationAspect {
    private final UserRepository userRepository;

    /**
     * Intercept tất cả method có @RequireActiveAccount annotation
     */
    @Before("@annotation(com.example.e_commerce_techshop.annotations.RequireActiveAccount)")
    public void validateUserAccount(JoinPoint joinPoint) throws Throwable {
        // Lấy thông tin user từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthenticated user trying to access protected method: {}", joinPoint.getSignature());
            return; // Spring Security sẽ xử lý unauthorized
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            log.warn("Principal is not a User instance: {}", principal.getClass());
            return;
        }

        User user = (User) principal;
        
        // Lấy annotation từ method
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireActiveAccount annotation = method.getAnnotation(RequireActiveAccount.class);

        // 1. Kiểm tra email đã verify chưa (nếu requireVerified = true)
        if (annotation.requireVerified()) {
            if (user.getEnable() == null || !user.getEnable()) {
                log.warn("User {} attempted to access protected resource without email verification", user.getEmail());
                throw new AccountNotVerifiedException(user.getId(), user.getEmail());
            }
        }

        // 2. Kiểm tra user có bị ban không
        if (user.getIsActive() == null || !user.getIsActive()) {
            // Kiểm tra xem có phải ban tạm thời đã hết hạn chưa
            if (user.getBannedUntil() != null && LocalDateTime.now().isAfter(user.getBannedUntil())) {
                // Ban đã hết hạn, có thể cho phép (nhưng nên update DB trước)
                log.info("User {} ban period has expired", user.getEmail());
                user.setIsActive(true);
                user.setBannedUntil(null);
                userRepository.save(user);
                return;
            }
            
            log.warn("Banned user {} attempted to access protected resource", user.getEmail());
            throw new UserBannedException(
                user.getId(),
                user.getBanReason() != null ? user.getBanReason() : "Vi phạm quy định",
                user.getBannedUntil()
            );
        }

        log.debug("User {} passed account validation", user.getEmail());
    }
}
