package com.example.e_commerce_techshop.services.store;

import com.example.e_commerce_techshop.dtos.b2c.store.StoreDTO;
import com.example.e_commerce_techshop.dtos.b2c.store.UpdateStoreDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.exceptions.StoreBannedException;
import com.example.e_commerce_techshop.models.Address;
import com.example.e_commerce_techshop.models.Notification;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.repositories.StoreRepository;
import com.example.e_commerce_techshop.responses.StoreResponse;
import com.example.e_commerce_techshop.services.FileUploadService;
import com.example.e_commerce_techshop.services.notification.INotificationService;
import com.example.e_commerce_techshop.services.productVariant.ProductVariantSerivce;
import com.example.e_commerce_techshop.services.refund.IRefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService implements IStoreService {
    
    private final StoreRepository storeRepository;
    private final FileUploadService fileUploadService;
    private final INotificationService notificationService;
    private final OrderRepository orderRepository;
    private final IRefundService refundService;
    private final ProductVariantSerivce productVariantService;

    @Override
    public StoreResponse createStore(StoreDTO storeDTO, User owner, MultipartFile logo) throws Exception {
     
        // Upload logo if provided
        String logoUrl = null;
        if (logo != null && !logo.isEmpty()) {
            logoUrl = fileUploadService.uploadFile(logo, "stores");
        }

        // Create store
        Store store = Store.builder()
                .name(storeDTO.getName())
                .description(storeDTO.getDescription())
                .logoUrl(logoUrl)
                .banner_url(null)
                .status(Store.StoreStatus.PENDING.name())
                .owner(owner)
                .address(Address.builder()
                    .province(storeDTO.getAddress().getProvince())
                    .ward(storeDTO.getAddress().getWard())
                    .homeAddress(storeDTO.getAddress().getHomeAddress())
                    .suggestedName(storeDTO.getAddress().getSuggestedName())
                    .build())
                .build();

        Store savedStore = storeRepository.save(store);
        
        // Tạo notification cho admin
        try {
            notificationService.createAdminNotification(
                "Cửa hàng mới đăng ký: " + savedStore.getName(),
                "Cửa hàng " + savedStore.getName() + " tại địa chỉ " + savedStore.getAddress().getHomeAddress() + " chờ phê duyệt",
                "STORE_APPROVAL",
                savedStore.getId()
            );
        } catch (Exception e) {
            log.error("Error creating admin notification for store: {}", e.getMessage());
        }
        
        return StoreResponse.fromStore(savedStore);
    }

    @Override
    public StoreResponse updateStore(String storeId, UpdateStoreDTO updateStoreDTO) throws Exception {
        
        Store existingStore = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));

        // Update fields
        existingStore.setName(updateStoreDTO.getName());
        existingStore.setDescription(updateStoreDTO.getDescription());

        existingStore.setAddress(Address.builder()
                .province(updateStoreDTO.getAddress().getProvince())
                .ward(updateStoreDTO.getAddress().getWard())
                .homeAddress(updateStoreDTO.getAddress().getHomeAddress())
                .suggestedName(updateStoreDTO.getAddress().getSuggestedName())
                .build());
        
        Store updatedStore = storeRepository.save(existingStore);
        
        return StoreResponse.fromStore(updatedStore);
    }

    @Override
    public StoreResponse getStoreById(String storeId) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));
        return StoreResponse.fromStore(store);
    }

    @Override
    public List<StoreResponse> getAllStores() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream().map(StoreResponse::fromStore).toList();
    }

    @Override
    public List<Store> getStoresByOwner(String ownerId) {
        List<Store> stores = storeRepository.findByOwnerId(ownerId);
        return stores;
    }

    @Override
    public StoreResponse approveStore(String storeId) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));
        
        store.setStatus(Store.StoreStatus.APPROVED.name());
        Store updatedStore = storeRepository.save(store);
        return StoreResponse.fromStore(updatedStore);
    }

    @Override
    public StoreResponse rejectStore(String storeId, String reason) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));
        
        store.setStatus(Store.StoreStatus.REJECTED.name());
        Store updatedStore = storeRepository.save(store);
        return StoreResponse.fromStore(updatedStore);
    }

    @Override
    public Page<StoreResponse> getPendingStores(Pageable pageable) {
        Page<Store> stores = storeRepository.findByStatus(Store.StoreStatus.PENDING.name(), pageable);
        return stores.map(StoreResponse::fromStore);
    }

    @Override
    public Page<StoreResponse> getApprovedStores(Pageable pageable) {
        Page<Store> stores = storeRepository.findByStatus(Store.StoreStatus.APPROVED.name(), pageable);
        return stores.map(StoreResponse::fromStore);
    }

    @Override
    public void updateStoreStatus(String storeId, String status) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));
        
        if (!Store.isValidStatus(status)) {
            String validStatuses = String.join(", ", Store.getValidStatuses());
            throw new IllegalArgumentException("Status không hợp lệ: '" + status + "'. Chỉ chấp nhận: " + validStatuses);
        }
        
        // Only ADMIN can set to APPROVED/REJECTED; owner/admin can set DELETED; anyone can set PENDING? keep existing rules minimal
        if (!"DELETED".equalsIgnoreCase(status)) {
            // TODO: check role from security context; currently all permitted as security is open
        }
        
        store.setStatus(status.toUpperCase());
        storeRepository.save(store);
    }

    @Override
    public Page<StoreResponse> getStoresByOwner(String ownerId, Pageable pageable) {
        Page<Store> stores = storeRepository.findByOwnerId(ownerId, pageable);
        return stores.map(StoreResponse::fromStore);
    }

    @Override
    public void updateStoreLogo(String storeId, MultipartFile logo) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));

        if (!Store.StoreStatus.APPROVED.name().equals(store.getStatus())) {
            throw new IllegalStateException("Chỉ có thể cập nhật logo cho cửa hàng đã được duyệt");
        }

        if (store.getLogoUrl() != null && !store.getLogoUrl().isEmpty()) {
            fileUploadService.deleteFile(store.getLogoUrl());
        }

        String logoUrl = fileUploadService.uploadFile(logo, "stores");
        store.setLogoUrl(logoUrl);
        storeRepository.save(store);
    }

    @Override
    public void updateStoreBanner(String storeId, MultipartFile banner) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));

        if (!Store.StoreStatus.APPROVED.name().equals(store.getStatus())) {
            throw new IllegalStateException("Chỉ có thể cập nhật banner cho cửa hàng đã được duyệt");
        }

        if (store.getBanner_url() != null && !store.getBanner_url().isEmpty()) {
            fileUploadService.deleteFile(store.getBanner_url());
        }

        String bannerUrl = fileUploadService.uploadFile(banner, "stores");
        store.setBanner_url(bannerUrl);
        storeRepository.save(store);
    }

    @Override
    public Store getStoreByIdAndOwnerId(String storeId, String ownerId) throws Exception {
        return storeRepository.findByIdAndOwnerId(storeId, ownerId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng hoặc bạn không phải chủ sở hữu"));
    }

    @Override
    public void validateStoreNotBanned(String storeId) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng với ID: " + storeId));
        
        if (Store.StoreStatus.BANNED.name().equals(store.getStatus())) {
            throw new StoreBannedException(storeId, store.getName());
        }
    }

    @Override
    public StoreResponse banStore(String storeId, String reason) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));
        
        // Cập nhật status thành BANNED
        store.setStatus(Store.StoreStatus.BANNED.name());
        storeRepository.save(store);
        
        // Xóa toàn bộ cache product variants trong Redis
        productVariantService.clearAllProductVariantCache();
        
        log.info("Store {} has been banned. Reason: {}", storeId, reason);
        
        // Thông báo cho shop owner
        notificationService.createUserNotification(
                store.getOwner().getId(),
                "Cửa hàng đã bị khóa",
                String.format("Cửa hàng '%s' của bạn đã bị khóa bởi quản trị viên. Lý do: %s", 
                        store.getName(), reason),
                store.getId()
        );
        
        // Hủy tất cả đơn hàng PENDING
        cancelPendingOrdersForBannedStore(store);
        
        // Thông báo admin
        notificationService.createAdminNotification(
                "Cửa hàng đã bị ban",
                String.format("Cửa hàng '%s' (ID: %s) đã bị ban. Lý do: %s", 
                        store.getName(), storeId, reason),
                Notification.NotificationType.SYSTEM.name(),
                storeId
        );
        
        return StoreResponse.fromStore(store);
    }

    @Override
    public StoreResponse unbanStore(String storeId) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));
        
        if (!Store.StoreStatus.BANNED.name().equals(store.getStatus())) {
            throw new IllegalStateException("Cửa hàng không ở trạng thái bị ban");
        }
        
        // Reset warning count khi unban
        store.setReturnWarningCount(0);
        store.setLastWarningMonth(null);
        
        // Cập nhật status thành APPROVED
        store.setStatus(Store.StoreStatus.APPROVED.name());
        storeRepository.save(store);
        
        // Xóa toàn bộ cache product variants trong Redis
        productVariantService.clearAllProductVariantCache();
        
        log.info("Store {} has been unbanned", storeId);
        
        // Thông báo cho shop owner
        notificationService.createUserNotification(
                store.getOwner().getId(),
                "Cửa hàng đã được gỡ khóa",
                String.format("Cửa hàng '%s' của bạn đã được gỡ khóa và có thể hoạt động trở lại.", 
                        store.getName()),
                store.getId()
        );
        
        // Thông báo admin
        notificationService.createAdminNotification(
                "Cửa hàng đã được gỡ ban",
                String.format("Cửa hàng '%s' (ID: %s) đã được gỡ ban và có thể hoạt động trở lại.", 
                        store.getName(), storeId),
                Notification.NotificationType.SYSTEM.name(),
                storeId
        );
        
        return StoreResponse.fromStore(store);
    }

    private void cancelPendingOrdersForBannedStore(Store store) {
        try {
            // Lấy các đơn hàng PENDING của shop
            List<Order> pendingOrders = orderRepository.findByStoreIdAndStatus(store.getId(),
                    Order.OrderStatus.PENDING.name());

            log.info("Found {} pending orders to cancel for banned store {}", pendingOrders.size(), store.getId());

            for (Order order : pendingOrders) {
                try {
                    // Hủy đơn hàng
                    order.setStatus(Order.OrderStatus.CANCELLED.name());
                    orderRepository.save(order);

                    // Nếu đã thanh toán, tạo yêu cầu hoàn tiền
                    if (Order.PaymentStatus.PAID.name().equals(order.getPaymentStatus())) {
                        if ("MOMO".equals(order.getPaymentMethod()) || "VNPAY".equals(order.getPaymentMethod())) {
                            // Tự động hoàn tiền
                            try {
                                refundService.createRefundRequest(order, order.getTotalPrice());
                                log.info("Auto refund initiated for cancelled order {} due to store ban",
                                        order.getId());
                            } catch (Exception e) {
                                log.error("Error processing auto refund for order {}: {}", order.getId(),
                                        e.getMessage());
                                // Thông báo admin xử lý thủ công
                                notificationService.createAdminNotification(
                                        "Lỗi hoàn tiền tự động - Shop bị ban",
                                        String.format(
                                                "Đơn hàng #%s: Không thể hoàn tiền tự động %,.0f đ cho khách %s. Shop %s bị ban. Vui lòng xử lý thủ công.",
                                                order.getId(), order.getTotalPrice().doubleValue(),
                                                order.getBuyer().getFullName(), store.getName()),
                                        Notification.NotificationType.REFUND_REQUEST.name(),
                                        order.getId());
                            }
                        }
                    }

                    // Thông báo cho khách hàng
                    notificationService.createUserNotification(
                            order.getBuyer().getId(),
                            "Đơn hàng đã bị hủy",
                            String.format(
                                    "Đơn hàng #%s từ cửa hàng %s đã bị hủy do cửa hàng vi phạm chính sách. Nếu bạn đã thanh toán, tiền sẽ được hoàn lại.",
                                    order.getId(), store.getName()),
                            order.getId());

                    log.info("Cancelled pending order {} for banned store {}", order.getId(), store.getId());
                } catch (Exception e) {
                    log.error("Error cancelling order {} for banned store: {}", order.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error cancelling pending orders for banned store {}: {}", store.getId(), e.getMessage());
        }
    }
}