# 📦 Return Request & Dispute Models Documentation

## Tổng Quan

Hệ thống trả hàng và giải quyết tranh chấp bao gồm 2 models chính:
- **ReturnRequest**: Quản lý yêu cầu trả hàng từ người mua
- **Dispute**: Quản lý các khiếu nại/tranh chấp giữa buyer và store

---

## 1️⃣ ReturnRequest Model

### 📋 Mô Tả
Model lưu trữ thông tin về yêu cầu trả hàng từ người mua sau khi đã nhận hàng.

### 🗂️ Collection
```
return_requests
```

### 📊 Cấu Trúc Dữ Liệu

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | ID của yêu cầu trả hàng |
| `order` | Order (DBRef) | Đơn hàng cần trả |
| `buyer` | User (DBRef) | Người mua yêu cầu trả hàng |
| `store` | Store (DBRef) | Cửa hàng nhận yêu cầu |
| `reason` | String | Lý do trả hàng |
| `description` | String | Mô tả chi tiết vấn đề |
| `evidenceMedia` | List\<String\> | URL ảnh/video minh chứng từ buyer |
| `refundAmount` | BigDecimal | Số tiền yêu cầu hoàn trả |
| `status` | String | Trạng thái yêu cầu (enum) |
| `storeResponse` | String | Phản hồi từ store |
| `storeRejectReason` | String | Lý do từ chối từ store |
| `storeEvidenceMedia` | List\<String\> | Ảnh/video minh chứng từ store |
| `adminDecision` | String | Quyết định từ admin (nếu có tranh chấp) |
| `adminDecisionReason` | String | Lý do quyết định từ admin |
| `adminHandler` | User (DBRef) | Admin xử lý tranh chấp |

### 🏷️ Store Dispute Fields (Tranh chấp hàng trả về)

Các trường này được sử dụng khi store khiếu nại về chất lượng hàng trả về:

| Field | Type | Description |
|-------|------|-------------|
| `storeDisputedReturnedGoods` | boolean | Store đã khiếu nại hàng trả về chưa |
| `storeReturnDisputeReason` | String | Lý do store khiếu nại |
| `storeReturnDisputeDescription` | String | Mô tả chi tiết vấn đề |
| `storeReturnDisputeMedia` | List\<String\> | Ảnh/video minh chứng hàng có vấn đề |
| `adminReturnDisputeDecision` | String | Quyết định của admin |
| `adminReturnDisputeReason` | String | Lý do quyết định của admin |

### 📌 ReturnStatus Enum

```java
public enum ReturnStatus {
    PENDING,            // Chờ store xem xét
    APPROVED,           // Store chấp nhận trả hàng
    REJECTED,           // Store từ chối trả hàng
    DISPUTED,           // Buyer khiếu nại, chờ admin quyết định
    READY_TO_RETURN,    // Chờ shipper đến lấy hàng trả
    RETURNING,          // Shipper đang trả hàng về shop
    RETURNED,           // Hàng đã trả về shop
    RETURN_DISPUTED,    // Store khiếu nại hàng trả về có vấn đề
    REFUNDED,           // Đã hoàn tiền cho buyer
    REFUND_TO_STORE,    // Hoàn tiền cho store (store thắng dispute)
    CLOSED              // Đóng yêu cầu (từ chối cuối cùng)
}
```

### 🔄 Flow Trạng Thái

#### **Flow Thành Công (Không Tranh Chấp)**
```
PENDING → APPROVED → READY_TO_RETURN → RETURNING → RETURNED → REFUNDED
```

#### **Flow Có Tranh Chấp Từ Chối**
```
PENDING → REJECTED → DISPUTED → READY_TO_RETURN (Admin chấp nhận)
                              → CLOSED (Admin từ chối)
```

#### **Flow Có Tranh Chấp Chất Lượng**
```
RETURNED → RETURN_DISPUTED → REFUNDED (Buyer thắng)
                          → REFUND_TO_STORE (Store thắng)
```

### 📝 ReturnReason Enum

Các lý do trả hàng phổ biến:

```java
public enum ReturnReason {
    DEFECTIVE_PRODUCT,      // Sản phẩm bị lỗi/hỏng
    WRONG_PRODUCT,          // Giao sai sản phẩm
    MISSING_ITEMS,          // Thiếu sản phẩm
    NOT_AS_DESCRIBED,       // Không đúng mô tả
    DAMAGED_PACKAGING,      // Bao bì bị hư hại
    QUALITY_ISSUE,          // Vấn đề chất lượng
    CHANGE_OF_MIND,         // Đổi ý
    OTHER                   // Lý do khác
}
```

---

## 2️⃣ Dispute Model

### 📋 Mô Tả
Model lưu trữ thông tin về các khiếu nại/tranh chấp giữa buyer và store. Dùng làm chứng cứ cho admin xử lý.

### 🗂️ Collection
```
disputes
```

### 📊 Cấu Trúc Dữ Liệu

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | ID của dispute |
| `returnRequest` | ReturnRequest (DBRef) | Yêu cầu trả hàng liên quan |
| `order` | Order (DBRef) | Đơn hàng liên quan |
| `buyer` | User (DBRef) | Người mua |
| `store` | Store (DBRef) | Cửa hàng |
| `disputeType` | String | Loại tranh chấp (enum) |
| `status` | String | Trạng thái tranh chấp (enum) |
| `messages` | List\<DisputeMessage\> | Danh sách tin nhắn/bằng chứng |
| `adminHandler` | User (DBRef) | Admin xử lý |
| `finalDecision` | String | Quyết định cuối cùng |
| `decisionReason` | String | Lý do quyết định |
| `resolvedAt` | LocalDateTime | Thời gian giải quyết |
| `winner` | String | Bên thắng (BUYER/STORE) |

### 🏷️ DisputeType Enum

```java
public enum DisputeType {
    RETURN_REJECTION,       // Buyer khiếu nại store từ chối trả hàng
    RETURN_QUALITY          // Store khiếu nại hàng trả về có vấn đề
}
```

### 📌 DisputeStatus Enum

```java
public enum DisputeStatus {
    OPEN,           // Mới mở tranh chấp
    IN_REVIEW,      // Admin đang xem xét
    RESOLVED,       // Đã giải quyết (có kết quả rõ ràng)
    CLOSED          // Đã đóng (không giải quyết)
}
```

### 🏆 DisputeWinner Enum

```java
public enum DisputeWinner {
    BUYER,          // Buyer thắng
    STORE           // Store thắng
}
```

### 💬 DisputeMessage Class

Lưu trữ tin nhắn/bằng chứng trong tranh chấp:

```java
public static class DisputeMessage {
    private String senderId;           // ID người gửi
    private String senderType;         // BUYER, STORE, ADMIN
    private String senderName;         // Tên người gửi
    private String content;            // Nội dung tin nhắn
    private List<String> attachments;  // URL ảnh/video đính kèm
    private LocalDateTime sentAt;      // Thời gian gửi
}
```

---

## 🔗 Quan Hệ Giữa Các Models

```
Order (DELIVERED)
    ↓
ReturnRequest (PENDING)
    ↓ (nếu bị từ chối)
Dispute (RETURN_REJECTION)
    ↓ (Admin quyết định APPROVE)
ReturnRequest (READY_TO_RETURN → RETURNING → RETURNED)
    ↓ (Store phát hiện vấn đề)
Dispute (RETURN_QUALITY)
    ↓ (Admin quyết định)
ReturnRequest (REFUNDED hoặc REFUND_TO_STORE)
```

---

## 🎯 Use Cases

### Use Case 1: Trả Hàng Thành Công (Không Tranh Chấp)

1. **Buyer tạo yêu cầu trả hàng**
   - `POST /api/v1/buyer/orders/{orderId}/return`
   - ReturnRequest: `PENDING`

2. **Store chấp nhận**
   - `PUT /api/v1/b2c/returns/store/{storeId}/returnRequest/{id}/respond`
   - ReturnRequest: `READY_TO_RETURN`
   - Tạo Shipment mới cho việc trả hàng

3. **Shipper lấy và trả hàng về**
   - ReturnRequest: `RETURNING` → `RETURNED`

4. **Store xác nhận hàng OK**
   - `PUT /api/v1/b2c/returns/store/{storeId}/returnRequest/{id}/confirm-ok`
   - ReturnRequest: `REFUNDED`
   - Hoàn tiền cho Buyer

### Use Case 2: Store Từ Chối → Buyer Khiếu Nại

1. **Store từ chối trả hàng**
   - ReturnRequest: `REJECTED`

2. **Buyer tạo khiếu nại**
   - `POST /api/v1/buyer/orders/returns/{returnRequestId}/dispute`
   - Dispute: `OPEN`, Type: `RETURN_REJECTION`
   - ReturnRequest: `DISPUTED`

3. **Admin xem xét**
   - `GET /api/v1/admin/disputes/{disputeId}`
   - Dispute: `IN_REVIEW`

4. **Admin quyết định**
   - `PUT /api/v1/admin/disputes/{disputeId}/resolve`
   - **Nếu APPROVE_RETURN**: Dispute: `RESOLVED`, Winner: `BUYER`
     - ReturnRequest: `READY_TO_RETURN` → tiếp tục flow trả hàng
   - **Nếu REJECT_RETURN**: Dispute: `RESOLVED`, Winner: `STORE`
     - ReturnRequest: `CLOSED`

### Use Case 3: Store Khiếu Nại Hàng Trả Về Có Vấn Đề

1. **Hàng đã trả về shop**
   - ReturnRequest: `RETURNED`

2. **Store phát hiện vấn đề và khiếu nại**
   - `POST /api/v1/b2c/returns/store/{storeId}/returnRequest/{id}/dispute-quality`
   - Dispute: `OPEN`, Type: `RETURN_QUALITY`
   - ReturnRequest: `RETURN_DISPUTED`

3. **Admin xem xét bằng chứng**
   - Xem ảnh/video từ cả buyer (lúc yêu cầu trả) và store (lúc nhận hàng)

4. **Admin quyết định**
   - `PUT /api/v1/admin/disputes/{disputeId}/resolve-quality`
   - **Nếu APPROVE_STORE**: Dispute: `RESOLVED`, Winner: `STORE`
     - ReturnRequest: `REFUND_TO_STORE`
     - Hoàn tiền về ví Store
   - **Nếu REJECT_STORE**: Dispute: `RESOLVED`, Winner: `BUYER`
     - ReturnRequest: `REFUNDED`
     - Hoàn tiền cho Buyer

---

## 📊 Bảng Quyết Định Admin

### Tranh Chấp RETURN_REJECTION

| Decision | Winner | ReturnRequest Status | Hành Động |
|----------|--------|---------------------|-----------|
| `APPROVE_RETURN` | `BUYER` | `READY_TO_RETURN` | Cho phép trả hàng, chuẩn bị shipment |
| `REJECT_RETURN` | `STORE` | `CLOSED` | Giữ nguyên từ chối, đóng yêu cầu |

### Tranh Chấp RETURN_QUALITY

| Decision | Winner | ReturnRequest Status | Hành Động |
|----------|--------|---------------------|-----------|
| `APPROVE_STORE` | `STORE` | `REFUND_TO_STORE` | Hoàn tiền về ví Store |
| `REJECT_STORE` | `BUYER` | `REFUNDED` | Hoàn tiền cho Buyer |

---

## 🔐 Validation Rules

### ReturnRequest
- Chỉ tạo được khi Order có status = `DELIVERED`
- Mỗi Order chỉ có thể có 1 ReturnRequest active (không tính `CLOSED`)
- `refundAmount` = `order.totalPrice`

### Dispute
- Chỉ tạo `RETURN_REJECTION` khi ReturnRequest = `REJECTED`
- Chỉ tạo `RETURN_QUALITY` khi ReturnRequest = `RETURNED`
- Mỗi ReturnRequest chỉ có tối đa 1 dispute cho mỗi loại
- Không thể thêm tin nhắn khi status = `RESOLVED` hoặc `CLOSED`

---

## 🚀 API Endpoints Summary

### Buyer APIs
- `POST /buyer/orders/{orderId}/return` - Tạo yêu cầu trả hàng
- `GET /buyer/orders/returns` - Danh sách yêu cầu trả hàng
- `POST /buyer/orders/returns/{id}/dispute` - Khiếu nại khi bị từ chối
- `POST /buyer/orders/disputes/{id}/message` - Thêm tin nhắn vào dispute

### Store APIs
- `GET /b2c/returns/store/{storeId}` - Danh sách yêu cầu trả hàng
- `PUT /b2c/returns/store/{storeId}/returnRequest/{id}/respond` - Chấp nhận/Từ chối
- `PUT /b2c/returns/store/{storeId}/returnRequest/{id}/confirm-ok` - Xác nhận hàng OK
- `POST /b2c/returns/store/{storeId}/returnRequest/{id}/dispute-quality` - Khiếu nại chất lượng
- `POST /b2c/returns/store/{storeId}/disputes/{id}/message` - Phản hồi dispute

### Admin APIs
- `GET /admin/disputes` - Danh sách tất cả disputes
- `GET /admin/disputes/{id}` - Chi tiết dispute (có tất cả bằng chứng)
- `PUT /admin/disputes/{id}/resolve` - Quyết định dispute từ chối trả hàng
- `PUT /admin/disputes/{id}/resolve-quality` - Quyết định dispute chất lượng
- `POST /admin/disputes/{id}/message` - Thêm tin nhắn/yêu cầu bằng chứng

---
