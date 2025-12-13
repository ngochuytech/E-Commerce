package com.example.e_commerce_techshop.controllers.admin;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_commerce_techshop.dtos.shipper.ShipperRegisterDTO;
import com.example.e_commerce_techshop.dtos.shipper.ShipperUpdateInfoDTO;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.user.UserResponse;
import com.example.e_commerce_techshop.services.user.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/admin/shipper")
@RequiredArgsConstructor
@Validated
@Tag(name = "Admin - Shipper Management", description = "APIs for admin to manage shipper accounts")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminShipperController {

    private final IUserService userService;

    @GetMapping("")
    @Operation(summary = "Lấy danh sách shipper", description = "Lấy danh sách tất cả tài khoản shipper với phân trang và filter")
    public ResponseEntity<?> getAllShippers(
            @Parameter(description = "Filter by name") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by email") @RequestParam(required = false) String email,
            @Parameter(description = "Filter by phone") @RequestParam(required = false) String phone,
            @Parameter(description = "Filter by status (active/banned)") @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir) {

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());

        Page<User> shippers = userService.getAllShippers(name, email, phone, status, pageable);
        Page<UserResponse> responsePage = shippers.map(UserResponse::fromUser);

        return ResponseEntity.ok(ApiResponse.ok(responsePage));
    }

    @GetMapping("/{shipperId}")
    @Operation(summary = "Lấy chi tiết shipper", description = "Lấy thông tin chi tiết của một shipper")
    public ResponseEntity<?> getShipperDetail(
            @Parameter(description = "Shipper ID", required = true) @PathVariable String shipperId) throws Exception {

        User shipper = userService.getUserById(shipperId);

        // Kiểm tra có phải shipper không
        if (!shipper.getRoles().contains("SHIPPER")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Người dùng này không phải là shipper"));
        }

        return ResponseEntity.ok(ApiResponse.ok(UserResponse.fromUser(shipper)));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Thống kê shipper", description = "Lấy thống kê tổng quan về shipper (tổng số, activate, banned)")
    public ResponseEntity<?> getShipperStatistics() {

        Map<String, Long> stats = userService.getShipperStatistics();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo tài khoản shipper", description = "Admin tạo tài khoản mới cho shipper với avatar")
    public ResponseEntity<?> createShipper(
            @Parameter(description = "Thông tin shipper", required = true) @RequestPart("dto") @Valid ShipperRegisterDTO registerDTO,
            @Parameter(description = "File ảnh đại diện cho shipper", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart(value = "avatar", required = false) MultipartFile avatarFile,
            HttpServletRequest request) throws Exception {

        userService.createShipperAccount(registerDTO, avatarFile, getSiteURL(request));

        return ResponseEntity.ok(ApiResponse.ok("Tạo tài khoản shipper thành công"));
    }

    @PutMapping("/shipper/{shipperId}")
    @Operation(summary = "Cập nhật thông tin shipper", description = "Admin cập nhật thông tin cho tài khoản shipper")
    public ResponseEntity<?> updateShipperInfo(@RequestBody @Valid ShipperUpdateInfoDTO updateDTO,
            @Parameter(description = "Shipper ID", required = true) @PathVariable String shipperId) throws Exception {

        User shipper = userService.getUserById(shipperId);

        if (!shipper.getRoles().contains("SHIPPER")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Người dùng này không phải là shipper"));
        }

        userService.updateShipperInfo(shipperId, updateDTO);

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thông tin shipper thành công"));
    }

    @PutMapping("/{shipperId}/activate")
    @Operation(summary = "Kích hoạt tài khoản shipper", description = "Admin kích hoạt lại tài khoản shipper")
    public ResponseEntity<?> activateShipper(
            @Parameter(description = "Shipper ID", required = true) @PathVariable String shipperId) throws Exception {

        userService.activateShipper(shipperId);
        return ResponseEntity.ok(
                "Đã kích hoạt tài khoản shipper");
    }

    /**
     * Reset mật khẩu cho shipper
     */
    @PostMapping("/{shipperId}/reset-password")
    @Operation(summary = "Reset mật khẩu shipper", description = "Admin reset mật khẩu cho shipper về mật khẩu ngẫu nhiên")
    public ResponseEntity<?> resetShipperPassword(
            @Parameter(description = "Shipper ID", required = true) @PathVariable String shipperId) throws Exception {

        User shipper = userService.getUserById(shipperId);

        if (!shipper.getRoles().contains("SHIPPER")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Người dùng này không phải là shipper"));
        }

        // Generate mật khẩu ngẫu nhiên
        String newPassword = generateRandomPassword(12);
        
        // Reset mật khẩu
        userService.updatePassword(shipper, newPassword);

        // Trả về mật khẩu mới cho admin
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã reset mật khẩu thành công");
        response.put("newPassword", newPassword);
        response.put("email", shipper.getEmail());
        response.put("note", "Vui lòng gửi mật khẩu mới này cho shipper");

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Generate mật khẩu ngẫu nhiên an toàn
     */
    private String generateRandomPassword(int length) {
        final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
        final String DIGITS = "0123456789";
        final String SPECIAL_CHARS = "@#$%&*!";
        final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARS;
        
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);
        
        // Đảm bảo có ít nhất 1 ký tự của mỗi loại
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));
        
        // Fill phần còn lại
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }
        
        // Shuffle để random vị trí
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }

    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }
}
