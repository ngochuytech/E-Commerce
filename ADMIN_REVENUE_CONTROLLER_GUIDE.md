# 📊 Admin Revenue Controller - Hướng Dẫn Chi Tiết

## 📋 Mục Lục
1. [Tổng Quan](#tổng-quan)
2. [Cấu Trúc Dữ Liệu](#cấu-trúc-dữ-liệu)
3. [API Endpoints](#api-endpoints)
4. [Ví Dụ Sử Dụng](#ví-dụ-sử-dụng)
5. [Response Format](#response-format)
6. [Lỗi Thường Gặp](#lỗi-thường-gặp)

---

## 🎯 Tổng Quan

`AdminRevenueController` quản lý doanh thu và chi phí của **sàn thương mại điện tử**. Hệ thống ghi nhận:

- **SERVICE_FEE**: Phí dịch vụ từ mỗi đơn hàng (5,000đ)
- **PLATFORM_DISCOUNT_LOSS**: Tiền lỗ do sàn cấp discount

### Quy Trình Hoạt Động

```
Khi order được tạo:
  ├─ Lưu storeDiscountAmount (shop chịu)
  └─ Lưu platformDiscountAmount (sàn chịu)

Khi order → DELIVERED:
  ├─ Tạo AdminRevenue (SERVICE_FEE: 5000đ)
  └─ Tạo AdminRevenue (PLATFORM_DISCOUNT_LOSS: tiền lỗ sàn)

Admin xem doanh thu:
  ├─ Tổng phí dịch vụ
  ├─ Tổng tiền lỗ từ discount
  └─ Doanh thu ròng = Phí - Lỗ
```

---

## 📦 Cấu Trúc Dữ Liệu

### AdminRevenue Model

```java
@Document(collection = "admin_revenues")
public class AdminRevenue extends BaseEntity {
    private String id;              // MongoDB ID
    private Order order;            // Liên kết đến order
    private BigDecimal serviceFee;  // Tiền (phí dịch vụ hoặc lỗ discount)
    private String revenueType;     // SERVICE_FEE hoặc PLATFORM_DISCOUNT_LOSS
    private String description;     // Mô tả chi tiết
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Fields Trong Order

```java
public class Order extends BaseEntity {
    private BigDecimal storeDiscountAmount;      // Tiền shop chịu
    private BigDecimal platformDiscountAmount;   // Tiền sàn chịu
    // ...other fields
}
```

---

## 🔌 API Endpoints

### 1️⃣ GET `/admin/revenues/statistics`

**Mô Tả**: Xem thống kê tổng quát doanh thu sàn

**Method**: `GET`

**Authentication**: ✅ Yêu cầu JWT + Role ADMIN

**Parameters**: Không có

**Response**:
```json
{
  "success": true,
  "data": {
    "totalServiceFee": 5000000,           // Tổng phí dịch vụ
    "totalPlatformDiscountLoss": 800000,  // Tổng tiền lỗ từ discount
    "netRevenue": 4200000,                // Doanh thu ròng (phí - lỗ)
    "serviceFeeCount": 1000,              // Số order có phí
    "platformDiscountLossCount": 160      // Số order có lỗ discount
  }
}
```

**Ví Dụ cURL**:
```bash
curl -X GET "http://localhost:8080/api/v1/admin/revenues/statistics" \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 2️⃣ GET `/admin/revenues/service-fees`

**Mô Tả**: Xem chi tiết danh sách phí dịch vụ (phân trang)

**Method**: `GET`

**Parameters**:
| Tham số | Kiểu | Mặc định | Mô Tả |
|---------|------|---------|-------|
| page | int | 0 | Trang thứ bao nhiêu |
| size | int | 10 | Số bản ghi trên 1 trang |

**Response**:
```json
{
  "success": true,
  "data": {
    "revenues": [
      {
        "id": "rev_001",
        "order": {
          "id": "ord_123",
          "totalPrice": 105000,
          "productPrice": 100000,
          "serviceFee": 5000
        },
        "store": {
          "id": "store_001",
          "name": "Shop Điện Tử ABC"
        },
        "serviceFee": 5000,
        "revenueType": "SERVICE_FEE",
        "description": "Phí dịch vụ từ đơn hàng #ord_123 - Trạng thái DELIVERED",
        "createdAt": "2025-11-20T10:30:00",
        "updatedAt": "2025-11-20T10:30:00"
      }
    ],
    "page": 0,
    "size": 10,
    "total": 1000,              // Tổng số phí dịch vụ
    "totalAmount": 5000000      // Tổng tiền phí
  }
}
```

**Ví Dụ cURL**:
```bash
curl -X GET "http://localhost:8080/api/v1/admin/revenues/service-fees?page=0&size=20" \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 3️⃣ GET `/admin/revenues/platform-discount-losses`

**Mô Tả**: Xem chi tiết danh sách tiền lỗ từ discount của sàn

**Method**: `GET`

**Parameters**:
| Tham số | Kiểu | Mặc định | Mô Tả |
|---------|------|---------|-------|
| page | int | 0 | Trang thứ bao nhiêu |
| size | int | 10 | Số bản ghi trên 1 trang |

**Response**:
```json
{
  "success": true,
  "data": {
    "revenues": [
      {
        "id": "rev_002",
        "order": {
          "id": "ord_124",
          "totalPrice": 95000,
          "productPrice": 100000,
          "serviceFee": 5000
        },
        "store": {
          "id": "store_002",
          "name": "Shop Công Nghệ XYZ"
        },
        "serviceFee": 5000,
        "revenueType": "PLATFORM_DISCOUNT_LOSS",
        "description": "Tiền giảm giá sàn chịu từ đơn hàng #ord_124",
        "createdAt": "2025-11-19T14:45:00",
        "updatedAt": "2025-11-19T14:45:00"
      }
    ],
    "page": 0,
    "size": 10,
    "total": 160,               // Tổng số lỗ discount
    "totalAmount": 800000       // Tổng tiền lỗ
  }
}
```

**Ví Dụ cURL**:
```bash
curl -X GET "http://localhost:8080/api/v1/admin/revenues/platform-discount-losses?page=0&size=20" \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 4️⃣ GET `/admin/revenues/date-range`

**Mô Tả**: Xem doanh thu trong khoảng thời gian nhất định

**Method**: `GET`

**Parameters**:
| Tham số | Kiểu | Bắt Buộc | Mô Tả |
|---------|------|---------|-------|
| startDate | string | ✅ | Ngày bắt đầu (yyyy-MM-dd) |
| endDate | string | ✅ | Ngày kết thúc (yyyy-MM-dd) |
| page | int | ❌ | Trang thứ bao nhiêu (default: 0) |
| size | int | ❌ | Số bản ghi trên 1 trang (default: 10) |

**Response**:
```json
{
  "success": true,
  "data": {
    "revenues": [
      {
        "id": "rev_003",
        "order": { ... },
        "store": { ... },
        "serviceFee": 5000,
        "revenueType": "SERVICE_FEE",
        "description": "...",
        "createdAt": "2025-11-15T09:00:00",
        "updatedAt": "2025-11-15T09:00:00"
      }
    ],
    "startDate": "2025-11-01",
    "endDate": "2025-11-30",
    "page": 0,
    "size": 10,
    "total": 300,               // Tổng doanh thu trong tháng
    "totalAmount": 1500000      // Tổng tiền trong tháng
  }
}
```

**Ví Dụ cURL**:
```bash
curl -X GET "http://localhost:8080/api/v1/admin/revenues/date-range?startDate=2025-11-01&endDate=2025-11-30&page=0&size=20" \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 5️⃣ GET `/admin/revenues`

**Mô Tả**: Xem tất cả doanh thu với filter theo loại

**Method**: `GET`

**Parameters**:
| Tham số | Kiểu | Mô Tả |
|---------|------|-------|
| revenueType | string | Filter: `SERVICE_FEE` hoặc `PLATFORM_DISCOUNT_LOSS` (optional) |
| page | int | Trang thứ bao nhiêu (default: 0) |
| size | int | Số bản ghi trên 1 trang (default: 10) |

**Response**:
```json
{
  "success": true,
  "data": {
    "revenues": [ ... ],
    "page": 0,
    "size": 10,
    "total": 1160    // Tổng số record (phí + lỗ)
  }
}
```

**Ví Dụ cURL**:
```bash
# Xem tất cả doanh thu
curl -X GET "http://localhost:8080/api/v1/admin/revenues?page=0&size=20" \
  -H "Authorization: Bearer your-jwt-token"

# Xem chỉ phí dịch vụ
curl -X GET "http://localhost:8080/api/v1/admin/revenues?revenueType=SERVICE_FEE&page=0&size=20" \
  -H "Authorization: Bearer your-jwt-token"

# Xem chỉ lỗ discount
curl -X GET "http://localhost:8080/api/v1/admin/revenues?revenueType=PLATFORM_DISCOUNT_LOSS&page=0&size=20" \
  -H "Authorization: Bearer your-jwt-token"
```

---

## 💡 Ví Dụ Sử Dụng

### Ví Dụ 1: Dashboard Tổng Quát

```bash
# Lấy thống kê tổng quát
curl -X GET "http://localhost:8080/api/v1/admin/revenues/statistics" \
  -H "Authorization: Bearer eyJhbGc..."

# Response
{
  "success": true,
  "data": {
    "totalServiceFee": 5000000,        // Tính từ 1000 orders
    "totalPlatformDiscountLoss": 800000, // 160 orders có discount
    "netRevenue": 4200000,             // Doanh thu ròng
    "serviceFeeCount": 1000,
    "platformDiscountLossCount": 160
  }
}

# Giải thích:
# - Sàn thu phí: 5,000,000đ từ 1000 đơn hàng
# - Sàn lỗ discount: 800,000đ từ 160 đơn hàng
# - Doanh thu thực: 4,200,000đ
```

### Ví Dụ 2: Xem Phí Dịch Vụ Chi Tiết

```bash
curl -X GET "http://localhost:8080/api/v1/admin/revenues/service-fees?page=0&size=5" \
  -H "Authorization: Bearer eyJhbGc..."

# Response
{
  "success": true,
  "data": {
    "revenues": [
      {
        "id": "rev_001",
        "order": {
          "id": "ord_001",
          "totalPrice": 105000,
          "productPrice": 100000,
          "serviceFee": 5000,
          "status": "DELIVERED"
        },
        "store": {
          "id": "store_001",
          "name": "Điện Tử ABC"
        },
        "serviceFee": 5000,
        "revenueType": "SERVICE_FEE",
        "description": "Phí dịch vụ từ đơn hàng #ord_001 - Trạng thái DELIVERED",
        "createdAt": "2025-11-20T10:30:00",
        "updatedAt": "2025-11-20T10:30:00"
      },
      // ... 4 records khác
    ],
    "page": 0,
    "size": 5,
    "total": 1000,
    "totalAmount": 5000000
  }
}
```

### Ví Dụ 3: Xem Doanh Thu Tháng 11

```bash
curl -X GET "http://localhost:8080/api/v1/admin/revenues/date-range?startDate=2025-11-01&endDate=2025-11-30&page=0&size=10" \
  -H "Authorization: Bearer eyJhbGc..."

# Response
{
  "success": true,
  "data": {
    "revenues": [
      {
        "id": "rev_100",
        "order": { "id": "ord_050", ... },
        "serviceFee": 5000,
        "revenueType": "SERVICE_FEE",
        "createdAt": "2025-11-15T09:00:00"
      },
      // ... 9 records khác
    ],
    "startDate": "2025-11-01",
    "endDate": "2025-11-30",
    "page": 0,
    "size": 10,
    "total": 300,
    "totalAmount": 1500000
  }
}

# Giải thích:
# - Tháng 11 có 300 transaction (phí + lỗ)
# - Tổng doanh thu: 1,500,000đ
```

### Ví Dụ 4: Filter Theo Loại Doanh Thu

```bash
# Xem chỉ các lỗ discount
curl -X GET "http://localhost:8080/api/v1/admin/revenues?revenueType=PLATFORM_DISCOUNT_LOSS&page=0&size=5" \
  -H "Authorization: Bearer eyJhbGc..."

# Response
{
  "success": true,
  "data": {
    "revenues": [
      {
        "id": "rev_201",
        "order": { "id": "ord_075", ... },
        "serviceFee": 50000,
        "revenueType": "PLATFORM_DISCOUNT_LOSS",
        "description": "Tiền giảm giá sàn chịu từ đơn hàng #ord_075",
        "createdAt": "2025-11-19T14:45:00"
      },
      // ... 4 records khác
    ],
    "page": 0,
    "size": 5,
    "total": 160
  }
}
```

---

## 📊 Response Format

### Success Response

```json
{
  "success": true,
  "data": {
    // ... endpoint-specific data
  }
}
```

### Error Response

```json
{
  "success": false,
  "data": null,
  "error": "Access Denied - Only ADMIN can access this resource"
}
```

---

## 🐛 Lỗi Thường Gặp

### ❌ Lỗi 1: 401 Unauthorized

```json
{
  "success": false,
  "data": null,
  "error": "Unauthorized"
}
```

**Nguyên Nhân**: 
- Không gửi JWT token
- Token đã hết hạn
- Token không hợp lệ

**Giải Pháp**:
- Gửi header: `Authorization: Bearer your-valid-jwt-token`
- Refresh token nếu hết hạn

---

### ❌ Lỗi 2: 403 Forbidden

```json
{
  "success": false,
  "data": null,
  "error": "Access Denied"
}
```

**Nguyên Nhân**: 
- User không có role ADMIN
- Chỉ ADMIN mới có quyền truy cập

**Giải Pháp**:
- Đăng nhập với tài khoản admin
- Liên hệ quản trị viên để được cấp quyền

---

### ❌ Lỗi 3: 400 Bad Request (date-range)

```json
{
  "success": false,
  "data": null,
  "error": "Invalid date format"
}
```

**Nguyên Nhân**: 
- Format ngày không đúng
- Thiếu tham số startDate hoặc endDate

**Giải Pháp**:
- Sử dụng format: `yyyy-MM-dd`
- Ví dụ: `2025-11-01` ✅ (Đúng)
- Sai: `11/01/2025` ❌ (Sai)

---

### ❌ Lỗi 4: 500 Internal Server Error

```json
{
  "success": false,
  "data": null,
  "error": "Database connection failed"
}
```

**Nguyên Nhân**: 
- Lỗi server hoặc database
- Connection timeout

**Giải Pháp**:
- Kiểm tra server logs
- Kiểm tra kết nối database
- Retry request sau vài giây

---

## 📈 Công Thức Tính Doanh Thu

```
Doanh Thu Sàn = Phí Dịch Vụ - Lỗ Từ Discount

Ví dụ:
├─ Phí dịch vụ (1000 orders × 5000đ) = 5,000,000đ
├─ Lỗ discount (160 orders) = 800,000đ
└─ Doanh thu ròng = 5,000,000 - 800,000 = 4,200,000đ
```

---

## 🔐 Bảo Mật

- ✅ Tất cả endpoints yêu cầu **JWT Authentication**
- ✅ Tất cả endpoints yêu cầu **Role ADMIN**
- ✅ Không thể truy cập dữ liệu của shop khác
- ✅ Dữ liệu được mã hóa trong database

---

## 📝 Tổng Kết

| Endpoint | Method | Mục Đích | Auth |
|----------|--------|---------|------|
| `/admin/revenues/statistics` | GET | Xem tổng quát doanh thu | 🔐 ADMIN |
| `/admin/revenues/service-fees` | GET | Xem chi tiết phí dịch vụ | 🔐 ADMIN |
| `/admin/revenues/platform-discount-losses` | GET | Xem chi tiết lỗ discount | 🔐 ADMIN |
| `/admin/revenues/date-range` | GET | Xem doanh thu theo khoảng thời gian | 🔐 ADMIN |
| `/admin/revenues` | GET | Xem tất cả với filter | 🔐 ADMIN |

---

**Last Updated**: 2025-11-21  
**Maintained By**: Admin Revenue System
