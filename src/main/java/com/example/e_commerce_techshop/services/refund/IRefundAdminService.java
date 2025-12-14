package com.example.e_commerce_techshop.services.refund;

import com.example.e_commerce_techshop.dtos.admin.ProcessRefundRequestDTO;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.admin.RefundRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface IRefundAdminService {
    
    /**
     * Lấy danh sách yêu cầu hoàn tiền với filter và phân trang
     */
    Page<RefundRequestResponse> getRefundRequests(String status, Pageable pageable);
    
    /**
     * Xem chi tiết yêu cầu hoàn tiền
     */
    RefundRequestResponse getRefundRequestDetail(String id) throws Exception;
    
    /**
     * Thống kê yêu cầu hoàn tiền
     */
    Map<String, Object> getRefundStatistics();
    
    /**
     * Xử lý yêu cầu hoàn tiền (duyệt/từ chối)
     */
    RefundRequestResponse processRefundRequest(ProcessRefundRequestDTO dto, User admin) throws Exception;
}
