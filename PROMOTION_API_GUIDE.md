# 🎁 Promotion API Guide - E-Commerce TechShop

## 📋 Tổng Quan

Hệ thống quản lý khuyến mãi hỗ trợ 3 loại API:
- **Public APIs**: Xem promotions (không cần authentication)
- **B2C APIs**: Store owner quản lý promotions của cửa hàng
- **Admin APIs**: Platform admin quản lý promotions toàn sàn

---

## 🏗️ Promotion Structure

### 1. Promotion Entity

```json
{
  "id": "string",
  "title": "Flash Sale Cuối Tuần",
  "code": "FLASH50",
  "type": "PERCENTAGE | FIXED_AMOUNT",
  "applicableFor": "ORDER | SHIPPING",
  "discountType": "ORDER | CATEGORY",
  "discountValue": 50,
  "maxDiscountValue": 100000,
  "minOrderValue": 200000,
  "usageLimit": 1000,
  "usedCount": 245,
  "startDate": "2025-10-28T00:00:00",
  "endDate": "2025-11-28T23:59:59",
  "status": "ACTIVE | INACTIVE | DELETED",
  "issuer": "PLATFORM | STORE",
  "store": { "id": "...", "name": "..." }
}
```

### 2. Promotion Types

| Type | Mô tả | Ví dụ |
|------|-------|-------|
| `PERCENTAGE` | Giảm theo phần trăm | `discountValue: 10` = giảm 10% |
| `FIXED_AMOUNT` | Giảm số tiền cố định | `discountValue: 50000` = giảm 50k VNĐ |

### 3. Applicable For

| ApplicableFor | Áp dụng cho | Ví dụ |
|---------------|-------------|-------|
| `ORDER` | Đơn hàng | Giảm 10% giá trị đơn hàng |
| `SHIPPING` | Phí vận chuyển | Free ship, giảm 15k phí ship |

### 4. Issuer Types

| Issuer | Phạm vi | Ai tạo? |
|--------|---------|---------|
| `PLATFORM` | Toàn sàn (tất cả stores) | Platform Admin |
| `STORE` | Chỉ cửa hàng đó | Store Owner |

### 5. Status Types

| Status | Mô tả | Hành động |
|--------|-------|-----------|
| `ACTIVE` | Đang hoạt động | Có thể sử dụng |
| `INACTIVE` | Tạm dừng | Không thể sử dụng |
| `DELETED` | Đã xóa (soft delete) | Không hiển thị cho user |

---

## 🌐 Public APIs (No Authentication)

### 1. Get All Active Promotions

```
GET /api/v1/promotions/active
```

**Query Parameters:**
- `page` (int, default: 0): Số trang
- `size` (int, default: 10): Số items mỗi trang
- `sortBy` (string, default: "createdAt"): Trường sort
- `sortDir` (string, default: "desc"): Hướng sort (asc/desc)

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "64f1a2b3c4d5e6f7a8b9c0d1",
        "title": "Giảm giá mùa hè",
        "code": "SUMMER2025",
        "type": "PERCENTAGE",
        "applicableFor": "ORDER",
        "discountValue": 15,
        "maxDiscountValue": 100000,
        "minOrderValue": 500000,
        "issuer": "PLATFORM"
      }
    ],
    "totalElements": 25,
    "totalPages": 3,
    "number": 0
  },
  "message": "Success"
}
```

---

### 2. Get Active Promotions By Store

```
GET /api/v1/promotions/active/store/{storeId}
```

**Path Parameters:**
- `storeId` (string, required): ID của cửa hàng

**Example:**
```bash
curl -X GET "{{baseUrl}}/api/v1/promotions/active/store/64f1a2b3c4d5e6f7a8b9c0d1?page=0&size=10"
```

---

### 3. Get All Platform Promotions

```
GET /api/v1/promotions/platform
```

**Mô tả:** Lấy tất cả promotions của sàn (issuer = PLATFORM)

**Example:**
```bash
curl -X GET "{{baseUrl}}/api/v1/promotions/platform?page=0&size=20"
```

---

### 4. Get Promotion By ID

```
GET /api/v1/promotions/{promotionId}
```

**Example:**
```bash
curl -X GET "{{baseUrl}}/api/v1/promotions/64f1a2b3c4d5e6f7a8b9c0d1"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "64f1a2b3c4d5e6f7a8b9c0d1",
    "title": "Flash Sale 50%",
    "code": "FLASH50",
    "type": "PERCENTAGE",
    "applicableFor": "ORDER",
    "discountValue": 50,
    "maxDiscountValue": 200000,
    "minOrderValue": 300000,
    "usageLimit": 500,
    "usedCount": 123,
    "status": "ACTIVE",
    "issuer": "STORE",
    "store": {
      "id": "store_123",
      "name": "TechShop VN"
    }
  }
}
```

---

### 5. Get Promotions By Store

```
GET /api/v1/promotions/store/{storeId}
```

**Mô tả:** Lấy tất cả promotions (active + inactive) của store

---

### 6. Get Promotions By Type

```
GET /api/v1/promotions/type/{type}
```

**Path Parameters:**
- `type`: `PERCENTAGE` hoặc `FIXED_AMOUNT`

**Example:**
```bash
curl -X GET "{{baseUrl}}/api/v1/promotions/type/PERCENTAGE"
```

---

### 7. Validate Promotion

```
GET /api/v1/promotions/validate/{promotionId}?orderValue=500000
```

**Query Parameters:**
- `orderValue` (long, required): Giá trị đơn hàng cần validate

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "64f1a2b3c4d5e6f7a8b9c0d1",
    "title": "Giảm 10%",
    "code": "SALE10",
    "canApply": true,
    "reason": "Promotion can be applied"
  }
}
```

**Validation rules:**
- ✅ Status = ACTIVE
- ✅ Thời gian hiệu lực (startDate ≤ now ≤ endDate)
- ✅ orderValue ≥ minOrderValue
- ✅ usedCount < usageLimit

---

### 8. Calculate Discount Amount

```
GET /api/v1/promotions/calculate-discount/{promotionId}?orderValue=500000
```

**Response:**
```json
{
  "success": true,
  "data": 50000,
  "message": "Discount calculated"
}
```

**Calculation Logic:**

**PERCENTAGE:**
```javascript
discount = (orderValue * discountValue) / 100
if (discount > maxDiscountValue) {
  discount = maxDiscountValue
}
```

**FIXED_AMOUNT:**
```javascript
discount = discountValue
if (discount > orderValue) {
  discount = orderValue
}
```

---

## 🏪 B2C APIs (Store Owner)

**Authentication:** Bearer Token (Store Owner)

### 1. Create Store Promotion

```
POST /api/v1/b2c/promotions/store/{storeId}
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "title": "Flash Sale Cuối Tuần",
  "code": "WEEKEND50",
  "type": "PERCENTAGE",
  "applicableFor": "ORDER",
  "discountType": "ORDER",
  "discountValue": 50,
  "maxDiscountValue": 100000,
  "minOrderValue": 200000,
  "usageLimit": 100,
  "startDate": "2025-10-28T00:00:00",
  "endDate": "2025-11-01T23:59:59"
}
```

**Validation Rules:**

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `title` | string | ✅ | Không rỗng |
| `code` | string | ✅ | 4-20 ký tự, CHỮ HOA + SỐ, unique |
| `type` | string | ✅ | PERCENTAGE \| FIXED_AMOUNT |
| `applicableFor` | string | ✅ | ORDER \| SHIPPING |
| `discountType` | string | ✅ | ORDER \| CATEGORY |
| `discountValue` | long | ✅ | > 0 |
| `startDate` | datetime | ✅ | ISO 8601 format |
| `endDate` | datetime | ✅ | > startDate |
| `minOrderValue` | long | ❌ | ≥ 0 |
| `maxDiscountValue` | long | ❌ | > 0 (required if type = PERCENTAGE) |
| `usageLimit` | int | ❌ | > 0 |

**Business Rules:**
- ✅ `type = FIXED_AMOUNT` → **KHÔNG ĐƯỢC** có `maxDiscountValue`
- ✅ `type = PERCENTAGE` → **BẮT BUỘC** có `maxDiscountValue`
- ✅ `issuer` tự động = `STORE`
- ✅ `storeId` tự động gán từ path parameter
- ✅ Chỉ store owner mới được tạo

**Example:**
```bash
curl -X POST "{{baseUrl}}/api/v1/b2c/promotions/store/64f1a2b3c4d5e6f7a8b9c0d1" \
  -H "Authorization: Bearer {{token}}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Giảm 20% Điện Thoại",
    "code": "PHONE20",
    "type": "PERCENTAGE",
    "applicableFor": "ORDER",
    "discountType": "ORDER",
    "discountValue": 20,
    "maxDiscountValue": 500000,
    "minOrderValue": 1000000,
    "usageLimit": 50,
    "startDate": "2025-10-28T00:00:00",
    "endDate": "2025-12-31T23:59:59"
  }'
```

---

### 2. Update Store Promotion

```
PUT /api/v1/b2c/promotions/{promotionId}
Authorization: Bearer {token}
```

**Request Body:** (Giống CreatePromotionDTO)

**Authorization:**
- ✅ Chỉ store owner của promotion đó mới update được
- ✅ Validate: `promotion.store.owner.id == currentUser.id`

**Example:**
```bash
curl -X PUT "{{baseUrl}}/api/v1/b2c/promotions/64f1a2b3c4d5e6f7a8b9c0d1" \
  -H "Authorization: Bearer {{token}}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Giảm 30% Điện Thoại (Updated)",
    "code": "PHONE30",
    "type": "PERCENTAGE",
    "applicableFor": "ORDER",
    "discountType": "ORDER",
    "discountValue": 30,
    "maxDiscountValue": 700000,
    "minOrderValue": 1500000,
    "usageLimit": 100,
    "startDate": "2025-10-28T00:00:00",
    "endDate": "2025-12-31T23:59:59"
  }'
```

---

### 3. Activate Promotion

```
PUT /api/v1/b2c/promotions/{promotionId}/activate
Authorization: Bearer {token}
```

**Mô tả:** Kích hoạt promotion (status → ACTIVE)

**Example:**
```bash
curl -X PUT "{{baseUrl}}/api/v1/b2c/promotions/64f1a2b3c4d5e6f7a8b9c0d1/activate" \
  -H "Authorization: Bearer {{token}}"
```

**Response:**
```json
{
  "success": true,
  "data": null,
  "message": "Kích hoạt khuyến mãi thành công!"
}
```

---

### 4. Deactivate Promotion

```
PUT /api/v1/b2c/promotions/{promotionId}/deactivate
Authorization: Bearer {token}
```

**Mô tả:** Vô hiệu hóa promotion (status → INACTIVE)

**Example:**
```bash
curl -X PUT "{{baseUrl}}/api/v1/b2c/promotions/64f1a2b3c4d5e6f7a8b9c0d1/deactivate" \
  -H "Authorization: Bearer {{token}}"
```

---

### 5. Delete Store Promotion

```
DELETE /api/v1/b2c/promotions/{promotionId}
Authorization: Bearer {token}
```

**Mô tả:** Soft delete promotion (status → DELETED)

**Example:**
```bash
curl -X DELETE "{{baseUrl}}/api/v1/b2c/promotions/64f1a2b3c4d5e6f7a8b9c0d1" \
  -H "Authorization: Bearer {{token}}"
```

**Response:**
```json
{
  "success": true,
  "data": null,
  "message": "Xóa khuyến mãi thành công!"
}
```

---

### 6. Get Promotion By ID

```
GET /api/v1/b2c/promotions/{promotionId}
Authorization: Bearer {token}
```

---

### 7. Calculate Discount

```
POST /api/v1/b2c/promotions/{promotionId}/calculate-discount?orderValue=500000
Authorization: Bearer {token}
```

---

## 👑 Admin APIs (Platform Admin)

**Authentication:** Bearer Token (Role: ADMIN)

### 1. Create Platform Promotion

```
POST /api/v1/admin/promotions/platform
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "title": "Flash Sale Toàn Sàn",
  "code": "PLATFORM50",
  "type": "PERCENTAGE",
  "applicableFor": "ORDER",
  "discountType": "ORDER",
  "discountValue": 50,
  "maxDiscountValue": 500000,
  "minOrderValue": 1000000,
  "usageLimit": 1000,
  "startDate": "2025-11-01T00:00:00",
  "endDate": "2025-11-11T23:59:59"
}
```

**Auto-assignment:**
- ✅ `issuer` = `PLATFORM`
- ✅ `storeId` = `null`

**Example:**
```bash
curl -X POST "{{baseUrl}}/api/v1/admin/promotions/platform" \
  -H "Authorization: Bearer {{adminToken}}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Siêu Sale 11.11",
    "code": "SALE1111",
    "type": "PERCENTAGE",
    "applicableFor": "ORDER",
    "discountType": "ORDER",
    "discountValue": 20,
    "maxDiscountValue": 1000000,
    "minOrderValue": 500000,
    "usageLimit": 5000,
    "startDate": "2025-11-11T00:00:00",
    "endDate": "2025-11-11T23:59:59"
  }'
```

---

### 2. Update Platform Promotion

```
PUT /api/v1/admin/promotions/platform/{promotionId}
Authorization: Bearer {token}
```

**Validation:**
- ✅ Chỉ update được promotions có `issuer = PLATFORM`

---

### 3. Get All Promotions (Admin View)

```
GET /api/v1/admin/promotions
Authorization: Bearer {token}
```

**Mô tả:** Xem tất cả promotions (platform + stores) - Admin oversight

---

### 4. Get Promotion By ID (Admin View)

```
GET /api/v1/admin/promotions/{promotionId}
Authorization: Bearer {token}
```

---

### 5. Get Promotions By Store (Admin View)

```
GET /api/v1/admin/promotions/store/{storeId}
Authorization: Bearer {token}
```

---

### 6. Delete Promotion (Admin)

```
DELETE /api/v1/admin/promotions/{promotionId}
Authorization: Bearer {token}
```

**Mô tả:** Xóa bất kỳ promotion nào (platform hoặc store)

---

### 7. Activate Promotion (Admin)

```
PUT /api/v1/admin/promotions/{promotionId}/activate
Authorization: Bearer {token}
```

---

### 8. Deactivate Promotion (Admin)

```
PUT /api/v1/admin/promotions/{promotionId}/deactivate
Authorization: Bearer {token}
```

---

### 9. Admin Reports

#### 9.1. Get Expired Platform Promotions

```
GET /api/v1/admin/promotions/reports/expired
Authorization: Bearer {token}
```

**Mô tả:** Lấy promotions của sàn đã hết hạn

**Filter:**
- ✅ `issuer = PLATFORM`
- ✅ `endDate < now`

---

#### 9.2. Get Deleted Platform Promotions

```
GET /api/v1/admin/promotions/reports/deleted
Authorization: Bearer {token}
```

**Filter:**
- ✅ `issuer = PLATFORM`
- ✅ `status = DELETED`

---

#### 9.3. Get Active Platform Promotions

```
GET /api/v1/admin/promotions/reports/active
Authorization: Bearer {token}
```

**Filter:**
- ✅ `issuer = PLATFORM`
- ✅ `status = ACTIVE`

---

#### 9.4. Get Inactive Platform Promotions

```
GET /api/v1/admin/promotions/reports/inactive
Authorization: Bearer {token}
```

**Filter:**
- ✅ `issuer = PLATFORM`
- ✅ `status = INACTIVE`

---

## 🎯 Use Cases & Examples

### Use Case 1: Flash Sale Store (Store Owner)

**Bước 1: Tạo promotion**
```bash
POST /api/v1/b2c/promotions/store/store_123
{
  "title": "Flash Sale 2H",
  "code": "FLASH2H",
  "type": "PERCENTAGE",
  "applicableFor": "ORDER",
  "discountType": "ORDER",
  "discountValue": 30,
  "maxDiscountValue": 200000,
  "minOrderValue": 500000,
  "usageLimit": 50,
  "startDate": "2025-10-28T14:00:00",
  "endDate": "2025-10-28T16:00:00"
}
```

**Bước 2: Kích hoạt**
```bash
PUT /api/v1/b2c/promotions/{promotionId}/activate
```

**Bước 3: Theo dõi (dùng Public API)**
```bash
GET /api/v1/promotions/{promotionId}
→ Check usedCount, còn bao nhiêu lượt
```

**Bước 4: Vô hiệu hóa khi hết slot**
```bash
PUT /api/v1/b2c/promotions/{promotionId}/deactivate
```

---

### Use Case 2: Platform Sale 11.11 (Admin)

**Bước 1: Tạo platform promotion**
```bash
POST /api/v1/admin/promotions/platform
{
  "title": "Sale 11.11 Toàn Sàn",
  "code": "SALE1111",
  "type": "PERCENTAGE",
  "applicableFor": "ORDER",
  "discountType": "ORDER",
  "discountValue": 20,
  "maxDiscountValue": 1000000,
  "minOrderValue": 300000,
  "usageLimit": 10000,
  "startDate": "2025-11-11T00:00:00",
  "endDate": "2025-11-11T23:59:59"
}
```

**Bước 2: User checkout áp dụng mã**
```json
{
  "platform_promotions": {
    "order_promotion_code": "SALE1111",
    "shipping_promotion_code": null
  }
}
```

**Bước 3: Admin theo dõi reports**
```bash
GET /api/v1/admin/promotions/reports/active
→ Xem các promotion đang chạy

GET /api/v1/admin/promotions/{promotionId}
→ Check usedCount real-time
```

---

### Use Case 3: Freeship Campaign (Platform)

**Tạo platform shipping promotion:**
```bash
POST /api/v1/admin/promotions/platform
{
  "title": "Free Ship Toàn Quốc",
  "code": "FREESHIP100",
  "type": "PERCENTAGE",
  "applicableFor": "SHIPPING",
  "discountType": "ORDER",
  "discountValue": 100,
  "maxDiscountValue": 30000,
  "minOrderValue": 200000,
  "usageLimit": 5000,
  "startDate": "2025-10-28T00:00:00",
  "endDate": "2025-12-31T23:59:59"
}
```

**User checkout:**
```json
{
  "platform_promotions": {
    "order_promotion_code": null,
    "shipping_promotion_code": "FREESHIP100"
  }
}
```

**Kết quả:**
```
Shipping fee: 30,000 VNĐ
Platform SHIPPING discount: -30,000 VNĐ (100%)
→ Final shipping fee: 0 VNĐ
```

---

## 🔐 Authorization Matrix

| API | Role Required | Additional Check |
|-----|---------------|------------------|
| Public APIs | None | - |
| B2C Create | SELLER/B2C | Must own the store |
| B2C Update | SELLER/B2C | Must own the promotion's store |
| B2C Delete | SELLER/B2C | Must own the promotion's store |
| B2C Activate/Deactivate | SELLER/B2C | Must own the promotion's store |
| Admin Create Platform | ADMIN | - |
| Admin Update Platform | ADMIN | Promotion must be issuer=PLATFORM |
| Admin Delete | ADMIN | Can delete any promotion |
| Admin Activate/Deactivate | ADMIN | Can manage any promotion |
| Admin Reports | ADMIN | - |

---

## 📊 Error Responses

### 400 Bad Request

```json
{
  "success": false,
  "data": null,
  "message": "Mã khuyến mãi phải từ 4-20 ký tự, chỉ chứa chữ in hoa và số"
}
```

**Common errors:**
- Code format invalid
- Type = FIXED_AMOUNT nhưng có maxDiscountValue
- Type = PERCENTAGE nhưng thiếu maxDiscountValue
- endDate <= startDate
- discountValue <= 0

---

### 401 Unauthorized

```json
{
  "success": false,
  "data": null,
  "message": "Unauthorized"
}
```

---

### 403 Forbidden

```json
{
  "success": false,
  "data": null,
  "message": "Bạn không có quyền truy cập cửa hàng này"
}
```

---

### 404 Not Found

```json
{
  "success": false,
  "data": null,
  "message": "Không tìm thấy khuyến mãi với ID: 64f1a2b3c4d5e6f7a8b9c0d1"
}
```

---

### 409 Conflict

```json
{
  "success": false,
  "data": null,
  "message": "Mã khuyến mãi đã tồn tại: FLASH50"
}
```

---

## 🧪 Testing Guide

### Test Case 1: Create PERCENTAGE Promotion

**Valid:**
```json
{
  "code": "SALE20",
  "type": "PERCENTAGE",
  "discountValue": 20,
  "maxDiscountValue": 100000,
  ...
}
```
✅ Expected: Success

**Invalid (missing maxDiscountValue):**
```json
{
  "code": "SALE20",
  "type": "PERCENTAGE",
  "discountValue": 20,
  ...
}
```
❌ Expected: 400 "Type PERCENTAGE bắt buộc phải có maxDiscountValue"

---

### Test Case 2: Create FIXED_AMOUNT Promotion

**Valid:**
```json
{
  "code": "FIXED50K",
  "type": "FIXED_AMOUNT",
  "discountValue": 50000,
  ...
}
```
✅ Expected: Success

**Invalid (has maxDiscountValue):**
```json
{
  "code": "FIXED50K",
  "type": "FIXED_AMOUNT",
  "discountValue": 50000,
  "maxDiscountValue": 100000,
  ...
}
```
❌ Expected: 400 "Type FIXED_AMOUNT không được có maxDiscountValue"

---

### Test Case 3: Authorization

**Store Owner A tries to update Store B's promotion:**
```bash
PUT /api/v1/b2c/promotions/{promotionId_of_Store_B}
Authorization: Bearer {token_of_Owner_A}
```
❌ Expected: 403 Forbidden

---

### Test Case 4: Usage Limit

**Scenario:**
- Promotion: usageLimit = 100, usedCount = 99
- User checkout from 3 stores with platform promotion

```json
{
  "platform_promotions": {
    "order_promotion_code": "PLATFORM_CODE"
  }
}
```

❌ Expected: 400 "Mã giảm giá sàn chỉ còn 1 lượt sử dụng nhưng bạn đang đặt hàng từ 3 cửa hàng"

---

## 📝 Best Practices

### 1. Code Naming Convention

✅ **Good:**
- `FLASH2025` - Flash sale năm 2025
- `NEWUSER50` - Giảm 50% cho user mới
- `FREESHIP100` - Free ship 100%
- `SUMMER20` - Giảm 20% mùa hè

❌ **Bad:**
- `abc123` - Không rõ ý nghĩa
- `sale` - Quá chung chung
- `promo-2025` - Có ký tự đặc biệt

---

### 2. Usage Limit Planning

**Platform Promotion:**
```
Expected orders per day: 1000
Campaign duration: 3 days
Safety buffer: 20%
→ usageLimit = 1000 × 3 × 1.2 = 3600
```

**Store Promotion:**
```
Expected daily sales: 50
Campaign duration: 7 days
→ usageLimit = 50 × 7 = 350
```

---

### 3. Time Planning

```json
{
  "startDate": "2025-11-11T00:00:00",  // Bắt đầu 00:00
  "endDate": "2025-11-11T23:59:59"     // Kết thúc 23:59:59
}
```

**Lưu ý:**
- Timezone: Server time (UTC+7)
- Kiểm tra `now` vs `startDate/endDate` khi validate

---

### 4. Discount Value Guidelines

**PERCENTAGE:**
- Thường: 5% - 30%
- Flash sale: 40% - 70%
- Max: 90% (vì 100% = free)

**FIXED_AMOUNT:**
- Small: 10,000 - 50,000 VNĐ
- Medium: 50,000 - 200,000 VNĐ
- Large: 200,000 - 1,000,000 VNĐ

---

**Last Updated**: October 28, 2025
**Version**: 2.0