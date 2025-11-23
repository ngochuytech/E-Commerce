package com.example.e_commerce_techshop.controllers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
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
import jakarta.servlet.http.Cookie;
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
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
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

        Cookie refreshTokenCookie = new Cookie("refreshToken", jwtToken.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
        refreshTokenCookie.setAttribute("SameSite", "Strict"); // Chống CSRF
        response.addCookie(refreshTokenCookie);

        List<String> roles = user.getAuthorities() != null
                ? user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
                : List.of();

        LoginResponse loginResponse = LoginResponse.builder()
                .message("Đăng nhập thành công")
                .token(jwtToken.getToken())
                .username(user.getFullName())
                .id(user.getId())
                .roles(roles)
                .isActive(user.getIsActive())
                .enable(user.getEnable())
                .build();
        return ResponseEntity.ok(ApiResponse.ok(loginResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and revoke refresh token")
    public ResponseEntity<?> logout(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            String refreshToken = Arrays.stream(cookies)
                    .filter(cookie -> "refreshToken".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
            if (refreshToken != null) {
                tokenService.revokeToken(refreshToken);
            }
        }
        Cookie deleteCookie = new Cookie("refreshToken", null);
        deleteCookie.setHttpOnly(true);
        deleteCookie.setSecure(true);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);
        response.addCookie(deleteCookie);
        return ResponseEntity.ok(ApiResponse.ok("Đăng xuất thành công"));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token from cookie")
    public ResponseEntity<?> refreshToken(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không tìm thấy refresh token"));
        }
        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
        Token tokenEntity = tokenService.findByRefreshToken(refreshToken);
        if (tokenEntity == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Refresh token không hợp lệ"));
        }
        User user = userService.getUserById(tokenEntity.getUser().getId());
        Token newToken = tokenService.refreshToken(refreshToken, user);
        Cookie refreshTokenCookie = new Cookie("refreshToken", newToken.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshTokenCookie);
        LoginResponse loginResponse = LoginResponse.builder()
                .message("Làm mới token thành công")
                .token(newToken.getToken())
                .username(user.getFullName())
                .enable(user.getEnable())
                .isActive(user.getIsActive())
                .roles(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .id(user.getId())
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
        return userAgent != null && userAgent.toLowerCase().contains("mobile");
    }

    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change user password", description = "Change the password of the currently authenticated user")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid ChangePasswordDTO changePasswordDTO) throws Exception {
        userService.changePassword(currentUser, changePasswordDTO.getCurrentPassword(),
                changePasswordDTO.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("Đổi mật khẩu thành công"));
    }

    @PostMapping("/send-verification-email")
    @Operation(summary = "Send verification email", description = "Send a verification email to the user")
    public ResponseEntity<?> sendVerificationEmail(
            HttpServletRequest request,
            @RequestParam("email") String email) throws Exception {
        userService.sendVerificationEmailAgain(email, getSiteURL(request));
        return ResponseEntity.ok(ApiResponse.ok("Đã gửi email xác minh"));
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
            HttpServletResponse response,
            @Parameter(description = "Google authorization code") @RequestBody GoogleCodeRequest request)
            throws Exception {
        String userAgent = httpServletRequest.getHeader("User-Agent");
        User user = googleAuthService.loginWithGoogle(request);
        String token = jwtTokenProvider.generateToken(user);
        Token jwtToken = tokenService.addToken(user, token, isMobileDevice(userAgent));

        Cookie refreshTokenCookie = new Cookie("refreshToken", jwtToken.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshTokenCookie);

        List<String> roles = user.getAuthorities() != null
                ? user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
                : List.of();

        LoginResponse loginResponse = LoginResponse.builder()
                .message("Đăng nhập thành công!")
                .token(jwtToken.getToken())
                .tokenType(jwtToken.getTokenType())
                .username(user.getFullName())
                .roles(roles)
                .enable(user.getEnable())
                .isActive(user.getIsActive())
                .id(user.getId())
                .build();

        return ResponseEntity.ok(ApiResponse.ok(loginResponse));
    }
}
