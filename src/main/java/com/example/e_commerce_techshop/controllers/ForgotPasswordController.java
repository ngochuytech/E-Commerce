package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.SendGridEmailService;
import com.example.e_commerce_techshop.services.user.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@Tag(name = "Password Reset", description = "API for forgot password functionality - sends new random password via email")
public class ForgotPasswordController {
    private final IUserService userService;
    private final SendGridEmailService sendGridEmailService;

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Generate new random password and send to user's email address")
    public ResponseEntity<?> processForgotPassword(
            HttpServletRequest request,
            @Parameter(description = "User's email address", example = "user@example.com") @RequestParam String email)
            throws Exception {
            User user = userService.findByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Email không tồn tại"));
            }

            // Tạo mật khẩu ngẫu nhiên (8-12 ký tự, bao gồm chữ hoa, chữ thường, số)
            String newPassword = generateRandomPassword();
            
            // Cập nhật mật khẩu mới cho user
            userService.updatePassword(user, newPassword);
            
            // Gửi email với mật khẩu mới
            sendNewPasswordEmail(user.getEmail(), newPassword);

            return ResponseEntity.ok(ApiResponse.ok("Mật khẩu mới đã được gửi đến email của bạn"));

    }

    /**
     * Tạo mật khẩu ngẫu nhiên có độ dài 10 ký tự
     * Bao gồm: chữ hoa, chữ thường, số và ký tự đặc biệt
     */
    private String generateRandomPassword() {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "@#$%";
        
        String allChars = upperCase + lowerCase + numbers + specialChars;
        
        StringBuilder password = new StringBuilder();
        
        // Đảm bảo có ít nhất 1 ký tự mỗi loại
        password.append(upperCase.charAt((int) (Math.random() * upperCase.length())));
        password.append(lowerCase.charAt((int) (Math.random() * lowerCase.length())));
        password.append(numbers.charAt((int) (Math.random() * numbers.length())));
        password.append(specialChars.charAt((int) (Math.random() * specialChars.length())));
        
        // Thêm 6 ký tự ngẫu nhiên nữa (tổng 10 ký tự)
        for (int i = 0; i < 6; i++) {
            password.append(allChars.charAt((int) (Math.random() * allChars.length())));
        }
        
        // Shuffle các ký tự để random hơn
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
    
    private void sendNewPasswordEmail(String email, String newPassword) throws IOException {
        String subject = "Mật khẩu mới cho tài khoản TechShop";
        
        String htmlContent = "<html><body style=\"font-family: Arial, sans-serif;\">" +
                "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;\">" +
                "<h2 style=\"color: #4CAF50;\">🔑 Mật khẩu mới TechShop</h2>" +
                "<p>Bạn đã yêu cầu đặt lại mật khẩu. Dưới đây là mật khẩu mới của bạn:</p>" +
                "<div style=\"background-color: #f5f5f5; padding: 15px; margin: 20px 0; border-radius: 5px; text-align: center;\">" +
                "<p style=\"margin: 0; color: #666;\">Mật khẩu mới:</p>" +
                "<h3 style=\"margin: 10px 0; color: #333; font-family: 'Courier New', monospace; letter-spacing: 2px;\">" + 
                newPassword + "</h3>" +
                "</div>" +
                "<p><strong>⚠️ Lưu ý quan trọng:</strong></p>" +
                "<ul style=\"color: #666;\">" +
                "<li>Vui lòng đổi mật khẩu này sau khi đăng nhập để bảo mật tài khoản</li>" +
                "<li>Không chia sẻ mật khẩu này với bất kỳ ai</li>" +
                "<li>Nếu không phải bạn yêu cầu, vui lòng liên hệ bộ phận hỗ trợ ngay</li>" +
                "</ul>" +
                "<hr style=\"border: none; border-top: 1px solid #ddd; margin: 20px 0;\">" +
                "<p style=\"color: #999; font-size: 12px;\">Email này được gửi tự động, vui lòng không trả lời.</p>" +
                "<p style=\"color: #999; font-size: 12px;\">© 2025 TechShop E-commerce. All rights reserved.</p>" +
                "</div></body></html>";
        
        sendGridEmailService.sendEmail(email, subject, htmlContent);
    }
}
