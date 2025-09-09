package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.dtos.UserLoginDTO;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private IUserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginDTO userLoginDTO, BindingResult result){
        try {
            if(result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(ApiResponse.error(errorMessages.toString()));
            }
            User user = userService.loginUser(userLoginDTO);
            return ResponseEntity.ok("Login thanh cong");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
