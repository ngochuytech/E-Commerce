package com.example.e_commerce_techshop.services.store;

import com.example.e_commerce_techshop.dtos.b2c.store.StoreDTO;
import com.example.e_commerce_techshop.dtos.b2c.store.UpdateStoreDTO;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.StoreResponse;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IStoreService {
    Store getStoreByIdAndOwnerId(String storeId, String ownerId) throws Exception;  
    // Shop (B2C)
    StoreResponse createStore(StoreDTO storeDTO, User owner, MultipartFile logo) throws Exception;
    StoreResponse updateStore(String storeId, UpdateStoreDTO storeDTO) throws Exception;
    void updateStoreLogo(String storeId, MultipartFile logo) throws Exception;
    void updateStoreBanner(String storeId, MultipartFile banner) throws Exception;
    StoreResponse getStoreById(String storeId) throws Exception;
    List<StoreResponse> getAllStores();
    List<Store> getStoresByOwner(String ownerId);
    Page<StoreResponse> getStoresByOwner(String ownerId, Pageable pageable);

    // Kiểm tra shop có bị banned không và throw exception nếu bị banned
    void validateStoreNotBanned(String storeId) throws Exception;

    // Admin
    StoreResponse approveStore(String storeId) throws Exception;
    StoreResponse rejectStore(String storeId, String reason) throws Exception;
    Page<StoreResponse> getPendingStores(Pageable pageable);
    Page<StoreResponse> getApprovedStores(Pageable pageable);
    void updateStoreStatus(String storeId, String status) throws Exception;
    
    // Ban/Unban store
    StoreResponse banStore(String storeId, String reason) throws Exception;
    StoreResponse unbanStore(String storeId) throws Exception;
}
