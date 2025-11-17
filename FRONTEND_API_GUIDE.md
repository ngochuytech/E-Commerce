# Frontend Developer Guide - Payment & Wallet APIs

## Tổng quan

Tài liệu này hướng dẫn các developers frontend về cách tích hợp 3 API chính liên quan đến thanh toán và quản lý ví điện tử trong hệ thống e-commerce.

**3 API chính:**
1. **Admin Withdrawal Management** - Quản lý yêu cầu rút tiền (dành cho admin)
2. **B2C Wallet Management** - Quản lý ví của store (dành cho store owner)
3. **Buyer Payment Management** - Xử lý thanh toán qua VNPay (dành cho khách hàng)

---

## 1. Admin Withdrawal Management API

**Base URL:** `{api.prefix}/admin/withdrawals`  
**Authentication:** Required (Bearer Token)  
**User Role:** Admin only

### 1.1 Lấy danh sách tất cả yêu cầu rút tiền

```
GET /admin/withdrawals
```

**Mô tả:** Lấy danh sách các yêu cầu rút tiền của tất cả store với phân trang và bộ lọc theo trạng thái.

**Query Parameters:**
| Parameter | Type | Required | Default | Example | Mô tả |
|-----------|------|----------|---------|---------|-------|
| status | string | No | - | PENDING | Lọc theo trạng thái: `PENDING`, `APPROVED`, `REJECTED`, `COMPLETED` |
| page | integer | No | 1 | 1 | Trang cần lấy (bắt đầu từ 1) |
| size | integer | No | 10 | 10 | Số lượng items trên một trang |

**Request Example (JavaScript/Fetch):**
```javascript
// Lấy tất cả yêu cầu rút tiền chưa xử lý
const response = await fetch('/api/admin/withdrawals?status=PENDING&page=1&size=10', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

const data = await response.json();
```

**Response Success (200):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": "64f1a2b3c4d5e6f7a8b9c0d1",
        "storeId": "64f1a2b3c4d5e6f7a8b9c0d2",
        "storeName": "Tech Store Vietnam",
        "amount": 5000000,
        "bankName": "Vietcombank",
        "bankAccountNumber": "0011001234567",
        "bankAccountName": "NGUYEN VAN A",
        "status": "PENDING",
        "requestDate": "2024-11-18T10:30:00Z",
        "approvalDate": null,
        "completionDate": null,
        "note": null,
        "adminNote": null
      },
      {
        "id": "64f1a2b3c4d5e6f7a8b9c0d3",
        "storeId": "64f1a2b3c4d5e6f7a8b9c0d4",
        "storeName": "Laptop Center",
        "amount": 10000000,
        "bankName": "Techcombank",
        "bankAccountNumber": "0021002345678",
        "bankAccountName": "TRAN THI B",
        "status": "APPROVED",
        "requestDate": "2024-11-17T14:20:00Z",
        "approvalDate": "2024-11-18T09:00:00Z",
        "completionDate": null,
        "note": null,
        "adminNote": "Approved for processing"
      }
    ],
    "totalElements": 25,
    "totalPages": 3,
    "currentPage": 1,
    "pageSize": 10
  }
}
```

**Response Error (400):**
```json
{
  "code": 400,
  "message": "Invalid parameters",
  "data": null
}
```

---

### 1.2 Duyệt yêu cầu rút tiền

```
PUT /admin/withdrawals/{requestId}/approve
```

**Mô tả:** Admin duyệt (chấp thuận) một yêu cầu rút tiền và có thể thêm ghi chú.

**Path Parameters:**
| Parameter | Type | Required | Example | Mô tả |
|-----------|------|----------|---------|-------|
| requestId | string | Yes | 64f1a2b3c4d5e6f7a8b9c0d1 | ID của yêu cầu rút tiền |

**Query Parameters:**
| Parameter | Type | Required | Example | Mô tả |
|-----------|------|----------|---------|-------|
| adminNote | string | No | Approved for processing | Ghi chú của admin về lý do duyệt |

**Request Example:**
```javascript
const requestId = "64f1a2b3c4d5e6f7a8b9c0d1";
const adminNote = "Approved for processing";

const response = await fetch(
  `/api/admin/withdrawals/${requestId}/approve?adminNote=${encodeURIComponent(adminNote)}`,
  {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  }
);

const data = await response.json();
```

**Response Success (200):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "64f1a2b3c4d5e6f7a8b9c0d1",
    "storeId": "64f1a2b3c4d5e6f7a8b9c0d2",
    "storeName": "Tech Store Vietnam",
    "amount": 5000000,
    "bankName": "Vietcombank",
    "bankAccountNumber": "0011001234567",
    "bankAccountName": "NGUYEN VAN A",
    "status": "APPROVED",
    "requestDate": "2024-11-18T10:30:00Z",
    "approvalDate": "2024-11-18T15:45:00Z",
    "completionDate": null,
    "note": null,
    "adminNote": "Approved for processing"
  }
}
```

---

### 1.3 Từ chối yêu cầu rút tiền

```
PUT /admin/withdrawals/{requestId}/reject
```

**Mô tả:** Admin từ chối một yêu cầu rút tiền và cung cấp lý do từ chối.

**Path Parameters:**
| Parameter | Type | Required | Example | Mô tả |
|-----------|------|----------|---------|-------|
| requestId | string | Yes | 64f1a2b3c4d5e6f7a8b9c0d1 | ID của yêu cầu rút tiền |

**Query Parameters:**
| Parameter | Type | Required | Example | Mô tả |
|-----------|------|----------|---------|-------|
| adminNote | string | No | Invalid bank account information | Lý do từ chối |

**Request Example:**
```javascript
const requestId = "64f1a2b3c4d5e6f7a8b9c0d1";
const rejectionReason = "Invalid bank account information";

const response = await fetch(
  `/api/admin/withdrawals/${requestId}/reject?adminNote=${encodeURIComponent(rejectionReason)}`,
  {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  }
);

const data = await response.json();
```

**Response Success (200):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "64f1a2b3c4d5e6f7a8b9c0d1",
    "storeId": "64f1a2b3c4d5e6f7a8b9c0d2",
    "storeName": "Tech Store Vietnam",
    "amount": 5000000,
    "status": "REJECTED",
    "requestDate": "2024-11-18T10:30:00Z",
    "approvalDate": null,
    "completionDate": null,
    "adminNote": "Invalid bank account information"
  }
}
```

---

### 1.4 Hoàn thành chuyển tiền

```
PUT /admin/withdrawals/{requestId}/complete
```

**Mô tả:** Admin đánh dấu yêu cầu rút tiền đã hoàn thành (sau khi đã thực hiện chuyển khoản ngân hàng).

**Path Parameters:**
| Parameter | Type | Required | Example | Mô tả |
|-----------|------|----------|---------|-------|
| requestId | string | Yes | 64f1a2b3c4d5e6f7a8b9c0d1 | ID của yêu cầu rút tiền |

**Request Example:**
```javascript
const requestId = "64f1a2b3c4d5e6f7a8b9c0d1";

const response = await fetch(
  `/api/admin/withdrawals/${requestId}/complete`,
  {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  }
);

const data = await response.json();
```

**Response Success (200):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "64f1a2b3c4d5e6f7a8b9c0d1",
    "storeId": "64f1a2b3c4d5e6f7a8b9c0d2",
    "storeName": "Tech Store Vietnam",
    "amount": 5000000,
    "status": "COMPLETED",
    "requestDate": "2024-11-18T10:30:00Z",
    "approvalDate": "2024-11-18T15:45:00Z",
    "completionDate": "2024-11-19T08:30:00Z",
    "adminNote": "Transfer completed"
  }
}
```

---

## 2. B2C Wallet Management API

**Base URL:** `{api.prefix}/b2c/wallet`  
**Authentication:** Required (Bearer Token)  
**User Role:** Store Owner

### 2.1 Lấy thông tin ví của store

```
GET /wallet/store/{storeId}
```

**Mô tả:** Lấy thông tin số dư ví và chi tiết tài khoản ví của một store.

**Path Parameters:**
| Parameter | Type | Required | Example | Mô tả |
|-----------|------|----------|---------|-------|
| storeId | string | Yes | 64f1a2b3c4d5e6f7a8b9c0d1 | ID của store |

**Request Example:**
```javascript
const storeId = "64f1a2b3c4d5e6f7a8b9c0d1";

const response = await fetch(`/api/b2c/wallet/store/${storeId}`, {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

const data = await response.json();
```

**Response Success (200):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "64f1a2b3c4d5e6f7a8b9c0d5",
    "storeId": "64f1a2b3c4d5e6f7a8b9c0d1",
    "balance": 15500000,
    "totalDeposit": 50000000,
    "totalWithdrawal": 34500000,
    "totalCommission": 0,
    "lastUpdated": "2024-11-18T20:15:00Z",
    "createdAt": "2024-01-15T10:00:00Z"
  }
}
```

**Response Error (400):**
```json
{
  "code": 400,
  "message": "Store not found or unauthorized access",
  "data": null
}
```

---

### 2.2 Lấy lịch sử giao dịch

```
GET /wallet/store/{storeId}/transactions
```

**Mô tả:** Lấy danh sách lịch sử giao dịch của store với phân trang, bao gồm deposit, rút tiền, hoa hồng.

**Path Parameters:**
| Parameter | Type | Required | Example | Mô tả |
|-----------|------|----------|---------|-------|
| storeId | string | Yes | 64f1a2b3c4d5e6f7a8b9c0d1 | ID của store |

**Query Parameters:**
| Parameter | Type | Required | Default | Example | Mô tả |
|-----------|------|----------|---------|---------|-------|
| page | integer | No | 1 | 1 | Trang cần lấy |
| size | integer | No | 10 | 10 | Số lượng items trên một trang |

**Request Example:**
```javascript
const storeId = "64f1a2b3c4d5e6f7a8b9c0d1";

const response = await fetch(
  `/api/b2c/wallet/store/${storeId}/transactions?page=1&size=20`,
  {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  }
);

const data = await response.json();
```

**Response Success (200):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": "64f1a2b3c4d5e6f7a8b9c0d6",
        "walletId": "64f1a2b3c4d5e6f7a8b9c0d5",
        "type": "DEPOSIT",
        "amount": 10000000,
        "description": "Deposit from payment order #ORD123456",
        "balanceBefore": 5500000,
        "balanceAfter": 15500000,
        "referenceId": "ORD123456",
        "createdAt": "2024-11-18T20:15:00Z"
      },
      {
        "id": "64f1a2b3c4d5e6f7a8b9c0d7",
        "walletId": "64f1a2b3c4d5e6f7a8b9c0d5",
        "type": "WITHDRAWAL",
        "amount": -5000000,
        "description": "Withdrawal request #WD001",
        "balanceBefore": 20500000,
        "balanceAfter": 15500000,
        "referenceId": "WD001",
        "createdAt": "2024-11-17T15:30:00Z"
      },
      {
        "id": "64f1a2b3c4d5e6f7a8b9c0d8",
        "walletId": "64f1a2b3c4d5e6f7a8b9c0d5",
        "type": "COMMISSION",
        "amount": -500000,
        "description": "Commission fee for order #ORD123450",
        "balanceBefore": 21000000,
        "balanceAfter": 20500000,
        "referenceId": "ORD123450",
        "createdAt": "2024-11-16T10:20:00Z"
      }
    ],
    "totalElements": 45,
    "totalPages": 3,
    "currentPage": 1,
    "pageSize": 20
  }
}
```

---

### 2.3 Tạo yêu cầu rút tiền

```
POST /wallet/store/{storeId}/withdrawal
```

**Mô tả:** Tạo một yêu cầu rút tiền mới. Yêu cầu phải được admin duyệt trước khi thực hiện chuyển khoản.

**Path Parameters:**
| Parameter | Type | Required | Example | Mô tả |
|-----------|------|----------|---------|-------|
| storeId | string | Yes | 64f1a2b3c4d5e6f7a8b9c0d1 | ID của store |

**Request Body:**
```json
{
  "amount": 5000000,
  "bankName": "Vietcombank",
  "bankAccountNumber": "0011001234567",
  "bankAccountName": "NGUYEN VAN A",
  "note": "Withdrawal for business expenses"
}
```

**Request Parameters:**
| Field | Type | Required | Constraints | Mô tả |
|-------|------|----------|-------------|-------|
| amount | number | Yes | > 0, <= balance | Số tiền muốn rút (VND) |
| bankName | string | Yes | 1-100 chars | Tên ngân hàng |
| bankAccountNumber | string | Yes | 1-50 chars | Số tài khoản ngân hàng |
| bankAccountName | string | Yes | 1-100 chars | Tên chủ tài khoản |
| note | string | No | 0-500 chars | Ghi chú thêm |

**Request Example (JavaScript/Axios):**
```javascript
import axios from 'axios';

const storeId = "64f1a2b3c4d5e6f7a8b9c0d1";
const withdrawalData = {
  amount: 5000000,
  bankName: "Vietcombank",
  bankAccountNumber: "0011001234567",
  bankAccountName: "NGUYEN VAN A",
  note: "Monthly withdrawal"
};

try {
  const response = await axios.post(
    `/api/b2c/wallet/store/${storeId}/withdrawal`,
    withdrawalData,
    {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    }
  );
  
  console.log('Withdrawal request created:', response.data.data);
} catch (error) {
  console.error('Error:', error.response.data);
}
```

**Response Success (200):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "64f1a2b3c4d5e6f7a8b9c0d9",
    "storeId": "64f1a2b3c4d5e6f7a8b9c0d1",
    "amount": 5000000,
    "bankName": "Vietcombank",
    "bankAccountNumber": "0011001234567",
    "bankAccountName": "NGUYEN VAN A",
    "status": "PENDING",
    "note": "Monthly withdrawal",
    "requestDate": "2024-11-18T21:30:00Z",
    "approvalDate": null,
    "completionDate": null
  }
}
```

**Response Error (400):**
```json
{
  "code": 400,
  "message": "Insufficient balance. Available: 3000000, Requested: 5000000",
  "data": null
}
```

---

### 2.4 Lấy danh sách yêu cầu rút tiền của store

```
GET /wallet/store/{storeId}/withdrawals
```

**Mô tả:** Lấy danh sách tất cả yêu cầu rút tiền của store với phân trang.

**Path Parameters:**
| Parameter | Type | Required | Example | Mô tả |
|-----------|------|----------|---------|-------|
| storeId | string | Yes | 64f1a2b3c4d5e6f7a8b9c0d1 | ID của store |

**Query Parameters:**
| Parameter | Type | Required | Default | Example | Mô tả |
|-----------|------|----------|---------|---------|-------|
| page | integer | No | 1 | 1 | Trang cần lấy |
| size | integer | No | 10 | 10 | Số lượng items trên một trang |

**Request Example:**
```javascript
const storeId = "64f1a2b3c4d5e6f7a8b9c0d1";

const response = await fetch(
  `/api/b2c/wallet/store/${storeId}/withdrawals?page=1&size=10`,
  {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  }
);

const data = await response.json();
```

**Response Success (200):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": "64f1a2b3c4d5e6f7a8b9c0d9",
        "storeId": "64f1a2b3c4d5e6f7a8b9c0d1",
        "amount": 5000000,
        "bankName": "Vietcombank",
        "bankAccountNumber": "0011001234567",
        "bankAccountName": "NGUYEN VAN A",
        "status": "PENDING",
        "requestDate": "2024-11-18T21:30:00Z",
        "approvalDate": null,
        "completionDate": null
      },
      {
        "id": "64f1a2b3c4d5e6f7a8b9c0e0",
        "storeId": "64f1a2b3c4d5e6f7a8b9c0d1",
        "amount": 3000000,
        "bankName": "Techcombank",
        "bankAccountNumber": "0021002345678",
        "bankAccountName": "NGUYEN VAN A",
        "status": "COMPLETED",
        "requestDate": "2024-11-10T14:20:00Z",
        "approvalDate": "2024-11-11T09:00:00Z",
        "completionDate": "2024-11-12T08:30:00Z"
      }
    ],
    "totalElements": 12,
    "totalPages": 2,
    "currentPage": 1,
    "pageSize": 10
  }
}
```

---

### 2.5 Lấy chi tiết yêu cầu rút tiền

```
GET /wallet/store/{storeId}/withdrawal/{requestId}
```

**Mô tả:** Lấy thông tin chi tiết của một yêu cầu rút tiền cụ thể.

**Path Parameters:**
| Parameter | Type | Required | Example | Mô tả |
|-----------|------|----------|---------|-------|
| storeId | string | Yes | 64f1a2b3c4d5e6f7a8b9c0d1 | ID của store |
| requestId | string | Yes | 64f1a2b3c4d5e6f7a8b9c0d9 | ID của yêu cầu rút tiền |

**Request Example:**
```javascript
const storeId = "64f1a2b3c4d5e6f7a8b9c0d1";
const requestId = "64f1a2b3c4d5e6f7a8b9c0d9";

const response = await fetch(
  `/api/b2c/wallet/store/${storeId}/withdrawal/${requestId}`,
  {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  }
);

const data = await response.json();
```

**Response Success (200):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "64f1a2b3c4d5e6f7a8b9c0d9",
    "storeId": "64f1a2b3c4d5e6f7a8b9c0d1",
    "storeName": "Tech Store Vietnam",
    "amount": 5000000,
    "bankName": "Vietcombank",
    "bankAccountNumber": "0011001234567",
    "bankAccountName": "NGUYEN VAN A",
    "status": "PENDING",
    "note": "Monthly withdrawal",
    "adminNote": null,
    "requestDate": "2024-11-18T21:30:00Z",
    "approvalDate": null,
    "completionDate": null
  }
}
```

---

## 3. Buyer Payment Management API

**Base URL:** `{api.prefix}/buyer/payments`  
**Authentication:** Required (Bearer Token)  
**User Role:** Buyer  
**Payment Gateway:** VNPay

### 3.1 Tạo URL thanh toán VNPay

```
POST /buyer/payments/create_payment_url
```

**Mô tả:** Tạo URL thanh toán để người dùng thanh toán qua cổng VNPay. Sau khi click vào URL, khách hàng sẽ được chuyển hướng đến trang VNPay.

**Request Body:**
```json
{
  "amount": 5000000,
  "orderInfo": "Order #ORD123456",
  "bankCode": "NCB",
  "language": "vn"
}
```

**Request Parameters:**
| Field | Type | Required | Constraints | Mô tả |
|-------|------|----------|-------------|-------|
| amount | number | Yes | > 0 | Số tiền thanh toán (VND) |
| orderInfo | string | Yes | 1-200 chars | Thông tin đơn hàng |
| bankCode | string | No | - | Mã ngân hàng. VD: `NCB` (Ngoại Thương), `VCCB` (Vietcombank), ... Nếu không có, sẽ hiển thị danh sách ngân hàng |
| language | string | No | vn, en | Ngôn ngữ giao diện VNPay (`vn`: Tiếng Việt, `en`: Tiếng Anh). Mặc định: `vn` |

**Request Example (JavaScript/Axios):**
```javascript
import axios from 'axios';

const paymentData = {
  amount: 5000000,
  orderInfo: "Order #ORD123456 - Laptop ASUS",
  bankCode: "NCB",
  language: "vn"
};

try {
  const response = await axios.post(
    '/api/buyer/payments/create_payment_url',
    paymentData,
    {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    }
  );
  
  // Lấy payment URL
  const paymentUrl = response.data.data;
  
  // Chuyển hướng đến trang thanh toán VNPay
  window.location.href = paymentUrl;
} catch (error) {
  console.error('Error creating payment:', error.response.data);
}
```

**Response Success (200):**
```json
{
  "code": 200,
  "message": "Success",
  "data": "https://sandbox.vnpayment.vn/paygate?vnp_Amount=5000000&vnp_CreateDate=20241118213000&vnp_CurrCode=VND&vnp_IpAddr=192.168.1.1&vnp_Locale=vn&vnp_OrderInfo=Order%20%23ORD123456&vnp_OrderType=other&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A3000%2Fpayment-result&vnp_TmnCode=2L3HCISJ&vnp_TransactionNo=20241118213000&vnp_TxnRef=ORD20241118213000&vnp_SecureHash=abcdef123456..."
}
```

**Response Error (400):**
```json
{
  "code": 400,
  "message": "Invalid payment details - amount must be greater than 0",
  "data": null
}
```

---

### 3.2 Kiểm tra trạng thái giao dịch

```
POST /buyer/payments/query
```

**Mô tả:** Kiểm tra trạng thái thanh toán của một giao dịch từ VNPay (dùng để xác nhận thanh toán thành công).

**Request Body:**
```json
{
  "orderId": "ORD123456",
  "transactionDate": "20241118"
}
```

**Request Parameters:**
| Field | Type | Required | Constraints | Mô tả |
|-------|------|----------|-------------|-------|
| orderId | string | Yes | - | Mã đơn hàng gốc |
| transactionDate | string | Yes | YYYYMMDD | Ngày giao dịch (định dạng YYYYMMDD) |

**Request Example:**
```javascript
const queryData = {
  orderId: "ORD123456",
  transactionDate: "20241118"
};

const response = await axios.post(
  '/api/buyer/payments/query',
  queryData,
  {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  }
);

const transactionStatus = response.data.data;
console.log('Transaction status:', transactionStatus);
```

**Response Success (200):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "orderId": "ORD123456",
    "status": "SUCCESS",
    "message": "Transaction successful",
    "transactionNo": "VNP20241118213000",
    "amount": 5000000,
    "paymentDate": "20241118213000"
  }
}
```

**Possible Response Status Values:**
- `SUCCESS` - Thanh toán thành công
- `PENDING` - Giao dịch đang chờ xử lý
- `FAILED` - Thanh toán thất bại
- `CANCELLED` - Giao dịch bị hủy

---

### 3.3 Hoàn lại tiền (Refund)

```
POST /buyer/payments/refund
```

**Mô tả:** Hoàn lại tiền cho khách hàng (Refund). Thường được gọi bởi admin khi đơn hàng bị hủy.

**Request Body:**
```json
{
  "orderId": "ORD123456",
  "amount": 5000000,
  "transactionDate": "20241118",
  "reason": "Customer requested cancellation"
}
```

**Request Parameters:**
| Field | Type | Required | Constraints | Mô tả |
|-------|------|----------|-------------|-------|
| orderId | string | Yes | - | Mã đơn hàng gốc |
| amount | number | Yes | > 0 | Số tiền hoàn lại (VND) |
| transactionDate | string | Yes | YYYYMMDD | Ngày giao dịch gốc (YYYYMMDD) |
| reason | string | No | 0-200 chars | Lý do hoàn lại tiền |

**Request Example (JavaScript/Axios):**
```javascript
const refundData = {
  orderId: "ORD123456",
  amount: 5000000,
  transactionDate: "20241118",
  reason: "Customer requested cancellation"
};

try {
  const response = await axios.post(
    '/api/buyer/payments/refund',
    refundData,
    {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    }
  );
  
  console.log('Refund response:', response.data.data);
} catch (error) {
  if (error.response?.status === 400) {
    console.error('Validation errors:', error.response.data.message);
  }
}
```

**Response Success (200):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "orderId": "ORD123456",
    "refundStatus": "PROCESSING",
    "refundAmount": 5000000,
    "originalTransactionNo": "VNP20241118213000",
    "refundTransactionNo": "REF20241119100000",
    "reason": "Customer requested cancellation",
    "requestDate": "2024-11-19T10:00:00Z"
  }
}
```

**Response Error (400):**
```json
{
  "code": 400,
  "message": "Refund amount exceeds original transaction amount",
  "data": null
}
```

---

## 4. Common Response Format

Tất cả API đều trả về response theo định dạng chung sau:

### Success Response
```json
{
  "code": 200,
  "message": "Success",
  "data": { /* response data */ }
}
```

### Error Response
```json
{
  "code": 400,
  "message": "Error message describing what went wrong",
  "data": null
}
```

### HTTP Status Codes
| Code | Meaning | Mô tả |
|------|---------|-------|
| 200 | OK | Yêu cầu thành công |
| 400 | Bad Request | Dữ liệu không hợp lệ hoặc yêu cầu thất bại |
| 401 | Unauthorized | Token không hợp lệ hoặc hết hạn |
| 403 | Forbidden | Không có quyền truy cập API này |
| 404 | Not Found | Resource không tồn tại |
| 500 | Internal Server Error | Lỗi server |

---

## 5. Authentication

### Bearer Token
Tất cả API yêu cầu authentication bằng Bearer Token được gửi trong header `Authorization`:

```javascript
const headers = {
  'Authorization': `Bearer ${accessToken}`,
  'Content-Type': 'application/json'
};
```

### Getting Access Token
Token được lấy từ endpoint login:

```javascript
// Login to get token
const loginResponse = await axios.post('/api/auth/login', {
  username: 'user@example.com',
  password: 'password123'
});

const accessToken = loginResponse.data.data.accessToken;

// Store token in localStorage or sessionStorage
localStorage.setItem('accessToken', accessToken);

// Use token in subsequent requests
const config = {
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }
};
```

---

## 6. Error Handling Best Practices

### Frontend Error Handling Example
```javascript
import axios from 'axios';

// Create axios instance with default config
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add request interceptor to attach token
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// Add response interceptor for error handling
api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      // Token expired or invalid - redirect to login
      localStorage.removeItem('accessToken');
      window.location.href = '/login';
    } else if (error.response?.status === 403) {
      // No permission
      console.error('You do not have permission to access this resource');
    } else if (error.response?.status === 400) {
      // Bad request - show error message
      const errorMessage = error.response.data?.message || 'Invalid request';
      console.error('Validation error:', errorMessage);
    }
    return Promise.reject(error);
  }
);

// Usage
try {
  const result = await api.get('/admin/withdrawals?status=PENDING');
  console.log(result.data); // API response
} catch (error) {
  console.error('API call failed:', error.message);
}
```

---

## 7. Status Values Reference

### Withdrawal Request Status
- `PENDING` - Chờ duyệt từ admin
- `APPROVED` - Đã được admin duyệt
- `REJECTED` - Bị admin từ chối
- `COMPLETED` - Đã hoàn thành chuyển tiền

### Transaction Types
- `DEPOSIT` - Nạp tiền vào ví
- `WITHDRAWAL` - Rút tiền từ ví
- `COMMISSION` - Hoa hồng (bị trừ)

### Payment Status (VNPay Query)
- `SUCCESS` - Thanh toán thành công
- `PENDING` - Đang chờ xử lý
- `FAILED` - Thanh toán thất bại
- `CANCELLED` - Bị hủy

---

## 8. Postman Collection

Bạn có thể import collection sau vào Postman để test các API:

```json
{
  "info": {
    "name": "E-Commerce Payment & Wallet APIs",
    "description": "Collection for testing payment and wallet management APIs",
    "version": "1.0.0"
  },
  "item": [
    {
      "name": "Admin Withdrawals",
      "item": [
        {
          "name": "Get All Withdrawals",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/admin/withdrawals?status=PENDING&page=1&size=10",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ]
          }
        }
      ]
    }
  ]
}
```

---

## 9. Integration Checklist

Khi tích hợp các API này, hãy kiểm tra:

- [ ] Đã setup Bearer Token authentication
- [ ] Đã handle các HTTP status codes (200, 400, 401, 403, 404)
- [ ] Đã implement error handling và hiển thị error messages cho user
- [ ] Đã setup request/response interceptors (nếu dùng Axios)
- [ ] Đã validate input data trước khi gửi request
- [ ] Đã handle loading states (loading spinner)
- [ ] Đã implement token refresh logic (nếu token hết hạn)
- [ ] Đã test các API endpoints với Postman hoặc Swagger UI
- [ ] Đã implement proper logging cho debugging
- [ ] Đã handle timeout cho các request

---

**Last Updated:** 2024-11-18  