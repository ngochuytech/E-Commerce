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

    @GetMapping
    @Operation(summary = "Lấy địa chỉ người dùng", description = "Lấy tất cả địa chỉ giao hàng của người dùng hiện tại")
    public ResponseEntity<?> getUserAddress(@AuthenticationPrincipal User currentUser) throws Exception {
        var addresses = addressService.getUserAddress(currentUser.getEmail());

        if (addresses == null || addresses.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ok("Bạn chưa có địa chỉ nào"));
        }

        return ResponseEntity.ok(ApiResponse.ok(addresses));
    }

    @GetMapping("/check")
    @Operation(summary = "Kiểm tra người dùng có địa chỉ", description = "Kiểm tra xem người dùng hiện tại đã cấu hình địa chỉ giao hàng chưa. Hữu ích cho việc xác thực khi thanh toán")
    public ResponseEntity<?> checkHasAddress(@AuthenticationPrincipal User currentUser) throws Exception {
        boolean hasAddress = addressService.hasAddress(currentUser.getEmail());

        return ResponseEntity.ok(ApiResponse.ok(
                AddressResponse.check("Kiểm tra địa chỉ thành công", hasAddress, hasAddress ? 1L : 0L)));

    }

    @PostMapping
    @Operation(summary = "Tạo địa chỉ mới", description = "Tạo địa chỉ giao hàng mới cho người dùng hiện tại")
    public ResponseEntity<?> createAddress(
            @Parameter(description = "Thông tin địa chỉ bao gồm đường, thành phố, tỉnh, mã bưu điện và thông tin liên hệ", required = true, content = @Content(schema = @Schema(implementation = CreateAddressDTO.class))) @Valid @RequestBody CreateAddressDTO createAddressDTO,
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

    @PutMapping("/{addressId}")
    @Operation(summary = "Cập nhật địa chỉ", description = "Cập nhật một địa chỉ hiện có theo ID (chỉ số trong danh sách, bắt đầu từ 0)")
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

    @DeleteMapping("/{addressId}")
    @Operation(summary = "Xóa địa chỉ", description = "Xóa một địa chỉ cụ thể theo ID (chỉ số trong danh sách, bắt đầu từ 0). Người dùng có thể có nhiều địa chỉ và xóa từng địa chỉ riêng lẻ")
    public ResponseEntity<?> deleteAddress(
            @Parameter(description = "ID địa chỉ (chỉ số trong danh sách, bắt đầu từ 0)", example = "0") @PathVariable String addressId,
            @AuthenticationPrincipal User currentUser) throws Exception {
        addressService.deleteAddress(currentUser, addressId);

        return ResponseEntity.ok(ApiResponse.ok("Xóa địa chỉ thành công"));
    }
}