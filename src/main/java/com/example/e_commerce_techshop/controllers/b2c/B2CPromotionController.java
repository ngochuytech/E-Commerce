package com.example.e_commerce_techshop.controllers.b2c;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.dtos.b2c.promotion.CreatePromotionDTO;
import com.example.e_commerce_techshop.dtos.b2c.promotion.PromotionDTO;
import com.example.e_commerce_techshop.models.Promotion;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.PromotionResponse;
import com.example.e_commerce_techshop.services.promotion.IPromotionService;
import com.example.e_commerce_techshop.services.store.IStoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/b2c/promotions")
@RequiredArgsConstructor
@Tag(name = "B2C Promotion Management", description = "Quản lý khuyến mãi cho cửa hàng B2C")
@SecurityRequirement(name = "bearerAuth")
public class B2CPromotionController {

    private final IPromotionService promotionService;
    private final IStoreService storeService;

    private void validateUserStore(String ownerId, String storeId) throws Exception {
        Store store = storeService.getStoreByIdAndOwnerId(storeId, ownerId);

        if (store == null) {
            throw new RuntimeException("Bạn không có quyền truy cập cửa hàng này");
        }
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Lấy danh sách khuyến mãi theo cửa hàng", description = "Lấy tất cả các khuyến mãi cho một cửa hàng cụ thể (bao gồm cả đang hoạt động và không hoạt động)")
    public ResponseEntity<?> getPromotionsByStore(
            @Parameter(description = "ID của cửa hàng", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getPromotionsByStore(storeId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/store/{storeId}/active")
    @Operation(summary = "Lấy danh sách khuyến mãi đang hoạt động theo cửa hàng", description = "Lấy tất cả các khuyến mãi đang hoạt động cho một cửa hàng cụ thể")
    public ResponseEntity<?> getActivePromotionsByStore(
            @Parameter(description = "ID của cửa hàng", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getPromotionsByStoreAndStatus(storeId, Promotion.PromotionStatus.ACTIVE.name(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/store/{storeId}/inactive")
    @Operation(summary = "Lấy danh sách khuyến mãi không hoạt động theo cửa hàng", description = "Lấy tất cả các khuyến mãi không hoạt động cho một cửa hàng cụ thể")
    public ResponseEntity<?> getInactivePromotionsByStore(
            @Parameter(description = "ID của cửa hàng", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getPromotionsByStoreAndStatus(storeId, Promotion.PromotionStatus.INACTIVE.name(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/store/{storeId}/expired")
    @Operation(summary = "Lấy danh sách khuyến mãi đã hết hạn theo cửa hàng", description = "Lấy tất cả các khuyến mãi đã hết hạn cho một cửa hàng cụ thể")
    public ResponseEntity<?> getExpiredPromotionsByStore(
            @Parameter(description = "ID của cửa hàng", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getExpiredPromotionsByStore(storeId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/store/{storeId}/deleted")
    @Operation(summary = "Lấy danh sách khuyến mãi đã bị xóa theo cửa hàng", description = "Lấy tất cả các khuyến mãi đã bị xóa cho một cửa hàng cụ thể")
    public ResponseEntity<?> getDeletedPromotionsByStore(
            @Parameter(description = "ID của cửa hàng", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getPromotionsByStoreAndStatus(storeId, Promotion.PromotionStatus.DELETED.name(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/store/{storeId}/count-by-status")
    @Operation(summary = "Đếm số lượng khuyến mãi theo trạng thái")
    public ResponseEntity<?> countPromotionsByStatus(
            @Parameter(description = "ID của cửa hàng", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId) throws Exception {
        Map<String, Long> counts = promotionService.countPromotionsByStatus(storeId);
        return ResponseEntity.ok(ApiResponse.ok(counts));
    }

    // Promotion Management APIs - Store specific (Auto-assign issuer=STORE)
    @PostMapping("/store/{storeId}")
    @Operation(summary = "Tạo khuyến mãi cho cửa hàng", description = "Tạo khuyến mãi cho một cửa hàng cụ thể. Issuer và storeId sẽ được tự động gán. Chỉ chủ cửa hàng mới có thể sử dụng.")
    public ResponseEntity<?> createStorePromotion(
            @Parameter(description = "ID của cửa hàng tạo khuyến mãi", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Thông tin khuyến mãi (không cần issuer và storeId)", required = true, content = @Content(schema = @Schema(implementation = CreatePromotionDTO.class))) @Valid @RequestBody CreatePromotionDTO createPromotionDTO,
            @Parameter(hidden = true) BindingResult result,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
        }

        validateUserStore(currentUser.getId(), storeId);
        // Kiểm tra shop có bị banned không
        storeService.validateStoreNotBanned(storeId);

        promotionService.createStorePromotion(createPromotionDTO, storeId);
        return ResponseEntity.ok(ApiResponse.ok("Tạo khuyến mãi thành công"));
    }

    @PutMapping("/{promotionId}")
    @Operation(summary = "Cập nhật khuyến mãi", description = "Cập nhật thông tin khuyến mãi hiện có bao gồm quy tắc giảm giá và thời gian hiệu lực")
    public ResponseEntity<?> updatePromotion(
            @Parameter(description = "ID của khuyến mãi cần cập nhật", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(description = "Thông tin khuyến mãi cập nhật", required = true, content = @Content(schema = @Schema(implementation = PromotionDTO.class))) @RequestBody PromotionDTO promotionDTO,
            @Parameter(hidden = true) BindingResult result,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
        }

        promotionService.updateStorePromotion(promotionId, promotionDTO, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật khuyến mãi thành công"));
    }

    @PutMapping("/{promotionId}/activate")
    @Operation(summary = "Kích hoạt khuyến mãi", description = "Kích hoạt một khuyến mãi để khách hàng có thể sử dụng")
    public ResponseEntity<?> activatePromotion(
            @Parameter(description = "ID của khuyến mãi cần kích hoạt", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser)
            throws Exception {
        promotionService.activateStorePromotion(promotionId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Kích hoạt khuyến mãi thành công!"));
    }

    @PutMapping("/{promotionId}/deactivate")
    @Operation(summary = "Vô hiệu hóa khuyến mãi", description = "Vô hiệu hóa một khuyến mãi để ngăn khách hàng sử dụng")
    public ResponseEntity<?> deactivatePromotion(
            @Parameter(description = "ID của khuyến mãi cần vô hiệu hóa", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser)
            throws Exception {
        promotionService.deactivateStorePromotion(promotionId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Vô hiệu hóa khuyến mãi thành công!"));
    }

    @DeleteMapping("/{promotionId}")
    @Operation(summary = "Xóa khuyến mãi", description = "Xóa một khuyến mãi (xóa vĩnh viễn khỏi hệ thống)")
    public ResponseEntity<?> deletePromotion(
            @Parameter(description = "ID của khuyến mãi cần xóa", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser)
            throws Exception {
        promotionService.deleteStorePromotion(promotionId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa khuyến mãi thành công!"));
    }

}
