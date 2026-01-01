package com.example.e_commerce_techshop.services.address;

import java.util.List;

import com.example.e_commerce_techshop.dtos.buyer.address.AddressDTO;
import com.example.e_commerce_techshop.dtos.buyer.address.CreateAddressDTO;
import com.example.e_commerce_techshop.dtos.buyer.address.UpdateAddressDTO;
import com.example.e_commerce_techshop.models.User;

public interface IAddressService {

    /** Lấy địa chỉ của user */
    List<AddressDTO> getUserAddress(String userEmail) throws Exception;

    /** Tạo địa chỉ mới */
    AddressDTO createAddress(User user, CreateAddressDTO createAddressDTO) throws Exception;

    /** Cập nhật địa chỉ */
    AddressDTO updateAddress(User user, String addressId, UpdateAddressDTO updateAddressDTO) throws Exception;
    
    /**
     * Xóa địa chỉ của user
     * @param user
     * @param addressId ID hoặc index của địa chỉ cần xóa (có thể null để xóa tất cả)
     */
    void deleteAddress(User user, String addressId) throws Exception;

    /** Kiểm tra user có địa chỉ không */
    boolean hasAddress(String userEmail);
}