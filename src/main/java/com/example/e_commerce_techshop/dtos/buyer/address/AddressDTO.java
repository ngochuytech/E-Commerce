package com.example.e_commerce_techshop.dtos.buyer.address;

import lombok.*;

import com.example.e_commerce_techshop.models.Address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    
    private String id; // Chỉ có khi update hoặc response
    
    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    @Size(max = 255, message = "Tỉnh/Thành phố không được vượt quá 255 ký tự")
    private String province;

    @NotBlank(message = "Quận/Huyện không được để trống")
    @Size(max = 255, message = "Quận/Huyện không được vượt quá 255 ký tự")
    private String district;

    @NotBlank(message = "Phường/Xã không được để trống")
    @Size(max = 255, message = "Phường/Xã không được vượt quá 255 ký tự")
    private String ward;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    @Size(max = 255, message = "Địa chỉ chi tiết không được vượt quá 255 ký tự")
    private String homeAddress;

    @Size(max = 255, message = "Tên gợi ý không được vượt quá 255 ký tự")
    private String suggestedName;
    
    /**
     * Convert AddressDTO to Address entity
     */
    public static Address toEntity(AddressDTO dto) {
        return Address.builder()
                .province(dto.getProvince())
                .district(dto.getDistrict())
                .ward(dto.getWard())
                .homeAddress(dto.getHomeAddress())
                .suggestedName(dto.getSuggestedName())
                .build();
    }
    
    /**
     * Convert Address entity to AddressDTO
     */
    public static AddressDTO fromEntity(Address address) {
        return AddressDTO.builder()
                .id(null) // No ID for embedded documents
                .province(address.getProvince())
                .district(address.getDistrict())
                .ward(address.getWard())
                .homeAddress(address.getHomeAddress())
                .suggestedName(address.getSuggestedName())
                .build();
    }
    
    /**
     * Get full address as string
     */
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        
        if (homeAddress != null && !homeAddress.trim().isEmpty()) {
            fullAddress.append(homeAddress.trim());
        }
        
        if (ward != null && !ward.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(ward.trim());
        }
        
        if (district != null && !district.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(district.trim());
        }
        
        if (province != null && !province.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(province.trim());
        }
        
        return fullAddress.toString();
    }
}