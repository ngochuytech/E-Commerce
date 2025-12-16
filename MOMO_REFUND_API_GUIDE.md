# MoMo Refund API Documentation

## Tổng quan

Tài liệu này hướng dẫn cách sử dụng API hoàn tiền MoMo đã được tích hợp trong hệ thống e-commerce.

## Các API Endpoints

### 1. Tạo thanh toán MoMo
```
POST /api/v1/buyer/payments/momo/create_payment_request
```

**Request Body:**
```json
{
  "amount": 50000
}
```

> **Lưu ý:** `amount` phải là kiểu số (number), không phải string. Đơn vị: VND

**Response:**
```json
{
    "partnerCode": "MOMO",
    "orderId": "MOMO1702537200000",
    "requestId": "MOMO1702537200000",
    "amount": 50000,
    "responseTime": 1702537200000,
    "message": "Thành công.",
    "resultCode": 0,
    "payUrl": "https://test-payment.momo.vn/v2/gateway/pay?t=xxx"
}
```

### 2. Kiểm tra trạng thái thanh toán
```
GET /api/v1/buyer/payments/momo/check_status/{orderId}
```

**Response:**
```json
{
    "partnerCode": "MOMO",
    "orderId": "MOMO1702537200000",
    "requestId": "MOMO1702537200000",
    "amount": 50000,
    "transId": 2820086739,
    "resultCode": 0,
    "message": "Thành công.",
    "responseTime": 1702537200000
}
```

> **Lưu ý quan trọng:** Lưu lại `transId` từ response khi thanh toán thành công. Bạn sẽ cần nó để thực hiện hoàn tiền.

### 3. Hoàn tiền MoMo (Refund)
```
POST /api/v1/buyer/payments/momo/refund
```

**Request Body:**
```json
{
  "transId": 2820086739,
  "amount": 50000,
  "description": "Hoàn tiền do khách hàng hủy đơn"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| transId | Long | Yes | Mã giao dịch MoMo (từ response thanh toán thành công) |
| amount | Long | Yes | Số tiền hoàn (Min: 1,000 VND, Max: 50,000,000 VND) |
| description | String | Yes | Lý do hoàn tiền |

**Các loại hoàn tiền:**
- **Hoàn toàn bộ:** `amount` = số tiền đã thanh toán
- **Hoàn một phần:** `amount` < số tiền đã thanh toán

**Response thành công:**
```json
{
    "partnerCode": "MOMO",
    "orderId": "RF_1702537200000",
    "requestId": "MOMO_REFUND_1702537200000",
    "amount": 50000,
    "transId": 2820086740,
    "resultCode": 0,
    "message": "Thành công.",
    "responseTime": 1702537200000
}
```

### 4. Kiểm tra trạng thái hoàn tiền
```
GET /api/v1/buyer/payments/momo/refund/check_status/{orderId}
```

**Ví dụ:** `/api/v1/buyer/payments/momo/refund/check_status/RF_1702537200000`

**Response:**
```json
{
    "partnerCode": "MOMO",
    "orderId": "RF_1702537200000",
    "requestId": "MOMO_REFUND_QUERY_1702537200000",
    "resultCode": 0,
    "message": "Thành công.",
    "responseTime": 1702537200000,
    "refundTrans": [
      {
        "orderId": "RF_1702537200000",
        "amount": 50000,
        "resultCode": 0,
        "transId": 2820086740,
        "createdTime": 1702537200000
      }
    ]
}
```

## Mã kết quả (Result Codes)

| Result Code | Mô tả |
|-------------|-------|
| 0 | Giao dịch thành công |
| 1001 | Giao dịch đã tồn tại |
| 1002 | Giao dịch đang được xử lý |
| 1003 | Giao dịch bị từ chối |
| 1004 | Giao dịch đã hết hạn |
| 1005 | Tài khoản không đủ số dư |
| 1006 | Giao dịch không thành công |
| 1007 | Giao dịch không tìm thấy |
| 7002 | Giao dịch đang được xử lý bởi nhà cung cấp |

## Quy trình hoàn tiền

```
┌─────────────────────────────────────────────────────────────────────┐
│                        REFUND FLOW                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  1. Khách hàng yêu cầu hoàn tiền                                   │
│         │                                                            │
│         ▼                                                            │
│  2. Lấy transId từ đơn hàng đã thanh toán                          │
│         │                                                            │
│         ▼                                                            │
│  3. Gọi API /momo/refund với transId, amount, description          │
│         │                                                            │
│         ▼                                                            │
│  4. MoMo xử lý và hoàn tiền vào ví của khách hàng                  │
│         │                                                            │
│         ▼                                                            │
│  5. Kiểm tra trạng thái hoàn tiền (nếu cần)                        │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

## Cấu hình hiện tại (Test Environment)

```java
PARTNER_CODE = "MOMO"
ACCESS_KEY = "F8BBA842ECF85"
SECRET_KEY = "K951B6PE1waDMi640xX08PD3vg6EkVlz"
ENDPOINT = "https://test-payment.momo.vn/v2/gateway/api"
```

## Lưu ý quan trọng

1. **Môi trường test:** Các cấu hình hiện tại đang sử dụng môi trường test của MoMo. Khi chuyển sang production, cần cập nhật:
   - `PARTNER_CODE`
   - `ACCESS_KEY`
   - `SECRET_KEY`
   - `ENDPOINT` thành `https://payment.momo.vn/v2/gateway/api`

2. **Timeout:** Timeout tối thiểu khi gọi API refund là 30 giây.

3. **Số tiền hoàn:**
   - Tối thiểu: 1,000 VND
   - Tối đa: 50,000,000 VND
   - Với thanh toán bằng thẻ ATM nội địa: Tối thiểu 10,000 VND
   - Với thanh toán Tokenization: Tối đa 30,000,000 VND

4. **transId:** Luôn lưu lại `transId` từ response khi thanh toán thành công vào database để sử dụng cho việc hoàn tiền sau này.

## Ví dụ Code sử dụng Service

```java
@Autowired
private IMomoService momoService;

// Hoàn tiền
public void processRefund(Long transId, Long amount, String reason) {
    String result = momoService.refundPayment(transId, amount, reason);
    
    // Parse JSON response
    JSONObject response = new JSONObject(result);
    if (response.getInt("resultCode") == 0) {
        System.out.println("Hoàn tiền thành công!");
    } else {
        System.out.println("Hoàn tiền thất bại: " + response.getString("message"));
    }
}

// Kiểm tra trạng thái hoàn tiền
public void checkRefund(String refundOrderId) {
    String result = momoService.checkRefundStatus(refundOrderId);
    System.out.println("Trạng thái: " + result);
}
```
