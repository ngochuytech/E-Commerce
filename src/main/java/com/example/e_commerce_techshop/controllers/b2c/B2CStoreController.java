package com.example.e_commerce_techshop.controllers.b2c;

import com.example.e_commerce_techshop.dtos.b2c.store.StoreDTO;
import com.example.e_commerce_techshop.dtos.b2c.store.UpdateStoreDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.StoreResponse;
import com.example.e_commerce_techshop.services.store.IStoreService;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/b2c/stores")
@RequiredArgsConstructor
@Tag(name = "B2C Store Management", description = "API cho quản lý cửa hàng B2C")
@SecurityRequirement(name = "bearerAuth")
public class B2CStoreController {

    private final IStoreService storeService;

    @GetMapping("/my-stores")
    @Operation(summary = "Lấy cửa hàng của tôi", description = "Lấy tất cả cửa hàng thuộc sở hữu của người dùng hiện tại")
    public ResponseEntity<?> getMyStores(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        List<Store> stores = storeService.getStoresByOwner(currentUser.getId());

        List<StoreResponse> storeResponses = stores.stream()
                .map(StoreResponse::fromStore)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(storeResponses));
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo cửa hàng", description = "Tạo cửa hàng mới với thông tin cơ bản và logo tùy chọn. Cửa hàng sẽ ở trạng thái PENDING chờ duyệt bởi admin")
    public ResponseEntity<?> createStore(
            @Parameter(description = "Thông tin cửa hàng bao gồm tên, mô tả, địa chỉ, chi tiết liên hệ", required = true, content = @Content(schema = @Schema(implementation = StoreDTO.class))) @Valid @RequestPart("storeDTO") StoreDTO storeDTO,
            @Parameter(description = "Tệp hình ảnh logo cửa hàng tùy chọn", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart(value = "logo", required = false) MultipartFile logo,
            @AuthenticationPrincipal User currentUser) throws Exception {

        storeService.createStore(storeDTO, currentUser, logo);
        return ResponseEntity.ok(ApiResponse.ok("Tạo cửa hàng thành công!"));
    }

    @PutMapping("/{storeId}")
    @Operation(summary = "Cập nhật thông tin cửa hàng", description = "Cập nhật thông tin cơ bản của một cửa hàng hiện có")
    public ResponseEntity<?> updateStore(
            @Parameter(description = "ID của cửa hàng cần cập nhật", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Thông tin cửa hàng cập nhật", required = true, content = @Content(schema = @Schema(implementation = UpdateStoreDTO.class))) @RequestBody @Valid UpdateStoreDTO updateStoreDTO)
            throws Exception {
        storeService.validateStoreNotBanned(storeId);
        storeService.updateStore(storeId, updateStoreDTO);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật cửa hàng thành công!"));
    }

    @PutMapping("/{storeId}/approve")
    @Operation(summary = "Duyệt cửa hàng", description = "Duyệt đăng ký cửa hàng đang chờ (chức năng của Admin). Thay đổi trạng thái từ PENDING sang APPROVED")
    public ResponseEntity<?> approveStore(
            @Parameter(description = "ID của cửa hàng cần duyệt", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId)
            throws Exception {
        storeService.approveStore(storeId);
        return ResponseEntity.ok(ApiResponse.ok("Duyệt cửa hàng thành công!"));
    }

    @PutMapping("/{storeId}/reject")
    @Operation(summary = "Từ chối cửa hàng", description = "Từ chối đăng ký cửa hàng đang chờ (chức năng của Admin). Thay đổi trạng thái từ PENDING sang REJECTED")
    public ResponseEntity<?> rejectStore(
            @Parameter(description = "ID của cửa hàng cần từ chối", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Lý do từ chối", required = true, example = "Thông tin không đầy đủ") @RequestParam String reason)
            throws Exception {
        storeService.rejectStore(storeId, reason);
        return ResponseEntity.ok(ApiResponse.ok("Từ chối cửa hàng thành công!"));
    }

    @PutMapping(value = "/{storeId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật logo cửa hàng", description = "Cập nhật hình ảnh logo cho một cửa hàng cụ thể. Chỉ áp dụng cho các cửa hàng đã được duyệt. Thay thế logo hiện có nếu có")
    public ResponseEntity<?> updateStoreLogo(
            @Parameter(description = "ID của cửa hàng cần cập nhật logo", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Tệp hình ảnh logo (hỗ trợ JPG, PNG, GIF)", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestParam("file") MultipartFile file)
            throws Exception {
        storeService.validateStoreNotBanned(storeId);
        storeService.updateStoreLogo(storeId, file);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật logo thành công!"));
    }

    @PutMapping(value = "/{storeId}/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật banner cửa hàng", description = "Cập nhật hình ảnh banner cho một cửa hàng cụ thể. Chỉ áp dụng cho các cửa hàng đã được duyệt. Dùng để trang trí và thương hiệu cửa hàng")
    public ResponseEntity<?> updateStoreBanner(
            @Parameter(description = "ID của cửa hàng cần cập nhật banner", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Tệp hình ảnh banner (hỗ trợ JPG, PNG, GIF, kích thước khuyến nghị: 1200x400px)", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestParam("file") MultipartFile file)
            throws Exception {
        storeService.validateStoreNotBanned(storeId);
        storeService.updateStoreBanner(storeId, file);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật banner thành công!"));
    }

    @DeleteMapping("/{storeId}")
    @Operation(summary = "Xóa (mềm) cửa hàng", description = "Xóa (mềm) một cửa hàng bằng cách thay đổi trạng thái thành DELETED. Dữ liệu cửa hàng được giữ lại nhưng trở nên không hoạt động")
    public ResponseEntity<?> softDelete(
            @Parameter(description = "ID của cửa hàng cần xóa (mềm)", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId)
            throws Exception {
        storeService.updateStoreStatus(storeId, "DELETED");
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa (mềm) cửa hàng"));
    }
}
