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
import com.example.e_commerce_techshop.services.notification.INotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefundAdminService implements IRefundAdminService {
    
    private final RefundRequestRepository refundRequestRepository;
    private final OrderRepository orderRepository;
    private final INotificationService notificationService;

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
            // Validate
            if (dto.getRefundTransactionId() == null || dto.getRefundTransactionId().trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập mã giao dịch hoàn tiền");
            }
            
            // Duyệt hoàn tiền
            refundRequest.setStatus(RefundRequest.RefundStatus.COMPLETED.name());
            refundRequest.setRefundTransactionId(dto.getRefundTransactionId());
            refundRequest.setAdminNote(dto.getAdminNote());
            refundRequest.setProcessedBy(admin);
            
            // Cập nhật order
            order.setRefundStatus(Order.RefundStatus.COMPLETED.name());
            order.setRefundCompletedAt(LocalDateTime.now());
            order.setRefundTransactionId(dto.getRefundTransactionId());
            
            // Thông báo cho khách hàng
            
            notificationService.createUserNotification(
                    refundRequest.getBuyer().getId(),
                    "Hoàn tiền thành công",
                    String.format("Đơn hàng #%s đã được hoàn tiền %,.0f đ về tài khoản. Mã GD: %s",
                            order.getId(),
                            refundRequest.getRefundAmount().doubleValue(),
                            dto.getRefundTransactionId()),
                    order.getId());
            
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
