package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.dtos.UserDTO;
import com.example.e_commerce_techshop.dtos.UserLoginDTO;
import com.example.e_commerce_techshop.models.Token;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.user.LoginResponse;
import com.example.e_commerce_techshop.services.token.ITokenService;
import com.example.e_commerce_techshop.services.user.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
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
    private final IUserService userService;

    private final ITokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginDTO userLoginDTO, BindingResult result, HttpServletRequest request){
        try {
            if(result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(ApiResponse.error(errorMessages.toString()));
            }
            String token = userService.loginUser(userLoginDTO);
            String userAgent = request.getHeader("User-Agent");
            User user = userService.getUserByToken(token);
            Token jwtToken = tokenService.addToken(user, token, isMobileDevice(userAgent));
            LoginResponse loginResponse = LoginResponse.builder()
                    .message("Đăng nhập thành công")
                    .token(jwtToken.getToken())
                    .refreshToken(jwtToken.getRefreshToken())
                    .username(user.getFullName())
                    .id(user.getId())
                    .roles(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                    .build();
            return ResponseEntity.ok(ApiResponse.ok(loginResponse));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO userDTO, BindingResult result){
        try {
            if(result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
            }
            if(!userDTO.getPassword().equals(userDTO.getRetypePassword()))
                return ResponseEntity.badRequest().body(ApiResponse.error("Password doesn't match"));
            // CẦN KIỂM TRA USER ĐÃ XÁC MINH EMAIL CHƯA !!
            userService.createUser(userDTO);
            return ResponseEntity.ok(ApiResponse.ok("Đã đăng ký thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    boolean isMobileDevice(String userAgent){
        return userAgent.toLowerCase().contains("mobile");
    }
}
