# Hướng Dẫn B2CStatisticsController

## Giới Thiệu

`B2CStatisticsController` cung cấp các API cho các cửa hàng (seller) xem thống kê kinh doanh của riêng họ. Bao gồm doanh thu, số đơn hàng, tồn kho, v.v. Tất cả các endpoint yêu cầu xác thực bằng Bearer Token và quyền truy cập vào cửa hàng cụ thể.

**URL Base**: `${api.prefix}/b2c/statistics`
**Yêu cầu**: Bearer Token và quyền truy cập vào cửa hàng

---

## Các Endpoint

### 1. Lấy Thống Kê Tổng Quan (Overview)

**Endpoint**: `GET /b2c/statistics/overview`

**Mô tả**: Lấy thông tin tổng quan của cửa hàng như doanh thu hôm nay, số đơn hàng mới hôm nay, số sản phẩm

**Tham số**:
- `storeId` (required): ID của cửa hàng

**Query Example**:
```
GET /b2c/statistics/overview?storeId=shop_001
```

**Response**:
```json
{
  "success": true,
  "data": {
    "todayRevenue": 2500000,
    "newOrdersToday": 8,
    "variantAmount": 45
  }
}
```

**Giải thích**:
- `todayRevenue`: Doanh thu từ các đơn hàng đã giao hôm nay (VND)
- `newOrdersToday`: Số đơn hàng mới được tạo hôm nay (tất cả trạng thái)
- `variantAmount`: Tổng số sản phẩm (biến thể) của cửa hàng

---

### 2. Lấy Dữ Liệu Biểu Đồ Doanh Thu

**Endpoint**: `GET /b2c/statistics/revenue/chart-data`

**Mô tả**: Lấy dữ liệu doanh thu của cửa hàng được nhóm theo thời gian để vẽ biểu đồ đường

**Tham số**:
- `storeId` (required): ID của cửa hàng
- `period` (required): Kỳ thời gian - `WEEK`, `MONTH`, hoặc `YEAR`

**Query Example**:
```
GET /b2c/statistics/revenue/chart-data?storeId=shop_001&period=MONTH
```

**Response (MONTH)**:
```json
{
  "success": true,
  "data": {
    "labels": ["Tháng 9 2025", "Tháng 10 2025", "Tháng 11 2025"],
    "revenues": [5000000, 7500000, 6200000],
    "revenueLabel": "Doanh thu",
    "period": "MONTH"
  }
}
```

**Response (WEEK)**:
```json
{
  "success": true,
  "data": {
    "labels": ["Tuần 46/2025", "Tuần 47/2025", "Tuần 48/2025"],
    "revenues": [1200000, 1500000, 1800000],
    "revenueLabel": "Doanh thu",
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
    "revenues": [25000000, 18700000],
    "revenueLabel": "Doanh thu",
    "period": "YEAR"
  }
}
```

**Giải thích**:
- `labels`: Danh sách nhãn thời gian (tuần/tháng/năm)
- `revenues`: Danh sách doanh thu tương ứng (VND) - chỉ từ đơn hàng DELIVERED
- `revenueLabel`: Nhãn tiếng Việt cho doanh thu
- `period`: Kỳ thời gian đã sử dụng

---

### 3. Lấy Dữ Liệu Biểu Đồ Số Đơn Hàng

**Endpoint**: `GET /b2c/statistics/orders/chart-data`

**Mô tả**: Lấy số lượng đơn hàng đã giao của cửa hàng được nhóm theo thời gian để vẽ biểu đồ đường

**Tham số**:
- `storeId` (required): ID của cửa hàng
- `period` (required): Kỳ thời gian - `WEEK`, `MONTH`, hoặc `YEAR`

**Query Example**:
```
GET /b2c/statistics/orders/chart-data?storeId=shop_001&period=MONTH
```

**Response (MONTH)**:
```json
{
  "success": true,
  "data": {
    "labels": ["Tháng 9 2025", "Tháng 10 2025", "Tháng 11 2025"],
    "orderCounts": [25, 32, 28],
    "orderCountLabel": "Số đơn hàng",
    "period": "MONTH"
  }
}
```

**Response (WEEK)**:
```json
{
  "success": true,
  "data": {
    "labels": ["Tuần 46/2025", "Tuần 47/2025", "Tuần 48/2025"],
    "orderCounts": [6, 8, 7],
    "orderCountLabel": "Số đơn hàng",
    "period": "WEEK"
  }
}
```

**Giải thích**:
- `labels`: Danh sách nhãn thời gian (tuần/tháng/năm)
- `orderCounts`: Danh sách số lượng đơn hàng tương ứng - chỉ từ đơn hàng DELIVERED
- `orderCountLabel`: Nhãn tiếng Việt cho số đơn hàng
- `period`: Kỳ thời gian đã sử dụng

---

### 4. Lấy Số Đơn Hàng Theo Trạng Thái

**Endpoint**: `GET /b2c/statistics/orders/count-by-status`

**Mô tả**: Lấy số lượng đơn hàng của cửa hàng được phân loại theo trạng thái

**Tham số**:
- `storeId` (required): ID của cửa hàng

**Query Example**:
```
GET /b2c/statistics/orders/count-by-status?storeId=shop_001
```

**Response**:
```json
{
  "success": true,
  "data": {
    "totalOrders": 150,
    "pendingOrders": 5,
    "confirmedOrders": 12,
    "shippingOrders": 8,
    "deliveredOrders": 120,
    "cancelledOrders": 5
  }
}
```

**Giải thích**:
- `totalOrders`: Tổng số đơn hàng của cửa hàng
- `pendingOrders`: Số đơn hàng chờ xác nhận
- `confirmedOrders`: Số đơn hàng đã xác nhận
- `shippingOrders`: Số đơn hàng đang vận chuyển
- `deliveredOrders`: Số đơn hàng đã giao
- `cancelledOrders`: Số đơn hàng bị hủy

---

### 5. Lấy Số Sản Phẩm Theo Tình Trạng Tồn Kho

**Endpoint**: `GET /b2c/statistics/variant/count-by-stock-status`

**Mô tả**: Lấy số lượng sản phẩm của cửa hàng được phân loại theo tình trạng tồn kho

**Tham số**:
- `storeId` (required): ID của cửa hàng

**Query Example**:
```
GET /b2c/statistics/variant/count-by-stock-status?storeId=shop_001
```

**Response**:
```json
{
  "success": true,
  "data": {
    "totalProducts": 45,
    "lowStockProducts": 8,
    "outOfStockProducts": 3
  }
}
```

**Giải thích**:
- `totalProducts`: Tổng số sản phẩm (biến thể) của cửa hàng
- `lowStockProducts`: Số sản phẩm sắp hết hàng (0 < stock ≤ 10)
- `outOfStockProducts`: Số sản phẩm hết hàng (stock = 0)

---

## Xác Thực & Ủy Quyền

### Header Yêu Cầu

Tất cả các request phải bao gồm:
```
Authorization: Bearer {YOUR_JWT_TOKEN}
```

### Xác Minh Quyền Truy Cập

Hệ thống sẽ tự động kiểm tra xem user có sở hữu cửa hàng được yêu cầu hay không. Nếu không, sẽ trả về lỗi:

```json
{
  "success": false,
  "error": "Bạn không có quyền truy cập cửa hàng này"
}
```

---

## Lỗi Phổ Biến

| Mã Lỗi | Mô Tả | Giải Pháp |
|--------|-------|----------|
| 401 Unauthorized | Không có token hoặc token không hợp lệ | Thêm Bearer Token vào header `Authorization` |
| 403 Forbidden | Không có quyền truy cập cửa hàng | Đảm bảo user sở hữu cửa hàng được yêu cầu |
| 400 Bad Request | Tham số không hợp lệ | Kiểm tra `storeId` và `period` (WEEK/MONTH/YEAR) |
| 500 Internal Server Error | Lỗi server | Liên hệ quản trị viên hệ thống |

---

## Ví Dụ Sử Dụng (cURL)

### Lấy thống kê tổng quan
```bash
curl -X GET "http://localhost:8080/api/v1/b2c/statistics/overview?storeId=shop_001" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Lấy dữ liệu biểu đồ doanh thu tháng
```bash
curl -X GET "http://localhost:8080/api/v1/b2c/statistics/revenue/chart-data?storeId=shop_001&period=MONTH" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Lấy dữ liệu biểu đồ số đơn hàng tuần
```bash
curl -X GET "http://localhost:8080/api/v1/b2c/statistics/orders/chart-data?storeId=shop_001&period=WEEK" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Lấy số đơn hàng theo trạng thái
```bash
curl -X GET "http://localhost:8080/api/v1/b2c/statistics/orders/count-by-status?storeId=shop_001" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Lấy số sản phẩm theo tồn kho
```bash
curl -X GET "http://localhost:8080/api/v1/b2c/statistics/variant/count-by-stock-status?storeId=shop_001" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Ví Dụ Sử Dụng (JavaScript/Fetch API)

### Lấy thống kê tổng quan
```javascript
const storeId = 'shop_001';
const token = 'YOUR_JWT_TOKEN';

fetch(`/api/v1/b2c/statistics/overview?storeId=${storeId}`, {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  console.log('Doanh thu hôm nay:', data.data.todayRevenue);
  console.log('Đơn hàng mới hôm nay:', data.data.newOrdersToday);
  console.log('Tổng sản phẩm:', data.data.variantAmount);
})
.catch(error => console.error('Lỗi:', error));
```

### Lấy dữ liệu biểu đồ doanh thu
```javascript
const storeId = 'shop_001';
const period = 'MONTH'; // hoặc 'WEEK', 'YEAR'
const token = 'YOUR_JWT_TOKEN';

fetch(`/api/v1/b2c/statistics/revenue/chart-data?storeId=${storeId}&period=${period}`, {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  const chartData = data.data;
  console.log('Labels:', chartData.labels);
  console.log('Revenues:', chartData.revenues);
  // Sử dụng dữ liệu để vẽ biểu đồ
})
.catch(error => console.error('Lỗi:', error));
```

---

## Ghi Chú

- Tất cả các số tiền được tính bằng **Đồng Việt Nam (VND)**
- Thời gian được trả về theo **ISO 8601 format** (UTC)
- Dữ liệu doanh thu và đơn hàng chỉ tính từ những đơn hàng có trạng thái **DELIVERED**
- Khi lấy dữ liệu biểu đồ, hệ thống tự động nhóm và sắp xếp theo kỳ thời gian
- Sản phẩm sắp hết hàng được định nghĩa là: `0 < stock ≤ 10`
- Sản phẩm hết hàng được định nghĩa là: `stock = 0`
- Mỗi seller chỉ có thể truy cập dữ liệu của các cửa hàng mà họ sở hữu

---

## Trường Hợp Sử Dụng

### Ứng Dụng Dashboard
Hiển thị tổng quan kinh doanh của cửa hàng:
- Sử dụng `/overview` để lấy thống kê cơ bản
- Sử dụng `/revenue/chart-data` và `/orders/chart-data` để vẽ biểu đồ
- Sử dụng `/orders/count-by-status` để theo dõi trạng thái đơn hàng
- Sử dụng `/variant/count-by-stock-status` để quản lý tồn kho

### Báo Cáo Kinh Doanh
- Lấy dữ liệu với `period=MONTH` để tạo báo cáo hàng tháng
- Lấy dữ liệu với `period=YEAR` để tạo báo cáo hàng năm

### Cảnh Báo Tồn Kho
- Sử dụng `/variant/count-by-stock-status` để nhận biết sản phẩm sắp hết
- Gửi thông báo cho seller khi `lowStockProducts > 0`
