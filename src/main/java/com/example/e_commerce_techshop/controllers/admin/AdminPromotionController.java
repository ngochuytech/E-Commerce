package com.example.e_commerce_techshop.controllers.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import com.example.e_commerce_techshop.dtos.b2c.promotion.CreatePromotionDTO;
import com.example.e_commerce_techshop.dtos.b2c.promotion.PromotionDTO;
import com.example.e_commerce_techshop.models.Promotion;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.PromotionResponse;
import com.example.e_commerce_techshop.services.promotion.IPromotionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/admin/promotions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Promotion Management", description = "Promotion management APIs for Platform Admin - Create platform-wide promotions")
@SecurityRequirement(name = "bearerAuth")
public class AdminPromotionController {
    private final IPromotionService promotionService;

    // View all promotions (Admin oversight)
    @GetMapping
    @Operation(summary = "Lấy tất cả khuyến mãi (Admin)", description = "Lấy danh sách tất cả khuyến mãi trên nền tảng")
    public ResponseEntity<?> getAllPromotions(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Promotion> promotions = promotionService.getAllPromotions(pageable);
        Page<PromotionResponse> promotionResponses = promotions.map(PromotionResponse::fromPromotion);
        return ResponseEntity.ok(ApiResponse.ok(promotionResponses));
    }

    @GetMapping("/{promotionId}")
    @Operation(summary = "Lấy khuyến mãi theo ID (Admin)", description = "Lấy thông tin chi tiết của một khuyến mãi cụ thể - Xem bởi Admin")
    public ResponseEntity<?> getPromotionById(
            @Parameter(description = "ID of the promotion to retrieve", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId)
            throws Exception {
        Promotion promotion = promotionService.getPromotionById(promotionId);
        return ResponseEntity.ok(ApiResponse.ok(PromotionResponse.fromPromotion(promotion)));
    }

    @GetMapping("/reports/expired")
    @Operation(summary = "Lấy khuyến mãi đã hết hạn (Admin)", description = "Lấy danh sách các khuyến mãi trên nền tảng đã hết hạn - Xem bởi Admin")
    public ResponseEntity<?> getExpiredPromotions(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getExpiredPlatformPromotions(pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/reports/deleted")
    @Operation(summary = "Lấy khuyến mãi đã bị xóa (Admin)", description = "Lấy danh sách các khuyến mãi trên nền tảng đã bị xóa mềm - Xem bởi Admin")
    public ResponseEntity<?> getDeletedPromotions(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getDeletedPlatformPromotions(pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/reports/active")
    @Operation(summary = "Lấy khuyến mãi đang hoạt động (Admin)", description = "Lấy danh sách các khuyến mãi trên nền tảng đang hoạt động - Xem bởi Admin")
    public ResponseEntity<?> getActivePromotions(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getActivePlatformPromotions(pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/reports/inactive")
    @Operation(summary = "Lấy khuyến mãi không hoạt động (Admin)", description = "Lấy danh sách các khuyến mãi trên nền tảng không hoạt động - Xem bởi Admin")
    public ResponseEntity<?> getInactivePromotions(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getInactivePlatformPromotions(pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/reports/type/{type}")
    @Operation(summary = "Lấy khuyến mãi theo loại (Admin)", description = "Lấy danh sách tất cả các khuyến mãi theo loại cụ thể - Phân tích bởi Admin")
    public ResponseEntity<?> getPromotionsByType(
            @Parameter(description = "Loại khuyến mãi để lọc", required = true, example = "PERCENTAGE") @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getPromotionsByType(type, pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @PostMapping("/platform")
    @Operation(summary = "Tạo khuyến mãi nền tảng", description = "Tạo khuyến mãi trên toàn nền tảng. Người tạo sẽ được tự động gán là PLATFORM và storeId sẽ là null. Chỉ admin nền tảng mới có thể sử dụng.")
    public ResponseEntity<?> createPlatformPromotion(
            @Parameter(description = "Thông tin khuyến mãi (issuer và storeId không bắt buộc - tự động gán)", required = true, content = @Content(schema = @Schema(implementation = CreatePromotionDTO.class))) @Valid @RequestBody CreatePromotionDTO createPromotionDTO,
            @Parameter(hidden = true) BindingResult result) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
        }

        promotionService.createPlatformPromotion(createPromotionDTO);
        return ResponseEntity.ok(ApiResponse.ok("Tạo khuyến mãi thành công"));
    }

    @PutMapping("/platform/{promotionId}")
    @Operation(summary = "Cập nhật khuyến mãi nền tảng", description = "Cập nhật một khuyến mãi trên toàn nền tảng. Chỉ admin nền tảng mới có thể sử dụng.")
    public ResponseEntity<?> updatePlatformPromotion(
            @Parameter(description = "ID của khuyến mãi cần cập nhật", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(description = "Thông tin khuyến mãi", required = true, content = @Content(schema = @Schema(implementation = PromotionDTO.class))) @Valid @RequestBody PromotionDTO promotionDTO,
            @Parameter(hidden = true) BindingResult result) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
        }

        promotionService.updatePlatformPromotion(promotionId, promotionDTO);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật khuyến mãi thành công"));
    }

    // Promotion Status Management
    @PutMapping("/{promotionId}/activate")
    @Operation(summary = "Kích hoạt khuyến mãi (Admin)", description = "Kích hoạt bất kỳ khuyến mãi nào để nó có thể được sử dụng")
    public ResponseEntity<?> activatePromotion(
            @Parameter(description = "ID của khuyến mãi cần kích hoạt", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId)
            throws Exception {
        promotionService.activatePromotion(promotionId);
        return ResponseEntity.ok(ApiResponse.ok("Kích hoạt khuyến mãi thành công!"));
    }

    @PutMapping("/{promotionId}/deactivate")
    @Operation(summary = "Vô hiệu hóa khuyến mãi (Admin)", description = "Vô hiệu hóa bất kỳ khuyến mãi nào để ngăn nó được sử dụng")
    public ResponseEntity<?> deactivatePromotion(
            @Parameter(description = "ID của khuyến mãi cần vô hiệu hóa", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId)
            throws Exception {
        promotionService.deactivatePromotion(promotionId);
        return ResponseEntity.ok(ApiResponse.ok("Vô hiệu hóa khuyến mãi thành công!"));
    }

    // Promotion Management
    @DeleteMapping("/{promotionId}")
    @Operation(summary = "Xóa khuyến mãi (Admin)", description = "Xóa vĩnh viễn bất kỳ khuyến mãi nào - nền tảng hoặc cửa hàng")
    public ResponseEntity<?> deletePromotion(
            @Parameter(description = "ID của khuyến mãi cần xóa", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId)
            throws Exception {
        promotionService.deletePromotion(promotionId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa khuyến mãi thành công!"));
    }

}
