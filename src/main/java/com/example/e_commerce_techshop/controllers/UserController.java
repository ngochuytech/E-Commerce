package com.example.e_commerce_techshop.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_commerce_techshop.components.JwtTokenProvider;
import com.example.e_commerce_techshop.dtos.GoogleCodeRequest;
import com.example.e_commerce_techshop.dtos.UserLoginDTO;
import com.example.e_commerce_techshop.dtos.user.ChangePasswordDTO;
import com.example.e_commerce_techshop.dtos.user.UserRegisterDTO;
import com.example.e_commerce_techshop.models.Token;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.user.LoginResponse;
import com.example.e_commerce_techshop.responses.user.UserResponse;
import com.example.e_commerce_techshop.services.GoogleAuthService;
import com.example.e_commerce_techshop.services.token.ITokenService;
import com.example.e_commerce_techshop.services.user.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "User Management", description = "APIs for user registration, authentication and profile management")
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    private final ITokenService tokenService;

    private final GoogleAuthService googleAuthService;

    private final JwtTokenProvider jwtTokenProvider;


    @GetMapping("/current")
    @Operation(summary = "Get current user profile", description = "Retrieve profile information of the currently authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user) throws Exception {
        UserResponse userResponse = UserResponse.fromUser(user);
        return ResponseEntity.ok(ApiResponse.ok(userResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with email and password, returns JWT token")
    public ResponseEntity<?> login(
            @Parameter(description = "User login credentials") @RequestBody @Valid UserLoginDTO userLoginDTO,
            BindingResult result,
            HttpServletRequest request) throws Exception {
        if (result.hasErrors()) {
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
        
        List<String> roles = user.getAuthorities() != null 
            ? user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList()
            : List.of();
        
        LoginResponse loginResponse = LoginResponse.builder()
                .message("Đăng nhập thành công")
                .token(jwtToken.getToken())
                .refreshToken(jwtToken.getRefreshToken())
                .username(user.getFullName())
                .id(user.getId())
                .roles(roles)
                .build();
        return ResponseEntity.ok(ApiResponse.ok(loginResponse));
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user account, sends email verification")
    public ResponseEntity<?> register(
            HttpServletRequest request,
            @Parameter(description = "User registration information") @RequestBody @Valid UserRegisterDTO userDTO,
            BindingResult result) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
        }
        if (!userDTO.getPassword().equals(userDTO.getRetypePassword()))
            return ResponseEntity.badRequest().body(ApiResponse.error("Password doesn't match"));
        // CẦN KIỂM TRA USER ĐÃ XÁC MINH EMAIL CHƯA !!
        userService.createUser(userDTO, getSiteURL(request));
        return ResponseEntity.ok(ApiResponse.ok("Đã đăng ký thành công! Cần xác minh email"));
    }

    @PutMapping("/avatar")
    @Operation(summary = "Update user avatar", description = "Update the avatar image of the currently authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> updateAvatar(
            @Parameter(description = "New avatar image file", required = true) @RequestParam("avatarFile") MultipartFile avatarFile,
            @AuthenticationPrincipal User currentUser) throws Exception {
        userService.updateUserAvatar(currentUser, avatarFile);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật ảnh đại diện thành công"));
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify user email", description = "Verify user account using verification code sent via email")
    public ResponseEntity<?> verify(
            @Parameter(description = "Email verification code", example = "abc123def456") @RequestParam("code") String code) {
        if (userService.verifyUser(code))
            return ResponseEntity.ok(ApiResponse.ok("Đã xác minh tài khoản thành công!"));
        else
            return ResponseEntity.badRequest().body(ApiResponse.error("Xác minh tài khoản thất bại! Do tài khoản của" +
                    "bạn đã được xác minh hoặc mã code xác nhận không chính xác."));
    }

    private boolean isMobileDevice(String userAgent) {
        return userAgent.toLowerCase().contains("mobile");
    }

    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid ChangePasswordDTO changePasswordDTO) throws Exception {
        userService.changePassword(currentUser, changePasswordDTO.getCurrentPassword(), changePasswordDTO.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("Đổi mật khẩu thành công"));
    }

    @GetMapping("/auth/social-login")
    public void socialAuth(HttpServletResponse response) throws IOException {
        String url = googleAuthService.generateAuthUrl();
        response.sendRedirect(url);
    }

    @PostMapping("/auth/social/callback")
    @Operation(summary = "Google OAuth callback", description = "Handle Google OAuth authentication callback and login user")
    public ResponseEntity<?> callback(
            HttpServletRequest httpServletRequest,
            @Parameter(description = "Google authorization code") @RequestBody GoogleCodeRequest request)
            throws Exception {
        String userAgent = httpServletRequest.getHeader("User-Agent");
        User user = googleAuthService.loginWithGoogle(request);
        String token = jwtTokenProvider.generateToken(user);
        Token jwtToken = tokenService.addToken(user, token, isMobileDevice(userAgent));

        List<String> roles = user.getAuthorities() != null 
            ? user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList()
            : List.of();

        LoginResponse loginResponse = LoginResponse.builder()
                .message("Đăng nhập thành công!")
                .token(jwtToken.getToken())
                .tokenType(jwtToken.getTokenType())
                .refreshToken(jwtToken.getRefreshToken())
                .username(user.getFullName())
                .roles(roles)
                .id(user.getId())
                .build();

        return ResponseEntity.ok(ApiResponse.ok(loginResponse));
    }
}
