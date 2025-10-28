package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.dtos.ResetPasswordDTO;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.SendGridEmailService;
import com.example.e_commerce_techshop.services.user.IUserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@Tag(name = "Password Reset", description = "APIs for forgot password and reset password functionality")
public class ForgotPasswordController {
    private final JavaMailSender javaMailSender;

    private final IUserService userService;

    private final SendGridEmailService sendGridEmailService; 
    
    @Value("${spring.mail.properties.from:ngochuymail25@gmail.com}")
    private String fromAddress;
    
    @Value("${spring.mail.properties.from-name:TechShop E-commerce}")
    private String senderName;

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Send password reset email with token to user's email address")
    public ResponseEntity<?> processForgotPassword(
            HttpServletRequest request,
            @Parameter(description = "User's email address", example = "user@example.com") @RequestParam String email)
            throws Exception {
            User user = userService.findByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Email không tồn tại"));
            }

            String token = UUID.randomUUID().toString();
            user.setResetPasswordToken(token);
            userService.updateResetPasswordToken(token, email);

            String resetPasswordLink = getSiteURL(request) + "/reset-password?token=" + token;
            
            // ✅ Sử dụng SendGrid API
            sendResetPasswordEmail(user.getEmail(), resetPasswordLink);

            return ResponseEntity.ok(ApiResponse.ok("Email đặt lại mật khẩu đã được gửi"));

    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token", description = "Reset user password using the token received via email")
    public ResponseEntity<?> resetPassword(
            @Parameter(description = "Reset password data with token and new password") @RequestBody ResetPasswordDTO resetPasswordDTO) {
        User user = userService.getUserByResetPasswordToken(resetPasswordDTO.getToken());
        if (user == null)
            return ResponseEntity.badRequest().body(ApiResponse.error("Mã token không hợp lệ"));
        userService.updatePassword(user, resetPasswordDTO.getPassword());
        return ResponseEntity.ok(ApiResponse.ok("Đã đổi mật khẩu thành công"));
    }

    public void sendEmail(String recipientEmail, String link)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(recipientEmail);

        String subject = "Yêu cầu đặt lại mật khẩu";

        String content = "<p>Xin chào,</p>"
                + "<p>Bạn đã gửi yêu cầu đặt lại mật khẩu.</p>"
                + "<p>Click vào link bên dưới để thực hiện thao tác:</p>"
                + "<p><a href=\"" + link + "\">Đặt lại mật khẩu</a></p>"
                + "<br>"
                + "<p>Bỏ qua email này nếu bạn không yêu cầu đổi mật khẩu.</p>"
                + "<p>Cảm ơn,<br>" + senderName + "</p>";

        helper.setSubject(subject);

        helper.setText(content, true);

        javaMailSender.send(message);
    }

    private void sendResetPasswordEmail(String email, String resetLink) throws IOException {
        String subject = "Yêu cầu đặt lại mật khẩu";
        
        String htmlContent = "<html><body style=\"font-family: Arial, sans-serif;\">" +
                "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                "<h2>Đặt lại mật khẩu TechShop</h2>" +
                "<p>Bạn đã yêu cầu đặt lại mật khẩu. Click vào link bên dưới để tiếp tục:</p>" +
                "<a href=\"" + resetLink + "\" style=\"display: inline-block; padding: 12px 30px; " +
                "background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px;\">" +
                "Đặt lại mật khẩu</a>" +
                "<p>Link có hiệu lực trong 30 phút.</p>" +
                "<p>Nếu không phải bạn yêu cầu, vui lòng bỏ qua email này.</p>" +
                "</div></body></html>";
        
        sendGridEmailService.sendEmail(email, subject, htmlContent);
    }

    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }
}
