package com.example.e_commerce_techshop.responses.buyer;

import com.example.e_commerce_techshop.dtos.buyer.address.AddressDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponse {
    
    private String message;        // Thông báo kết quả
    private AddressDTO address;    // Địa chỉ đơn lẻ
    private List<AddressDTO> addresses; // Danh sách địa chỉ (không dùng trong OneToOne)
    private Long count;           // Số lượng địa chỉ
    private Boolean hasAddress;   // Có địa chỉ hay không
    
    /**
     * Single address response
     */
    public static AddressResponse single(String message, AddressDTO address) {
        return AddressResponse.builder()
                .message(message)
                .address(address)
                .build();
    }
    
    /**
     * Multiple addresses response
     */
    public static AddressResponse list(String message, List<AddressDTO> addresses) {
        return AddressResponse.builder()
                .message(message)
                .addresses(addresses)
                .count((long) addresses.size())
                .build();
    }
    
    /**
     * Address check response
     */
    public static AddressResponse check(String message, boolean hasAddress, long count) {
        return AddressResponse.builder()
                .message(message)
                .hasAddress(hasAddress)
                .count(count)
                .build();
    }
    
    /**
     * Simple message response
     */
    public static AddressResponse message(String message) {
        return AddressResponse.builder()
                .message(message)
                .build();
    }
}