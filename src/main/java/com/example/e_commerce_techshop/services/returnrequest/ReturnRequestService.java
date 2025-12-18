package com.example.e_commerce_techshop.services.returnrequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_commerce_techshop.dtos.admin.DisputeDecisionDTO;
import com.example.e_commerce_techshop.dtos.admin.ReturnQualityDecisionDTO;
import com.example.e_commerce_techshop.dtos.b2c.ReturnQualityDisputeDTO;
import com.example.e_commerce_techshop.dtos.b2c.ReturnResponseDTO;
import com.example.e_commerce_techshop.dtos.buyer.DisputeRequestDTO;
import com.example.e_commerce_techshop.dtos.buyer.ReturnRequestDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Dispute;
import com.example.e_commerce_techshop.models.Notification;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.RefundRequest;
import com.example.e_commerce_techshop.models.ReturnRequest;
import com.example.e_commerce_techshop.models.Shipment;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.DisputeRepository;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.repositories.RefundRequestRepository;
import com.example.e_commerce_techshop.repositories.ReturnRequestRepository;
import com.example.e_commerce_techshop.repositories.ShipmentRepository;
import com.example.e_commerce_techshop.services.CloudinaryService;
import com.example.e_commerce_techshop.services.notification.INotificationService;
import com.example.e_commerce_techshop.services.wallet.IWalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnRequestService implements IReturnRequestService {

    private final ReturnRequestRepository returnRequestRepository;
    private final DisputeRepository disputeRepository;
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final INotificationService notificationService;
    private final IWalletService walletService;
    private final CloudinaryService cloudinaryService;
    private final RefundRequestRepository refundRequestRepository;
    private final com.example.e_commerce_techshop.services.refund.IRefundService refundService;

    // ==================== BUYER APIs ====================

    @Override
    @Transactional
    public ReturnRequest createReturnRequest(User buyer, String orderId, ReturnRequestDTO dto,
            List<MultipartFile> evidenceFiles) throws Exception {
        // Kiểm tra order tồn tại
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Kiểm tra order thuộc về buyer
        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new IllegalStateException("Bạn không có quyền yêu cầu trả hàng cho đơn hàng này");
        }

        // Kiểm tra trạng thái order phải là DELIVERED
        if (!Order.OrderStatus.DELIVERED.name().equals(order.getStatus())) {
            throw new IllegalStateException("Chỉ có thể yêu cầu trả hàng khi đơn hàng đã được giao (DELIVERED)");
        }

        // Kiểm tra đã có yêu cầu trả hàng chưa (không tính các yêu cầu đã đóng)
        if (returnRequestRepository.existsByOrderIdAndStatusNot(orderId, ReturnRequest.ReturnStatus.CLOSED.name())) {
            throw new IllegalStateException("Đã có yêu cầu trả hàng cho đơn hàng này");
        }

        if (evidenceFiles == null || evidenceFiles.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp ít nhất 1 hình ảnh/video minh chứng");
        }
        List<String> evidenceUrls = cloudinaryService.uploadMediaFiles(evidenceFiles, "return-request-evidence");

        // Validate thông tin tài khoản ngân hàng nếu thanh toán COD
        if ("COD".equals(order.getPaymentMethod())) {
            if (dto.getBankName() == null || dto.getBankName().trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng cung cấp tên ngân hàng để nhận tiền hoàn");
            }
            if (dto.getBankAccountNumber() == null || dto.getBankAccountNumber().trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng cung cấp số tài khoản ngân hàng để nhận tiền hoàn");
            }
            if (dto.getBankAccountName() == null || dto.getBankAccountName().trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng cung cấp tên chủ tài khoản để nhận tiền hoàn");
            }
        }

        // Tạo return request
        ReturnRequest returnRequest = ReturnRequest.builder()
                .order(order)
                .buyer(buyer)
                .store(order.getStore())
                .reason(dto.getReason())
                .description(dto.getDescription())
                .evidenceMedia(evidenceUrls)
                .refundAmount(order.getTotalPrice())
                .status(ReturnRequest.ReturnStatus.PENDING.name())
                .bankName(dto.getBankName())
                .bankAccountNumber(dto.getBankAccountNumber())
                .bankAccountName(dto.getBankAccountName())
                .build();

        ReturnRequest savedRequest = returnRequestRepository.save(returnRequest);

        // Cập nhật flag hasReturnRequest trong Order
        order.setReturnRequestId(savedRequest.getId());
        orderRepository.save(order);

        // Thông báo cho store
        try {
            notificationService.createStoreNotification(order.getStore().getId(),
                    "Yêu cầu trả hàng mới",
                    String.format("Khách hàng %s yêu cầu trả hàng cho đơn #%s. Lý do: %s",
                            buyer.getFullName(), orderId, dto.getReason()),
                    orderId);
        } catch (Exception e) {
            log.warn("Error sending notification to store: {}", e.getMessage());
        }

        log.info("Buyer {} created return request for order {}", buyer.getId(), orderId);
        return savedRequest;
    }

    @Override
    public Page<ReturnRequest> getBuyerReturnRequests(User buyer, String status, Pageable pageable) throws Exception {
        if (status != null && !status.isEmpty()) {
            return returnRequestRepository.findByBuyerIdAndStatus(buyer.getId(), status, pageable);
        }
        return returnRequestRepository.findByBuyerId(buyer.getId(), pageable);
    }

    @Override
    public ReturnRequest getReturnRequestDetail(User buyer, String returnRequestId) throws Exception {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(
                        () -> new DataNotFoundException("Không tìm thấy yêu cầu trả hàng với ID: " + returnRequestId));

        if (!returnRequest.getBuyer().getId().equals(buyer.getId())) {
            throw new IllegalStateException("Bạn không có quyền xem yêu cầu trả hàng này");
        }

        return returnRequest;
    }

    @Override
    public List<Dispute> getDisputesByReturnRequest(String returnRequestId) throws Exception {
        // Lấy tất cả disputes liên quan đến return request này
        return disputeRepository.findByReturnRequestId(returnRequestId);
    }

    @Override
    @Transactional
    public Dispute createDispute(User buyer, String returnRequestId, DisputeRequestDTO dto,
            List<MultipartFile> evidenceFiles) throws Exception {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(
                        () -> new DataNotFoundException("Không tìm thấy yêu cầu trả hàng với ID: " + returnRequestId));

        // Kiểm tra return request thuộc về buyer
        if (!returnRequest.getBuyer().getId().equals(buyer.getId())) {
            throw new IllegalStateException("Bạn không có quyền khiếu nại yêu cầu trả hàng này");
        }

        // Chỉ có thể khiếu nại khi bị từ chối
        if (!ReturnRequest.ReturnStatus.REJECTED.name().equals(returnRequest.getStatus())) {
            throw new IllegalStateException("Chỉ có thể khiếu nại khi yêu cầu trả hàng bị từ chối");
        }

        // Kiểm tra đã có dispute chưa
        if (disputeRepository.existsByReturnRequestId(returnRequestId)) {
            throw new IllegalStateException("Đã có khiếu nại cho yêu cầu trả hàng này");
        }

        // Upload attachment files to Cloudinary
        List<String> attachmentUrls = null;
        if (evidenceFiles != null && evidenceFiles.size() > 5) {
            throw new IllegalArgumentException("Số lượng hình ảnh/video đính kèm tối đa là 5");
        }
        if (evidenceFiles != null && !evidenceFiles.isEmpty())
            attachmentUrls = cloudinaryService.uploadMediaFiles(evidenceFiles, "dispute-evidence");

        // Tạo message đầu tiên từ buyer
        Dispute.DisputeMessage firstMessage = Dispute.DisputeMessage.builder()
                .senderId(buyer.getId())
                .senderType("BUYER")
                .senderName(buyer.getFullName())
                .content(dto.getContent())
                .attachments(attachmentUrls)
                .sentAt(LocalDateTime.now())
                .build();

        List<Dispute.DisputeMessage> messages = new ArrayList<>();
        messages.add(firstMessage);

        // Tạo dispute
        Dispute dispute = Dispute.builder()
                .returnRequest(returnRequest)
                .order(returnRequest.getOrder())
                .buyer(buyer)
                .store(returnRequest.getStore())
                .disputeType(Dispute.DisputeType.RETURN_REJECTION.name()) // Buyer khiếu nại store từ chối
                .status(Dispute.DisputeStatus.OPEN.name())
                .messages(messages)
                .build();

        Dispute savedDispute = disputeRepository.save(dispute);

        // Cập nhật status của return request
        returnRequest.setStatus(ReturnRequest.ReturnStatus.DISPUTED.name());
        returnRequestRepository.save(returnRequest);

        try {
            // Thông báo cho store
            notificationService.createStoreNotification(returnRequest.getStore().getId(),
                    "Khiếu nại mới từ khách hàng",
                    String.format(
                            "Khách hàng %s đã khiếu nại về việc từ chối trả hàng đơn #%s. Sàn sẽ xem xét và quyết định.",
                            buyer.getFullName(), returnRequest.getOrder().getId()),
                    returnRequest.getOrder().getId());

            // Thông báo cho admin
            notificationService.createAdminNotification(
                    "Khiếu nại trả hàng mới",
                    String.format(
                            "Khách hàng %s đã khiếu nại về việc từ chối trả hàng đơn #%s của cửa hàng %s. Vui lòng xem xét.",
                            buyer.getFullName(), returnRequest.getOrder().getId(),
                            returnRequest.getStore().getName()),
                    Notification.NotificationType.DISPUTE.name(),
                    returnRequest.getOrder().getId());
        } catch (Exception e) {
            log.warn("Error sending notification to store: {}", e.getMessage());
        }

        log.info("Buyer {} created dispute for return request {}", buyer.getId(), returnRequestId);
        return savedDispute;
    }

    @Override
    @Transactional
    public Dispute addDisputeMessage(User buyer, String disputeId, DisputeRequestDTO dto,
            List<MultipartFile> attachmentFiles) throws Exception {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khiếu nại với ID: " + disputeId));

        // Kiểm tra dispute thuộc về buyer
        if (!dispute.getBuyer().getId().equals(buyer.getId())) {
            throw new IllegalStateException("Bạn không có quyền thêm tin nhắn vào khiếu nại này");
        }

        // Kiểm tra dispute chưa được giải quyết
        if (Dispute.DisputeStatus.RESOLVED.name().equals(dispute.getStatus()) ||
                Dispute.DisputeStatus.CLOSED.name().equals(dispute.getStatus())) {
            throw new IllegalStateException("Khiếu nại đã được giải quyết, không thể thêm tin nhắn");
        }

        if (attachmentFiles != null && attachmentFiles.size() > 5) {
            throw new IllegalArgumentException("Số lượng hình ảnh/video đính kèm tối đa là 5");
        }
        List<String> attachmentUrls = null;
        if (attachmentFiles != null && !attachmentFiles.isEmpty())
            attachmentUrls = cloudinaryService.uploadMediaFiles(attachmentFiles, "dispute-evidence");

        // Thêm message
        Dispute.DisputeMessage message = Dispute.DisputeMessage.builder()
                .senderId(buyer.getId())
                .senderType("BUYER")
                .senderName(buyer.getFullName())
                .content(dto.getContent())
                .attachments(attachmentUrls)
                .sentAt(LocalDateTime.now())
                .build();

        if (dispute.getMessages() == null) {
            dispute.setMessages(new ArrayList<>());
        }
        dispute.getMessages().add(message);

        return disputeRepository.save(dispute);
    }

    @Override
    public Page<Dispute> getBuyerDisputes(User buyer, Pageable pageable) throws Exception {
        return disputeRepository.findByBuyerId(buyer.getId(), pageable);
    }

    // ==================== STORE APIs ====================

    @Override
    public Page<ReturnRequest> getStoreReturnRequests(String storeId, String status, Pageable pageable)
            throws Exception {
        if (status != null && !status.isEmpty()) {
            return returnRequestRepository.findByStoreIdAndStatus(storeId, status, pageable);
        }
        return returnRequestRepository.findByStoreId(storeId, pageable);
    }

    @Override
    public ReturnRequest getStoreReturnRequestDetail(String storeId, String returnRequestId) throws Exception {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(
                        () -> new DataNotFoundException("Không tìm thấy yêu cầu trả hàng với ID: " + returnRequestId));

        if (!returnRequest.getStore().getId().equals(storeId)) {
            throw new IllegalStateException("Bạn không có quyền xem yêu cầu trả hàng này");
        }

        return returnRequest;
    }

    @Override
    @Transactional
    public ReturnRequest respondToReturnRequest(String storeId, String returnRequestId, ReturnResponseDTO dto,
            List<MultipartFile> evidenceFiles)
            throws Exception {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(
                        () -> new DataNotFoundException("Không tìm thấy yêu cầu trả hàng với ID: " + returnRequestId));

        // Kiểm tra return request thuộc về store
        if (!returnRequest.getStore().getId().equals(storeId)) {
            throw new IllegalStateException("Bạn không có quyền phản hồi yêu cầu trả hàng này");
        }

        // Kiểm tra trạng thái phải là PENDING
        if (!ReturnRequest.ReturnStatus.PENDING.name().equals(returnRequest.getStatus())) {
            throw new IllegalStateException("Yêu cầu trả hàng này đã được xử lý");
        }

        Order order = returnRequest.getOrder();

        if (dto.isApproved()) {
            // Store chấp nhận trả hàng -> chuyển sang READY_TO_RETURN để shipper lấy hàng
            returnRequest.setStatus(ReturnRequest.ReturnStatus.READY_TO_RETURN.name());
            returnRequest.setStoreResponse("Chấp nhận yêu cầu trả hàng");
            returnRequestRepository.save(returnRequest);

            order.setStatus(Order.OrderStatus.RETURNING.name());
            orderRepository.save(order);

            // Chuẩn bị shipment để shipper lấy hàng trả về
            prepareReturnShipment(returnRequest);

            // Thông báo cho buyer
            try {
                notificationService.createUserNotification(returnRequest.getBuyer().getId(),
                        "Yêu cầu trả hàng được chấp nhận",
                        String.format(
                                "Cửa hàng %s đã chấp nhận yêu cầu trả hàng cho đơn #%s. Shipper sẽ đến lấy hàng sớm.",
                                returnRequest.getStore().getName(), order.getId()),
                        order.getId());
            } catch (Exception e) {
                log.warn("Error sending notification to buyer: {}", e.getMessage());
            }

            log.info("Store {} approved return request {}, shipment prepared for return", storeId, returnRequestId);
        } else {
            // Store từ chối trả hàng
            if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng cung cấp lý do từ chối");
            }

            returnRequest.setStatus(ReturnRequest.ReturnStatus.REJECTED.name());
            returnRequest.setStoreResponse("Từ chối yêu cầu trả hàng");
            returnRequest.setStoreRejectReason(dto.getReason());

            List<String> evidenceUrls = null;
            if (evidenceFiles != null && !evidenceFiles.isEmpty()) {
                evidenceUrls = cloudinaryService.uploadMediaFiles(evidenceFiles, "return-response-evidence");
            }

            returnRequest.setStoreEvidenceMedia(evidenceUrls);

            // Thông báo cho buyer
            try {
                notificationService.createUserNotification(returnRequest.getBuyer().getId(),
                        "Yêu cầu trả hàng bị từ chối",
                        String.format(
                                "Cửa hàng %s đã từ chối yêu cầu trả hàng cho đơn #%s. Lý do: %s. Bạn có thể khiếu nại nếu không đồng ý.",
                                returnRequest.getStore().getName(), order.getId(), dto.getReason()),
                        order.getId());
            } catch (Exception e) {
                log.warn("Error sending notification to buyer: {}", e.getMessage());
            }

            log.info("Store {} rejected return request {} with reason: {}", storeId, returnRequestId, dto.getReason());
        }

        return returnRequestRepository.save(returnRequest);
    }

    @Override
    @Transactional
    public Dispute addStoreDisputeMessage(String storeId, String disputeId, DisputeRequestDTO dto,
            List<MultipartFile> evidenceFiles) throws Exception {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khiếu nại với ID: " + disputeId));

        // Kiểm tra dispute thuộc về store
        if (!dispute.getStore().getId().equals(storeId)) {
            throw new IllegalStateException("Bạn không có quyền thêm tin nhắn vào khiếu nại này");
        }

        // Kiểm tra dispute chưa được giải quyết
        if (Dispute.DisputeStatus.RESOLVED.name().equals(dispute.getStatus()) ||
                Dispute.DisputeStatus.CLOSED.name().equals(dispute.getStatus())) {
            throw new IllegalStateException("Khiếu nại đã được giải quyết, không thể thêm tin nhắn");
        }

        // Upload attachment files to Cloudinary
        if (evidenceFiles != null && evidenceFiles.size() > 5) {
            throw new IllegalArgumentException("Số lượng hình ảnh/video đính kèm tối đa là 5");
        }
        List<String> attachmentUrls = null;
        if (evidenceFiles != null && !evidenceFiles.isEmpty())
            attachmentUrls = cloudinaryService.uploadMediaFiles(evidenceFiles, "dispute-evidence");

        // Thêm message
        Dispute.DisputeMessage message = Dispute.DisputeMessage.builder()
                .senderId(storeId)
                .senderType("STORE")
                .senderName(dispute.getStore().getName())
                .content(dto.getContent())
                .attachments(attachmentUrls)
                .sentAt(LocalDateTime.now())
                .build();

        if (dispute.getMessages() == null) {
            dispute.setMessages(new ArrayList<>());
        }
        dispute.getMessages().add(message);

        // Thông báo cho buyer
        try {
            notificationService.createUserNotification(dispute.getBuyer().getId(),
                    "Cửa hàng phản hồi khiếu nại",
                    String.format("Cửa hàng %s đã phản hồi khiếu nại của bạn cho đơn #%s",
                            dispute.getStore().getName(), dispute.getOrder().getId()),
                    dispute.getOrder().getId());
        } catch (Exception e) {
            log.warn("Error sending notification to buyer: {}", e.getMessage());
        }

        return disputeRepository.save(dispute);
    }

    @Override
    public Page<Dispute> getStoreDisputes(String storeId, Pageable pageable) throws Exception {
        return disputeRepository.findByStoreId(storeId, pageable);
    }

    // ==================== ADMIN APIs ====================

    @Override
    public Page<ReturnRequest> getAllReturnRequests(String status, Pageable pageable) throws Exception {
        if (status != null && !status.isEmpty()) {
            return returnRequestRepository.findByStatus(status, pageable);
        }
        return returnRequestRepository.findAll(pageable);
    }

    @Override
    public ReturnRequest getReturnRequestDetailForAdmin(String returnRequestId) throws Exception {
        return returnRequestRepository.findById(returnRequestId)
                .orElseThrow(
                        () -> new DataNotFoundException("Không tìm thấy yêu cầu trả hàng với ID: " + returnRequestId));
    }

    @Override
    public Page<Dispute> getAllDisputes(String status, String disputeType, Pageable pageable) throws Exception {
        if (status != null && !status.isEmpty() && disputeType != null && !disputeType.isEmpty()) {
            return disputeRepository.findByStatusAndDisputeType(status, disputeType, pageable);
        } else if (status != null && !status.isEmpty()) {
            return disputeRepository.findByStatus(status, pageable);
        } else if (disputeType != null && !disputeType.isEmpty()) {
            return disputeRepository.findByDisputeType(disputeType, pageable);
        }
        return disputeRepository.findAll(pageable);
    }

    @Override
    public Dispute getDisputeDetail(String disputeId) throws Exception {
        return disputeRepository.findById(disputeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khiếu nại với ID: " + disputeId));
    }

    @Override
    @Transactional
    public Dispute resolveDispute(User admin, String disputeId, DisputeDecisionDTO dto) throws Exception {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khiếu nại với ID: " + disputeId));

        // Kiểm tra dispute chưa được giải quyết
        if (Dispute.DisputeStatus.RESOLVED.name().equals(dispute.getStatus()) ||
                Dispute.DisputeStatus.CLOSED.name().equals(dispute.getStatus())) {
            throw new IllegalStateException("Khiếu nại này đã được giải quyết");
        }

        // Kiểm tra loại dispute phải là RETURN_REJECTION
        if (!Dispute.DisputeType.RETURN_REJECTION.name().equals(dispute.getDisputeType())) {
            throw new IllegalStateException("Loại khiếu nại không hợp lệ. Vui lòng sử dụng API phù hợp.");
        }

        ReturnRequest returnRequest = dispute.getReturnRequest();
        Order order = dispute.getOrder();

        // Validate decision
        if (!"APPROVE_RETURN".equalsIgnoreCase(dto.getDecision()) &&
                !"REJECT_RETURN".equalsIgnoreCase(dto.getDecision())) {
            throw new IllegalArgumentException("Decision phải là APPROVE_RETURN hoặc REJECT_RETURN");
        }

        // Admin quyết định
        dispute.setAdminHandler(admin);
        dispute.setDecisionReason(dto.getReason());
        dispute.setResolvedAt(LocalDateTime.now());

        if ("APPROVE_RETURN".equalsIgnoreCase(dto.getDecision())) {
            // Admin chấp nhận cho buyer trả hàng
            dispute.setFinalDecision("APPROVE_RETURN");
            dispute.setWinner(Dispute.DisputeWinner.BUYER.name());
            dispute.setStatus(Dispute.DisputeStatus.RESOLVED.name());

            // Cập nhật return request thành READY_TO_RETURN (chờ shipper lấy hàng)
            returnRequest.setStatus(ReturnRequest.ReturnStatus.READY_TO_RETURN.name());
            returnRequest.setAdminDecision("APPROVE_RETURN");
            returnRequest.setAdminDecisionReason(dto.getReason());
            returnRequest.setAdminHandler(admin);
            returnRequestRepository.save(returnRequest);

            order.setStatus(Order.OrderStatus.RETURNING.name());
            orderRepository.save(order);

            // Chuẩn bị shipment để shipper lấy hàng trả về
            prepareReturnShipment(returnRequest);

            // Thông báo cho buyer
            try {
                notificationService.createUserNotification(dispute.getBuyer().getId(),
                        "Khiếu nại được chấp nhận",
                        String.format(
                                "Admin đã chấp nhận khiếu nại của bạn cho đơn #%s. Yêu cầu trả hàng được duyệt. Shipper sẽ đến lấy hàng sớm.",
                                order.getId()),
                        order.getId());
            } catch (Exception e) {
                log.warn("Error sending notification to buyer: {}", e.getMessage());
            }

            // Thông báo cho store
            try {
                notificationService.createStoreNotification(dispute.getStore().getId(),
                        "Admin đã quyết định khiếu nại",
                        String.format(
                                "Admin đã chấp nhận yêu cầu trả hàng của khách cho đơn #%s. Lý do: %s. Shipper sẽ lấy hàng và trả về cho bạn.",
                                order.getId(), dto.getReason()),
                        order.getId());
            } catch (Exception e) {
                log.warn("Error sending notification to store: {}", e.getMessage());
            }

            log.info("Admin {} approved dispute {} - buyer wins, shipment prepared for return", admin.getId(),
                    disputeId);
        } else {
            // Admin từ chối, giữ nguyên quyết định của store
            dispute.setFinalDecision("REJECT_RETURN");
            dispute.setWinner(Dispute.DisputeWinner.STORE.name());
            dispute.setStatus(Dispute.DisputeStatus.RESOLVED.name());

            // Cập nhật return request thành CLOSED
            returnRequest.setStatus(ReturnRequest.ReturnStatus.CLOSED.name());
            returnRequest.setAdminDecision("REJECT_RETURN");
            returnRequest.setAdminDecisionReason(dto.getReason());
            returnRequest.setAdminHandler(admin);
            returnRequestRepository.save(returnRequest);

            // Clear flag hasReturnRequest trong Order (vì đã CLOSED)

            // Thông báo cho buyer
            try {
                notificationService.createUserNotification(dispute.getBuyer().getId(),
                        "Khiếu nại bị từ chối",
                        String.format("Admin đã xem xét và từ chối khiếu nại của bạn cho đơn #%s. Lý do: %s",
                                order.getId(), dto.getReason()),
                        order.getId());
            } catch (Exception e) {
                log.warn("Error sending notification to buyer: {}", e.getMessage());
            }

            // Thông báo cho store
            try {
                notificationService.createStoreNotification(dispute.getStore().getId(),
                        "Admin đã quyết định khiếu nại",
                        String.format("Admin đã giữ nguyên quyết định từ chối trả hàng của bạn cho đơn #%s.",
                                order.getId()),
                        order.getId());
            } catch (Exception e) {
                log.warn("Error sending notification to store: {}", e.getMessage());
            }

            log.info("Admin {} rejected dispute {} - store wins", admin.getId(), disputeId);
        }

        return disputeRepository.save(dispute);
    }

    @Override
    @Transactional
    public Dispute addAdminDisputeMessage(User admin, String disputeId, DisputeRequestDTO dto) throws Exception {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khiếu nại với ID: " + disputeId));

        // Thêm message từ admin
        Dispute.DisputeMessage message = Dispute.DisputeMessage.builder()
                .senderId(admin.getId())
                .senderType("ADMIN")
                .senderName("Admin - " + admin.getFullName())
                .content(dto.getContent())
                .sentAt(LocalDateTime.now())
                .build();

        if (dispute.getMessages() == null) {
            dispute.setMessages(new ArrayList<>());
        }
        dispute.getMessages().add(message);

        // Cập nhật status thành IN_REVIEW nếu đang OPEN
        if (Dispute.DisputeStatus.OPEN.name().equals(dispute.getStatus())) {
            dispute.setStatus(Dispute.DisputeStatus.IN_REVIEW.name());
            dispute.setAdminHandler(admin);
        }

        // Thông báo cho buyer và store
        try {
            notificationService.createUserNotification(dispute.getBuyer().getId(),
                    "Admin phản hồi khiếu nại",
                    String.format("Admin đã phản hồi khiếu nại của bạn cho đơn #%s", dispute.getOrder().getId()),
                    dispute.getOrder().getId());

            notificationService.createStoreNotification(dispute.getStore().getId(),
                    "Admin phản hồi khiếu nại",
                    String.format("Admin đã phản hồi khiếu nại cho đơn #%s", dispute.getOrder().getId()),
                    dispute.getOrder().getId());
        } catch (Exception e) {
            log.warn("Error sending notification: {}", e.getMessage());
        }

        return disputeRepository.save(dispute);
    }

    // ==================== SHIPPER APIs ====================

    @Override
    @Transactional
    public ReturnRequest updateReturnShipmentStatus(String returnRequestId, String status) throws Exception {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(
                        () -> new DataNotFoundException("Không tìm thấy yêu cầu trả hàng với ID: " + returnRequestId));

        Order order = returnRequest.getOrder();

        switch (status) {
            case "RETURNING":
                // Shipper đang lấy hàng trả về
                if (!ReturnRequest.ReturnStatus.APPROVED.name().equals(returnRequest.getStatus())) {
                    throw new IllegalStateException("Chỉ có thể bắt đầu trả hàng khi yêu cầu được chấp nhận");
                }
                returnRequest.setStatus(ReturnRequest.ReturnStatus.RETURNING.name());

                // Thông báo
                try {
                    notificationService.createUserNotification(returnRequest.getBuyer().getId(),
                            "Shipper đang đến lấy hàng trả",
                            String.format("Shipper đang đến lấy hàng trả cho đơn #%s", order.getId()),
                            order.getId());

                    notificationService.createStoreNotification(returnRequest.getStore().getId(),
                            "Shipper đang trả hàng về",
                            String.format("Shipper đang lấy hàng trả từ khách và sẽ trả về cho đơn #%s", order.getId()),
                            order.getId());
                } catch (Exception e) {
                    log.warn("Error sending notification: {}", e.getMessage());
                }
                break;

            case "RETURNED":
                // Hàng đã trả về shop - KHÔNG tự động hoàn tiền, chờ store xác nhận
                if (!ReturnRequest.ReturnStatus.RETURNING.name().equals(returnRequest.getStatus())) {
                    throw new IllegalStateException("Chỉ có thể hoàn thành trả hàng khi đang trong quá trình trả");
                }
                returnRequest.setStatus(ReturnRequest.ReturnStatus.RETURNED.name());

                // Cập nhật order status
                order.setStatus(Order.OrderStatus.RETURNED.name());
                orderRepository.save(order);

                // Cập nhật shipment status
                Shipment shipment = shipmentRepository.findByOrderId(order.getId())
                        .orElse(null);
                if (shipment != null) {
                    shipment.setStatus(Shipment.ShipmentStatus.RETURNED.name());
                    if (shipment.getHistory() == null) {
                        shipment.setHistory(new ArrayList<>());
                    }
                    shipment.getHistory().add(LocalDateTime.now() + ": Đã trả hàng về shop thành công (RETURNED)");
                    shipmentRepository.save(shipment);
                }

                // Thông báo - Store cần kiểm tra hàng trước khi xác nhận hoàn tiền
                try {
                    notificationService.createUserNotification(returnRequest.getBuyer().getId(),
                            "Hàng đã trả về shop",
                            String.format(
                                    "Đơn hàng #%s đã được trả về shop. Vui lòng chờ shop kiểm tra và xác nhận hoàn tiền.",
                                    order.getId()),
                            order.getId());

                    notificationService.createStoreNotification(returnRequest.getStore().getId(),
                            "Đã nhận hàng trả về",
                            String.format(
                                    "Hàng trả từ đơn #%s đã được shipper giao về. Vui lòng kiểm tra hàng và xác nhận hoàn tiền cho khách hoặc khiếu nại nếu hàng có vấn đề.",
                                    order.getId()),
                            order.getId());
                } catch (Exception e) {
                    log.warn("Error sending notification: {}", e.getMessage());
                }
                break;

            default:
                throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
        }

        log.info("Return request {} updated to status {}", returnRequestId, status);
        return returnRequestRepository.save(returnRequest);
    }

    // ==================== STORE - Xử lý hàng trả về ====================

    @Override
    @Transactional
    public Dispute createReturnQualityDispute(String storeId, String returnRequestId, ReturnQualityDisputeDTO dto,
            List<MultipartFile> evidenceFiles)
            throws Exception {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(
                        () -> new DataNotFoundException("Không tìm thấy yêu cầu trả hàng với ID: " + returnRequestId));

        // Kiểm tra return request thuộc về store
        if (!returnRequest.getStore().getId().equals(storeId)) {
            throw new IllegalStateException("Bạn không có quyền khiếu nại yêu cầu trả hàng này");
        }

        // Chỉ có thể khiếu nại khi hàng đã được trả về (RETURNED)
        if (!ReturnRequest.ReturnStatus.RETURNED.name().equals(returnRequest.getStatus())) {
            throw new IllegalStateException("Chỉ có thể khiếu nại khi hàng đã được trả về shop (RETURNED)");
        }

        // Kiểm tra đã có dispute về chất lượng hàng trả về chưa
        if (returnRequest.isStoreDisputedReturnedGoods()) {
            throw new IllegalStateException("Đã có khiếu nại về hàng trả về cho yêu cầu này");
        }

        // Cập nhật return request
        returnRequest.setStoreDisputedReturnedGoods(true);
        returnRequest.setStoreReturnDisputeReason(dto.getReason());
        returnRequest.setStoreReturnDisputeDescription(dto.getDescription());
        returnRequest.setStatus(ReturnRequest.ReturnStatus.RETURN_DISPUTED.name());

        List<String> evidenceUrls = null;
        if (evidenceFiles != null && !evidenceFiles.isEmpty()) {
            evidenceUrls = cloudinaryService.uploadMediaFiles(evidenceFiles, "return-quality-dispute");
        }
        returnRequest.setStoreReturnDisputeMedia(evidenceUrls);

        // Tạo message đầu tiên từ store
        Dispute.DisputeMessage firstMessage = Dispute.DisputeMessage.builder()
                .senderId(storeId)
                .senderType("STORE")
                .senderName(returnRequest.getStore().getName())
                .content(String.format("Lý do: %s\n\nMô tả: %s", dto.getReason(), dto.getDescription()))
                .attachments(dto.getEvidenceImages())
                .sentAt(LocalDateTime.now())
                .build();

        List<Dispute.DisputeMessage> messages = new ArrayList<>();
        messages.add(firstMessage);

        // Tạo dispute mới về chất lượng hàng trả về
        Dispute dispute = Dispute.builder()
                .returnRequest(returnRequest)
                .order(returnRequest.getOrder())
                .buyer(returnRequest.getBuyer())
                .store(returnRequest.getStore())
                .disputeType(Dispute.DisputeType.RETURN_QUALITY.name()) // Store khiếu nại hàng trả về
                .status(Dispute.DisputeStatus.OPEN.name())
                .messages(messages)
                .build();

        Dispute savedDispute = disputeRepository.save(dispute);
        returnRequestRepository.save(returnRequest);

        // Thông báo cho buyer
        try {
            notificationService.createUserNotification(returnRequest.getBuyer().getId(),
                    "Cửa hàng khiếu nại hàng trả về",
                    String.format(
                            "Cửa hàng %s đã khiếu nại rằng hàng trả về từ đơn #%s có vấn đề. Sàn sẽ xem xét và quyết định.",
                            returnRequest.getStore().getName(), returnRequest.getOrder().getId()),
                    returnRequest.getOrder().getId());
            notificationService.createAdminNotification(
                    "Khiếu nại hàng trả về mới",
                    String.format(
                            "Cửa hàng %s đã khiếu nại rằng hàng trả về từ đơn #%s có vấn đề. Vui lòng xem xét và giải quyết.",
                            returnRequest.getStore().getName(), returnRequest.getOrder().getId()),
                    Notification.NotificationType.DISPUTE.name(),
                    returnRequest.getOrder().getId());
        } catch (Exception e) {
            log.warn("Error sending notification to buyer: {}", e.getMessage());
        }

        log.info("Store {} created return quality dispute for return request {}", storeId, returnRequestId);
        return savedDispute;
    }

    @Override
    @Transactional
    public ReturnRequest confirmReturnedGoodsOk(String storeId, String returnRequestId) throws Exception {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(
                        () -> new DataNotFoundException("Không tìm thấy yêu cầu trả hàng với ID: " + returnRequestId));

        // Kiểm tra return request thuộc về store
        if (!returnRequest.getStore().getId().equals(storeId)) {
            throw new IllegalStateException("Bạn không có quyền xác nhận yêu cầu trả hàng này");
        }

        // Chỉ có thể xác nhận khi hàng đã được trả về (RETURNED)
        if (!ReturnRequest.ReturnStatus.RETURNED.name().equals(returnRequest.getStatus())) {
            throw new IllegalStateException("Chỉ có thể xác nhận khi hàng đã được trả về shop (RETURNED)");
        }

        Order order = returnRequest.getOrder();

        // Store xác nhận hàng OK
        returnRequest.setStatus(ReturnRequest.ReturnStatus.REFUNDED.name());
        returnRequest.setStoreResponse("Xác nhận hàng trả về đạt yêu cầu, đồng ý hoàn tiền");

        // Xử lý hoàn tiền theo phương thức thanh toán
        if ("COD".equals(order.getPaymentMethod())) {
            // Tạo RefundRequest cho admin xử lý chuyển khoản thủ công
            try {
                RefundRequest refundRequest = RefundRequest.builder()
                        .order(order)
                        .buyer(order.getBuyer())
                        .refundAmount(returnRequest.getRefundAmount())
                        .paymentMethod(RefundRequest.PaymentMethod.BANK_TRANSFER.name()) // Hoàn qua ngân hàng
                        .bankName(returnRequest.getBankName())
                        .bankAccountNumber(returnRequest.getBankAccountNumber())
                        .bankAccountName(returnRequest.getBankAccountName())
                        .status(RefundRequest.RefundStatus.PENDING.name())
                        .build();
                refundRequestRepository.save(refundRequest);

                // Thông báo cho admin với thông tin tài khoản
                String bankInfo = String.format(
                        "Thông tin chuyển khoản:\n- Ngân hàng: %s\n- Số TK: %s\n- Tên TK: %s",
                        returnRequest.getBankName(),
                        returnRequest.getBankAccountNumber(),
                        returnRequest.getBankAccountName());

                notificationService.createAdminNotification(
                        "Yêu cầu hoàn tiền COD",
                        String.format(
                                "Đơn hàng #%s: Cần chuyển khoản hoàn tiền %,.0f đ cho khách hàng %s.\n%s",
                                order.getId(),
                                returnRequest.getRefundAmount().doubleValue(),
                                returnRequest.getBuyer().getFullName(),
                                bankInfo),
                        Notification.NotificationType.REFUND_REQUEST.name(),
                        order.getId());

                log.info("Created manual refund request for COD order {}, bank: {}, account: {}",
                        order.getId(), returnRequest.getBankName(), returnRequest.getBankAccountNumber());
            } catch (Exception e) {
                log.error("Error creating refund request: {}", e.getMessage());
                throw new RuntimeException("Lỗi khi tạo yêu cầu hoàn tiền: " + e.getMessage());
            }
        } else {
            // Tự động hoàn tiền qua MoMo
            try {
                refundService.createRefundRequest(order, order.getTotalPrice());
                log.info("Auto refund initiated for return request {}, amount: {}",
                        returnRequestId, returnRequest.getRefundAmount());
            } catch (Exception e) {
                log.error("Error processing auto refund: {}", e.getMessage());

                // Nếu hoàn tiền tự động thất bại, thông báo admin xử lý thủ công
                try {
                    notificationService.createAdminNotification(
                            "Lỗi hoàn tiền tự động",
                            String.format(
                                    "Đơn hàng #%s: Không thể hoàn tiền tự động %,.0f đ cho khách hàng %s. Lỗi: %s. Vui lòng xử lý thủ công.",
                                    order.getId(),
                                    returnRequest.getRefundAmount().doubleValue(),
                                    returnRequest.getBuyer().getFullName(),
                                    e.getMessage()),
                            Notification.NotificationType.REFUND_REQUEST.name(),
                            order.getId());
                } catch (Exception notifEx) {
                    log.warn("Error sending admin notification: {}", notifEx.getMessage());
                }
                throw new RuntimeException("Lỗi khi hoàn tiền: " + e.getMessage());
            }
        }

        log.info("Store {} confirmed returned goods OK for return request {}, refund processed", storeId,
                returnRequestId);
        return returnRequestRepository.save(returnRequest);
    }

    @Override
    @Transactional
    public ReturnRequest resolveReturnQualityDispute(User admin, String disputeId, ReturnQualityDecisionDTO dto)
            throws Exception {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khiếu nại với ID: " + disputeId));

        // Kiểm tra dispute chưa được giải quyết
        if (Dispute.DisputeStatus.RESOLVED.name().equals(dispute.getStatus()) ||
                Dispute.DisputeStatus.CLOSED.name().equals(dispute.getStatus())) {
            throw new IllegalStateException("Khiếu nại này đã được giải quyết");
        }

        // Kiểm tra loại dispute phải là RETURN_QUALITY
        if (!Dispute.DisputeType.RETURN_QUALITY.name().equals(dispute.getDisputeType())) {
            throw new IllegalStateException("Loại khiếu nại không hợp lệ. Vui lòng sử dụng API phù hợp.");
        }

        ReturnRequest returnRequest = dispute.getReturnRequest();
        Order order = dispute.getOrder();
        BigDecimal totalRefundAmount = returnRequest.getRefundAmount();

        // Validate decision
        if (!"APPROVE_STORE".equalsIgnoreCase(dto.getDecision()) &&
                !"REJECT_STORE".equalsIgnoreCase(dto.getDecision()) &&
                !"PARTIAL_REFUND".equalsIgnoreCase(dto.getDecision())) {
            throw new IllegalArgumentException("Decision phải là APPROVE_STORE, REJECT_STORE hoặc PARTIAL_REFUND");
        }

        // Validate partial refund amount nếu decision là PARTIAL_REFUND
        if ("PARTIAL_REFUND".equalsIgnoreCase(dto.getDecision())) {
            if (dto.getPartialRefundAmount() == null) {
                throw new IllegalArgumentException("Vui lòng nhập số tiền hoàn lại cho buyer (partialRefundAmount)");
            }
            if (dto.getPartialRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Số tiền hoàn lại phải lớn hơn 0");
            }
            if (dto.getPartialRefundAmount().compareTo(totalRefundAmount) >= 0) {
                throw new IllegalArgumentException(
                        String.format("Số tiền hoàn lại cho buyer phải nhỏ hơn tổng giá trị đơn hàng (%,.0f đ). Nếu muốn hoàn toàn bộ, hãy chọn REJECT_STORE", 
                                totalRefundAmount.doubleValue()));
            }
        }

        // Admin quyết định
        dispute.setAdminHandler(admin);
        dispute.setDecisionReason(dto.getReason());
        dispute.setResolvedAt(LocalDateTime.now());

        if ("APPROVE_STORE".equalsIgnoreCase(dto.getDecision())) {
            // Admin chấp nhận khiếu nại của store -> Store thắng hoàn toàn, toàn bộ tiền cho store
            dispute.setFinalDecision("APPROVE_STORE");
            dispute.setWinner(Dispute.DisputeWinner.STORE.name());
            dispute.setStatus(Dispute.DisputeStatus.RESOLVED.name());

            // Cập nhật return request
            returnRequest.setStatus(ReturnRequest.ReturnStatus.REFUND_TO_STORE.name());
            returnRequest.setAdminReturnDisputeDecision("APPROVE_STORE");
            returnRequest.setAdminReturnDisputeReason(dto.getReason());
            returnRequestRepository.save(returnRequest);

            // Hoàn tiền cho store - cộng vào ví của store
            walletService.transferPendingToBalance(
                    returnRequest.getStore().getId(),
                    order.getId(),
                    totalRefundAmount,
                    String.format("Hoàn tiền từ tranh chấp hàng trả về đơn #%s - Store thắng kiện", order.getId()));

            log.info("Refunded {} to store {} for order {}",
                    totalRefundAmount,
                    returnRequest.getStore().getId(), order.getId());

            // Thông báo cho store
            try {
                notificationService.createStoreNotification(dispute.getStore().getId(),
                        "Khiếu nại được chấp nhận",
                        String.format(
                                "Admin đã chấp nhận khiếu nại của bạn về hàng trả về từ đơn #%s. Tiền %,.0f đ đã được hoàn vào ví shop.",
                                order.getId(), totalRefundAmount.doubleValue()),
                        order.getId());
            } catch (Exception e) {
                log.warn("Error sending notification to store: {}", e.getMessage());
            }

            // Thông báo cho buyer
            try {
                notificationService.createUserNotification(dispute.getBuyer().getId(),
                        "Khiếu nại bị từ chối",
                        String.format(
                                "Admin đã xem xét và chấp nhận khiếu nại của cửa hàng về hàng trả về từ đơn #%s. Bạn không được hoàn tiền. Lý do: %s",
                                order.getId(), dto.getReason()),
                        order.getId());
            } catch (Exception e) {
                log.warn("Error sending notification to buyer: {}", e.getMessage());
            }

            log.info("Admin {} approved return quality dispute {} - store wins 100%, refund {} to store", 
                    admin.getId(), disputeId, totalRefundAmount);
                    
        } else if ("PARTIAL_REFUND".equalsIgnoreCase(dto.getDecision())) {
            // Store thắng nhưng buyer được hoàn một phần tiền
            BigDecimal buyerRefundAmount = dto.getPartialRefundAmount();
            BigDecimal storeRefundAmount = totalRefundAmount.subtract(buyerRefundAmount);
            
            dispute.setFinalDecision("PARTIAL_REFUND");
            dispute.setWinner(Dispute.DisputeWinner.STORE.name()); // Store vẫn thắng
            dispute.setStatus(Dispute.DisputeStatus.RESOLVED.name());

            // Cập nhật return request
            returnRequest.setStatus(ReturnRequest.ReturnStatus.PARTIAL_REFUND.name());
            returnRequest.setAdminReturnDisputeDecision("PARTIAL_REFUND");
            returnRequest.setAdminReturnDisputeReason(dto.getReason());
            returnRequest.setPartialRefundToBuyer(buyerRefundAmount);
            returnRequest.setPartialRefundToStore(storeRefundAmount);
            returnRequestRepository.save(returnRequest);

            // Hoàn tiền cho store (phần store được giữ lại)
            walletService.transferPendingToBalance(
                    returnRequest.getStore().getId(),
                    order.getId(),
                    storeRefundAmount,
                    String.format("Hoàn tiền từ tranh chấp đơn #%s - Store thắng kiện (hoàn một phần cho buyer)", order.getId()));

            log.info("Partial refund for order {}: Store gets {}, Buyer gets {}",
                    order.getId(), storeRefundAmount, buyerRefundAmount);

            // Hoàn tiền cho buyer
            processPartialRefundToBuyer(order, returnRequest, buyerRefundAmount);

            // Thông báo cho store
            try {
                notificationService.createStoreNotification(dispute.getStore().getId(),
                        "Khiếu nại được chấp nhận - Hoàn tiền một phần",
                        String.format(
                                "Admin đã chấp nhận khiếu nại của bạn về hàng trả về từ đơn #%s. Bạn nhận được %,.0f đ, buyer được hoàn lại %,.0f đ.",
                                order.getId(), storeRefundAmount.doubleValue(), buyerRefundAmount.doubleValue()),
                        order.getId());
            } catch (Exception e) {
                log.warn("Error sending notification to store: {}", e.getMessage());
            }

            // Thông báo cho buyer
            try {
                notificationService.createUserNotification(dispute.getBuyer().getId(),
                        "Hoàn tiền một phần",
                        String.format(
                                "Admin đã giải quyết tranh chấp đơn #%s. Bạn được hoàn lại %,.0f đ (trên tổng %,.0f đ). Lý do: %s",
                                order.getId(), buyerRefundAmount.doubleValue(), totalRefundAmount.doubleValue(), dto.getReason()),
                        order.getId());
            } catch (Exception e) {
                log.warn("Error sending notification to buyer: {}", e.getMessage());
            }

            log.info("Admin {} resolved return quality dispute {} with PARTIAL_REFUND - Store gets {}, Buyer gets {}", 
                    admin.getId(), disputeId, storeRefundAmount, buyerRefundAmount);

            log.info("Admin {} approved return quality dispute {} - store wins, refund to store", admin.getId(),
                    disputeId);
        } else {
            // Admin từ chối khiếu nại của store -> Buyer thắng, tạo yêu cầu hoàn tiền cho
            // admin xử lý
            dispute.setFinalDecision("REJECT_STORE");
            dispute.setWinner(Dispute.DisputeWinner.BUYER.name());
            dispute.setStatus(Dispute.DisputeStatus.RESOLVED.name());

            // Cập nhật return request
            returnRequest.setStatus(ReturnRequest.ReturnStatus.REFUNDED.name());
            returnRequest.setAdminReturnDisputeDecision("REJECT_STORE");
            returnRequest.setAdminReturnDisputeReason(dto.getReason());
            returnRequestRepository.save(returnRequest);

            // Xử lý hoàn tiền theo phương thức thanh toán
            if ("COD".equals(order.getPaymentMethod())) {
                // Tạo RefundRequest cho admin xử lý chuyển khoản thủ công
                try {
                    RefundRequest refundRequest = RefundRequest
                            .builder()
                            .order(order)
                            .buyer(order.getBuyer())
                            .refundAmount(returnRequest.getRefundAmount())
                            .paymentMethod(RefundRequest.PaymentMethod.BANK_TRANSFER.name())
                            .bankName(returnRequest.getBankName())
                            .bankAccountNumber(returnRequest.getBankAccountNumber())
                            .bankAccountName(returnRequest.getBankAccountName())
                            .status(RefundRequest.RefundStatus.PENDING.name())
                            .build();
                    refundRequestRepository.save(refundRequest);

                    // Thông báo cho admin với thông tin tài khoản
                    String bankInfo = String.format(
                            "Thông tin chuyển khoản:\n- Ngân hàng: %s\n- Số TK: %s\n- Tên TK: %s",
                            returnRequest.getBankName(),
                            returnRequest.getBankAccountNumber(),
                            returnRequest.getBankAccountName());

                    notificationService.createAdminNotification(
                            "Yêu cầu hoàn tiền COD (Tranh chấp)",
                            String.format(
                                    "Đơn hàng #%s: Buyer thắng tranh chấp. Cần chuyển khoản hoàn tiền %,.0f đ cho khách hàng %s.\n%s",
                                    order.getId(),
                                    returnRequest.getRefundAmount().doubleValue(),
                                    returnRequest.getBuyer().getFullName(),
                                    bankInfo),
                            Notification.NotificationType.REFUND_REQUEST.name(),
                            order.getId());

                    log.info("Created manual refund request for COD order {} after dispute, buyer wins",
                            order.getId());
                } catch (Exception e) {
                    log.error("Error creating refund request: {}", e.getMessage());
                    throw new RuntimeException("Lỗi khi tạo yêu cầu hoàn tiền: " + e.getMessage());
                }
            } else {
                // Tự động hoàn tiền cho buyer qua MoMo/VNPAY
                try {
                    refundService.createRefundRequest(order, returnRequest.getRefundAmount());
                    log.info("Auto refund initiated for return quality dispute {}, buyer wins, amount: {}",
                            disputeId, returnRequest.getRefundAmount());
                } catch (Exception e) {
                    log.error("Error processing auto refund: {}", e.getMessage());

                    // Nếu hoàn tiền tự động thất bại, thông báo admin xử lý thủ công
                    try {
                        notificationService.createAdminNotification(
                                "Lỗi hoàn tiền tự động",
                                String.format(
                                        "Đơn hàng #%s: Không thể hoàn tiền tự động %,.0f đ cho khách hàng %s sau tranh chấp. Lỗi: %s. Vui lòng xử lý thủ công.",
                                        order.getId(),
                                        returnRequest.getRefundAmount().doubleValue(),
                                        returnRequest.getBuyer().getFullName(),
                                        e.getMessage()),
                                Notification.NotificationType.REFUND_REQUEST.name(),
                                order.getId());
                    } catch (Exception notifEx) {
                        log.warn("Error sending admin notification: {}", notifEx.getMessage());
                    }
                    throw new RuntimeException("Lỗi khi hoàn tiền: " + e.getMessage());
                }
            }

            // Thông báo cho store
            try {
                notificationService.createStoreNotification(dispute.getStore().getId(),
                        "Khiếu nại bị từ chối",
                        String.format(
                                "Admin đã từ chối khiếu nại của bạn về hàng trả về từ đơn #%s. Tiền sẽ được hoàn cho khách hàng. Lý do: %s",
                                order.getId(), dto.getReason()),
                        order.getId());
            } catch (Exception e) {
                log.warn("Error sending notification to store: {}", e.getMessage());
            }

            log.info("Admin {} rejected return quality dispute {} - buyer wins, refund request created", admin.getId(),
                    disputeId);
        }

        disputeRepository.save(dispute);
        return returnRequestRepository.save(returnRequest);
    }

    // ==================== HELPER METHODS ====================

    @Override
    @Transactional
    public void prepareReturnShipment(ReturnRequest returnRequest) throws Exception {
        Order order = returnRequest.getOrder();

        // Tạo shipment MỚI cho việc trả hàng (không dùng lại shipment cũ)
        // Shipment này sẽ lấy hàng từ buyer và giao về store
        List<String> history = new ArrayList<>();
        history.add(LocalDateTime.now() + ": Tạo shipment trả hàng - Chờ shipper lấy hàng (RETURN - READY_TO_PICK)");

        Shipment.Address fromAddress = Shipment.Address.builder()
                .homeAddress(order.getAddress().getHomeAddress())
                .ward(order.getAddress().getWard())
                .province(order.getAddress().getProvince())
                .suggestedName(order.getAddress().getSuggestedName())
                .build();

        Shipment.Address toAddress = Shipment.Address.builder()
                .homeAddress(order.getStore().getAddress().getHomeAddress())
                .ward(order.getStore().getAddress().getWard())
                .province(order.getStore().getAddress().getProvince())
                .suggestedName(order.getStore().getName())
                .build();

        Shipment returnShipment = Shipment.builder()
                .order(order)
                .store(order.getStore())
                .fromAddress(fromAddress) // Địa chỉ lấy hàng = địa chỉ buyer (nơi có hàng cần trả)
                .toAddress(toAddress) // Địa chỉ giao hàng = địa chỉ store (nơi nhận hàng trả về)
                .status(Shipment.ShipmentStatus.READY_TO_PICK.name())
                .carrier(null)
                .isReturnShipment(true)
                .history(history)
                .expectedDeliveryDate(LocalDateTime.now().plusDays(3))
                .build();

        shipmentRepository.save(returnShipment);

        log.info("Created NEW return shipment for order {}, status: READY_TO_PICK", order.getId());
    }

    @Override
    @Transactional
    public ReturnRequest cancelReturnRequest(User buyer, String returnRequestId) throws Exception {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy yêu cầu trả hàng với ID: " + returnRequestId));

        // Kiểm tra quyền sở hữu
        if (!returnRequest.getBuyer().getId().equals(buyer.getId())) {
            throw new IllegalStateException("Bạn không có quyền hủy yêu cầu trả hàng này");
        }

        // Chỉ cho phép hủy khi status là PENDING 
        String currentStatus = returnRequest.getStatus();
        if (!ReturnRequest.ReturnStatus.PENDING.name().equals(currentStatus)) {
            throw new IllegalStateException(
                    "Chỉ có thể hủy yêu cầu trả hàng khi trạng thái là PENDING. Trạng thái hiện tại: " + currentStatus);
        }

        // Cập nhật status thành CLOSED
        returnRequest.setStatus(ReturnRequest.ReturnStatus.CLOSED.name());
        returnRequest.setStoreResponse("Buyer đã hủy yêu cầu trả hàng");
        returnRequestRepository.save(returnRequest);

        // Clear flag hasReturnRequest và hoàn thiện Order
        Order order = returnRequest.getOrder();
        order.setStatus(Order.OrderStatus.COMPLETED.name());
        orderRepository.save(order);

        // Thanh toán cho shop (chuyển tiền từ pending sang balance)
        try {
            walletService.transferPendingToBalance(
                    order.getStore().getId(),
                    order.getId(),
                    order.getTotalPrice(),
                    String.format("Thanh toán đơn hàng #%s - Buyer đã hủy yêu cầu trả hàng", order.getId()));
            
            log.info("Transferred {} to store {} for order {} after buyer cancelled return request",
                    order.getTotalPrice(), order.getStore().getId(), order.getId());
        } catch (Exception e) {
            log.error("Error transferring payment to store: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi thanh toán cho cửa hàng: " + e.getMessage());
        }

        // Thông báo cho store
        try {
            notificationService.createStoreNotification(
                    order.getStore().getId(),
                    "Yêu cầu trả hàng đã bị hủy",
                    String.format("Khách hàng %s đã hủy yêu cầu trả hàng cho đơn #%s",
                            buyer.getFullName(), order.getId()),
                    order.getId());
        } catch (Exception e) {
            log.warn("Error sending notification to store: {}", e.getMessage());
        }

        log.info("Buyer {} cancelled return request {} for order {}", buyer.getId(), returnRequestId, order.getId());
        return returnRequest;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Xử lý hoàn tiền một phần cho buyer (khi store thắng dispute nhưng buyer được hoàn một phần)
     */
    private void processPartialRefundToBuyer(Order order, ReturnRequest returnRequest, BigDecimal refundAmount) throws Exception {
        if ("COD".equals(order.getPaymentMethod())) {
            // Tạo RefundRequest cho admin xử lý chuyển khoản thủ công
            try {
                RefundRequest refundReq = RefundRequest.builder()
                        .order(order)
                        .buyer(order.getBuyer())
                        .refundAmount(refundAmount)
                        .paymentMethod(RefundRequest.PaymentMethod.BANK_TRANSFER.name())
                        .bankName(returnRequest.getBankName())
                        .bankAccountNumber(returnRequest.getBankAccountNumber())
                        .bankAccountName(returnRequest.getBankAccountName())
                        .status(RefundRequest.RefundStatus.PENDING.name())
                        .adminNote(String.format("Hoàn tiền một phần - Store thắng tranh chấp nhưng buyer được hoàn %,.0f đ", refundAmount.doubleValue()))
                        .build();
                refundRequestRepository.save(refundReq);

                // Thông báo cho admin
                String bankInfo = String.format(
                        "Thông tin chuyển khoản:\n- Ngân hàng: %s\n- Số TK: %s\n- Tên TK: %s",
                        returnRequest.getBankName(),
                        returnRequest.getBankAccountNumber(),
                        returnRequest.getBankAccountName());

                notificationService.createAdminNotification(
                        "Yêu cầu hoàn tiền một phần (COD)",
                        String.format(
                                "Đơn hàng #%s: Hoàn tiền MỘT PHẦN cho buyer. Cần chuyển khoản %,.0f đ cho khách hàng %s.\n%s",
                                order.getId(),
                                refundAmount.doubleValue(),
                                returnRequest.getBuyer().getFullName(),
                                bankInfo),
                        Notification.NotificationType.REFUND_REQUEST.name(),
                        order.getId());

                log.info("Created partial refund request for COD order {}, amount: {}", order.getId(), refundAmount);
            } catch (Exception e) {
                log.error("Error creating partial refund request: {}", e.getMessage());
                throw new RuntimeException("Lỗi khi tạo yêu cầu hoàn tiền một phần: " + e.getMessage());
            }
        } else {
            // Hoàn tiền qua MoMo/VNPAY - tự động hoàn tiền một phần
            try {
                // Gọi API MoMo để hoàn tiền tự động
                if ("MOMO".equalsIgnoreCase(order.getPaymentMethod())) {
                    try {
                        refundService.createRefundRequest(order, refundAmount);

                        // Thông báo cho buyer
                        try {
                            notificationService.createUserNotification(
                                    order.getBuyer().getId(),
                                    "Đã hoàn tiền một phần",
                                    String.format("Đã hoàn %,.0f đ vào ví MoMo của bạn cho đơn #%s", 
                                            refundAmount.doubleValue(), order.getId()),
                                    order.getId());
                        } catch (Exception e) {
                            log.warn("Error sending refund notification to buyer: {}", e.getMessage());
                        }
                    } catch (Exception momoEx) {
                        log.error("MoMo partial refund failed for order {}: {}", order.getId(), momoEx.getMessage());

                        // Thông báo admin xử lý thủ công
                        notificationService.createAdminNotification(
                                "Lỗi hoàn tiền một phần MoMo",
                                String.format(
                                        "Đơn hàng #%s: Không thể hoàn tiền tự động %,.0f đ qua MoMo cho khách hàng %s. " +
                                        "Lỗi: %s. Vui lòng xử lý thủ công với transactionId: %s",
                                        order.getId(),
                                        refundAmount.doubleValue(),
                                        returnRequest.getBuyer().getFullName(),
                                        momoEx.getMessage(),
                                        order.getMomoTransId()),
                                Notification.NotificationType.REFUND_REQUEST.name(),
                                order.getId());
                        
                        throw new RuntimeException("Lỗi khi hoàn tiền một phần qua MoMo: " + momoEx.getMessage());
                    }
                } else {
                    // VNPAY hoặc payment method khác - thông báo admin xử lý thủ công
                    notificationService.createAdminNotification(
                            "Yêu cầu hoàn tiền một phần (" + order.getPaymentMethod() + ")",
                            String.format(
                                    "Đơn hàng #%s: Cần hoàn tiền MỘT PHẦN %,.0f đ cho khách hàng %s qua %s. ",
                                    order.getId(),
                                    refundAmount.doubleValue(),
                                    returnRequest.getBuyer().getFullName(),
                                    order.getPaymentMethod()),
                            Notification.NotificationType.REFUND_REQUEST.name(),
                            order.getId());
                }

                log.info("Created partial refund request for {} order {}, amount: {}", 
                        order.getPaymentMethod(), order.getId(), refundAmount);
            } catch (Exception e) {
                log.error("Error creating partial refund request: {}", e.getMessage());
                throw new RuntimeException("Lỗi khi tạo yêu cầu hoàn tiền một phần: " + e.getMessage());
            }
        }
    }
}
