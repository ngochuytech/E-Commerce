package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.math.BigDecimal;
import java.util.List;

/**
 * Model lưu yêu cầu trả hàng từ người mua
 */
@Document(collection = "return_requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnRequest extends BaseEntity {
    @Id
    private String id;

    @DBRef
    private Order order;

    @DBRef
    private User buyer;

    @DBRef
    private Store store;

    // Lý do trả hàng từ buyer
    private String reason;

    // Mô tả chi tiết vấn đề
    private String description;

    // Danh sách URL hình ảnh/video minh chứng từ buyer
    private List<String> evidenceMedia;

    // Số tiền yêu cầu hoàn trả
    private BigDecimal refundAmount;

    // Trạng thái yêu cầu trả hàng
    private String status;

    // Phản hồi từ store
    private String storeResponse;

    // Lý do từ chối từ store (nếu có)
    private String storeRejectReason;

    // Hình ảnh minh chứng từ store (nếu từ chối)
    private List<String> storeEvidenceMedia;

    // Quyết định cuối cùng từ admin (nếu có tranh chấp)
    private String adminDecision;

    // Lý do quyết định từ admin
    private String adminDecisionReason;

    // Admin xử lý tranh chấp
    @DBRef
    private User adminHandler;

    // ========== STORE DISPUTE AFTER RECEIVING RETURNED GOODS ==========

    // Store khiếu nại về hàng trả về (hàng có vấn đề)
    private boolean storeDisputedReturnedGoods;

    // Lý do store khiếu nại hàng trả về
    private String storeReturnDisputeReason;

    // Mô tả chi tiết vấn đề hàng trả về
    private String storeReturnDisputeDescription;

    // Hình ảnh minh chứng hàng trả về có vấn đề
    private List<String> storeReturnDisputeMedia;

    // Quyết định của admin cho dispute hàng trả về
    private String adminReturnDisputeDecision;

    // Lý do quyết định của admin cho dispute hàng trả về
    private String adminReturnDisputeReason;

    // ========== PARTIAL REFUND (Hoàn tiền một phần) ==========

    // Số tiền hoàn lại cho buyer (khi store thắng nhưng buyer được hoàn một phần)
    private BigDecimal partialRefundToBuyer;

    // Số tiền store được giữ lại (phần còn lại sau khi trừ partialRefundToBuyer)
    private BigDecimal partialRefundToStore;

    // ========== BANK ACCOUNT FOR COD REFUND ==========

    // Tên ngân hàng (cho hoàn tiền COD)
    private String bankName;

    // Số tài khoản ngân hàng (cho hoàn tiền COD)
    private String bankAccountNumber;

    // Tên chủ tài khoản (cho hoàn tiền COD)
    private String bankAccountName;

    /**
     * Các trạng thái của yêu cầu trả hàng:
     * - PENDING: Đang chờ store xem xét
     * - APPROVED: Store đã chấp nhận trả hàng
     * - REJECTED: Store từ chối trả hàng
     * - DISPUTED: Buyer khiếu nại, chờ Admin quyết định
     * - READY_TO_RETURN: Chờ shipper đến lấy hàng trả (sau khi được approve)
     * - RETURNING: Shipper đang lấy hàng trả về
     * - RETURNED: Hàng đã trả về shop
     * - RETURN_DISPUTED: Store khiếu nại hàng trả về có vấn đề
     * - REFUNDED: Đã hoàn tiền cho buyer
     * - REFUND_TO_STORE: Hoàn tiền cho store (store thắng dispute hàng trả về)
     * - CLOSED: Yêu cầu trả hàng bị đóng (từ chối cuối cùng)
     */
    public enum ReturnStatus {
        PENDING, // Chờ store xem xét
        APPROVED, // Store chấp nhận
        REJECTED, // Store từ chối
        DISPUTED, // Buyer khiếu nại, chờ admin
        READY_TO_RETURN, // Chờ shipper đến lấy hàng trả
        RETURNING, // Đang trả hàng về shop
        RETURNED, // Đã trả hàng về shop
        RETURN_DISPUTED, // Store khiếu nại hàng trả về có vấn đề
        REFUNDED, // Đã hoàn tiền cho buyer,
        PARTIAL_REFUND,
        REFUND_TO_STORE, // Hoàn tiền cho store (store thắng)
        CLOSED // Đóng yêu cầu (từ chối cuối cùng)
    }

    /**
     * Các lý do trả hàng phổ biến
     */
    public enum ReturnReason {
        DEFECTIVE_PRODUCT, // Sản phẩm bị lỗi/hỏng
        WRONG_PRODUCT, // Giao sai sản phẩm
        MISSING_ITEMS, // Thiếu sản phẩm
        NOT_AS_DESCRIBED, // Không đúng mô tả
        DAMAGED_PACKAGING, // Bao bì bị hư hại
        QUALITY_ISSUE, // Vấn đề chất lượng
        CHANGE_OF_MIND, // Đổi ý (nếu shop cho phép)
        OTHER // Lý do khác
    }
}
