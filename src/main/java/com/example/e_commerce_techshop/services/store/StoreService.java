package com.example.e_commerce_techshop.services.store;

import com.example.e_commerce_techshop.dtos.b2c.store.StoreDTO;
import com.example.e_commerce_techshop.dtos.b2c.store.UpdateStoreDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Address;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.StoreRepository;
import com.example.e_commerce_techshop.responses.StoreResponse;
import com.example.e_commerce_techshop.services.FileUploadService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService implements IStoreService {
    
    private final StoreRepository storeRepository;
    private final FileUploadService fileUploadService;

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
    public Store uploadBanner(String storeId, MultipartFile banner) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));

        // Delete old banner if exists
        if (store.getBanner_url() != null && !store.getBanner_url().isEmpty()) {
            fileUploadService.deleteFile(store.getBanner_url());
        }

        // Upload new banner
        String bannerUrl = fileUploadService.uploadFile(banner, "stores");
        store.setBanner_url(bannerUrl);
        return storeRepository.save(store);
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
                .orElse(null);
    }
}