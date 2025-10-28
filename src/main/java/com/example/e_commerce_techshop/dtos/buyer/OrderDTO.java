package com.example.e_commerce_techshop.dtos.buyer;

import lombok.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    @NotNull(message = "Danh sách sản phẩm không được để trống")
    @Valid
    private List<SelectedCartItem> selectedItems;
    
    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // COD, VNPAY, MOMO, etc.
    
    // Mã giảm giá của sàn (áp dụng cho tất cả đơn hàng)
    private PlatformPromotions platformPromotions;
    
    // Map: storeId -> StorePromotions (có thể có cả mã Order và Shipping)
    private Map<String, StorePromotions> storePromotions;
    
    private String note;

    @NotNull(message = "Địa chỉ không được để trống")
    @Valid
    private AddressDTO address;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlatformPromotions {
        private String orderPromotionCode;    // Mã giảm giá đơn hàng của sàn (có thể null)
        private String shippingPromotionCode; // Mã giảm giá vận chuyển của sàn (có thể null)
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StorePromotions {
        private String orderPromotionCode;    // Mã giảm giá đơn hàng của cửa hàng (có thể null)
        private String shippingPromotionCode; // Mã giảm giá vận chuyển của cửa hàng (có thể null)
    }

    @Data
    @Builder
    public static class AddressDTO {
        @NotBlank(message = "Tỉnh/Thành phố không được để trống")
        private String province;

        @NotBlank(message = "Phường/Xã không được để trống")
        private String ward;

        @NotBlank(message = "Địa chỉ chi tiết không được để trống")
        private String homeAddress;

        @NotBlank(message = "Số điện thoại không được để trống (address.phone)")
        private String phone;

        private String suggestedName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SelectedCartItem {
        @NotBlank(message = "Product variant ID không được để trống")
        private String productVariantId;
        
        private String colorId; // Có thể null nếu sản phẩm không có màu
    }
}
