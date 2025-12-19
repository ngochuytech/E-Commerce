package com.example.e_commerce_techshop.controllers.admin;

import com.example.e_commerce_techshop.models.Address;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.shipping.RegionalShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("${api.prefix}/test/shipping")
@RequiredArgsConstructor
@Tag(name = "Test Shipping", description = "API test tính phí ship theo vùng miền")
public class TestShippingController {
    
    private final RegionalShippingService regionalShippingService;
    
    /**
     * Test tính phí ship theo vùng
     */
    @GetMapping("/calculate")
    @Operation(summary = "Test tính phí ship", description = "Tính phí ship từ tỉnh đến tỉnh")
    public ResponseEntity<?> calculateShippingFee(
            @RequestParam String fromProvince,
            @RequestParam String toProvince,
            @RequestParam(defaultValue = "1") Integer quantity) {
        
        Address fromAddress = Address.builder().province(fromProvince).build();
        Address toAddress = Address.builder().province(toProvince).build();
        Integer weight = quantity * 500; // 500g/sản phẩm
        
        BigDecimal fee = regionalShippingService.calculateShippingFee(fromAddress, toAddress, weight);
        
        Map<String, Object> result = new HashMap<>();
        result.put("fromProvince", fromProvince);
        result.put("toProvince", toProvince);
        result.put("quantity", quantity);
        result.put("weight", weight + "g");
        result.put("shippingFee", fee);
        result.put("shippingFeeFormatted", String.format("%,d đ", fee.intValue()));
        
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
    
    /**
     * Xem tất cả tỉnh/thành theo vùng
     */
    @GetMapping("/provinces")
    @Operation(summary = "Danh sách tỉnh theo vùng", description = "Xem 63 tỉnh/thành phân theo 3 vùng miền")
    public ResponseEntity<?> getAllProvinces() {
        Map<String, Set<String>> regions = regionalShippingService.getAllProvincesByRegion();
        
        Map<String, Object> result = new HashMap<>();
        result.put("regions", regions);
        result.put("totalProvinces", regions.values().stream().mapToInt(Set::size).sum());
        result.put("note", "Tất cả 63 tỉnh/thành Việt Nam đều được hỗ trợ");
        
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
    
    /**
     * Kiểm tra tỉnh có được hỗ trợ không
     */
    @GetMapping("/check-province")
    @Operation(summary = "Kiểm tra tỉnh", description = "Kiểm tra tỉnh/thành có được hỗ trợ không")
    public ResponseEntity<?> checkProvince(@RequestParam String province) {
        boolean supported = regionalShippingService.isProvinceSupported(province);
        
        Map<String, Object> result = new HashMap<>();
        result.put("province", province);
        result.put("supported", supported);
        result.put("message", supported 
                ? "Tỉnh/thành này được hỗ trợ" 
                : "Tỉnh/thành này chưa có trong hệ thống");
        
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
    
    /**
     * Xem bảng giá ship
     */
    @GetMapping("/price-table")
    @Operation(summary = "Bảng giá ship", description = "Xem bảng giá ship theo khoảng cách")
    public ResponseEntity<?> getPriceTable() {
        Map<String, Object> priceTable = new LinkedHashMap<>();
        priceTable.put("sameCityFee", "15,000 đ (Cùng tỉnh/thành)");
        priceTable.put("sameRegionFee", "30,000 đ (Cùng vùng miền)");
        priceTable.put("nearbyRegionFee", "45,000 đ (Vùng lân cận)");
        priceTable.put("farRegionFee", "60,000 đ (Vùng xa - Bắc ↔ Nam)");
        priceTable.put("weightSurcharge", "5,000 đ/kg (sau 1kg đầu tiên)");
        
        Map<String, Object> examples = new LinkedHashMap<>();
        examples.put("HCM → HCM (1 sản phẩm)", "15,000 đ");
        examples.put("HCM → Đồng Nai (2 sản phẩm)", "30,000 đ (cùng vùng Nam)");
        examples.put("HCM → Đà Nẵng (3 sản phẩm)", "45,000 + 5,000 = 50,000 đ");
        examples.put("HCM → Hà Nội (5 sản phẩm)", "60,000 + 10,000 = 70,000 đ");
        
        Map<String, Object> result = new HashMap<>();
        result.put("priceTable", priceTable);
        result.put("examples", examples);
        result.put("note", "Phí ship tự động theo khoảng cách và trọng lượng");
        
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
