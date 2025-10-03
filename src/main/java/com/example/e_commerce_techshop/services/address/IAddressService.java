package com.example.e_commerce_techshop.services.address;

import com.example.e_commerce_techshop.dtos.buyer.address.AddressDTO;

public interface IAddressService {
    
    /**
     * Lấy địa chỉ của user (chỉ có 1 địa chỉ/user)
     */
    AddressDTO getUserAddress(String userEmail) throws Exception;
    
    /**
     * Tạo hoặc cập nhật địa chỉ của user
     */
    AddressDTO createOrUpdateAddress(String userEmail, AddressDTO addressDTO) throws Exception;
    
    /**
     * Xóa địa chỉ của user
     */
    void deleteAddress(String userEmail) throws Exception;
    
    /**
     * Kiểm tra user có địa chỉ không
     */
    boolean hasAddress(String userEmail);
}