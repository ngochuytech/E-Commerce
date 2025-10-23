package com.example.e_commerce_techshop.services.address;

import com.example.e_commerce_techshop.dtos.buyer.address.AddressDTO;
import com.example.e_commerce_techshop.dtos.buyer.address.CreateAddressDTO;
import com.example.e_commerce_techshop.dtos.buyer.address.UpdateAddressDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Address;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService implements IAddressService {

    private final UserRepository userRepository;

    @Override
    public List<AddressDTO> getUserAddress(String userEmail) throws Exception {
        User user = getUserByEmail(userEmail);
        
        if (user.getAddress() == null || user.getAddress().isEmpty()) {
            throw new DataNotFoundException("Người dùng chưa có địa chỉ");
        }
        
        return user.getAddress().stream()
                .map(AddressDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public AddressDTO createAddress(User user, CreateAddressDTO createAddressDTO) throws Exception {
        List<Address> addresses = user.getAddress();
        if (addresses == null) {
            addresses = new ArrayList<>();
        }
        
        // Tạo địa chỉ mới
        Address newAddress = CreateAddressDTO.toEntity(createAddressDTO);
        newAddress.setId(UUID.randomUUID().toString()); // 
        // Nếu đây là địa chỉ đầu tiên hoặc user muốn set làm default
        if (addresses.isEmpty() || createAddressDTO.isDefault()) {
            // Bỏ default của các địa chỉ khác
            addresses.forEach(addr -> addr.setDefault(false));
            newAddress.setDefault(true);
        } else {
            newAddress.setDefault(false);
        }
        
        addresses.add(newAddress);
        user.setAddress(addresses);
        userRepository.save(user);
        
        // Trả về AddressDTO với index làm ID
        return AddressDTO.builder()
                .id(String.valueOf(addresses.size() - 1))
                .province(newAddress.getProvince())
                .ward(newAddress.getWard())
                .homeAddress(newAddress.getHomeAddress())
                .suggestedName(newAddress.getSuggestedName())
                .phone(newAddress.getPhone())
                .isDefault(newAddress.isDefault())
                .build();
    }

    @Override
    @Transactional
    public AddressDTO updateAddress(User user, String addressId, UpdateAddressDTO updateAddressDTO) throws Exception {
        List<Address> addresses = user.getAddress();
        
        if (addresses == null || addresses.isEmpty()) {
            throw new DataNotFoundException("Người dùng chưa có địa chỉ nào");
        }
        
        // Update địa chỉ
        Address existingAddress = addresses.stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy địa chỉ với ID: " + addressId));
        existingAddress.setProvince(updateAddressDTO.getProvince());
        existingAddress.setWard(updateAddressDTO.getWard());
        existingAddress.setHomeAddress(updateAddressDTO.getHomeAddress());
        existingAddress.setSuggestedName(updateAddressDTO.getSuggestedName());
        existingAddress.setPhone(updateAddressDTO.getPhone());
        
        // Xử lý default
        if (updateAddressDTO.isDefault()) {
            // Bỏ default của các địa chỉ khác
            addresses.forEach(addr -> addr.setDefault(false));
            existingAddress.setDefault(true);
        }
        
        user.setAddress(addresses);
        userRepository.save(user);
        
        // Trả về AddressDTO
        return AddressDTO.builder()
                .id(addressId)
                .province(existingAddress.getProvince())
                .ward(existingAddress.getWard())
                .homeAddress(existingAddress.getHomeAddress())
                .suggestedName(existingAddress.getSuggestedName())
                .phone(existingAddress.getPhone())
                .isDefault(existingAddress.isDefault())
                .build();
    }

    @Override
    @Transactional
    public void deleteAddress(User user, String addressId) throws Exception {
        if (user.getAddress() == null || user.getAddress().isEmpty()) {
            throw new DataNotFoundException("Người dùng không có địa chỉ để xóa");
        }
        
        List<Address> addresses = user.getAddress();
        
        // Nếu có addressId, xóa địa chỉ cụ thể
        if (addressId != null && !addressId.isEmpty()) {
            // Có thể dùng index: addressId = "0", "1", "2"...
            try {
                int index = Integer.parseInt(addressId);
                if (index >= 0 && index < addresses.size()) {
                    addresses.remove(index);
                } else {
                    throw new DataNotFoundException("Không tìm thấy địa chỉ với ID: " + addressId);
                }
            } catch (NumberFormatException e) {
                throw new DataNotFoundException("ID địa chỉ không hợp lệ");
            }
        } else {
            // Nếu không có addressId, xóa tất cả
            addresses.clear();
        }
        
        user.setAddress(addresses);
        userRepository.save(user);
    }

    @Override
    public boolean hasAddress(String userEmail) {
        try {
            User user = getUserByEmail(userEmail);
            return user.getAddress() != null && !user.getAddress().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Helper method to get user by email
     */
    private User getUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với email: " + userEmail));
    }
}