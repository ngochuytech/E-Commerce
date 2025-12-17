package com.example.e_commerce_techshop.controllers.b2c;

import com.example.e_commerce_techshop.models.Shipment;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ShipmentResponse;
import com.example.e_commerce_techshop.services.shipment.IShipmentService;
import com.example.e_commerce_techshop.services.store.IStoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/b2c/shipments")
@RequiredArgsConstructor
@Tag(name = "B2C Shipment Management", description = "APIs for store owners to manage shipments and track deliveries")
@SecurityRequirement(name = "bearerAuth")
public class B2CShipmentController {

    private final IShipmentService shipmentService;
    private final IStoreService storeService;

    private void validateUserStore(User currentUser, String storeId) {
        List<Store> userStores = storeService.getStoresByOwner(currentUser.getId());
        boolean hasStore = userStores.stream()
                .anyMatch(store -> store.getId().equals(storeId));

        if (!hasStore) {
            throw new RuntimeException("Bạn không có quyền truy cập cửa hàng này");
        }
    }

    /**
     * Lấy danh sách shipment của store
     */
    @GetMapping("/store/{storeId}")
    @Operation(summary = "Lấy danh sách shipment của store", description = "Lấy danh sách phân trang tất cả các shipment của một cửa hàng")
    public ResponseEntity<?> getStoreShipments(
            @Parameter(description = "Store ID", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Filter by status (PICKING_UP, SHIPPING, DELIVERED, FAILED)", example = "SHIPPING") @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size)
            throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        Page<Shipment> shipments = shipmentService.getStoreShipments(storeId, status, pageable);
        Page<ShipmentResponse> shipmentResponses = shipments.map(ShipmentResponse::fromShipment);
        return ResponseEntity.ok(ApiResponse.ok(shipmentResponses));
    }

    @GetMapping("/store/{storeId}/count-by-status")
    @Operation(summary = "Lấy chi tiết shipment", description = "Lấy chi tiết thông tin của một shipment cụ thể")
    public ResponseEntity<?> getShipmentById(
            @Parameter(description = "Store ID", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId)
            throws Exception {
        Map<String, Long> shipmentCountByStatus = shipmentService.getShipmentCountByStatus(storeId);
        return ResponseEntity.ok(ApiResponse.ok(shipmentCountByStatus));
    }

    @PostMapping("/order/{orderId}")
    @Operation(summary = "Tạo shipment cho đơn hàng", description = "Tạo shipment khi người bán đã chuẩn bị xong đơn hàng và sẵn sàng giao cho shipper")
    public ResponseEntity<?> createShipment(
            @Parameter(description = "Order ID", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String orderId,
            @Parameter(description = "Store ID", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @RequestParam String storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser)
            throws Exception {
        validateUserStore(currentUser, storeId);
        Shipment shipment = shipmentService.createShipment(orderId);
        return ResponseEntity.ok(ApiResponse.ok(ShipmentResponse.fromShipment(shipment)));
    }
}
