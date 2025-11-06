package com.example.e_commerce_techshop.services.scheduled;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnbanScheduledService {
    private final UserRepository userRepository;

    // Chạy mỗi giờ để kiểm tra và tự động mở chặn
    @Scheduled(cron = "0 0 * * * *") // Mỗi giờ
    public void autoUnbanUsers() {
        log.info("Bắt đầu kiểm tra người dùng hết hạn chặn...");

        List<User> bannedUsers = userRepository.findByIsActiveFalseAndBannedUntilNotNull();
        LocalDateTime now = LocalDateTime.now();

        int unbannedCount = 0;
        for (User user : bannedUsers) {
            if (user.getBannedUntil() != null && now.isAfter(user.getBannedUntil())) {
                user.setIsActive(true);
                user.setBanReason(null);
                user.setBannedAt(null);
                user.setBannedUntil(null);
                user.setBannedBy(null);
                userRepository.save(user);
                unbannedCount++;
                log.info("Tự động mở chặn cho user: {}", user.getEmail());
            }
        }

        log.info("Hoàn thành. Đã mở chặn {} người dùng", unbannedCount);
    }
}
