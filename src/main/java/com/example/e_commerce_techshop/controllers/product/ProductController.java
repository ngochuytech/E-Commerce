package com.example.e_commerce_techshop.controllers.product;

import com.example.e_commerce_techshop.dtos.b2c.product.ProductDTO;
import com.example.e_commerce_techshop.models.Product;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ProductResponse;
import com.example.e_commerce_techshop.services.product.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final IProductService productService;

    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO productDTO, BindingResult result){
        try {
            if(result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
            }
            productService.createProduct(productDTO);
            return ResponseEntity.ok(ApiResponse.ok("Tạo sản phẩm mới thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") String id){
        try {
            ProductResponse productResponse = productService.findProductById(id);
            return ResponseEntity.ok(ApiResponse.ok(productResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getProductByName(@RequestParam("name") String name){
        try {
            List<ProductResponse> responseList = productService.findProductByName(name);
            return ResponseEntity.ok(ApiResponse.ok(responseList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/category/{name}")
    public ResponseEntity<?> getProductByCategory(@PathVariable("name") String category){
        try {
            List<ProductResponse> responseList = productService.findProductByCategory(category);
            return ResponseEntity.ok(ApiResponse.ok(responseList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/category/{category}/brand/{brand}")
    public ResponseEntity<?> getProductByCategoryAndBrand(@PathVariable("category") String category, @PathVariable("brand") String brand){
        try {
            List<ProductResponse> responseList = productService.findProductByCategoryAndBrand(category, brand);
            return ResponseEntity.ok(ApiResponse.ok(responseList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable("id") String productId, @RequestBody ProductDTO productDTO, BindingResult result){
        try {
            if(result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
            }
            productService.updateProduct(productId, productDTO);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật sản phẩm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/update-status/{id}")
    public ResponseEntity<?> updateStatusProduct(@PathVariable("id") String productId, @RequestParam("status") String status){
        try {
            if(!Product.getValidStatus(status))
                throw new Exception("Status không hợp lệ");
            productService.updateStatus(productId, status);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái sản phẩm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable("id") String productId){
        try {
            productService.deleteProduct(productId);
            return ResponseEntity.ok(ApiResponse.ok("Xóa sản phẩm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

