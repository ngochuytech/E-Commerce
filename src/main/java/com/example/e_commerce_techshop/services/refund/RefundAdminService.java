package com.example.e_commerce_techshop.services.refund;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_commerce_techshop.dtos.admin.ProcessRefundRequestDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.RefundRequest;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.repositories.RefundRequestRepository;
import com.example.e_commerce_techshop.responses.admin.RefundRequestResponse;
import com.example.e_commerce_techshop.services.momo.IMomoService;
import com.example.e_commerce_techshop.services.notification.INotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefundAdminService implements IRefundAdminService {
    
    private final RefundRequestRepository refundRequestRepository;
    private final OrderRepository orderRepository;
    private final INotificationService notificationService;
    private final IMomoService momoService;

    @Override
    public Page<RefundRequestResponse> getRefundRequests(String status, Pageable pageable) {
        Page<RefundRequest> refundRequests;
        
        if (status != null && !status.trim().isEmpty()) {
            refundRequests = refundRequestRepository.findByStatus(status.toUpperCase(), pageable);
        } else {
            refundRequests = refundRequestRepository.findAll(pageable);
        }
        
        return refundRequests.map(RefundRequestResponse::fromRefundRequest);
    }

    @Override
    public RefundRequestResponse getRefundRequestDetail(String id) throws Exception {
        RefundRequest refundRequest = refundRequestRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy yêu cầu hoàn tiền"));
        
        return RefundRequestResponse.fromRefundRequest(refundRequest);
    }

    @Override
    public Map<String, Object> getRefundStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long pending = refundRequestRepository.countByStatus(RefundRequest.RefundStatus.PENDING.name());
        long completed = refundRequestRepository.countByStatus(RefundRequest.RefundStatus.COMPLETED.name());
        long rejected = refundRequestRepository.countByStatus(RefundRequest.RefundStatus.REJECTED.name());
        long total = pending + completed + rejected;
        
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("completed", completed);
        stats.put("rejected", rejected);
        
        // Tính tổng tiền cần hoàn (pending + processing)
        // Có thể thêm query aggregate nếu cần
        
        return stats;
    }

    @Override
    @Transactional
    public RefundRequestResponse processRefundRequest(ProcessRefundRequestDTO dto, User admin) throws Exception {
        RefundRequest refundRequest = refundRequestRepository.findById(dto.getRefundRequestId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy yêu cầu hoàn tiền"));
        
        // Kiểm tra trạng thái hiện tại
        if (RefundRequest.RefundStatus.COMPLETED.name().equals(refundRequest.getStatus())) {
            throw new IllegalArgumentException("Yêu cầu hoàn tiền này đã được xử lý");
        }
        
        if (RefundRequest.RefundStatus.REJECTED.name().equals(refundRequest.getStatus())) {
            throw new IllegalArgumentException("Yêu cầu hoàn tiền này đã bị từ chối");
        }
        
        Order order = refundRequest.getOrder();
        
        if ("APPROVE".equalsIgnoreCase(dto.getAction())) {
            // Xử lý theo phương thức thanh toán
            String paymentMethod = order.getPaymentMethod();
            
            if ("COD".equalsIgnoreCase(paymentMethod)) {
                // COD: Admin phải nhập mã giao dịch chuyển khoản thủ công
                if (dto.getRefundTransactionId() == null || dto.getRefundTransactionId().trim().isEmpty()) {
                    throw new IllegalArgumentException("Đơn COD cần nhập mã giao dịch chuyển khoản (refundTransactionId)");
                }
                
                // Duyệt hoàn tiền COD
                refundRequest.setStatus(RefundRequest.RefundStatus.COMPLETED.name());
                refundRequest.setRefundTransactionId(dto.getRefundTransactionId());
                refundRequest.setAdminNote(dto.getAdminNote());
                refundRequest.setProcessedBy(admin);
                
                // Cập nhật order với thông tin manual refund
                order.setRefundStatus(Order.RefundStatus.COMPLETED.name());
                order.setRefundCompletedAt(LocalDateTime.now());
                order.setManualRefundTransactionRef(dto.getRefundTransactionId());
                order.setManualRefundTransferredAt(LocalDateTime.now());
                order.setManualRefundNote(dto.getAdminNote());
                
                // Thông báo cho khách hàng
                notificationService.createUserNotification(
                        refundRequest.getBuyer().getId(),
                        "Hoàn tiền thành công",
                        String.format("Đơn hàng #%s đã được hoàn tiền %,.0f đ về tài khoản ngân hàng của bạn. Mã GD: %s",
                                order.getId(),
                                refundRequest.getRefundAmount().doubleValue(),
                                dto.getRefundTransactionId()),
                        order.getId());
                        
            } else if ("MOMO".equalsIgnoreCase(paymentMethod)) {
                // MOMO: Tự động hoàn tiền qua payment gateway
                String momoTransId = order.getMomoTransId();
                if (momoTransId == null || momoTransId.trim().isEmpty()) {
                    throw new IllegalArgumentException("Không tìm thấy mã giao dịch MoMo (đơn hàng chưa thanh toán hoặc mất thông tin transId)");
                }
                
                // Gọi MoMo API để hoàn tiền
                Long transIdLong = Long.parseLong(momoTransId);
                Long amountLong = refundRequest.getRefundAmount().longValue();
                String description = String.format("Hoàn tiền đơn hàng #%s - Admin: %s", order.getId(), admin.getFullName());
                
                String momoResponse = momoService.refundPayment(transIdLong, amountLong, description);
                
                // Parse response từ MoMo
                org.cloudinary.json.JSONObject json = new org.cloudinary.json.JSONObject(momoResponse);
                int resultCode = json.getInt("resultCode");
                String refundTransId = json.optString("transId", "");
                String momoRefundOrderId = json.optString("orderId", ""); // RF_xxx
                
                if (resultCode == 0) {
                    // Hoàn tiền thành công
                    refundRequest.setStatus(RefundRequest.RefundStatus.COMPLETED.name());
                    refundRequest.setRefundTransactionId(refundTransId);
                    refundRequest.setAdminNote(dto.getAdminNote());
                    refundRequest.setProcessedBy(admin);
                    
                    // Cập nhật order
                    order.setRefundStatus(Order.RefundStatus.COMPLETED.name());
                    order.setRefundCompletedAt(LocalDateTime.now());
                    order.setRefundTransactionId(refundTransId);
                    order.setMomoRefundOrderId(momoRefundOrderId);
                    
                    // Thông báo cho khách hàng
                    notificationService.createUserNotification(
                            refundRequest.getBuyer().getId(),
                            "Hoàn tiền thành công",
                            String.format("Đơn hàng #%s đã được hoàn tiền %,.0f đ về ví MoMo. Mã GD: %s",
                                    order.getId(),
                                    refundRequest.getRefundAmount().doubleValue(),
                                    refundTransId),
                            order.getId());
                } else {
                    // Hoàn tiền thất bại
                    String errorMessage = json.optString("message", "Lỗi không xác định");
                    throw new RuntimeException("Hoàn tiền MoMo thất bại: " + errorMessage + " (ResultCode: " + resultCode + ")");
                }
                
            } else {
                throw new IllegalArgumentException("Phương thức thanh toán không hỗ trợ hoàn tiền: " + paymentMethod);
            }
            
        } else if ("REJECT".equalsIgnoreCase(dto.getAction())) {
            // Validate
            if (dto.getRejectionReason() == null || dto.getRejectionReason().trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập lý do từ chối");
            }
            
            // Từ chối hoàn tiền
            refundRequest.setStatus(RefundRequest.RefundStatus.REJECTED.name());
            refundRequest.setRejectionReason(dto.getRejectionReason());
            refundRequest.setAdminNote(dto.getAdminNote());
            refundRequest.setProcessedBy(admin);
            
            // Cập nhật order
            order.setRefundStatus(Order.RefundStatus.FAILED.name());
            
            // Thông báo cho khách hàng
            notificationService.createUserNotification(
                    refundRequest.getBuyer().getId(),
                    "Yêu cầu hoàn tiền bị từ chối",
                    String.format("Yêu cầu hoàn tiền cho đơn hàng #%s đã bị từ chối. Lý do: %s. Vui lòng liên hệ CSKH để biết thêm chi tiết.",
                            order.getId(),
                            dto.getRejectionReason()),
                    order.getId());
        } else {
            throw new IllegalArgumentException("Action không hợp lệ. Chỉ chấp nhận APPROVE hoặc REJECT");
        }
        
        refundRequestRepository.save(refundRequest);
        orderRepository.save(order);
        
        return RefundRequestResponse.fromRefundRequest(refundRequest);
    }
}
