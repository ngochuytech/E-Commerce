package com.example.e_commerce_techshop.services.shipping;

import com.example.e_commerce_techshop.models.Address;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service tính phí ship theo vùng miền Việt Nam
 * Đơn giản, không phụ thuộc API bên thứ 3, dễ mở rộng
 * 
 * Hỗ trợ tất cả 63 tỉnh/thành phố Việt Nam (cập nhật 2024):
 * - Miền Bắc: 25 tỉnh/thành
 * - Miền Trung: 19 tỉnh/thành  
 * - Miền Nam: 19 tỉnh/thành
 * 
 * Tự động chuẩn hóa tên tỉnh/thành từ các định dạng:
 * - "Thành phố Hà Nội" -> "Hà Nội"
 * - "Tỉnh Thanh Hóa" -> "Thanh Hóa"
 * - "TP.HCM" / "Sài Gòn" -> "Hồ Chí Minh"
 */
@Service
@Slf4j
public class RegionalShippingService {
    
    // Phí ship cơ bản theo khoảng cách
    private static final BigDecimal SAME_CITY_FEE = BigDecimal.valueOf(15000);      // Cùng tỉnh/thành
    private static final BigDecimal SAME_REGION_FEE = BigDecimal.valueOf(30000);    // Cùng vùng
    private static final BigDecimal NEARBY_REGION_FEE = BigDecimal.valueOf(45000);  // Vùng lân cận
    private static final BigDecimal FAR_REGION_FEE = BigDecimal.valueOf(60000);     // Vùng xa
    
    // Phụ phí theo trọng lượng (mỗi 1kg thêm)
    private static final BigDecimal WEIGHT_SURCHARGE_PER_KG = BigDecimal.valueOf(5000);
    
    // Định nghĩa 3 vùng miền
    private static final Map<String, Set<String>> REGIONS = new HashMap<>();
    
    static {
        // Vùng Bắc (Miền Bắc) - 25 tỉnh/thành
        REGIONS.put("NORTH", new HashSet<>(Arrays.asList(
            "Hà Nội", "Hải Phòng", "Hải Dương", "Hưng Yên", "Bắc Ninh", "Vĩnh Phúc",
            "Thái Nguyên", "Quảng Ninh", "Bắc Giang", "Lạng Sơn", "Cao Bằng", "Hà Giang",
            "Lai Châu", "Sơn La", "Điện Biên", "Lào Cai", "Yên Bái", "Tuyên Quang",
            "Phú Thọ", "Hòa Bình", "Ninh Bình", "Nam Định", "Thái Bình", "Hà Nam", "Bắc Kạn"
        )));
        
        // Vùng Trung (Miền Trung) - 19 tỉnh/thành
        REGIONS.put("CENTRAL", new HashSet<>(Arrays.asList(
            "Thanh Hóa", "Nghệ An", "Hà Tĩnh", "Quảng Bình", "Quảng Trị", "Thừa Thiên Huế",
            "Đà Nẵng", "Quảng Nam", "Quảng Ngãi", "Bình Định", "Phú Yên", "Khánh Hòa",
            "Ninh Thuận", "Bình Thuận", "Kon Tum", "Gia Lai", "Đắk Lắk", "Đắk Nông", "Lâm Đồng"
        )));
        
        // Vùng Nam (Miền Nam) - 19 tỉnh/thành
        REGIONS.put("SOUTH", new HashSet<>(Arrays.asList(
            "Hồ Chí Minh", "Bình Dương", "Đồng Nai", "Bà Rịa - Vũng Tàu", "Tây Ninh",
            "Bình Phước", "Long An", "Tiền Giang", "Bến Tre", "Trà Vinh", "Vĩnh Long",
            "Đồng Tháp", "An Giang", "Kiên Giang", "Cần Thơ", "Hậu Giang", "Sóc Trăng",
            "Bạc Liêu", "Cà Mau"
        )));
    }
    
    /**
     * Tính phí ship dựa trên vùng miền và trọng lượng
     * 
     * @param fromAddress Địa chỉ gửi hàng
     * @param toAddress Địa chỉ nhận hàng
     * @param weightInGrams Trọng lượng (gram)
     * @return Phí ship (VND)
     */
    public BigDecimal calculateShippingFee(Address fromAddress, Address toAddress, Integer weightInGrams) {
        if (fromAddress == null || toAddress == null) {
            log.warn("Address is null, using default fee");
            return SAME_REGION_FEE;
        }
        
        String fromProvince = normalizeProvinceName(fromAddress.getProvince());
        String toProvince = normalizeProvinceName(toAddress.getProvince());
        
        // 1. Cùng tỉnh/thành
        if (fromProvince.equalsIgnoreCase(toProvince)) {
            BigDecimal baseFee = SAME_CITY_FEE;
            BigDecimal weightSurcharge = calculateWeightSurcharge(weightInGrams);
            log.info("Shipping fee (same city): {} + {} = {}", baseFee, weightSurcharge, baseFee.add(weightSurcharge));
            return baseFee.add(weightSurcharge);
        }
        
        // 2. Xác định vùng của từng tỉnh
        String fromRegion = getRegion(fromProvince);
        String toRegion = getRegion(toProvince);
        
        if (fromRegion == null || toRegion == null) {
            log.warn("Cannot determine region for: {} -> {}, using same region fee", fromProvince, toProvince);
            return SAME_REGION_FEE;
        }
        
        // 3. Tính phí theo khoảng cách vùng
        BigDecimal baseFee = calculateBaseFeeByRegion(fromRegion, toRegion);
        BigDecimal weightSurcharge = calculateWeightSurcharge(weightInGrams);
        
        BigDecimal totalFee = baseFee.add(weightSurcharge);
        log.info("Shipping fee: {} ({}) -> {} ({}): Base={}, Weight={}, Total={}", 
                fromProvince, fromRegion, toProvince, toRegion, baseFee, weightSurcharge, totalFee);
        
        return totalFee;
    }
    
    /**
     * Tính phí cơ bản theo khoảng cách vùng
     */
    private BigDecimal calculateBaseFeeByRegion(String fromRegion, String toRegion) {
        // Cùng vùng
        if (fromRegion.equals(toRegion)) {
            return SAME_REGION_FEE;
        }
        
        // Bắc <-> Trung hoặc Trung <-> Nam (vùng lân cận)
        if ((fromRegion.equals("NORTH") && toRegion.equals("CENTRAL")) ||
            (fromRegion.equals("CENTRAL") && toRegion.equals("NORTH")) ||
            (fromRegion.equals("CENTRAL") && toRegion.equals("SOUTH")) ||
            (fromRegion.equals("SOUTH") && toRegion.equals("CENTRAL"))) {
            return NEARBY_REGION_FEE;
        }
        
        // Bắc <-> Nam (vùng xa nhất)
        if ((fromRegion.equals("NORTH") && toRegion.equals("SOUTH")) ||
            (fromRegion.equals("SOUTH") && toRegion.equals("NORTH"))) {
            return FAR_REGION_FEE;
        }
        
        return SAME_REGION_FEE;
    }
    
    /**
     * Tính phụ phí theo trọng lượng
     * Mỗi 1kg thêm 5,000đ
     */
    private BigDecimal calculateWeightSurcharge(Integer weightInGrams) {
        if (weightInGrams == null || weightInGrams <= 1000) {
            return BigDecimal.ZERO; // <= 1kg không tính phụ phí
        }
        
        // Tính số kg vượt quá 1kg đầu tiên
        int extraKg = (weightInGrams - 1000) / 1000;
        if ((weightInGrams - 1000) % 1000 > 0) {
            extraKg++; // Làm tròn lên
        }
        
        return WEIGHT_SURCHARGE_PER_KG.multiply(BigDecimal.valueOf(extraKg));
    }
    
    /**
     * Xác định vùng miền của tỉnh/thành
     */
    private String getRegion(String province) {
        for (Map.Entry<String, Set<String>> entry : REGIONS.entrySet()) {
            if (entry.getValue().contains(province)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Chuẩn hóa tên tỉnh/thành (loại bỏ TP., Thành phố, Tỉnh)
     * Hỗ trợ cả định dạng chính thức từ JSON và định dạng thông thường
     */
    private String normalizeProvinceName(String province) {
        if (province == null) return "";
        
        String normalized = province.trim();
        
        // Loại bỏ tiền tố chính thức
        normalized = normalized.replaceAll("^(TP\\.|Thành phố|Tỉnh)\\s+", "");
        
        // Mapping các tên viết khác nhau
        Map<String, String> nameMapping = new HashMap<>();
        nameMapping.put("TP.HCM", "Hồ Chí Minh");
        nameMapping.put("TPHCM", "Hồ Chí Minh");
        nameMapping.put("Sài Gòn", "Hồ Chí Minh");
        nameMapping.put("Saigon", "Hồ Chí Minh");
        nameMapping.put("HCM", "Hồ Chí Minh");
        nameMapping.put("Huế", "Thừa Thiên Huế");
        nameMapping.put("Bà Rịa-Vũng Tàu", "Bà Rịa - Vũng Tàu");
        
        return nameMapping.getOrDefault(normalized, normalized);
    }
    
    /**
     * Lấy danh sách tất cả tỉnh/thành theo vùng (dùng cho API test)
     */
    public Map<String, Set<String>> getAllProvincesByRegion() {
        return new HashMap<>(REGIONS);
    }
    
    /**
     * Kiểm tra tỉnh/thành có được hỗ trợ không
     */
    public boolean isProvinceSupported(String province) {
        String normalized = normalizeProvinceName(province);
        return getRegion(normalized) != null;
    }
}
