package com.example.e_commerce_techshop.services.store;

import com.example.e_commerce_techshop.dtos.b2c.store.StoreDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Address;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.StoreRepository;
import com.example.e_commerce_techshop.repositories.UserRepository;
import com.example.e_commerce_techshop.responses.StoreResponse;
import com.example.e_commerce_techshop.services.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService implements IStoreService {
    
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    @Override
    public StoreResponse createStore(StoreDTO storeDTO, MultipartFile logo) throws Exception {
        
        // Validate owner exists - if not provided, use a default owner for testing
        User owner;
        if (storeDTO.getOwnerId() != null && !storeDTO.getOwnerId().isEmpty()) {
            System.out.println("StoreService - Looking for user with ID: " + storeDTO.getOwnerId());
            owner = userRepository.findByEmail(storeDTO.getOwnerId())
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy chủ sở hữu với Email: " + storeDTO.getOwnerId()));
            System.out.println("StoreService - Found owner: " + owner.getFullName() + " (" + owner.getEmail() + ")");
        } else {
            // For testing purposes, get the first user as default owner
            System.out.println("StoreService - No ownerId provided, getting first user");
            owner = userRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new DataNotFoundException("Không có user nào trong hệ thống. Vui lòng tạo user trước!"));
            System.out.println("StoreService - Using default owner: " + owner.getFullName() + " (" + owner.getEmail() + ")");
        }

     
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
                .status(storeDTO.getStatus() == null || storeDTO.getStatus().isBlank() ? "PENDING" : storeDTO.getStatus())
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
    public StoreResponse updateStore(String storeId, StoreDTO storeDTO) throws Exception {
        System.out.println("StoreService - Updating store: " + storeId);
        System.out.println("StoreService - Logo URL: " + storeDTO.getLogoUrl());
        System.out.println("StoreService - Banner URL: " + storeDTO.getBannerUrl());
        
        Store existingStore = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));

        // Update fields
        existingStore.setName(storeDTO.getName());
        existingStore.setDescription(storeDTO.getDescription());
        existingStore.setLogoUrl(storeDTO.getLogoUrl());
        existingStore.setBanner_url(storeDTO.getBannerUrl());
        if (storeDTO.getStatus() != null && !storeDTO.getStatus().isBlank()) {
            existingStore.setStatus(storeDTO.getStatus());
        }

        existingStore.setAddress(Address.builder()
                .province(storeDTO.getAddress().getProvince())
                .ward(storeDTO.getAddress().getWard())
                .homeAddress(storeDTO.getAddress().getHomeAddress())
                .suggestedName(storeDTO.getAddress().getSuggestedName())
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
    public List<StoreResponse> getStoresByOwner(String ownerId) {
        List<Store> stores = storeRepository.findByOwnerId(ownerId);
        return stores.stream().map(StoreResponse::fromStore).toList();
    }

    @Override
    public StoreResponse approveStore(String storeId) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));
        
        store.setStatus("APPROVED");
        Store updatedStore = storeRepository.save(store);
        return StoreResponse.fromStore(updatedStore);
    }

    @Override
    public StoreResponse rejectStore(String storeId, String reason) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));
        
        store.setStatus("REJECTED");
        Store updatedStore = storeRepository.save(store);
        return StoreResponse.fromStore(updatedStore);
    }

    @Override
    public List<StoreResponse> getPendingStores() {
        List<Store> stores = storeRepository.findByStatus("PENDING");
        return stores.stream().map(StoreResponse::fromStore).toList();
    }

    @Override
    public List<StoreResponse> getApprovedStores() {
        List<Store> stores = storeRepository.findByStatus("APPROVED");
        return stores.stream().map(StoreResponse::fromStore).toList();
    }

    @Override
    public void updateStoreStatus(String storeId, String status) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));
        
        // ✅ VALIDATE STATUS
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
            System.out.println("Deleting old banner: " + store.getBanner_url());
            fileUploadService.deleteFile(store.getBanner_url());
        }

        // Upload new banner
        String bannerUrl = fileUploadService.uploadFile(banner, "stores");
        store.setBanner_url(bannerUrl);
        return storeRepository.save(store);
    }
}