# Tài liệu API mới - Hệ thống E-Commerce TechShop

## Mục lục
1. [Tổng quan](#tổng-quan)
2. [Buyer APIs](#buyer-apis)
3. [Store/B2C APIs](#storeb2c-apis)
4. [Admin APIs](#admin-apis)
5. [Shipper APIs](#shipper-apis)
6. [Scheduled Services](#scheduled-services)

---

## Tổng quan

Tài liệu này mô tả các API mới được thêm vào hệ thống, bao gồm:
- **Quản lý đơn hàng**: Xác nhận hoàn tất đơn hàng
- **Trả hàng/Hoàn tiền**: Tạo yêu cầu trả hàng, phản hồi, khiếu nại
- **Khiếu nại 2 giai đoạn**: Khiếu nại từ chối trả hàng + Khiếu nại chất lượng hàng trả
- **Vận chuyển**: Quản lý trạng thái shipment cho shipper
- **Upload đa phương tiện**: Hỗ trợ upload ảnh/video minh chứng qua Cloudinary

### Base URL
```
/api/v1
```

### Authentication
Tất cả API đều yêu cầu Bearer Token (JWT) trong header:
```
Authorization: Bearer <token>
```

---

## Buyer APIs

### Base Path: `/buyer/orders`

---

### 1. Xác nhận hoàn tất đơn hàng

Buyer xác nhận đã nhận hàng và hoàn tất đơn hàng.

**Endpoint:** `PUT /buyer/orders/{orderId}/complete`

**Yêu cầu:**
- Đơn hàng phải ở trạng thái `DELIVERED`

**Request:**
```http
PUT /api/v1/buyer/orders/670e8b8b9b3c4a1b2c3d4e5f/complete
Authorization: Bearer <token>
```

**Response thành công (200):**
```json
{
  "success": true,
  "data": {
    "id": "670e8b8b9b3c4a1b2c3d4e5f",
    "status": "COMPLETED",
    "completedAt": "2024-12-14T10:30:00",
    ...
  },
  "message": "Đơn hàng đã được xác nhận hoàn tất"
}
```

**Response lỗi (400):**
```json
{
  "success": false,
  "message": "Chỉ có thể xác nhận hoàn tất đơn hàng đã giao"
}
```

---

### 2. Tạo yêu cầu trả hàng

Tạo yêu cầu trả hàng cho đơn hàng đã giao.

**Endpoint:** `POST /buyer/orders/{orderId}/return`

**Content-Type:** `multipart/form-data`

**Parameters:**
| Tên | Loại | Bắt buộc | Mô tả |
|-----|------|----------|-------|
| reason | string | ✅ | Lý do trả hàng |
| description | string | ❌ | Mô tả chi tiết |
| evidenceFiles | file[] | ❌ | Ảnh/video minh chứng (tối đa 5 file) |

**Yêu cầu:**
- Đơn hàng phải ở trạng thái `DELIVERED`
- File hỗ trợ: JPEG, PNG, WebP, MP4, MPEG, MOV, AVI, WebM
- Kích thước tối đa: Ảnh 30MB, Video 100MB

**Request:**
```http
POST /api/v1/buyer/orders/670e8b8b9b3c4a1b2c3d4e5f/return
Authorization: Bearer <token>
Content-Type: multipart/form-data

reason: Sản phẩm bị lỗi
description: Màn hình bị chết điểm ảnh
evidenceFiles: [file1.jpg, file2.mp4]
```

**Response thành công (200):**
```json
{
  "success": true,
  "data": {
    "id": "return_123",
    "orderId": "670e8b8b9b3c4a1b2c3d4e5f",
    "status": "PENDING",
    "reason": "Sản phẩm bị lỗi",
    "description": "Màn hình bị chết điểm ảnh",
    "evidenceMedia": [
      "https://res.cloudinary.com/.../file1.jpg",
      "https://res.cloudinary.com/.../file2.mp4"
    ],
    "createdAt": "2024-12-14T10:00:00"
  }
}
```

---

### 3. Danh sách yêu cầu trả hàng

Lấy danh sách yêu cầu trả hàng của buyer.

**Endpoint:** `GET /buyer/orders/returns`

**Query Parameters:**
| Tên | Loại | Mặc định | Mô tả |
|-----|------|----------|-------|
| status | string | - | Lọc theo trạng thái |
| page | int | 0 | Số trang |
| size | int | 10 | Số lượng mỗi trang |

**Request:**
```http
GET /api/v1/buyer/orders/returns?status=PENDING&page=0&size=10
Authorization: Bearer <token>
```

**Response thành công (200):**
```json
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 5,
    "totalPages": 1,
    "number": 0
  }
}
```

---

### 4. Chi tiết yêu cầu trả hàng

Xem chi tiết một yêu cầu trả hàng.

**Endpoint:** `GET /buyer/orders/returns/{returnRequestId}`

**Request:**
```http
GET /api/v1/buyer/orders/returns/return_123
Authorization: Bearer <token>
```

---

### 5. Tạo khiếu nại (khi bị từ chối trả hàng)

Tạo khiếu nại khi store từ chối yêu cầu trả hàng.

**Endpoint:** `POST /buyer/orders/returns/{returnRequestId}/dispute`

**Content-Type:** `multipart/form-data`

**Parameters:**
| Tên | Loại | Bắt buộc | Mô tả |
|-----|------|----------|-------|
| content | string | ✅ | Nội dung khiếu nại |
| attachmentFiles | file[] | ❌ | Ảnh/video đính kèm (tối đa 5 file) |

**Yêu cầu:**
- Yêu cầu trả hàng phải ở trạng thái `REJECTED`

**Request:**
```http
POST /api/v1/buyer/orders/returns/return_123/dispute
Authorization: Bearer <token>
Content-Type: multipart/form-data

content: Tôi không đồng ý với quyết định từ chối...
attachmentFiles: [evidence1.jpg, evidence2.mp4]
```

**Response thành công (200):**
```json
{
  "success": true,
  "data": {
    "id": "dispute_123",
    "returnRequestId": "return_123",
    "disputeType": "RETURN_REJECTION",
    "status": "OPEN",
    "messages": [
      {
        "senderId": "buyer_123",
        "senderType": "BUYER",
        "senderName": "Nguyễn Văn A",
        "content": "Tôi không đồng ý với quyết định từ chối...",
        "attachments": ["https://..."],
        "sentAt": "2024-12-14T10:00:00"
      }
    ]
  }
}
```

---

### 6. Thêm tin nhắn vào khiếu nại

Buyer thêm tin nhắn/bằng chứng vào khiếu nại đang mở.

**Endpoint:** `POST /buyer/orders/disputes/{disputeId}/message`

**Content-Type:** `multipart/form-data`

**Parameters:**
| Tên | Loại | Bắt buộc | Mô tả |
|-----|------|----------|-------|
| content | string | ✅ | Nội dung tin nhắn |
| attachmentFiles | file[] | ❌ | Ảnh/video đính kèm |

**Request:**
```http
POST /api/v1/buyer/orders/disputes/dispute_123/message
Authorization: Bearer <token>
Content-Type: multipart/form-data

content: Đây là thêm bằng chứng...
attachmentFiles: [more_evidence.jpg]
```

---

### 7. Danh sách khiếu nại

Lấy danh sách khiếu nại của buyer.

**Endpoint:** `GET /buyer/orders/disputes`

**Query Parameters:**
| Tên | Loại | Mặc định | Mô tả |
|-----|------|----------|-------|
| page | int | 0 | Số trang |
| size | int | 10 | Số lượng mỗi trang |

---

## Store/B2C APIs

### Base Path: `/b2c/returnRequest`

---

### 1. Phản hồi yêu cầu trả hàng

Store chấp nhận hoặc từ chối yêu cầu trả hàng.

**Endpoint:** `PUT /b2c/returnRequest/{id}/respond`

**Content-Type:** `multipart/form-data`

**Parameters:**
| Tên | Loại | Bắt buộc | Mô tả |
|-----|------|----------|-------|
| approved | boolean | ✅ | true = chấp nhận, false = từ chối |
| storeResponse | string | ❌ | Phản hồi của store |
| evidenceFiles | file[] | ❌ | Ảnh/video minh chứng (khi từ chối) |

**Request - Chấp nhận:**
```http
PUT /api/v1/b2c/returnRequest/return_123/respond
Authorization: Bearer <token>
Content-Type: multipart/form-data

approved: true
storeResponse: Chấp nhận trả hàng
```

**Request - Từ chối:**
```http
PUT /api/v1/b2c/returnRequest/return_123/respond
Authorization: Bearer <token>
Content-Type: multipart/form-data

approved: false
storeResponse: Sản phẩm không thuộc diện trả hàng
evidenceFiles: [proof.jpg]
```

**Lưu ý:**
- Khi chấp nhận: Status chuyển sang `READY_TO_RETURN`, tạo Shipment mới cho việc trả hàng
- Khi từ chối: Status chuyển sang `REJECTED`, buyer có thể khiếu nại

---

### 2. Khiếu nại chất lượng hàng trả về

Store khiếu nại khi nhận được hàng trả về có vấn đề.

**Endpoint:** `POST /b2c/returnRequest/{id}/dispute-quality`

**Content-Type:** `multipart/form-data`

**Parameters:**
| Tên | Loại | Bắt buộc | Mô tả |
|-----|------|----------|-------|
| reason | string | ✅ | Lý do khiếu nại |
| description | string | ❌ | Mô tả chi tiết |
| evidenceFiles | file[] | ❌ | Ảnh/video minh chứng |

**Yêu cầu:**
- Yêu cầu trả hàng phải ở trạng thái `RETURNED`

**Request:**
```http
POST /api/v1/b2c/returnRequest/return_123/dispute-quality
Authorization: Bearer <token>
Content-Type: multipart/form-data

reason: Hàng trả về bị hư hỏng nặng
description: Màn hình bị vỡ, không phải lỗi ban đầu
evidenceFiles: [damage1.jpg, damage2.mp4]
```

**Response thành công (200):**
```json
{
  "success": true,
  "data": {
    "id": "dispute_456",
    "returnRequestId": "return_123",
    "disputeType": "RETURN_QUALITY",
    "status": "OPEN",
    "messages": [...]
  }
}
```

---

### 3. Thêm tin nhắn vào khiếu nại (Store)

Store thêm tin nhắn/bằng chứng vào khiếu nại.

**Endpoint:** `POST /b2c/disputes/{id}/message`

**Content-Type:** `multipart/form-data`

**Parameters:**
| Tên | Loại | Bắt buộc | Mô tả |
|-----|------|----------|-------|
| content | string | ✅ | Nội dung tin nhắn |
| attachmentFiles | file[] | ❌ | Ảnh/video đính kèm |

---

## Admin APIs

### Base Path: `/admin/disputes`

**Yêu cầu quyền:** `ROLE_ADMIN`

---

### 1. Danh sách khiếu nại

Lấy danh sách tất cả khiếu nại với bộ lọc.

**Endpoint:** `GET /admin/disputes`

**Query Parameters:**
| Tên | Loại | Mặc định | Mô tả |
|-----|------|----------|-------|
| status | string | - | Lọc theo trạng thái (OPEN, IN_REVIEW, RESOLVED, CLOSED) |
| disputeType | string | - | Lọc theo loại (RETURN_REJECTION, RETURN_QUALITY) |
| page | int | 0 | Số trang |
| size | int | 10 | Số lượng mỗi trang |

**Request:**
```http
GET /api/v1/admin/disputes?status=OPEN&disputeType=RETURN_REJECTION&page=0&size=10
Authorization: Bearer <admin_token>
```

---

### 2. Chi tiết khiếu nại

Xem chi tiết một khiếu nại.

**Endpoint:** `GET /admin/disputes/{id}`

**Request:**
```http
GET /api/v1/admin/disputes/dispute_123
Authorization: Bearer <admin_token>
```

**Response thành công (200):**
```json
{
  "success": true,
  "data": {
    "id": "dispute_123",
    "returnRequestId": "return_123",
    "disputeType": "RETURN_REJECTION",
    "status": "OPEN",
    "buyerId": "buyer_123",
    "buyerName": "Nguyễn Văn A",
    "storeId": "store_123",
    "storeName": "TechStore",
    "messages": [...],
    "createdAt": "2024-12-14T10:00:00"
  }
}
```

---

### 3. Giải quyết khiếu nại từ chối trả hàng

Admin đưa ra quyết định cho khiếu nại RETURN_REJECTION.

**Endpoint:** `PUT /admin/disputes/{id}/resolve`

**Content-Type:** `application/json`

**Request Body:**
```json
{
  "decision": "APPROVE_RETURN",
  "adminNote": "Buyer có lý, chấp nhận trả hàng"
}
```

**Decision values:**
| Giá trị | Mô tả |
|---------|-------|
| `APPROVE_RETURN` | Buyer thắng - Cho phép trả hàng, tạo shipment trả hàng |
| `REJECT_RETURN` | Store thắng - Từ chối trả hàng vĩnh viễn |

**Response thành công (200):**
```json
{
  "success": true,
  "data": {
    "id": "return_123",
    "status": "READY_TO_RETURN",
    ...
  },
  "message": "Khiếu nại đã được giải quyết"
}
```

---

### 4. Giải quyết khiếu nại chất lượng hàng trả

Admin đưa ra quyết định cho khiếu nại RETURN_QUALITY.

**Endpoint:** `PUT /admin/disputes/{id}/resolve-quality`

**Content-Type:** `application/json`

**Request Body:**
```json
{
  "decision": "APPROVE_STORE",
  "adminNote": "Hàng trả về thực sự bị hư hỏng do buyer"
}
```

**Decision values:**
| Giá trị | Mô tả |
|---------|-------|
| `APPROVE_STORE` | Store thắng - Hoàn tiền cho store |
| `REJECT_STORE` | Buyer thắng - Hoàn tiền cho buyer |

**Kết quả:**
- `APPROVE_STORE`: ReturnRequest status → `REFUND_TO_STORE`, tiền hoàn về ví store
- `REJECT_STORE`: ReturnRequest status → `REFUNDED`, tiền hoàn về ví buyer

---

### 5. Thêm tin nhắn Admin

Admin thêm tin nhắn vào khiếu nại.

**Endpoint:** `POST /admin/disputes/{id}/message`

**Content-Type:** `multipart/form-data`

**Parameters:**
| Tên | Loại | Bắt buộc | Mô tả |
|-----|------|----------|-------|
| content | string | ✅ | Nội dung tin nhắn |
| attachmentFiles | file[] | ❌ | File đính kèm |

---

## Shipper APIs

### Base Path: `/shipper/shipments`

---

### 1. Bắt đầu lấy hàng

Shipper bắt đầu lấy hàng từ kho.

**Endpoint:** `PUT /shipper/shipments/{shipmentId}/picking`

**Request:**
```http
PUT /api/v1/shipper/shipments/ship_123/picking
Authorization: Bearer <shipper_token>
```

**Chuyển trạng thái:** `READY_TO_PICK` → `PICKING`

---

### 2. Đã lấy hàng xong

Shipper đã lấy hàng xong.

**Endpoint:** `PUT /shipper/shipments/{shipmentId}/picked`

**Chuyển trạng thái:** `PICKING` → `PICKED`

---

### 3. Bắt đầu giao hàng

Shipper bắt đầu giao hàng cho khách.

**Endpoint:** `PUT /shipper/shipments/{shipmentId}/shipping`

**Chuyển trạng thái:** `PICKED` → `SHIPPING`

---

### 4. Đã giao hàng

Shipper xác nhận đã giao hàng thành công.

**Endpoint:** `PUT /shipper/shipments/{shipmentId}/delivered`

**Chuyển trạng thái:** `SHIPPING` → `DELIVERED`

**Tác động:** Order status cũng chuyển sang `DELIVERED`

---

### 5. Giao hàng thất bại

Shipper báo giao hàng không thành công.

**Endpoint:** `PUT /shipper/shipments/{shipmentId}/delivered-fail`

**Request Body:**
```json
{
  "reason": "Khách không có nhà"
}
```

**Chuyển trạng thái:** `SHIPPING` → `DELIVERED_FAIL`

---

### 6. Bắt đầu trả hàng về store

Shipper bắt đầu lấy hàng từ buyer để trả về store.

**Endpoint:** `PUT /shipper/shipments/{shipmentId}/returning`

**Chuyển trạng thái:** `READY_TO_PICK` → `RETURNING`

**Lưu ý:** Áp dụng cho shipment trả hàng

---

### 7. Đã trả hàng về store

Shipper đã giao hàng trả về cho store.

**Endpoint:** `PUT /shipper/shipments/{shipmentId}/returned`

**Chuyển trạng thái:** `RETURNING` → `RETURNED`

**Tác động:** ReturnRequest status chuyển sang `RETURNED`

---

## Scheduled Services

### 1. Tự động hoàn tất đơn hàng

**Chức năng:** Tự động chuyển đơn hàng `DELIVERED` sang `COMPLETED` sau 7 ngày nếu buyer không xác nhận.

**Cron:** Chạy mỗi ngày lúc 00:00

**File:** `OrderScheduledService.java`

```java
@Scheduled(cron = "0 0 0 * * ?")
public void autoCompleteDeliveredOrders() {
    // Tìm các đơn hàng DELIVERED quá 7 ngày
    // Chuyển sang COMPLETED
}
```

---

## Trạng thái và luồng xử lý

### Order Status Flow
```
PENDING → CONFIRMED → SHIPPING → DELIVERED → COMPLETED
    ↓                              ↓
CANCELLED                    RETURN_REQUESTED
```

### ReturnRequest Status Flow
```
PENDING → APPROVED → READY_TO_RETURN → RETURNING → RETURNED → REFUNDED
    ↓                                                  ↓
REJECTED → DISPUTED → READY_TO_RETURN           RETURN_DISPUTED
                ↓                                      ↓
            CLOSED                          REFUNDED / REFUND_TO_STORE
```

### Dispute Status Flow
```
OPEN → IN_REVIEW → RESOLVED
                      ↓
                   CLOSED
```

---

## Upload File

### Định dạng hỗ trợ

**Ảnh:**
- JPEG, PNG, WebP
- Kích thước tối đa: 30MB

**Video:**
- MP4, MPEG, MOV, AVI, WebM
- Kích thước tối đa: 100MB

### Số lượng file
- Tối đa 5 file mỗi request

### Storage
- Cloudinary (cloud storage)
- URL được lưu trong database

---

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": "Lỗi validation hoặc logic"
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Token không hợp lệ"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Không có quyền truy cập"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Không tìm thấy tài nguyên"
}
```

---

## Tổng kết API mới

| Role | Method | Endpoint | Mô tả |
|------|--------|----------|-------|
| Buyer | PUT | /buyer/orders/{id}/complete | Xác nhận hoàn tất đơn hàng |
| Buyer | POST | /buyer/orders/{id}/return | Tạo yêu cầu trả hàng |
| Buyer | GET | /buyer/orders/returns | Danh sách yêu cầu trả hàng |
| Buyer | GET | /buyer/orders/returns/{id} | Chi tiết yêu cầu trả hàng |
| Buyer | POST | /buyer/orders/returns/{id}/dispute | Tạo khiếu nại |
| Buyer | POST | /buyer/orders/disputes/{id}/message | Thêm tin nhắn khiếu nại |
| Buyer | GET | /buyer/orders/disputes | Danh sách khiếu nại |
| Store | PUT | /b2c/returnRequest/{id}/respond | Phản hồi yêu cầu trả hàng |
| Store | POST | /b2c/returnRequest/{id}/dispute-quality | Khiếu nại chất lượng hàng trả |
| Store | POST | /b2c/disputes/{id}/message | Thêm tin nhắn khiếu nại |
| Admin | GET | /admin/disputes | Danh sách khiếu nại |
| Admin | GET | /admin/disputes/{id} | Chi tiết khiếu nại |
| Admin | PUT | /admin/disputes/{id}/resolve | Giải quyết khiếu nại từ chối |
| Admin | PUT | /admin/disputes/{id}/resolve-quality | Giải quyết khiếu nại chất lượng |
| Admin | POST | /admin/disputes/{id}/message | Thêm tin nhắn Admin |
| Shipper | PUT | /shipper/shipments/{id}/picking | Bắt đầu lấy hàng |
| Shipper | PUT | /shipper/shipments/{id}/picked | Đã lấy hàng |
| Shipper | PUT | /shipper/shipments/{id}/shipping | Bắt đầu giao hàng |
| Shipper | PUT | /shipper/shipments/{id}/delivered | Đã giao hàng |
| Shipper | PUT | /shipper/shipments/{id}/delivered-fail | Giao hàng thất bại |
| Shipper | PUT | /shipper/shipments/{id}/returning | Bắt đầu trả hàng |
| Shipper | PUT | /shipper/shipments/{id}/returned | Đã trả hàng |
