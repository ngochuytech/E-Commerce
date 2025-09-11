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
public class UserService implements IUserService{

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

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
        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new Exception("Role not found"));

        if(role.getName().toUpperCase().equals(Role.ADMIN)){
            throw new Exception("You can't register an admin account");
        }

        String verificationCode = UUID.randomUUID().toString();

        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .fullName(userDTO.getFullName())
                .role(role)
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
        String verifyURL = siteURL + "/api/v1/users/verify?code=" + user.getVerificationCode();

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
}
