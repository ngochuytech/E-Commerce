package com.example.e_commerce_techshop.controllers.buyer;

import com.example.e_commerce_techshop.dtos.buyer.address.AddressDTO;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.buyer.AddressResponse;
import com.example.e_commerce_techshop.services.address.IAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/buyer/address")
@RequiredArgsConstructor
@Tag(name = "Buyer Address Management", description = "Address management APIs for buyers - Handle delivery address creation, updates, and management")
@SecurityRequirement(name = "bearerAuth")
public class BuyerAddressController {

    private final IAddressService addressService;

    /**
     * Lấy địa chỉ của user hiện tại
     * GET /api/v1/buyer/address
     */
    @GetMapping
    @Operation(summary = "Get user address", description = "Retrieve the current user's delivery address information")
    public ResponseEntity<?> getUserAddress(@AuthenticationPrincipal User currentUser) throws Exception {
        AddressDTO address = addressService.getUserAddress(currentUser.getEmail());

        if (address == null) {
            return ResponseEntity.ok(ApiResponse.ok(
                    AddressResponse.message("Bạn chưa có địa chỉ nào")));
        }

        return ResponseEntity.ok(ApiResponse.ok(
                AddressResponse.single("Lấy địa chỉ thành công", address)));

    }

    /**
     * Tạo hoặc cập nhật địa chỉ
     * POST /api/v1/buyer/address
     */
    @PostMapping
    @Operation(summary = "Create or update address", description = "Create a new address or update existing address for the current user. Each user can only have one address")
    public ResponseEntity<?> createOrUpdateAddress(
            @Parameter(description = "Address information including street, city, province, postal code, and contact details", required = true, content = @Content(schema = @Schema(implementation = AddressDTO.class))) @Valid @RequestBody AddressDTO addressDTO,
            @AuthenticationPrincipal User currentUser,
            @Parameter(hidden = true) BindingResult result) throws Exception {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Dữ liệu không hợp lệ: " +
                            result.getFieldError().getDefaultMessage()));
        }

        AddressDTO savedAddress = addressService.createOrUpdateAddress(currentUser.getEmail(), addressDTO);

        return ResponseEntity.ok(ApiResponse.ok(
                AddressResponse.single("Lưu địa chỉ thành công", savedAddress)));

    }

    /**
     * Xóa địa chỉ
     * DELETE /api/v1/buyer/address
     */
    @DeleteMapping
    @Operation(summary = "Delete address", description = "Delete the current user's address. User will need to create a new address for future orders")
    public ResponseEntity<?> deleteAddress(@AuthenticationPrincipal User currentUser) throws Exception {
        addressService.deleteAddress(currentUser.getEmail());

        return ResponseEntity.ok(ApiResponse.ok("Xóa địa chỉ thành công"));

    }

    /**
     * Kiểm tra user có địa chỉ không
     * GET /api/v1/buyer/address/check
     */
    @GetMapping("/check")
    @Operation(summary = "Check if user has address", description = "Check whether the current user has a delivery address configured. Useful for checkout validation")
    public ResponseEntity<?> checkHasAddress(@AuthenticationPrincipal User currentUser) throws Exception {
        boolean hasAddress = addressService.hasAddress(currentUser.getEmail());

        return ResponseEntity.ok(ApiResponse.ok(
                AddressResponse.check("Kiểm tra địa chỉ thành công", hasAddress, hasAddress ? 1L : 0L)));

    }
}