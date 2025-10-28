package com.example.e_commerce_techshop.services.user;

import com.example.e_commerce_techshop.components.JwtTokenProvider;
import com.example.e_commerce_techshop.dtos.UserLoginDTO;
import com.example.e_commerce_techshop.dtos.user.UserRegisterDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.exceptions.ExpiredTokenException;
import com.example.e_commerce_techshop.exceptions.JwtAuthenticationException;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.UserRepository;
import com.example.e_commerce_techshop.responses.user.UserResponse;
import com.example.e_commerce_techshop.services.FileUploadService;
import com.example.e_commerce_techshop.services.SendGridEmailService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final PasswordEncoder passwordEncoder;

    private final SendGridEmailService sendGridEmailService;
    
    private final FileUploadService fileUploadService;
    
    @Value("${spring.mail.properties.from:ngochuymail25@gmail.com}")
    private String fromAddress;
    
    @Value("${spring.mail.properties.from-name:TechShop E-commerce}")
    private String senderName;

    @Override
    public String loginUser(UserLoginDTO userLoginDTO) throws Exception {
        User user = userRepository.findByEmail(userLoginDTO.getEmail())
                .orElseThrow(() -> new Exception("Email hoặc mật khẩu không đúng!"));
        if(!user.isEnabled()){
            throw new JwtAuthenticationException("Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email để xác nhận.");
        }
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userLoginDTO.getEmail(),
                userLoginDTO.getPassword(),
                user.getAuthorities()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtTokenProvider.generateToken(user);
    }

    @Override
    public void createUser(UserRegisterDTO userDTO, String siteURL) throws Exception {
        String email = userDTO.getEmail();
        if(userRepository.existsByEmail(email)){
            throw new Exception("Email đã tồn tại");
        }

        String verificationCode = UUID.randomUUID().toString();

        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .fullName(userDTO.getFullName())
                .roles(List.of("USER"))
                .isActive(true)
                .enable(false)
                .verificationCode(verificationCode)
                .build();
        userRepository.save(newUser);
        sendVerificationEmail(newUser, siteURL);
    }

    @Override
    public boolean verifyUser(String verificationCode) {
        User user = userRepository.findByVerificationCode(verificationCode);
        if(user==null || user.isEnabled())
            return false;
        user.setEnable(true);
        user.setVerificationCode(null);
        userRepository.save(user);
        return true;
    }

    private void sendVerificationEmail(User user, String siteURL) throws IOException {
        String subject = "Xác nhận đăng ký tài khoản TechShop";
        String verifyURL = siteURL + "/api/v1/users/verify?code=" + user.getVerificationCode();
        
        String htmlContent = "<html><head><meta charset=\"UTF-8\"><style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }" +
                ".content { background-color: #f9f9f9; padding: 30px; }" +
                ".button { display: inline-block; padding: 12px 30px; background-color: #4CAF50; " +
                "color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }" +
                ".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }" +
                "</style></head><body><div class=\"container\">" +
                "<div class=\"header\"><h1>Chào mừng đến với TechShop!</h1></div>" +
                "<div class=\"content\">" +
                "<p>Xin chào <strong>" + user.getFullName() + "</strong>,</p>" +
                "<p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>TechShop E-commerce</strong>.</p>" +
                "<p>Để hoàn tất quá trình đăng ký, vui lòng xác nhận địa chỉ email của bạn bằng cách nhấn vào nút bên dưới:</p>" +
                "<div style=\"text-align: center;\">" +
                "<a href=\"" + verifyURL + "\" class=\"button\">Xác nhận Email</a>" +
                "</div>" +
                "<p>Hoặc copy link sau vào trình duyệt:</p>" +
                "<p style=\"word-break: break-all; color: #0066cc;\">" + verifyURL + "</p>" +
                "<p><strong>Lưu ý:</strong> Link xác nhận có hiệu lực trong 24 giờ.</p>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>Email này được gửi tự động, vui lòng không reply.</p>" +
                "<p>&copy; 2024 TechShop E-commerce. All rights reserved.</p>" +
                "</div></div></body></html>";

        sendGridEmailService.sendEmail(user.getEmail(), subject, htmlContent);
    }

    @Override
    public User getUserByToken(String token) throws Exception{
        if(jwtTokenProvider.isTokenExpired(token)){
            throw new ExpiredTokenException("Phiên đăng nhập của bản đã kết thúc");
        }
        String email = jwtTokenProvider.getUsername(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy user với Email này"));
    }

    @Override
    public void updateResetPasswordToken(String token, String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Tài khoản này chưa được đăng ký"));
        user.setResetPasswordToken(token);
        userRepository.save(user);
    }

    @Override
    public User getUserByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token);
    }

    @Override
    @Transactional
    public void updatePassword(User user, String newPassword){
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        userRepository.save(user);
    }

    @Override
    public UserResponse getCurrentUser(String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy user với Email này"));
        return UserResponse.fromUser(user);
    }

    @Override
    public User findByEmail(String email) throws Exception {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("Không tìm thấy user với Email này"));
    }

    @Override
    @Transactional
    public void updateUserAvatar(User user, MultipartFile avatarFile) throws Exception {
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new IllegalArgumentException("File ảnh không được để trống");
        }
        
        // Validate file type
        String contentType = avatarFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File phải là định dạng ảnh (jpg, png, gif, etc.)");
        }
        
        // Upload ảnh lên Cloudinary
        String avatarUrl = fileUploadService.uploadFile(avatarFile, "avatars");
        
        // Cập nhật URL avatar trong database
        user.setAvatar(avatarUrl);
        userRepository.save(user);
    }
}
