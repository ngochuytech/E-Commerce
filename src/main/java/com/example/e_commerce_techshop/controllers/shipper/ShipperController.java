package com.example.e_commerce_techshop.controllers.shipper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.models.Shipment;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ShipmentResponse;
import com.example.e_commerce_techshop.services.shipment.IShipmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/shipper")
@RequiredArgsConstructor
@Validated
@Tag(name = "Shipper Management", description = "APIs for shippers to manage pickup and delivery of orders")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('SHIPPER')")
public class ShipperController {

        private final IShipmentService shipmentService;

        @GetMapping("/shipments/ready-to-pickup")
        @Operation(summary = "Lấy danh sách đơn hàng cần lấy", description = "Lấy danh sách các đơn hàng đang chờ shipper đến lấy hàng (READY_TO_PICK)")
        public ResponseEntity<?> getPickingUpShipments(
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir)
                        throws Exception {

                Pageable pageable = PageRequest.of(page, size,
                                sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending()
                                                : Sort.by(sortBy).ascending());

                Page<Shipment> shipments = shipmentService.getShipmentsByStatus(
                                Shipment.ShipmentStatus.READY_TO_PICK.name(), pageable);

                Page<ShipmentResponse> responsePage = shipments.map(ShipmentResponse::fromShipment);
                return ResponseEntity.ok(ApiResponse.ok(responsePage));
        }

        @GetMapping("/shipment/{shipmentId}")
        @Operation(summary = "Lấy chi tiết shipment", description = "Lấy chi tiết thông tin của một shipment cụ thể")
        public ResponseEntity<?> getShipmentDetail(
                        @Parameter(description = "Shipment ID", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String shipmentId)
                        throws Exception {

                Shipment shipment = shipmentService.getShipmentById(shipmentId);
                return ResponseEntity.ok(ApiResponse.ok(ShipmentResponse.fromShipment(shipment)));
        }

        @PutMapping("/shipment/{shipmentId}/picking")
        @Operation(summary = "Shipper tới lấy hàng", description = "Shipper đang tới lấy đơn hàng từ shop")
        public ResponseEntity<?> confirmPickup(
                        @Parameter(description = "Shipment ID", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String shipmentId,
                        @AuthenticationPrincipal User shipper)
                        throws Exception {

                shipmentService.pickingShipment(shipmentId, shipper);
                return ResponseEntity.ok(ApiResponse.ok(
                                "Đã xác nhận tới lấy hàng thành công"));
        }

        @PutMapping("/shipment/{shipmentId}/picked")
        @Operation(summary = "Xác nhận đã lấy hàng", description = "Shipper xác nhận đã lấy hàng từ shop (PICKING -> PICKED)")
        public ResponseEntity<?> confirmPickedUp(
                        @Parameter(description = "Shipment ID", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String shipmentId,
                        @AuthenticationPrincipal User shipper)
                        throws Exception {

                shipmentService.pickedShipment(shipmentId, shipper);

                return ResponseEntity.ok(ApiResponse.ok(
                                "Đã xác nhận đã lấy hàng từ shop"));
        }

        /**
         * Shipper bắt đầu giao (PICKED -> SHIPPING)
         */
        @PutMapping("/shipment/{shipmentId}/shipping")
        @Operation(summary = "Bắt đầu giao hàng", description = "Shipper xác nhận đã lấy hàng và bắt đầu vận chuyển (PICKED -> SHIPPING)")
        public ResponseEntity<?> startShipping(
                        @Parameter(description = "Shipment ID", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String shipmentId,
                        @AuthenticationPrincipal User shipper)
                        throws Exception {

                shipmentService.shippingShipment(shipmentId, shipper);

                return ResponseEntity.ok(ApiResponse.ok(
                                "Đã xác nhận lấy hàng và bắt đầu giao hàng"));
        }

        /**
         * Shipper xác nhận đã giao hàng thành công (SHIPPING -> DELIVERED)
         */
        @PutMapping("/shipment/{shipmentId}/delivered")
        @Operation(summary = "Hoàn thành giao hàng", description = "Shipper xác nhận đã giao hàng thành công (SHIPPING -> DELIVERED)")
        public ResponseEntity<?> completeDelivery(
                        @Parameter(description = "Shipment ID", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String shipmentId,
                        @AuthenticationPrincipal User shipper)
                        throws Exception {

                shipmentService.deliverShipment(shipmentId, shipper);

                return ResponseEntity.ok(ApiResponse.ok(
                                "Đã xác nhận giao hàng thành công"));
        }

        /**
         * Shipper báo giao hàng thất bại (DELIVERED -> DELIVER_FAIL)
         */
        @PutMapping("/shipment/{shipmentId}/fail")
        @Operation(summary = "Báo giao hàng thất bại", description = "Shipper báo cáo giao hàng thất bại với lý do (DELIVERED -> DELIVER_FAIL)")
        public ResponseEntity<?> failDelivery(
                        @Parameter(description = "Shipment ID", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String shipmentId,
                        @RequestBody String reason) throws Exception {

                if (reason == null || reason.trim().isEmpty()) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.error("Vui lòng cung cấp lý do giao hàng thất bại"));
                }

                shipmentService.deliverFailShipment(shipmentId, reason);

                return ResponseEntity.ok(ApiResponse.ok(
                                "Đã báo giao hàng thất bại"));
        }

        @PutMapping("/shipment/{shipmentId}/returning")
        @Operation(summary = "Bắt đầu trả hàng về shop", description = "Shipper bắt đầu trả hàng về shop (DELIVER_FAIL -> RETURNING)")
        public ResponseEntity<?> startReturning(
                        @Parameter(description = "Shipment ID", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String shipmentId,
                        @AuthenticationPrincipal User shipper)
                        throws Exception {

                shipmentService.returningShipment(shipmentId, shipper);

                return ResponseEntity.ok(ApiResponse.ok(
                                "Đã bắt đầu trả hàng về shop"));
        }

        @PutMapping("/shipment/{shipmentId}/returned")
        @Operation(summary = "Hoàn thành trả hàng về shop", description = "Shipper hoàn thành trả hàng về shop (RETURNING -> RETURNED)")
        public ResponseEntity<?> completeReturning(
                        @Parameter(description = "Shipment ID", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String shipmentId,
                        @AuthenticationPrincipal User shipper)
                        throws Exception {

                shipmentService.returnedShipment(shipmentId, shipper);

                return ResponseEntity.ok(ApiResponse.ok(
                                "Đã hoàn thành trả hàng về shop"));
        }

        /**
         * Lấy lịch sử giao hàng của shipper
         */
        @GetMapping("/history")
        @Operation(summary = "Lịch sử giao hàng của shipper đang đăng nhập", description = "Lấy lịch sử các đơn hàng đã giao hoàn thành hoặc thất bại (DELIVERED + FAILED)")
        public ResponseEntity<?> getDeliveryHistory(
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Sort by field") @RequestParam(defaultValue = "updatedAt") String sortBy,
                        @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir,
                        @AuthenticationPrincipal User shipper)
                        throws Exception {

                Pageable pageable = PageRequest.of(page, size,
                                sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending()
                                                : Sort.by(sortBy).ascending());

                Page<Shipment> history = shipmentService.getShipperShipments(shipper, pageable);

                Page<ShipmentResponse> responsePage = history.map(ShipmentResponse::fromShipment);
                return ResponseEntity.ok(ApiResponse.ok(responsePage));
        }

}