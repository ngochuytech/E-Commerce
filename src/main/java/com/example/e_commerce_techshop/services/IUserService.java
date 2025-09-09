package com.example.e_commerce_techshop.services;

import com.example.e_commerce_techshop.dtos.UserDTO;
import com.example.e_commerce_techshop.dtos.UserLoginDTO;
import com.example.e_commerce_techshop.models.User;

public interface IUserService {
    User loginUser(UserLoginDTO userLoginDTO) throws Exception;

    User createUser(UserDTO userDTO) throws Exception;
}
