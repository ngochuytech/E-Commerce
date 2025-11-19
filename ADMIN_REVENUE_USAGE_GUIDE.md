# 📊 Admin Revenue API - Hướng Dẫn Chi Tiết

## 📖 Mục Lục
1. [Giới Thiệu](#giới-thiệu)
2. [Cách Hoạt Động](#cách-hoạt-động)
3. [API Endpoints](#api-endpoints)
4. [Ví Dụ Sử Dụng](#ví-dụ-sử-dụng)
5. [Response Format](#response-format)
6. [Lỗi Thường Gặp](#lỗi-thường-gặp)

---

## 🎯 Giới Thiệu

**Admin Revenue API** là hệ thống theo dõi doanh thu từ **phí dịch vụ (Service Fee)** mà admin thu từ mỗi order.

### Tính Năng:
- ✅ Tự động ghi nhận phí dịch vụ mỗi khi order tạo
- ✅ Cập nhật trạng thái phí khi order hoàn thành (DELIVERED)
- ✅ Xem thống kê doanh thu chi tiết
- ✅ Lọc phí theo trạng thái (PENDING/COLLECTED)
- ✅ Lọc phí theo khoảng thời gian
- ✅ Phân trang & sort dữ liệu

### Phí Dịch Vụ Là Gì?
- **Giá trị**: 5000đ cho mỗi order
- **Khi tạo**: Tự động ghi nhận khi order được tạo (status = PENDING)
- **Khi thu**: Tự động cập nhật khi order chuyển sang DELIVERED (status = COLLECTED)

---

## 🔄 Cách Hoạt Động

### 1️⃣ Khi Buyer Tạo Order (Checkout)

```
Buyer tạo Order
    ↓
Tính serviceFee = 5000đ
    ↓
Lưu Order vào database
    ↓
✨ Tự động tạo AdminRevenue record
    ├─ order: <liên kết đến Order>
    ├─ serviceFee: 5000
    ├─ status: PENDING (chưa thu)
    ├─ description: "Phí dịch vụ từ đơn hàng #[orderId]"
    └─ createdAt: [timestamp hiện tại]
```

**Code trong OrderService.checkout():**
```java
// Lưu phí dịch vụ cho admin
AdminRevenue adminRevenue = AdminRevenue.builder()
    .order(order)
    .serviceFee(serviceFee)  // 5000đ
    .revenueType("SERVICE_FEE")
    .status("PENDING")
    .description(String.format("Phí dịch vụ từ đơn hàng #%s", order.getId()))
    .build();
adminRevenueRepository.save(adminRevenue);
```

### 2️⃣ Khi Order Hoàn Thành (DELIVERED)

```
Store hoàn thành delivery
    ↓
Order status: SHIPPING → DELIVERED
    ↓
✨ Tự động cập nhật AdminRevenue
    ├─ status: PENDING → COLLECTED (đã thu)
    └─ updatedAt: [timestamp hiện tại]
```

**Code trong OrderService.updateOrderStatus():**
```java
if ("DELIVERED".equals(newStatus)) {
    addMoneyToStoreWallet(order);
    
    // Cập nhật AdminRevenue status
    adminRevenueRepository.findByOrderId(order.getId())
            .ifPresent(adminRevenue -> {
                adminRevenue.setStatus("COLLECTED");
                adminRevenueRepository.save(adminRevenue);
            });
}
```

### 3️⃣ Admin Xem Doanh Thu

Admin gọi API để xem:
- Tổng phí đã thu được bao nhiêu
- Bao nhiêu phí đang chờ (orders chưa giao)
- Chi tiết từng phí dịch vụ

---

## 📡 API Endpoints

### 1. GET `/admin/revenues/statistics` ⭐ 
**Xem thống kê tổng doanh thu**

#### Mô Tả:
Lấy thống kê tổng số phí dịch vụ: tổng cộng, đã thu, chưa thu

#### Parameters:
Không có parameter

#### Response:
```json
{
  "success": true,
  "data": {
    "totalServiceFee": 5000000,        // Tổng phí (5000 x 1000 orders)
    "collectedFee": 3000000,           // Đã thu (5000 x 600 orders DELIVERED)
    "pendingFee": 2000000,             // Chưa thu (5000 x 400 orders chưa giao)
    "totalCount": 1000,                // Tổng số order
    "collectedCount": 600,             // Số order đã giao
    "pendingCount": 400                // Số order chưa giao
  },
  "error": null
}
```

#### Swagger Example:
```
GET /api/v1/admin/revenues/statistics
Authorization: Bearer <your-jwt-token>
```

---

### 2. GET `/admin/revenues/pending` 🟡
**Xem phí dịch vụ chưa thu**

#### Mô Tả:
Lấy danh sách tất cả phí dịch vụ có trạng thái PENDING (chưa thu), với phân trang

#### Parameters:
| Parameter | Type | Default | Ví Dụ | Mô Tả |
|-----------|------|---------|-------|-------|
| page | int | 0 | 0 | Trang thứ bao nhiêu (0-indexed) |
| size | int | 10 | 20 | Số bản ghi trên 1 trang |
| sortBy | string | createdAt | createdAt | Sắp xếp theo field nào |
| sortDir | string | desc | asc | Thứ tự (asc/desc) |

#### Response:
```json
{
  "success": true,
  "data": {
    "revenues": [
      {
        "id": "rev_001",
        "orderId": "ord_123",
        "serviceFee": 5000,
        "revenueType": "SERVICE_FEE",
        "status": "PENDING",
        "description": "Phí dịch vụ từ đơn hàng #ord_123",
        "createdAt": "2025-11-19T10:30:00",
        "updatedAt": "2025-11-19T10:30:00"
      },
      {
        "id": "rev_002",
        "orderId": "ord_124",
        "serviceFee": 5000,
        "revenueType": "SERVICE_FEE",
        "status": "PENDING",
        "description": "Phí dịch vụ từ đơn hàng #ord_124",
        "createdAt": "2025-11-19T11:00:00",
        "updatedAt": "2025-11-19T11:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "total": 400,              // Tổng số phí chưa thu
    "totalAmount": 2000000     // Tổng tiền chưa thu (400 x 5000)
  },
  "error": null
}
```

#### Swagger Example:
```
GET /api/v1/admin/revenues/pending?page=0&size=20
Authorization: Bearer <your-jwt-token>
```

---

### 3. GET `/admin/revenues/collected` ✅
**Xem phí dịch vụ đã thu**

#### Mô Tả:
Lấy danh sách tất cả phí dịch vụ có trạng thái COLLECTED (đã thu), với phân trang

#### Parameters:
| Parameter | Type | Default | Ví Dụ | Mô Tả |
|-----------|------|---------|-------|-------|
| page | int | 0 | 0 | Trang thứ bao nhiêu |
| size | int | 10 | 20 | Số bản ghi trên 1 trang |
| sortBy | string | createdAt | createdAt | Sắp xếp theo field |
| sortDir | string | desc | asc | Thứ tự (asc/desc) |

#### Response:
```json
{
  "success": true,
  "data": {
    "revenues": [
      {
        "id": "rev_001",
        "orderId": "ord_123",
        "serviceFee": 5000,
        "revenueType": "SERVICE_FEE",
        "status": "COLLECTED",
        "description": "Phí dịch vụ từ đơn hàng #ord_123",
        "createdAt": "2025-11-19T10:30:00",
        "updatedAt": "2025-11-19T15:45:00"  // Cập nhật khi order DELIVERED
      }
    ],
    "page": 0,
    "size": 10,
    "total": 600,              // Tổng số phí đã thu
    "totalAmount": 3000000     // Tổng tiền đã thu (600 x 5000)
  },
  "error": null
}
```

#### Swagger Example:
```
GET /api/v1/admin/revenues/collected?page=0&size=20&sortDir=desc
Authorization: Bearer <your-jwt-token>
```

---

### 4. GET `/admin/revenues/date-range` 📅
**Xem phí dịch vụ theo khoảng thời gian**

#### Mô Tả:
Lấy danh sách phí dịch vụ trong 1 khoảng thời gian cụ thể (không phân biệt PENDING/COLLECTED)

#### Parameters:
| Parameter | Type | Bắt Buộc | Ví Dụ | Mô Tả |
|-----------|------|----------|--------|-------|
| startDate | string | ✅ | 2025-11-01 | Ngày bắt đầu (format: yyyy-MM-dd) |
| endDate | string | ✅ | 2025-11-30 | Ngày kết thúc (format: yyyy-MM-dd) |
| page | int | ❌ | 0 | Trang thứ bao nhiêu (default: 0) |
| size | int | ❌ | 20 | Số bản ghi trên 1 trang (default: 10) |

#### Response:
```json
{
  "success": true,
  "data": {
    "revenues": [
      {
        "id": "rev_001",
        "orderId": "ord_123",
        "serviceFee": 5000,
        "revenueType": "SERVICE_FEE",
        "status": "COLLECTED",
        "description": "Phí dịch vụ từ đơn hàng #ord_123",
        "createdAt": "2025-11-05T10:30:00",
        "updatedAt": "2025-11-06T15:45:00"
      }
    ],
    "startDate": "2025-11-01",
    "endDate": "2025-11-30",
    "page": 0,
    "size": 10,
    "total": 300,              // Số phí trong tháng 11
    "totalAmount": 1500000     // Tổng tiền trong tháng (300 x 5000)
  },
  "error": null
}
```

#### Swagger Example:
```
GET /api/v1/admin/revenues/date-range?startDate=2025-11-01&endDate=2025-11-30&page=0&size=20
Authorization: Bearer <your-jwt-token>
```

---

### 5. GET `/admin/revenues` 🔍
**Xem tất cả phí dịch vụ (có lọc theo status)**

#### Mô Tả:
Lấy danh sách tất cả phí dịch vụ, có thể lọc theo status (PENDING hoặc COLLECTED)

#### Parameters:
| Parameter | Type | Bắt Buộc | Ví Dụ | Mô Tả |
|-----------|------|----------|--------|-------|
| status | string | ❌ | PENDING | Lọc theo trạng thái (PENDING/COLLECTED), null = tất cả |
| page | int | ❌ | 0 | Trang thứ bao nhiêu (default: 0) |
| size | int | ❌ | 20 | Số bản ghi trên 1 trang (default: 10) |

#### Response:
```json
{
  "success": true,
  "data": {
    "revenues": [
      {
        "id": "rev_001",
        "orderId": "ord_123",
        "serviceFee": 5000,
        "revenueType": "SERVICE_FEE",
        "status": "PENDING",
        "description": "Phí dịch vụ từ đơn hàng #ord_123",
        "createdAt": "2025-11-19T10:30:00",
        "updatedAt": "2025-11-19T10:30:00"
      }
    ],
    "page": 0,
    "size": 10,
    "total": 1000             // Tổng số phí (nếu không lọc status)
  },
  "error": null
}
```

#### Swagger Examples:
```
# Xem tất cả phí
GET /api/v1/admin/revenues?page=0&size=20
Authorization: Bearer <your-jwt-token>

# Xem chỉ phí PENDING
GET /api/v1/admin/revenues?status=PENDING&page=0&size=20
Authorization: Bearer <your-jwt-token>

# Xem chỉ phí COLLECTED
GET /api/v1/admin/revenues?status=COLLECTED&page=0&size=20
Authorization: Bearer <your-jwt-token>
```

---

## 💡 Ví Dụ Sử Dụng

### Ví Dụ 1: Xem Dashboard Tổng Doanh Thu
```bash
# Gọi API
curl -X GET "http://localhost:8080/api/v1/admin/revenues/statistics" \
  -H "Authorization: Bearer your-jwt-token"

# Response
{
  "success": true,
  "data": {
    "totalServiceFee": 5000000,
    "collectedFee": 3000000,
    "pendingFee": 2000000,
    "totalCount": 1000,
    "collectedCount": 600,
    "pendingCount": 400
  }
}

# Giải thích:
# - Admin đã thu: 3,000,000đ từ 600 order đã giao
# - Admin chưa thu: 2,000,000đ từ 400 order chưa giao
# - Tổng cộng: 5,000,000đ
```

### Ví Dụ 2: Xem Phí Chưa Thu (để biết sẽ thu bao nhiêu)
```bash
curl -X GET "http://localhost:8080/api/v1/admin/revenues/pending?page=0&size=5" \
  -H "Authorization: Bearer your-jwt-token"

# Response
{
  "success": true,
  "data": {
    "revenues": [
      {
        "id": "507f1f77bcf86cd799439001",
        "orderId": "ord_001",
        "serviceFee": 5000,
        "status": "PENDING",
        "description": "Phí dịch vụ từ đơn hàng #ord_001",
        "createdAt": "2025-11-19T10:30:00"
      },
      // ... thêm 4 bản ghi khác
    ],
    "page": 0,
    "size": 5,
    "total": 400,
    "totalAmount": 2000000
  }
}

# Giải thích:
# - Có 400 order đang chưa giao
# - Tổng phí chưa thu: 2,000,000đ
```

### Ví Dụ 3: Xem Doanh Thu Tháng 11
```bash
curl -X GET "http://localhost:8080/api/v1/admin/revenues/date-range?startDate=2025-11-01&endDate=2025-11-30&page=0&size=10" \
  -H "Authorization: Bearer your-jwt-token"

# Response
{
  "success": true,
  "data": {
    "revenues": [
      {
        "id": "507f1f77bcf86cd799439001",
        "orderId": "ord_001",
        "serviceFee": 5000,
        "status": "COLLECTED",
        "createdAt": "2025-11-05T10:30:00"
      },
      // ... thêm order khác
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
# - Trong tháng 11 có 300 order bị "thu phí dịch vụ"
# - Tổng doanh thu: 1,500,000đ
```

### Ví Dụ 4: Lọc Phí Đã Thu
```bash
curl -X GET "http://localhost:8080/api/v1/admin/revenues?status=COLLECTED&page=0&size=10" \
  -H "Authorization: Bearer your-jwt-token"

# Response
{
  "success": true,
  "data": {
    "revenues": [
      {
        "id": "507f1f77bcf86cd799439001",
        "orderId": "ord_001",
        "serviceFee": 5000,
        "status": "COLLECTED",
        "createdAt": "2025-11-19T10:30:00",
        "updatedAt": "2025-11-19T15:45:00"
      }
      // ... thêm order khác
    ],
    "page": 0,
    "size": 10,
    "total": 600
  }
}

# Giải thích:
# - Đã thu từ 600 order
```

---

## 📦 Response Format

### Success Response (Status 200)
```json
{
  "success": true,
  "data": {
    // ... dữ liệu tương ứng với endpoint
  },
  "error": null
}
```

### Error Response (Status 400/401/403/500)
```json
{
  "success": false,
  "data": null,
  "error": "Thông báo lỗi"
}
```

### Error Cases:

| Status | Lỗi | Nguyên Nhân |
|--------|------|-----------|
| 401 | "Unauthorized" | JWT token không hợp lệ hoặc hết hạn |
| 403 | "Access Denied" | User không có role ADMIN |
| 400 | "Invalid date format" | Format ngày không đúng (phải yyyy-MM-dd) |
| 500 | "Internal Server Error" | Lỗi server |

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

**Nguyên Nhân:** 
- JWT token không được gửi
- JWT token hết hạn
- JWT token không hợp lệ

**Giải Pháp:**
```bash
# Kiểm tra header Authorization
# Phải có format: "Bearer <token>"

curl -X GET "http://localhost:8080/api/v1/admin/revenues/statistics" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### ❌ Lỗi 2: 403 Forbidden
```json
{
  "success": false,
  "data": null,
  "error": "Access Denied"
}
```

**Nguyên Nhân:** 
- User không có role ADMIN
- Chỉ ADMIN mới có quyền xem revenue

**Giải Pháp:**
- Đăng nhập với tài khoản admin
- Hoặc liên hệ admin để được cấp quyền

---

### ❌ Lỗi 3: Invalid Date Format
```json
{
  "success": false,
  "data": null,
  "error": "Text '11/01/2025' could not be parsed, invalid format"
}
```

**Nguyên Nhân:** Format ngày không đúng

**Giải Pháp:**
```bash
# ❌ SAI
GET /admin/revenues/date-range?startDate=11/01/2025&endDate=11/30/2025

# ✅ ĐÚNG
GET /admin/revenues/date-range?startDate=2025-11-01&endDate=2025-11-30
```

---

## 🔧 Cấu Hình & Triển Khai

### Swagger Documentation
Tất cả API này đã được tích hợp Swagger. Xem tại:
```
http://localhost:8080/swagger-ui.html
```

Tìm section: **"Admin Revenue Management"**

### Database Collection
```
Database: <your-db-name>
Collection: admin_revenues
```

### Fields trong AdminRevenue:
```java
{
  _id: ObjectId,                    // MongoDB ID
  order: DBRef,                     // Liên kết đến Order
  serviceFee: Decimal128,           // 5000
  revenueType: String,              // "SERVICE_FEE"
  status: String,                   // "PENDING" hoặc "COLLECTED"
  description: String,              // Mô tả
  createdAt: ISODate,               // Khi tạo
  updatedAt: ISODate                // Lần cập nhật gần nhất
}
```

---

## 📝 Tổng Kết

| Endpoint | Method | Mục Đích | Auth |
|----------|--------|---------|------|
| `/admin/revenues/statistics` | GET | Xem thống kê tổng doanh thu | 🔐 ADMIN |
| `/admin/revenues/pending` | GET | Xem phí chưa thu | 🔐 ADMIN |
| `/admin/revenues/collected` | GET | Xem phí đã thu | 🔐 ADMIN |
| `/admin/revenues/date-range` | GET | Xem phí theo ngày | 🔐 ADMIN |
| `/admin/revenues` | GET | Xem tất cả phí (lọc status) | 🔐 ADMIN |

**Lưu Ý:**
- 🔐 Tất cả API đều yêu cầu JWT token
- 🔐 Tất cả API đều yêu cầu role `ADMIN`
- ⚡ Mặc định phân trang: page=0, size=10
- 📅 Format ngày: `yyyy-MM-dd`

---

**Version:** 1.0  
**Last Updated:** 2025-11-19  
**Maintain By:** Admin Revenue System
