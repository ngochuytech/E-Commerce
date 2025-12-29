package com.example.e_commerce_techshop.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.models.Promotion;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.PromotionResponse;
import com.example.e_commerce_techshop.services.promotion.IPromotionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/promotions")
@RequiredArgsConstructor
@Tag(name = "Public Promotion APIs", description = "API công khai cho khuyến mãi")
public class PromotionController {

    private final IPromotionService promotionService;

    @GetMapping("")
    @Operation(summary = "Lấy tất cả khuyến mãi đang hoạt động cho khách hàng", description = "Lấy tất cả các khuyến mãi đang hoạt động trên tất cả các cửa hàng và nền tảng")
    public ResponseEntity<?> getActivePromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Promotion> promotions = promotionService.getAllPromotionForCustomer(pageable);
        Page<PromotionResponse> promotionResponses = promotions.map(PromotionResponse::fromPromotion);
        return ResponseEntity.ok(ApiResponse.ok(promotionResponses));
    }

    @GetMapping("/{promotionId}")
    @Operation(summary = "Lấy khuyến mãi theo ID", description = "Lấy thông tin chi tiết của một khuyến mãi cụ thể")
    public ResponseEntity<?> getPromotionById(
            @Parameter(description = "ID của khuyến mãi cần lấy", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId)
            throws Exception {
        Promotion promotion = promotionService.getPromotionById(promotionId);
        return ResponseEntity.ok(ApiResponse.ok(PromotionResponse.fromPromotion(promotion)));
    }

    @GetMapping("/code/{promotionCode}")
    @Operation(summary = "Lấy khuyến mãi theo mã", description = "Lấy thông tin chi tiết của một khuyến mãi cụ thể theo mã của nó")
    public ResponseEntity<?> getPromotionByCode(
            @Parameter(description = "Mã của khuyến mãi cần lấy", required = true, example = "BLACKFRIDAY") @PathVariable String promotionCode)
            throws Exception {
        Promotion promotion = promotionService.getPromotionByCode(promotionCode);
        return ResponseEntity.ok(ApiResponse.ok(PromotionResponse.fromPromotion(promotion)));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Lấy khuyến mãi theo loại", description = "Lấy tất cả các khuyến mãi đang hoạt động theo một loại cụ thể (PERCENTAGE, FIXED_AMOUNT, v.v.)")
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

    @GetMapping("/validate/{promotionId}")
    @Operation(summary = "Xác thực khuyến mãi", description = "Kiểm tra xem khuyến mãi có thể áp dụng cho đơn hàng với giá trị cụ thể hay không")
    public ResponseEntity<?> validatePromotion(
            @Parameter(description = "ID of the promotion to validate", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(description = "Order value to validate against promotion conditions", required = true, example = "500000") @RequestParam Long orderValue,
            @AuthenticationPrincipal User currentUser) throws Exception {
        promotionService.validatePromotionForUser(promotionId, orderValue, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Khuyến mãi hợp lệ và có thể áp dụng cho đơn hàng."));
    }

    @GetMapping("/calculate-discount/{promotionId}")
    @Operation(summary = "Tính toán số tiền giảm giá", description = "Tính toán chính xác số tiền giảm giá cho một giá trị đơn hàng cụ thể")
    public ResponseEntity<?> calculateDiscount(
            @Parameter(description = "ID of the promotion", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(description = "Order value to calculate discount for", required = true, example = "500000") @RequestParam Long orderValue)
            throws Exception {
        Long discount = promotionService.calculateDiscount(promotionId, orderValue);
        return ResponseEntity.ok(ApiResponse.ok(discount));
    }
}
