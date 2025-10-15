package com.example.e_commerce_techshop.controllers.buyer;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/buyer/address")
@RequiredArgsConstructor
@Tag(name = "Buyer Address Management", description = "Address management APIs for buyers - Handle delivery address creation, updates, and management")
@SecurityRequirement(name = "bearerAuth")
public class BuyerAddressController {

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
    @Operation(
        summary = "Get user address",
        description = "Retrieve the current user's delivery address information"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Address retrieved successfully or user has no address",
            content = @Content(schema = @Schema(implementation = AddressResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - error retrieving address",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
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
    @Operation(
        summary = "Create or update address",
        description = "Create a new address or update existing address for the current user. Each user can only have one address"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Address saved successfully",
            content = @Content(schema = @Schema(implementation = AddressResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - validation errors or save failed",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> createOrUpdateAddress(
            @Parameter(
                description = "Address information including street, city, province, postal code, and contact details",
                required = true,
                content = @Content(schema = @Schema(implementation = AddressDTO.class))
            )
            @Valid @RequestBody AddressDTO addressDTO,
            @Parameter(hidden = true) BindingResult result) {
        
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
    @Operation(
        summary = "Delete address",
        description = "Delete the current user's address. User will need to create a new address for future orders"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Address deleted successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - address not found or delete failed",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
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
    @Operation(
        summary = "Check if user has address",
        description = "Check whether the current user has a delivery address configured. Useful for checkout validation"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Address check completed successfully",
            content = @Content(schema = @Schema(implementation = AddressResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - error checking address",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
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