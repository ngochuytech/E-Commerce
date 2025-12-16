package com.example.e_commerce_techshop.services.returnrequest;

import com.example.e_commerce_techshop.dtos.admin.DisputeDecisionDTO;
import com.example.e_commerce_techshop.dtos.admin.ReturnQualityDecisionDTO;
import com.example.e_commerce_techshop.dtos.b2c.ReturnQualityDisputeDTO;
import com.example.e_commerce_techshop.dtos.b2c.ReturnResponseDTO;
import com.example.e_commerce_techshop.dtos.buyer.DisputeRequestDTO;
import com.example.e_commerce_techshop.dtos.buyer.ReturnRequestDTO;
import com.example.e_commerce_techshop.models.Dispute;
import com.example.e_commerce_techshop.models.ReturnRequest;
import com.example.e_commerce_techshop.models.User;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IReturnRequestService {

    // ==================== BUYER APIs ====================
    
    /**
     * Buyer tạo yêu cầu trả hàng
     */
    ReturnRequest createReturnRequest(User buyer, String orderId, ReturnRequestDTO dto, List<MultipartFile> evidenceFiles) throws Exception;

    /**
     * Buyer xem danh sách yêu cầu trả hàng của mình
     */
    Page<ReturnRequest> getBuyerReturnRequests(User buyer, String status, Pageable pageable) throws Exception;

    /**
     * Buyer xem chi tiết yêu cầu trả hàng
     */
    ReturnRequest getReturnRequestDetail(User buyer, String returnRequestId) throws Exception;

    /**
     * Buyer khiếu nại khi store từ chối
     */
    Dispute createDispute(User buyer, String returnRequestId, DisputeRequestDTO dto, List<MultipartFile> evidenceFiles) throws Exception;

    /**
     * Buyer thêm tin nhắn vào dispute
     */
    Dispute addDisputeMessage(User buyer, String disputeId, DisputeRequestDTO dto, List<MultipartFile> evidenceFiles) throws Exception;

    /**
     * Buyer xem danh sách dispute của mình
     */
    Page<Dispute> getBuyerDisputes(User buyer, Pageable pageable) throws Exception;

    // ==================== STORE APIs ====================
    
    /**
     * Store xem danh sách yêu cầu trả hàng
     */
    Page<ReturnRequest> getStoreReturnRequests(String storeId, String status, Pageable pageable) throws Exception;

    /**
     * Store xem chi tiết yêu cầu trả hàng
     */
    ReturnRequest getStoreReturnRequestDetail(String storeId, String returnRequestId) throws Exception;

    /**
     * Store phản hồi yêu cầu trả hàng (chấp nhận hoặc từ chối)
     */
    ReturnRequest respondToReturnRequest(String storeId, String returnRequestId, ReturnResponseDTO dto, List<MultipartFile> evidenceFiles) throws Exception;

    /**
     * Store thêm tin nhắn vào dispute
     */
    Dispute addStoreDisputeMessage(String storeId, String disputeId, DisputeRequestDTO dto, List<MultipartFile> evidenceFiles) throws Exception;

    /**
     * Store xem danh sách dispute của mình
     */
    Page<Dispute> getStoreDisputes(String storeId, Pageable pageable) throws Exception;

    /**
     * Store khiếu nại hàng trả về có vấn đề (sau khi nhận hàng từ shipper)
     */
    Dispute createReturnQualityDispute(String storeId, String returnRequestId, ReturnQualityDisputeDTO dto, List<MultipartFile> evidenceFiles) throws Exception;

    /**
     * Store xác nhận hàng trả về OK và đồng ý hoàn tiền cho buyer
     */
    ReturnRequest confirmReturnedGoodsOk(String storeId, String returnRequestId) throws Exception;

    // ==================== ADMIN APIs ====================
    
    /**
     * Admin xem tất cả yêu cầu trả hàng
     */
    Page<ReturnRequest> getAllReturnRequests(String status, Pageable pageable) throws Exception;

    /**
     * Admin xem chi tiết yêu cầu trả hàng
     */
    ReturnRequest getReturnRequestDetailForAdmin(String returnRequestId) throws Exception;

    /**
     * Admin xem tất cả dispute
     */
    Page<Dispute> getAllDisputes(String status, String disputeType, Pageable pageable) throws Exception;

    /**
     * Admin xem chi tiết dispute
     */
    Dispute getDisputeDetail(String disputeId) throws Exception;

    /**
     * Admin quyết định dispute (buyer khiếu nại store từ chối trả hàng)
     */
    Dispute resolveDispute(User admin, String disputeId, DisputeDecisionDTO dto) throws Exception;

    /**
     * Admin quyết định dispute hàng trả về (store khiếu nại hàng trả về có vấn đề)
     */
    ReturnRequest resolveReturnQualityDispute(User admin, String disputeId, ReturnQualityDecisionDTO dto) throws Exception;

    /**
     * Admin thêm tin nhắn vào dispute
     */
    Dispute addAdminDisputeMessage(User admin, String disputeId, DisputeRequestDTO dto) throws Exception;

    // ==================== SHIPPER/SHIPMENT APIs ====================
    
    /**
     * Cập nhật trạng thái return request khi shipper lấy hàng trả
     */
    ReturnRequest updateReturnShipmentStatus(String returnRequestId, String status) throws Exception;

    /**
     * Chuẩn bị shipment để shipper lấy hàng trả về (READY_TO_PICK cho return)
     */
    void prepareReturnShipment(ReturnRequest returnRequest) throws Exception;
}
