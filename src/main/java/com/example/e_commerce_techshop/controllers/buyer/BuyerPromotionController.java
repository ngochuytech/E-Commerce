package com.example.e_commerce_techshop.controllers.buyer;

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

import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.PromotionResponse;
import com.example.e_commerce_techshop.services.promotion.IPromotionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/buyer/promotions")
@RequiredArgsConstructor
@Tag(name = "Buyer Promotion APIs", description = "API cho quản lý khuyến mãi của người mua")
public class BuyerPromotionController {
    private final IPromotionService promotionService;

    @GetMapping("/store/{storeId}/available")
    @Operation(summary = "Lấy danh sách khuyến mãi đang hoạt động theo cửa hàng cho khách hàng", description = "Lấy tất cả các khuyến mãi đang hoạt động hiện tại cho một cửa hàng cụ thể")
    public ResponseEntity<?> getActivePromotionsByStore(
            @Parameter(description = "ID của cửa hàng (tùy chọn, null để chỉ lấy khuyến mãi của nền tảng)") @PathVariable("storeId") String storeId,
            @Parameter(description = "Giá trị đơn hàng để kiểm tra các khuyến mãi áp dụng", required = true, example = "500000") @RequestParam Long orderValue,
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotionResponses = promotionService.getStorePromotionsForCustomer(storeId, orderValue,
                currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotionResponses));
    }

    @GetMapping("/platform/available")
    @Operation(summary = "Lấy tất cả khuyến mãi nền tảng với trạng thái ACTIVE", description = "Lấy tất cả các khuyến mãi nền tảng đang hoạt động")
    public ResponseEntity<?> getPlatformPromotions(
            @Parameter(description = "Order value to check applicable promotions", required = true, example = "500000") @RequestParam Long orderValue,
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotionResponses = promotionService.getPlatformPromotionsForCustomer(orderValue, user,
                pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotionResponses));
    }
}
