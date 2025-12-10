package com.example.e_commerce_techshop.controllers.admin;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/admin/redis")
@RequiredArgsConstructor
public class AdminRedisController {
    private final StringRedisTemplate redisTemplate;

    @DeleteMapping("/clear-all")
    public ResponseEntity<?> clearAll() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();;
        return ResponseEntity.ok("Đã clear tất cả dữ liệu Redis thành công.");
    }
}
