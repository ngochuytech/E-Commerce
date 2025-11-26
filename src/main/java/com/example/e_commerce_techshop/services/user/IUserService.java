package com.example.e_commerce_techshop.services.user;

import com.example.e_commerce_techshop.dtos.UserLoginDTO;
import com.example.e_commerce_techshop.dtos.admin.user.BanUserDTO;
import com.example.e_commerce_techshop.dtos.user.UpdateUserDTO;
import com.example.e_commerce_techshop.dtos.user.UserRegisterDTO;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.user.UserResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    String loginUser(UserLoginDTO userLoginDTO) throws Exception;

    void createUser(UserRegisterDTO userDTO, String siteURL) throws Exception;

    boolean verifyUser(String verificationCode);

    void sendVerificationEmailAgain(String email, String siteURL) throws Exception;

    User getUserByToken(String token) throws Exception;

    User getUserById(String userId) throws Exception;

    void updateResetPasswordToken(String token, String email) throws Exception;

    User getUserByResetPasswordToken(String token);

    void changePassword(User currentUser, String currentPassword, String newPassword) throws Exception;

    void updatePassword(User user, String newPassword);

    UserResponse getCurrentUser(String email) throws Exception;

    User findByEmail(String email) throws Exception;

    void updateUserProfile(User currentUser, UpdateUserDTO userUpdateDTO) throws Exception;

    void updateUserAvatar(User user, MultipartFile avatarFile) throws Exception;

    // ADMIN
    Page<User> getAllUsers(String userName, String userEmail, String userPhone, Pageable pageable);

    User banUser(String adminId, BanUserDTO banUserDTO) throws Exception;

    User unbanUser(String adminId, String userId) throws Exception;

    boolean isUserBanned(String userId);
}
