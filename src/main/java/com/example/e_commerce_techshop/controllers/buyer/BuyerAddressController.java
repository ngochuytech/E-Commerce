package com.example.e_commerce_techshop.controllers.buyer;

import com.example.e_commerce_techshop.annotations.RequireActiveAccount;
import com.example.e_commerce_techshop.dtos.buyer.address.CreateAddressDTO;
import com.example.e_commerce_techshop.dtos.buyer.address.UpdateAddressDTO;
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
@RequireActiveAccount
@Tag(name = "Buyer Address Management", description = "Address management APIs for buyers - Handle delivery address creation, updates, and management")
@SecurityRequirement(name = "bearerAuth")
public class BuyerAddressController {

    private final IAddressService addressService;

    /**
     * Lấy địa chỉ của user hiện tại
     * GET /api/v1/buyer/address
     */
    @GetMapping
    @Operation(summary = "Get user addresses", description = "Retrieve all delivery addresses of the current user")
    public ResponseEntity<?> getUserAddress(@AuthenticationPrincipal User currentUser) throws Exception {
        var addresses = addressService.getUserAddress(currentUser.getEmail());

        if (addresses == null || addresses.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ok("Bạn chưa có địa chỉ nào"));
        }

        return ResponseEntity.ok(ApiResponse.ok(addresses));
    }

    /**
     * Tạo địa chỉ mới
     * POST /api/v1/buyer/address
     */
    @PostMapping
    @Operation(summary = "Create new address", description = "Create a new delivery address for the current user")
    public ResponseEntity<?> createAddress(
            @Parameter(description = "Address information including street, city, province, postal code, and contact details", required = true, content = @Content(schema = @Schema(implementation = CreateAddressDTO.class))) @Valid @RequestBody CreateAddressDTO createAddressDTO,
            @AuthenticationPrincipal User currentUser,
            @Parameter(hidden = true) BindingResult result) throws Exception {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Dữ liệu không hợp lệ: " +
                            result.getFieldError().getDefaultMessage()));
        }

        addressService.createAddress(currentUser, createAddressDTO);

        return ResponseEntity.ok(ApiResponse.ok("Tạo địa chỉ thành công"));
    }

    /**
     * Cập nhật địa chỉ
     * PUT /api/v1/buyer/address/{addressId}
     */
    @PutMapping("/{addressId}")
    @Operation(summary = "Update address", description = "Update an existing address by ID (index)")
    public ResponseEntity<?> updateAddress(
            @Parameter(description = "Address ID (index in the list, starting from 0)", example = "0") @PathVariable String addressId,
            @Parameter(description = "Updated address information", required = true, content = @Content(schema = @Schema(implementation = UpdateAddressDTO.class))) @Valid @RequestBody UpdateAddressDTO updateAddressDTO,
            @AuthenticationPrincipal User currentUser,
            @Parameter(hidden = true) BindingResult result) throws Exception {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Dữ liệu không hợp lệ: " +
                            result.getFieldError().getDefaultMessage()));
        }

        addressService.updateAddress(currentUser, addressId, updateAddressDTO);

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật địa chỉ thành công"));
    }

    /**
     * Xóa địa chỉ
     * DELETE /api/v1/buyer/address/{addressId}
     */
    @DeleteMapping("/{addressId}")
    @Operation(summary = "Delete address", description = "Delete a specific address by ID (index). User can have multiple addresses and delete them individually")
    public ResponseEntity<?> deleteAddress(
            @Parameter(description = "Address ID (index in the list, starting from 0)", example = "0") @PathVariable String addressId,
            @AuthenticationPrincipal User currentUser) throws Exception {
        addressService.deleteAddress(currentUser, addressId);

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