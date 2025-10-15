package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.components.JwtTokenProvider;
import com.example.e_commerce_techshop.dtos.GoogleCodeRequest;
import com.example.e_commerce_techshop.dtos.UserDTO;
import com.example.e_commerce_techshop.dtos.UserLoginDTO;
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@Tag(name = "User Management", description = "APIs for user registration, authentication and profile management")
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    private final ITokenService tokenService;

    private final GoogleAuthService googleAuthService;

    private final JwtTokenProvider jwtTokenProvider;

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng");
        }
        return authentication.getName(); // Returns email
    }

    @GetMapping("/current")
    @Operation(summary = "Get current user profile", 
               description = "Retrieve profile information of the currently authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User profile retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "User not authenticated or not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> getCurrentUser() {
        try {
            String email = getCurrentUserEmail();
            UserResponse userResponse = userService.getCurrentUser(email);
            return ResponseEntity.ok(ApiResponse.ok(userResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "User login", 
               description = "Authenticate user with email and password, returns JWT token")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid credentials or validation errors",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> login(
            @Parameter(description = "User login credentials")
            @RequestBody @Valid UserLoginDTO userLoginDTO, 
            BindingResult result, 
            HttpServletRequest request){
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
    @Operation(summary = "User registration", 
               description = "Register a new user account, sends email verification")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Registration successful, verification email sent",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Validation errors or user already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> register(
            HttpServletRequest request, 
            @Parameter(description = "User registration information")
            @RequestBody @Valid UserDTO userDTO, 
            BindingResult result){
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
            userService.createUser(userDTO, getSiteURL(request));
            return ResponseEntity.ok(ApiResponse.ok("Đã đăng ký thành công! Cần xác minh email"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify user email", 
               description = "Verify user account using verification code sent via email")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Account verified successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid verification code or account already verified",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> verify(
            @Parameter(description = "Email verification code", example = "abc123def456")
            @RequestParam("code") String code){
        if(userService.verifyUser(code))
            return ResponseEntity.ok(ApiResponse.ok("Đã xác minh tài khoản thành công!"));
        else
            return ResponseEntity.badRequest().body(ApiResponse.error("Xác minh tài khoản thất bại! Do tài khoản của" +
                    "bạn đã được xác minh hoặc mã code xác nhận không chính xác."));
    }

    private boolean isMobileDevice(String userAgent){
        return userAgent.toLowerCase().contains("mobile");
    }

    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }


    public void socialAuth(HttpServletResponse response) throws IOException {
        String url = googleAuthService.generateAuthUrl();
        response.sendRedirect(url);
    }

    @PostMapping("/auth/social/callback")
    @Operation(summary = "Google OAuth callback", 
               description = "Handle Google OAuth authentication callback and login user")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Google login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Google authentication failed"
        )
    })
    public ResponseEntity<?> callback(
            HttpServletRequest httpServletRequest,
            @Parameter(description = "Google authorization code")
            @RequestBody GoogleCodeRequest request){
        try {
            String userAgent = httpServletRequest.getHeader("User-Agent");
            User user = googleAuthService.loginWithGoogle(request);
            String token = jwtTokenProvider.generateToken(user);
            Token jwtToken = tokenService.addToken(user, token, isMobileDevice(userAgent));

            LoginResponse loginResponse = LoginResponse.builder()
                    .message("Đăng nhập thành công!")
                    .token(jwtToken.getToken())
                    .tokenType(jwtToken.getTokenType())
                    .refreshToken(jwtToken.getRefreshToken())
                    .username(user.getFullName())
                    .roles(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                    .id(user.getId())
                    .build();

            return ResponseEntity.ok(ApiResponse.ok(loginResponse));
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
