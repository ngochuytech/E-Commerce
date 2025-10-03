package com.example.e_commerce_techshop.services.address;

import com.example.e_commerce_techshop.dtos.buyer.address.AddressDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Address;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.AddressRepository;
import com.example.e_commerce_techshop.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService implements IAddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public AddressDTO getUserAddress(String userEmail) throws Exception {
        User user = getUserByEmail(userEmail);
        
        if (user.getAddress() == null) {
            throw new DataNotFoundException("Người dùng chưa có địa chỉ");
        }
        
        return AddressDTO.fromEntity(user.getAddress());
    }

    @Override
    @Transactional
    public AddressDTO createOrUpdateAddress(String userEmail, AddressDTO addressDTO) throws Exception {
        User user = getUserByEmail(userEmail);
        
        Address address;
        
        if (user.getAddress() != null) {
            // Update existing address
            address = user.getAddress();
            address.setProvince(addressDTO.getProvince());
            address.setDistrict(addressDTO.getDistrict());
            address.setWard(addressDTO.getWard());
            address.setHomeAddress(addressDTO.getHomeAddress());
            address.setSuggestedName(addressDTO.getSuggestedName());
        } else {
            // Create new address
            address = AddressDTO.toEntity(addressDTO);
            address = addressRepository.save(address);
            
            // Link address to user
            user.setAddress(address);
        }
        
        // Save changes
        address = addressRepository.save(address);
        userRepository.save(user);
        
        return AddressDTO.fromEntity(address);
    }

    @Override
    @Transactional
    public void deleteAddress(String userEmail) throws Exception {
        User user = getUserByEmail(userEmail);
        
        if (user.getAddress() == null) {
            throw new DataNotFoundException("Người dùng không có địa chỉ để xóa");
        }
        
        Address address = user.getAddress();
        
        // Unlink address from user
        user.setAddress(null);
        userRepository.save(user);
        
        // Delete address
        addressRepository.delete(address);
    }

    @Override
    public boolean hasAddress(String userEmail) {
        try {
            User user = getUserByEmail(userEmail);
            return user.getAddress() != null;
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