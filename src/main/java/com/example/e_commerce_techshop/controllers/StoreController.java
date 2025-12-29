package com.example.e_commerce_techshop.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.StoreResponse;
import com.example.e_commerce_techshop.services.store.IStoreService;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/stores")
@RequiredArgsConstructor
@Tag(name = "Store Browsing", description = "API cho việc duyệt cửa hàng - Xem và tìm kiếm cửa hàng")
public class StoreController {
    private final IStoreService storeService;

    @GetMapping("/{storeId}")
    @Operation(summary = "Lấy cửa hàng theo ID", 
               description = "Lấy thông tin chi tiết cửa hàng bao gồm mô tả, vị trí và thông tin liên hệ")
    public ResponseEntity<?> getStoreById(
            @Parameter(description = "Store ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable String storeId) throws Exception {
            StoreResponse storeResponse = storeService.getStoreById(storeId);
            return ResponseEntity.ok(ApiResponse.ok(storeResponse));
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả cửa hàng", 
               description = "Lấy danh sách tất cả các cửa hàng đã được phê duyệt và đang hoạt động")
    public ResponseEntity<?> getAllStores() {
        List<StoreResponse> stores = storeService.getAllStores();
        return ResponseEntity.ok(ApiResponse.ok(stores));
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Lấy cửa hàng theo chủ sở hữu", 
               description = "Lấy tất cả các cửa hàng thuộc sở hữu của một người dùng cụ thể")
    public ResponseEntity<?> getStoresByOwner(
            @Parameter(description = "Store owner/user ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable String ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<StoreResponse> storeResponses = storeService.getStoresByOwner(ownerId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(storeResponses));
    }
}
