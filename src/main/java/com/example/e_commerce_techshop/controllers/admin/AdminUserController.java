package com.example.e_commerce_techshop.controllers.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.dtos.admin.user.BanUserDTO;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.user.UserResponse;
import com.example.e_commerce_techshop.services.user.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "APIs for admin to manage users")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminUserController {
    private final IUserService userService;

        @Operation(summary = "Get users (admin)", description = "Retrieve paginated list of users with optional filters")
        @GetMapping("")
        public ResponseEntity<?> getAllUsers(
                        @Parameter(description = "Filter by full name") @RequestParam(required = false) String userName,
                        @Parameter(description = "Filter by email") @RequestParam(required = false) String userEmail,
                        @Parameter(description = "Filter by phone") @RequestParam(required = false) String userPhone,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users = userService.getAllUsers(userName, userEmail, userPhone, pageable);
        Page<UserResponse> userResponses = users.map(UserResponse::fromUser);
        return ResponseEntity.ok(ApiResponse.ok(userResponses));
    }

    @Operation(summary = "Ban a user", description = "Ban a user (temporary or permanent) by admin")
    @PostMapping("/ban")
    public ResponseEntity<?> banUser(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "BanUser payload", required = true)
            @RequestBody BanUserDTO banUserDTO,
            @AuthenticationPrincipal User admin) throws Exception{
           userService.banUser(admin.getId(), banUserDTO);
            return ResponseEntity.ok(ApiResponse.ok("Chặn người dùng thành công"));
    }

    @Operation(summary = "Unban a user", description = "Remove ban for a user")
    @PostMapping("/unban/{userId}")
    public ResponseEntity<?> unbanUser(
            @Parameter(description = "ID of user to unban") @PathVariable String userId,
            @AuthenticationPrincipal User admin) throws Exception{
            userService.unbanUser(admin.getId(), userId);

            return ResponseEntity.ok(ApiResponse.ok("Mở chặn người dùng thành công!"));
    }

        @Operation(summary = "Check ban status", description = "Check whether a user is currently banned")
        @GetMapping("/check-ban/{userId}")
        public ResponseEntity<?> checkBanStatus(@Parameter(description = "User ID") @PathVariable String userId) {
                boolean isBanned = userService.isUserBanned(userId);
                return ResponseEntity.ok(ApiResponse.ok("Trạng thái người dùng: " + (isBanned ? "Đã bị chặn" : "Đang hoạt động")));
        }
}
