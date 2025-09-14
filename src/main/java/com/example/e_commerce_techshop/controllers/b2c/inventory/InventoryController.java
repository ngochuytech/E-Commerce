package com.example.e_commerce_techshop.controllers.b2c.inventory;

import com.example.e_commerce_techshop.dtos.b2c.inventory.ProductVariantDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;
import com.example.e_commerce_techshop.services.inventory.IInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/b2c/inventory")
@RequiredArgsConstructor
public class InventoryController {
    
    private final IInventoryService inventoryService;

    // ProductVariant Management APIs
    @PostMapping("/variants/create")
    public ResponseEntity<?> createProductVariant(@Valid @RequestBody ProductVariantDTO variantDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
            }
            
            ProductVariantResponse variantResponse = inventoryService.createProductVariant(variantDTO);
            return ResponseEntity.ok(ApiResponse.ok("Tạo biến thể sản phẩm thành công!", variantResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/variants/{variantId}")
    public ResponseEntity<?> updateProductVariant(@PathVariable String variantId, @RequestBody ProductVariantDTO variantDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
            }
            
            ProductVariantResponse variantResponse = inventoryService.updateProductVariant(variantId, variantDTO);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật biến thể sản phẩm thành công!", variantResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/variants/{variantId}")
    public ResponseEntity<?> getProductVariantById(@PathVariable String variantId) {
        try {
            ProductVariantResponse variantResponse = inventoryService.getProductVariantById(variantId);
            return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin biến thể sản phẩm thành công!", variantResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/variants/product/{productId}")
    public ResponseEntity<?> getProductVariantsByProduct(@PathVariable String productId) {
        List<ProductVariantResponse> variants = inventoryService.getProductVariantsByProduct(productId);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách biến thể theo sản phẩm thành công!", variants));
    }

    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<?> deleteProductVariant(@PathVariable String variantId) {
        try {
            inventoryService.deleteProductVariant(variantId);
            return ResponseEntity.ok(ApiResponse.ok("Xóa biến thể sản phẩm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Stock Management APIs
    @PutMapping("/stock/{variantId}/update")
    public ResponseEntity<?> updateStock(@PathVariable String variantId, @RequestParam Integer newStock) {
        try {
            ProductVariantResponse variantResponse = inventoryService.updateStock(variantId, newStock);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật tồn kho thành công!", variantResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/stock/{variantId}/increase")
    public ResponseEntity<?> increaseStock(@PathVariable String variantId, @RequestParam Integer quantity) {
        try {
            ProductVariantResponse variantResponse = inventoryService.increaseStock(variantId, quantity);
            return ResponseEntity.ok(ApiResponse.ok("Tăng tồn kho thành công!", variantResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/stock/{variantId}/decrease")
    public ResponseEntity<?> decreaseStock(@PathVariable String variantId, @RequestParam Integer quantity) {
        try {
            ProductVariantResponse variantResponse = inventoryService.decreaseStock(variantId, quantity);
            return ResponseEntity.ok(ApiResponse.ok("Giảm tồn kho thành công!", variantResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Inventory Reports APIs
    @GetMapping("/reports/low-stock")
    public ResponseEntity<?> getLowStockItems(@RequestParam(defaultValue = "10") Integer threshold) {
        List<ProductVariantResponse> variants = inventoryService.getLowStockItems(threshold);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách hàng tồn kho thấp thành công!", variants));
    }

    @GetMapping("/reports/out-of-stock")
    public ResponseEntity<?> getOutOfStockItems() {
        List<ProductVariantResponse> variants = inventoryService.getOutOfStockItems();
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách hàng hết tồn kho thành công!", variants));
    }

    @GetMapping("/variants")
    public ResponseEntity<?> getAllVariants() {
        List<ProductVariantResponse> variants = inventoryService.getAllVariants();
        return ResponseEntity.ok(ApiResponse.ok("Lấy tất cả biến thể sản phẩm thành công!", variants));
    }
}
