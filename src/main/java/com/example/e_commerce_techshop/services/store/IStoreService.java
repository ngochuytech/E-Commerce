package com.example.e_commerce_techshop.services.store;

import com.example.e_commerce_techshop.dtos.b2c.store.StoreDTO;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.responses.StoreResponse;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface IStoreService {
    // Store Management APIs
    StoreResponse createStore(StoreDTO storeDTO, MultipartFile logo) throws Exception;
    Store uploadBanner(String storeId, MultipartFile banner) throws Exception;
    StoreResponse updateStore(String storeId, StoreDTO storeDTO) throws Exception;
    StoreResponse getStoreById(String storeId) throws Exception;
    List<StoreResponse> getAllStores();
    List<StoreResponse> getStoresByOwner(String ownerId);

    // Store Approval APIs
    StoreResponse approveStore(String storeId) throws Exception;
    StoreResponse rejectStore(String storeId, String reason) throws Exception;
    List<StoreResponse> getPendingStores();
    List<StoreResponse> getApprovedStores();
    
    // Store Status Management
    void updateStoreStatus(String storeId, String status) throws Exception;
}
