# Quy Trình Thanh Toán (Checkout Flow)

## 📋 Tổng quan

Hệ thống thanh toán của TechShop hỗ trợ **multi-store checkout** - người dùng có thể mua hàng từ nhiều cửa hàng khác nhau trong một lần thanh toán, mỗi cửa hàng sẽ tạo thành một đơn hàng riêng biệt.

---

## 🔄 Luồng xử lý chính

### **1. Input - Request Body**

```json
{
  "selectedItems": [
    {
      "productVariantId": "variant_id_1",
      "colorId": "color_id_1",
      "quantity": 2
    },
    {
      "productVariantId": "variant_id_2",
      "quantity": 1
    }
  ],
  "paymentMethod": "COD",
  "platformPromotions": {
    "orderPromotionCode": "PLATFORM50",
    "shippingPromotionCode": "FREESHIP",
    "applyShippingToStores": ["store_id_1"]
  },
  "storePromotions": {
    "store_id_1": "STORE20",
    "store_id_2": "STORE10"
  },
  "address": {
    "province": "Hồ Chí Minh",
    "ward": "Phường 1",
    "homeAddress": "123 Nguyễn Văn A",
    "phone": "0901234567",
    "suggestedName": "John Doe"
  },
  "note": "Giao hàng giờ hành chính"
}
```

---

## ⚙️ Các bước xử lý

### **Bước 1: Validation cơ bản**

✅ Kiểm tra:
- Payment method hợp lệ (`COD`, `VNPAY`, `MOMO`, etc.)
- Danh sách sản phẩm không rỗng
- Địa chỉ giao hàng đầy đủ

### **Bước 2: Validate sản phẩm & Nhóm theo Store**

Với mỗi sản phẩm trong `selectedItems`:

1. **Kiểm tra tồn tại:**
   - ProductVariant có trong database không?
   
2. **Kiểm tra stock:**
   - Nếu có `colorId`: Check `color.stock >= quantity`
   - Nếu không có màu: Check `variant.stock >= quantity`

3. **Kiểm tra trạng thái Store:**
   - Store phải có status = `APPROVED`

4. **Nhóm sản phẩm theo Store:**
   ```java
   Map<String, List<SelectedCartItem>> itemsByStore
   // Key: storeId
   // Value: Danh sách sản phẩm của store đó
   ```

5. **Tính tổng giá trị đơn hàng:**
   ```
   totalOrderValue = Σ (price × quantity)
   ```

---

### **Bước 3: Validate Platform Promotions**

#### **3.1. Platform ORDER Voucher**

Nếu có `platformPromotions.orderPromotionCode`:

✅ **Validate:**
- Promotion tồn tại trong database
- `issuer` = `PLATFORM`
- `applicableFor` = `ORDER`
- `status` = `ACTIVE`
- Trong thời gian hiệu lực: `startDate <= now <= endDate`
- Chưa hết lượt: `usedCount < usageLimit`
- Đủ giá trị tối thiểu: `totalOrderValue >= minOrderValue`

✅ **User-specific validation:**
- Nếu `isNewUserOnly = true` → User chưa có đơn hàng nào
- Nếu có `usageLimitPerUser` → User chưa dùng quá giới hạn

#### **3.2. Platform SHIPPING Voucher**

Nếu có `platformPromotions.shippingPromotionCode`:

✅ **Validate tương tự ORDER voucher**, nhưng:
- `applicableFor` = `SHIPPING`

✅ **Validate usage limit với số đơn:**
- Nếu có `applyShippingToStores`:
  ```
  remainingUsage >= applyShippingToStores.length
  ```
- Nếu không chỉ định → Áp dụng cho TẤT CẢ stores:
  ```
  remainingUsage >= numberOfStores
  ```

---

### **Bước 4: Sắp xếp Stores (Prioritization)**

**Mục đích:** Ưu tiên áp dụng Platform ORDER voucher cho đơn có giá trị cao

```java
// Sắp xếp stores theo tổng giá trị GIẢM DẦN
stores.sort((store1, store2) -> {
    BigDecimal total1 = calculateStoreTotal(store1.items);
    BigDecimal total2 = calculateStoreTotal(store2.items);
    return total2.compareTo(total1); // Cao -> Thấp
});
```

**Ví dụ:**
```
Store A: 800,000₫
Store B: 500,000₫
Store C: 200,000₫

→ Thứ tự xử lý: A → B → C
```

---

### **Bước 5: Tạo Order cho mỗi Store**

Với mỗi store (theo thứ tự đã sắp xếp):

#### **5.1. Tính tổng tiền ban đầu**

```java
storeTotal = Σ (price × quantity) của các sản phẩm trong store
```

#### **5.2. Áp dụng Promotion - Thứ tự ưu tiên**

```
① Store ORDER Voucher
    ↓
② Platform ORDER Voucher
    ↓
③ Platform SHIPPING Voucher (tính riêng)
```

---

#### **① Store ORDER Voucher**

Nếu có mã giảm giá của Store trong `storePromotions[storeId]`:

✅ **Validate:**
- `applicableFor` = `ORDER`
- `issuer` = `STORE`
- `store.id` = `storeId`
- Các điều kiện thông thường (status, date, usage limit, etc.)
- `storeTotal >= minOrderValue`

✅ **Tính discount:**
```java
if (type == PERCENTAGE) {
    discount = storeTotal × (discountValue / 100);
    discount = min(discount, maxDiscountValue); // Nếu có
} else {
    discount = discountValue;
}

currentTotal = storeTotal - discount;
```

---

#### **② Platform ORDER Voucher**

**Điều kiện áp dụng:**
- Platform voucher tồn tại
- Còn lượt sử dụng: `remainingUsage > 0`
- `currentTotal >= minOrderValue` (sau khi áp dụng store voucher)

```java
int remainingUsage = usageLimit - (usedCount + platformOrderPromotionUsed);

if (remainingUsage > 0 && currentTotal >= minOrderValue) {
    // Tính discount
    platformDiscount = calculateDiscount(currentTotal, platformPromotion);
    currentTotal = currentTotal - platformDiscount;
    platformOrderPromotionUsed++; // Tăng counter
}
```

**Ví dụ với 3 stores:**
```
Platform voucher: Giảm 100k, còn 2 lượt

Store A (800k):
  - Store voucher: -50k = 750k
  - Platform voucher: -100k = 650k ✅ (lượt 1)

Store B (500k):
  - Không có store voucher = 500k
  - Platform voucher: -100k = 400k ✅ (lượt 2)

Store C (200k):
  - Không có store voucher = 200k
  - Platform voucher: ❌ HẾT LƯỢT
```

---

#### **③ Platform SHIPPING Voucher**

**Áp dụng độc lập** với ORDER discount:

```java
BigDecimal shippingFee = BigDecimal.valueOf(30000); // Mặc định 30k

// Chỉ áp dụng nếu:
// - Store này nằm trong applyShippingToStores
// - storeTotal >= minOrderValue (giá gốc, không phải currentTotal)

if (applyShippingToStores.contains(storeId) && 
    storeTotal >= minOrderValue) {
    
    shippingDiscount = calculateDiscount(shippingFee, shippingPromotion);
    finalShippingFee = shippingFee - shippingDiscount;
}
```

---

#### **5.3. Tính tổng cuối cùng**

```java
finalTotal = storeTotal 
           - orderDiscount       // Store + Platform ORDER
           + finalShippingFee    // Shipping sau discount
```

**Đảm bảo:**
```java
finalTotal = max(finalTotal, 0);  // Không âm
finalShippingFee = max(finalShippingFee, 0);
```

---

### **Bước 6: Lưu Order vào Database**

```java
Order order = Order.builder()
    .buyer(user)
    .store(store)
    .promotions(appliedPromotions)  // List các promotion đã áp dụng
    .totalPrice(finalTotal)
    .shippingFee(finalShippingFee)
    .address(addressDTO)
    .paymentMethod(paymentMethod)
    .status("PENDING")
    .note(note)
    .build();

orderRepository.save(order);
```

---

### **Bước 7: Tạo OrderItems & Trừ Stock**

Với mỗi sản phẩm trong store:

#### **7.1. Trừ stock**

**Nếu có màu sắc:**
```java
color.stock = color.stock - quantity;
variant.stock = Σ(color.stock); // Cập nhật tổng stock
```

**Nếu không có màu:**
```java
variant.stock = variant.stock - quantity;
```

#### **7.2. Tạo OrderItem**

```java
OrderItem orderItem = OrderItem.builder()
    .order(order)
    .productVariant(productVariant)
    .quantity(quantity)
    .price(itemPrice)
    .colorId(colorId)
    .build();

orderItemRepository.save(orderItem);
```

---

### **Bước 8: Ghi nhận sử dụng Promotion**

Với mỗi promotion đã áp dụng:

```java
// Tăng usedCount
promotion.setUsedCount(promotion.getUsedCount() + 1);
promotionRepository.save(promotion);

// Tạo PromotionUsage record
PromotionUsage usage = PromotionUsage.builder()
    .promotion(promotion)
    .user(user)
    .order(order)
    .usedAt(LocalDateTime.now())
    .build();

promotionUsageRepository.save(usage);
```

---

### **Bước 9: Xóa sản phẩm khỏi Cart**

```java
cartService.removeSelectedItems(
    user, 
    productVariantIds, 
    colorIds
);
```

---

### **Bước 10: Return Response**

```json
{
  "success": true,
  "data": [
    {
      "id": "order_1",
      "storeId": "store_1",
      "storeName": "TechShop A",
      "totalPrice": 650000,
      "shippingFee": 30000,
      "status": "PENDING",
      "orderItems": [
        {
          "productName": "iPhone 15",
          "quantity": 1,
          "price": 500000
        }
      ],
      "promotions": [
        {
          "code": "STORE20",
          "discountValue": 50000
        },
        {
          "code": "PLATFORM50",
          "discountValue": 100000
        }
      ]
    },
    {
      "id": "order_2",
      "storeId": "store_2",
      "storeName": "TechShop B",
      "totalPrice": 430000,
      "shippingFee": 10000,
      "status": "PENDING",
      "orderItems": [...],
      "promotions": [...]
    }
  ]
}
```

---

## 📊 Ví dụ hoàn chỉnh

### **Scenario:**

**Giỏ hàng:**
- Store A: iPhone 15 (800,000₫) × 1
- Store B: AirPods (500,000₫) × 1
- Store C: Case (200,000₫) × 1

**Vouchers:**
- Store A voucher: `STORE50` - Giảm 50,000₫
- Platform ORDER voucher: `PLATFORM100` - Giảm 100,000₫ (còn 2 lượt)
- Platform SHIPPING voucher: `FREESHIP` - Giảm 20,000₫ (áp dụng cho Store A)

---

### **Xử lý:**

#### **Store A (800,000₫)** - Đơn có giá trị cao nhất

```
Giá gốc:           800,000₫
① Store voucher:    -50,000₫
= Giá tạm:         750,000₫
② Platform voucher:-100,000₫  [Lượt 1]
= Giá cuối:        650,000₫

Shipping:           30,000₫
③ Shipping voucher: -20,000₫
= Ship cuối:        10,000₫

TỔNG: 660,000₫
```

#### **Store B (500,000₫)**

```
Giá gốc:           500,000₫
① Store voucher:    N/A
= Giá tạm:         500,000₫
② Platform voucher:-100,000₫  [Lượt 2]
= Giá cuối:        400,000₫

Shipping:           30,000₫
③ Shipping voucher: N/A (không chọn)
= Ship cuối:        30,000₫

TỔNG: 430,000₫
```

#### **Store C (200,000₫)**

```
Giá gốc:           200,000₫
① Store voucher:    N/A
= Giá tạm:         200,000₫
② Platform voucher: ❌ HẾT LƯỢT
= Giá cuối:        200,000₫

Shipping:           30,000₫
③ Shipping voucher: N/A
= Ship cuối:        30,000₫

TỔNG: 230,000₫
```

---

### **Kết quả:**

```
3 đơn hàng được tạo:
- Order A: 660,000₫ (có 3 vouchers)
- Order B: 430,000₫ (có 1 voucher)
- Order C: 230,000₫ (không có voucher)

TỔNG THANH TOÁN: 1,320,000₫
(Tiết kiệm: 270,000₫)
```

---

## 🔐 Business Rules

### **1. Thứ tự áp dụng voucher ORDER**

```
Store ORDER → Platform ORDER
```

**Lý do:** Store voucher giảm giá trước, sau đó Platform voucher áp dụng trên giá đã giảm.

### **2. Platform ORDER voucher - Ưu tiên đơn cao**

Sắp xếp stores theo giá trị **giảm dần**, áp dụng voucher cho đến khi **hết lượt**.

**Lợi ích:** Tối đa hóa giá trị discount cho khách hàng.

### **3. Platform SHIPPING voucher - Selective application**

User có thể **chọn** stores nào nhận voucher ship, thay vì bắt buộc áp dụng cho tất cả.

**Use case:** 
- User có 3 stores, voucher ship còn 1 lượt
- User chọn áp dụng cho store có phí ship cao nhất

### **4. Min Order Value validation**

- **Store ORDER voucher:** Validate với `storeTotal` (giá gốc)
- **Platform ORDER voucher:** Validate với `currentTotal` (sau khi áp dụng store voucher)
- **Platform SHIPPING voucher:** Validate với `storeTotal` (giá gốc)

### **5. Usage Limit tracking**

- **usageLimit:** Tổng số lần có thể dùng (tất cả users)
- **usageLimitPerUser:** Số lần 1 user có thể dùng
- **usedCount:** Số lần đã dùng

```
remainingUsage = usageLimit - usedCount
```

### **6. New User Only**

Nếu `isNewUserOnly = true`:
```sql
SELECT COUNT(*) FROM orders WHERE buyer_id = ?
-- Phải = 0
```

---

## 🚨 Error Handling

### **Common Errors:**

| Error | Message | Reason |
|-------|---------|--------|
| `DataNotFoundException` | "Không tìm thấy sản phẩm" | ProductVariant không tồn tại |
| `IllegalArgumentException` | "Không đủ hàng trong kho" | `quantity > stock` |
| `IllegalArgumentException` | "Cửa hàng tạm thời đóng cửa" | Store status ≠ APPROVED |
| `InvalidPromotionException` | "Mã giảm giá không hợp lệ" | Promotion validation failed |
| `IllegalArgumentException` | "Mã chỉ còn X lượt" | `remainingUsage < số đơn muốn áp dụng` |

---

## 📝 Notes

### **Stock Management:**

- Stock được trừ **ngay khi tạo order**
- Khi **cancel order**, stock được **hoàn trả**

### **Promotion Snapshot:**

Hiện tại hệ thống lưu **reference** (`@DBRef`) đến Promotion.

**Khuyến nghị:** Nên lưu **snapshot** của promotion để tránh data inconsistency khi promotion thay đổi.

### **Transaction:**

Checkout process được wrap trong `@Transactional` để đảm bảo:
- Nếu **1 bước fail** → **Rollback tất cả**
- Không xảy ra tình trạng: Order được tạo nhưng stock không trừ

---

## 🔄 Order Status Workflow

```
PENDING
   ↓
CONFIRMED (Seller xác nhận)
   ↓
SHIPPING (Đang giao hàng)
   ↓
DELIVERED (Hoàn thành)

   ↓ (có thể cancel từ PENDING/CONFIRMED)
CANCELLED
```

**Buyer có thể cancel:** Chỉ khi status = `PENDING`

**Seller có thể update:** Theo workflow trên

---

## 🎯 API Endpoints

### **Checkout**

```http
POST /api/v1/buyer/orders/checkout
Authorization: Bearer <token>
Content-Type: application/json

{
  "selectedItems": [...],
  "paymentMethod": "COD",
  "platformPromotions": {...},
  "storePromotions": {...},
  "address": {...},
  "note": "..."
}
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "order_id",
      "totalPrice": 650000,
      "status": "PENDING",
      ...
    }
  ]
}
```

---

## 📌 TODO / Improvements

- [ ] Implement payment gateway integration (VNPAY, MOMO)
- [ ] Add retry mechanism for failed transactions
- [ ] Implement inventory reservation (hold stock for 15 minutes)
- [ ] Add promotion snapshot instead of @DBRef
- [ ] Implement webhook for order status updates
- [ ] Add order tracking number
- [ ] Implement refund process
- [ ] Add analytics for promotion effectiveness

---

**Last Updated:** November 2, 2025  
**Version:** 1.0  
**Author:** TechShop Development Team
