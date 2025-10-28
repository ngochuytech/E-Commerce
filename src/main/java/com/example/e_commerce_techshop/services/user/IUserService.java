package com.example.e_commerce_techshop.services.user;

import com.example.e_commerce_techshop.dtos.UserLoginDTO;
import com.example.e_commerce_techshop.dtos.user.UserRegisterDTO;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.user.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    String loginUser(UserLoginDTO userLoginDTO) throws Exception;

    void createUser(UserRegisterDTO userDTO, String siteURL) throws Exception;

    boolean verifyUser(String verificationCode);

    User getUserByToken(String token) throws Exception;

    void updateResetPasswordToken(String token, String email) throws Exception;

    User getUserByResetPasswordToken(String token);

    void updatePassword(User user, String newPassword);

    UserResponse getCurrentUser(String email) throws Exception;

    User findByEmail(String email) throws Exception;
    
    void updateUserAvatar(User user, MultipartFile avatarFile) throws Exception;
}
