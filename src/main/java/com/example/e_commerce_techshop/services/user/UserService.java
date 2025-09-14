package com.example.e_commerce_techshop.services.user;

import com.example.e_commerce_techshop.components.JwtTokenProvider;
import com.example.e_commerce_techshop.dtos.UserDTO;
import com.example.e_commerce_techshop.dtos.UserLoginDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.exceptions.ExpiredTokenException;
import com.example.e_commerce_techshop.models.Role;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.RoleRepository;
import com.example.e_commerce_techshop.repositories.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;


    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final PasswordEncoder passwordEncoder;

    private final JavaMailSender mailSender;

    @Override
    public String loginUser(UserLoginDTO userLoginDTO) throws Exception {
        User user = userRepository.findByEmail(userLoginDTO.getEmail())
                .orElseThrow(() -> new Exception("Email hoặc mật khẩu không đúng!"));
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userLoginDTO.getEmail(),
                userLoginDTO.getPassword(),
                user.getAuthorities()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtTokenProvider.generateToken(user);
    }

    @Override
    public void createUser(UserDTO userDTO, String siteURL) throws Exception {
        String email = userDTO.getEmail();
        if(userRepository.existsByEmail(email)){
            throw new Exception("Email đã tồn tại");
        }
        // Kiểm tra role ID hợp lệ
        if(userDTO.getRoleId() == 2){ // ADMIN role
            throw new Exception("You can't register an admin account");
        }

        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .fullName(userDTO.getFullName())
                .phone(userDTO.getPhone()) // Thêm phone
                .role(userDTO.getRoleId().intValue())
                .isActive(true)
                .build();
        userRepository.save(newUser);
    }

    @Override
    public boolean verifyUser(String verificationCode) {
        // Tạm thời return true vì không có verification_code trong database
        return true;
    }

    private void sendVerificationEmail(User user, String siteURL) throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String fromAddress = "ngochuymail25@gmail.com";
        String senderName = "E-commerce";
        String subject = "Xác nhận đăng ký tài khoản";
        String content = "Chào [[name]],<br>"
                + "Hãy click vào link bên dưới để tiến hành xác nhận đăng ký:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Cảm ơn,<br>"
                + "E-commerce.";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", user.getFullName());
        // Không có verification code vì đã xóa field này
        String verifyURL = siteURL + "/api/v1/users/verify?code=dummy";

        content = content.replace("[[URL]]", verifyURL);

        helper.setText(content, true);

        mailSender.send(message);
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
        // Tạm thời không implement vì không có reset_password_token trong database
        throw new Exception("Reset password feature not available");
    }

    @Override
    public User getUserByResetPasswordToken(String token) {
        // Tạm thời return null vì không có reset_password_token trong database
        return null;
    }

    @Override
    @Transactional
    public void updatePassword(User user, String newPassword){
        user.setPassword(passwordEncoder.encode(newPassword));
        // Không có resetPasswordToken field
        userRepository.save(user);
    }
}
