package com.example.e_commerce_techshop.services.address;

import com.example.e_commerce_techshop.dtos.buyer.address.AddressDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Address;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService implements IAddressService {

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
            // Update existing embedded address
            address = user.getAddress();
            address.setProvince(addressDTO.getProvince());
            address.setWard(addressDTO.getWard());
            address.setHomeAddress(addressDTO.getHomeAddress());
            address.setSuggestedName(addressDTO.getSuggestedName());
        } else {
            // Create new embedded address
            address = AddressDTO.toEntity(addressDTO);
            user.setAddress(address);
        }
        
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
        
        user.setAddress(null);
        userRepository.save(user);
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