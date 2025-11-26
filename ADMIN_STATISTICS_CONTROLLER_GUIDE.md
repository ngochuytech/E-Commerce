# Hướng Dẫn AdminStatisticsController

## Giới Thiệu

`AdminStatisticsController` cung cấp các API cho quản trị viên xem và quản lý thống kê doanh thu của toàn nền tảng. Tất cả các endpoint đều yêu cầu quyền `ADMIN` và phải xác thực bằng Bearer Token.

**URL Base**: `${api.prefix}/admin/statistics`
**Yêu cầu**: Quyền `ADMIN` và Bearer Token

---

## Các Endpoint

### 1. Lấy Thống Kê Tổng Quan (Overview)

**Endpoint**: `GET /admin/statistics/overview`

**Mô tả**: Lấy thông tin tổng quan về các mục cần phê duyệt trên nền tảng (cửa hàng chờ phê duyệt, sản phẩm chờ phê duyệt, v.v.)

**Tham số**: Không có

**Response**:
```json
{
  "success": true,
  "data": {
    "totalPendingStores": 5,
    "totalPendingProducts": 12,
    "totalPendingVariants": 8,
    "totalUsers": 150,
    "totalPromotions": 25
  }
}
```

**Giải thích**:
- `totalPendingStores`: Số lượng cửa hàng chờ phê duyệt
- `totalPendingProducts`: Số lượng sản phẩm chờ phê duyệt
- `totalPendingVariants`: Số lượng biến thể sản phẩm chờ phê duyệt
- `totalUsers`: Tổng số người dùng trên hệ thống
- `totalPromotions`: Tổng số chương trình khuyến mãi

---

### 2. Lấy Thống Kê Doanh Thu

**Endpoint**: `GET /admin/statistics/revenue`

**Mô tả**: Lấy tổng phí dịch vụ và tổng tiền lỗ từ các chương trình giảm giá nền tảng

**Tham số**: Không có

**Response**:
```json
{
  "success": true,
  "data": {
    "totalServiceFee": 5000000,
    "totalPlatformDiscountLoss": 1200000,
    "netRevenue": 3800000,
    "serviceFeeCount": 145,
    "platformDiscountLossCount": 28
  }
}
```

**Giải thích**:
- `totalServiceFee`: Tổng phí dịch vụ thu được (VND)
- `totalPlatformDiscountLoss`: Tổng tiền lỗ từ giảm giá nền tảng (VND)
- `netRevenue`: Doanh thu ròng = totalServiceFee - totalPlatformDiscountLoss (VND)
- `serviceFeeCount`: Số lượng giao dịch phí dịch vụ
- `platformDiscountLossCount`: Số lượng giao dịch giảm giá nền tảng

---

### 3. Lấy Danh Sách Phí Dịch Vụ

**Endpoint**: `GET /admin/statistics/service-fees`

**Mô tả**: Lấy danh sách phí dịch vụ với phân trang

**Tham số**:
- `page` (optional): Số trang (mặc định: 0)
- `size` (optional): Số bản ghi trên một trang (mặc định: 10)

**Query Example**:
```
GET /admin/statistics/service-fees?page=0&size=10
```

**Response**:
```json
{
  "success": true,
  "data": {
    "revenues": [
      {
        "id": "revenue_001",
        "orderId": "order_123",
        "amount": 50000,
        "revenueType": "SERVICE_FEE",
        "createdAt": "2025-11-27T10:30:00"
      },
      {
        "id": "revenue_002",
        "orderId": "order_124",
        "amount": 75000,
        "revenueType": "SERVICE_FEE",
        "createdAt": "2025-11-27T11:15:00"
      }
    ],
    "page": 0,
    "size": 10,
    "total": 145,
    "totalAmount": 5000000
  }
}
```

---

### 4. Lấy Danh Sách Mất Mát Từ Giảm Giá Nền Tảng

**Endpoint**: `GET /admin/statistics/platform-discount-losses`

**Mô tả**: Lấy danh sách các bản ghi mất mát từ giảm giá nền tảng với phân trang

**Tham số**:
- `page` (optional): Số trang (mặc định: 0)
- `size` (optional): Số bản ghi trên một trang (mặc định: 10)

**Query Example**:
```
GET /admin/statistics/platform-discount-losses?page=0&size=10
```

**Response**:
```json
{
  "success": true,
  "data": {
    "revenues": [
      {
        "id": "revenue_101",
        "orderId": "order_456",
        "amount": 50000,
        "revenueType": "PLATFORM_DISCOUNT_LOSS",
        "createdAt": "2025-11-26T14:20:00"
      }
    ],
    "page": 0,
    "size": 10,
    "total": 28,
    "totalAmount": 1200000
  }
}
```

---

### 5. Lấy Doanh Thu Trong Khoảng Thời Gian

**Endpoint**: `GET /admin/statistics/date-range`

**Mô tả**: Lấy tất cả các bản ghi doanh thu trong một khoảng thời gian cụ thể

**Tham số**:
- `startDate` (required): Ngày bắt đầu (định dạng: yyyy-MM-dd)
- `endDate` (required): Ngày kết thúc (định dạng: yyyy-MM-dd)
- `page` (optional): Số trang (mặc định: 0)
- `size` (optional): Số bản ghi trên một trang (mặc định: 10)

**Query Example**:
```
GET /admin/statistics/date-range?startDate=2025-11-01&endDate=2025-11-30&page=0&size=10
```

**Response**:
```json
{
  "success": true,
  "data": {
    "revenues": [
      {
        "id": "revenue_001",
        "orderId": "order_123",
        "amount": 50000,
        "revenueType": "SERVICE_FEE",
        "createdAt": "2025-11-15T10:30:00"
      }
    ],
    "startDate": "2025-11-01",
    "endDate": "2025-11-30",
    "page": 0,
    "size": 10,
    "total": 173,
    "totalAmount": 6200000
  }
}
```

---

### 6. Lấy Dữ Liệu Cho Biểu Đồ Doanh Thu

**Endpoint**: `GET /admin/statistics/chart-data`

**Mô tả**: Lấy dữ liệu doanh thu được nhóm theo thời gian để vẽ biểu đồ đường

**Tham số**:
- `period` (required): Kỳ thời gian - `WEEK`, `MONTH`, hoặc `YEAR`

**Query Example**:
```
GET /admin/statistics/chart-data?period=MONTH
```

**Response (MONTH)**:
```json
{
  "success": true,
  "data": {
    "labels": ["Tháng 9 2025", "Tháng 10 2025", "Tháng 11 2025"],
    "serviceFees": [2000000, 2500000, 1800000],
    "serviceFeeLabel": "Phí dịch vụ",
    "discountLosses": [300000, 500000, 400000],
    "discountLossLabel": "Mất mát từ giảm giá",
    "netRevenue": [1700000, 2000000, 1400000],
    "netRevenueLabel": "Doanh thu ròng",
    "period": "MONTH"
  }
}
```

**Response (WEEK)**:
```json
{
  "success": true,
  "data": {
    "labels": ["Tuần 47/2025", "Tuần 48/2025"],
    "serviceFees": [1200000, 1400000],
    "serviceFeeLabel": "Phí dịch vụ",
    "discountLosses": [200000, 250000],
    "discountLossLabel": "Mất mát từ giảm giá",
    "netRevenue": [1000000, 1150000],
    "netRevenueLabel": "Doanh thu ròng",
    "period": "WEEK"
  }
}
```

**Response (YEAR)**:
```json
{
  "success": true,
  "data": {
    "labels": ["Năm 2024", "Năm 2025"],
    "serviceFees": [8000000, 6200000],
    "serviceFeeLabel": "Phí dịch vụ",
    "discountLosses": [1500000, 1200000],
    "discountLossLabel": "Mất mát từ giảm giá",
    "netRevenue": [6500000, 5000000],
    "netRevenueLabel": "Doanh thu ròng",
    "period": "YEAR"
  }
}
```

**Giải thích**:
- `labels`: Danh sách nhãn thời gian (tuần/tháng/năm)
- `serviceFees`: Danh sách phí dịch vụ tương ứng (VND)
- `serviceFeeLabel`: Nhãn tiếng Việt cho phí dịch vụ
- `discountLosses`: Danh sách mất mát từ giảm giá (VND)
- `discountLossLabel`: Nhãn tiếng Việt cho mất mát giảm giá
- `netRevenue`: Danh sách doanh thu ròng (VND)
- `netRevenueLabel`: Nhãn tiếng Việt cho doanh thu ròng
- `period`: Kỳ thời gian đã sử dụng

---

## Lỗi Phổ Biến

| Mã Lỗi | Mô Tả | Giải Pháp |
|--------|-------|----------|
| 401 Unauthorized | Không có token hoặc token không hợp lệ | Thêm Bearer Token vào header `Authorization` |
| 403 Forbidden | Không có quyền ADMIN | Đảm bảo user có quyền ADMIN |
| 400 Bad Request | Tham số không hợp lệ (ví dụ: ngày tháng sai định dạng) | Kiểm tra định dạng tham số |
| 500 Internal Server Error | Lỗi server | Liên hệ quản trị viên hệ thống |

---

## Ví Dụ Sử Dụng (cURL)

### Lấy thống kê tổng quan
```bash
curl -X GET "http://localhost:8080/api/v1/admin/statistics/overview" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Lấy dữ liệu biểu đồ tháng
```bash
curl -X GET "http://localhost:8080/api/v1/admin/statistics/chart-data?period=MONTH" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Lấy phí dịch vụ trang 1 (10 bản ghi)
```bash
curl -X GET "http://localhost:8080/api/v1/admin/statistics/service-fees?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Lấy doanh thu trong tháng 11 năm 2025
```bash
curl -X GET "http://localhost:8080/api/v1/admin/statistics/date-range?startDate=2025-11-01&endDate=2025-11-30&page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Ghi Chú

- Tất cả các số tiền được tính bằng **Đồng Việt Nam (VND)**
- Thời gian được trả về theo **ISO 8601 format** (UTC)
- Các endpoint phân trang mặc định trả về 10 bản ghi nếu không chỉ định `size`
- Biểu đồ tự động nhóm dữ liệu theo kỳ thời gian và tính toán doanh thu ròng
