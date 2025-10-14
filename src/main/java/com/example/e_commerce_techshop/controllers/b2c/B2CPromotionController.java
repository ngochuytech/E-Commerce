package com.example.e_commerce_techshop.controllers.b2c;

import com.example.e_commerce_techshop.dtos.b2c.promotion.PromotionDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.PromotionResponse;
import com.example.e_commerce_techshop.services.promotion.IPromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/b2c/promotions")
@RequiredArgsConstructor
public class B2CPromotionController {
    
    private final IPromotionService promotionService;

    // Promotion Management APIs
    @PostMapping("/create")
    public ResponseEntity<?> createPromotion(@RequestBody PromotionDTO promotionDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
            }
            
            PromotionResponse promotionResponse = promotionService.createPromotion(promotionDTO);
            return ResponseEntity.ok(ApiResponse.ok(promotionResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{promotionId}")
    public ResponseEntity<?> updatePromotion(@PathVariable String promotionId, @RequestBody PromotionDTO promotionDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
            }
            
            promotionService.updatePromotion(promotionId, promotionDTO);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật khuyến mãi thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{promotionId}")
    public ResponseEntity<?> getPromotionById(@PathVariable String promotionId) {
        try {
            PromotionResponse promotionResponse = promotionService.getPromotionById(promotionId);
            return ResponseEntity.ok(ApiResponse.ok(promotionResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPromotions() {
        List<PromotionResponse> promotions = promotionService.getAllPromotions();
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<?> getPromotionsByStore(@PathVariable String storeId) {
        List<PromotionResponse> promotions = promotionService.getPromotionsByStore(storeId);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @DeleteMapping("/{promotionId}")
    public ResponseEntity<?> deletePromotion(@PathVariable String promotionId) {
        try {
            promotionService.deletePromotion(promotionId);
            return ResponseEntity.ok(ApiResponse.ok("Xóa khuyến mãi thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Promotion Status Management APIs
    @PutMapping("/{promotionId}/activate")
    public ResponseEntity<?> activatePromotion(@PathVariable String promotionId) {
        try {
            promotionService.activatePromotion(promotionId);
            return ResponseEntity.ok(ApiResponse.ok("Kích hoạt khuyến mãi thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{promotionId}/deactivate")
    public ResponseEntity<?> deactivatePromotion(@PathVariable String promotionId) {
        try {
            promotionService.deactivatePromotion(promotionId);
            return ResponseEntity.ok(ApiResponse.ok("Vô hiệu hóa khuyến mãi thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActivePromotions() {
        List<PromotionResponse> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/active/store/{storeId}")
    public ResponseEntity<?> getActivePromotionsByStore(@PathVariable String storeId) {
        List<PromotionResponse> promotions = promotionService.getActivePromotionsByStore(storeId);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    // Promotion Validation & Application APIs
    @PostMapping("/{promotionId}/validate")
    public ResponseEntity<?> validatePromotion(@PathVariable String promotionId, @RequestParam Long orderValue) {
        try {
            promotionService.validatePromotion(promotionId, orderValue);
            return ResponseEntity.ok(ApiResponse.ok("Khuyến mãi hợp lệ!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{promotionId}/calculate-discount")
    public ResponseEntity<?> calculateDiscount(@PathVariable String promotionId, @RequestParam Long orderValue) {
        try {
            Long discount = promotionService.calculateDiscount(promotionId, orderValue);
            return ResponseEntity.ok(ApiResponse.ok(discount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Promotion Reports APIs
    @GetMapping("/reports/expired")
    public ResponseEntity<?> getExpiredPromotions() {
        List<PromotionResponse> promotions = promotionService.getExpiredPromotions();
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/reports/type/{type}")
    public ResponseEntity<?> getPromotionsByType(@PathVariable String type) {
        List<PromotionResponse> promotions = promotionService.getPromotionsByType(type);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }
}
