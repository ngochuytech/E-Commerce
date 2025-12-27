package com.example.e_commerce_techshop.exceptions;

/**
 * Exception được ném ra khi cửa hàng bị banned cố gắng thực hiện các hành động bị cấm
 */
public class StoreBannedException extends RuntimeException {
    private String storeId;
    private String storeName;

    public StoreBannedException(String message) {
        super(message);
    }

    public StoreBannedException(String storeId, String storeName) {
        super(String.format("Cửa hàng '%s' đã bị khóa. Bạn không thể thực hiện hành động này. Vui lòng liên hệ admin để được hỗ trợ.", storeName));
        this.storeId = storeId;
        this.storeName = storeName;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getStoreName() {
        return storeName;
    }
}
