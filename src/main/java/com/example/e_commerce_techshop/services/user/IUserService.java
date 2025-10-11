package com.example.e_commerce_techshop.services.user;

import com.example.e_commerce_techshop.dtos.UserDTO;
import com.example.e_commerce_techshop.dtos.UserLoginDTO;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.user.UserResponse;

public interface IUserService {
    String loginUser(UserLoginDTO userLoginDTO) throws Exception;

    void createUser(UserDTO userDTO, String siteURL) throws Exception;

    boolean verifyUser(String verificationCode);

    User getUserByToken(String token) throws Exception;

    void updateResetPasswordToken(String token, String email) throws Exception;

    User getUserByResetPasswordToken(String token);

    void updatePassword(User user, String newPassword);

    UserResponse getCurrentUser(String email) throws Exception;
}
