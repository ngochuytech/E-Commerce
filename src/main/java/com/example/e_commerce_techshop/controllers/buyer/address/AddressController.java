package com.example.e_commerce_techshop.controllers.buyer.address;

import com.example.e_commerce_techshop.dtos.buyer.address.AddressDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.buyer.AddressResponse;
import com.example.e_commerce_techshop.services.address.IAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/buyer/address")
@RequiredArgsConstructor
public class AddressController {

    private final IAddressService addressService;

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng");
        }
        return authentication.getName();
    }

    /**
     * Lấy địa chỉ của user hiện tại
     * GET /api/v1/buyer/address
     */
    @GetMapping
    public ResponseEntity<?> getUserAddress() {
        try {
            String userEmail = getCurrentUserEmail();
            AddressDTO address = addressService.getUserAddress(userEmail);
            
            if (address == null) {
                return ResponseEntity.ok(ApiResponse.ok(
                        AddressResponse.message("Bạn chưa có địa chỉ nào")));
            }
            
            return ResponseEntity.ok(ApiResponse.ok(
                    AddressResponse.single("Lấy địa chỉ thành công", address)));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy địa chỉ: " + e.getMessage()));
        }
    }

    /**
     * Tạo hoặc cập nhật địa chỉ
     * POST /api/v1/buyer/address
     */
    @PostMapping
    public ResponseEntity<?> createOrUpdateAddress(
            @Valid @RequestBody AddressDTO addressDTO,
            BindingResult result) {
        
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Dữ liệu không hợp lệ: " + 
                            result.getFieldError().getDefaultMessage())
            );
        }

        try {
            String userEmail = getCurrentUserEmail();
            AddressDTO savedAddress = addressService.createOrUpdateAddress(userEmail, addressDTO);
            
            return ResponseEntity.ok(ApiResponse.ok(
                    AddressResponse.single("Lưu địa chỉ thành công", savedAddress)));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lưu địa chỉ: " + e.getMessage()));
        }
    }

    /**
     * Xóa địa chỉ
     * DELETE /api/v1/buyer/address
     */
    @DeleteMapping
    public ResponseEntity<?> deleteAddress() {
        try {
            String userEmail = getCurrentUserEmail();
            addressService.deleteAddress(userEmail);
            
            return ResponseEntity.ok(ApiResponse.ok("Xóa địa chỉ thành công"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi xóa địa chỉ: " + e.getMessage()));
        }
    }

    /**
     * Kiểm tra user có địa chỉ không
     * GET /api/v1/buyer/address/check
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkHasAddress() {
        try {
            String userEmail = getCurrentUserEmail();
            boolean hasAddress = addressService.hasAddress(userEmail);
            
            return ResponseEntity.ok(ApiResponse.ok(
                    AddressResponse.check("Kiểm tra địa chỉ thành công", hasAddress, hasAddress ? 1L : 0L)));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi kiểm tra địa chỉ: " + e.getMessage()));
        }
    }
}