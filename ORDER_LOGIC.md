# 📋 ORDER MANAGEMENT LOGIC - E-COMMERCE SYSTEM

## 🎯 TỔNG QUAN

**Order Management** là hệ thống quản lý đơn hàng cho người mua (Buyer) trong e-commerce platform. Hệ thống cho phép người dùng tạo đơn hàng từ giỏ hàng, theo dõi trạng thái và quản lý lịch sử mua hàng.

---

## 🔄 QUY TRÌNH MUA HÀNG

### 1. **FLOW CHÍNH**
```
Cart (có sản phẩm) → Checkout → Order Creation → Order Management
```

### 2. **USER JOURNEY**
```
1. User có sản phẩm trong cart
2. Click "Checkout"
3. Nhập thông tin giao hàng
4. Chọn phương thức thanh toán
5. Click "Place Order"
6. System tạo order với status PENDING
7. Cart được clear
8. User nhận thông báo đặt hàng thành công
```

---

## 🏗️ DATABASE SCHEMA

### **Orders Table**
```sql
CREATE TABLE orders (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    shipping_address TEXT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### **Order Items Table**
```sql
CREATE TABLE order_items (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_variant_id VARCHAR(36) NOT NULL,
    store_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(15,2) NOT NULL,
    
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_variant_id) REFERENCES product_variants(id),
    FOREIGN KEY (store_id) REFERENCES stores(id)
);
```

---

## 🔐 ORDER STATUS FLOW

### **Status Progression**
```
PENDING → CONFIRMED → SHIPPING → DELIVERED
    ↓
CANCELLED (chỉ được hủy khi PENDING)
```

### **Ý nghĩa từng Status**
| Status | Mô tả | Hành động cho phép |
|--------|-------|-------------------|
| **PENDING** | Đơn hàng vừa tạo, chờ xác nhận | Hủy đơn hàng |
| **CONFIRMED** | Đã xác nhận, chuẩn bị hàng | Không thể hủy |
| **SHIPPING** | Đang giao hàng | Không thể hủy |
| **DELIVERED** | Đã giao hàng thành công | Hoàn thành |
| **CANCELLED** | Đã hủy đơn hàng | Kết thúc |

---

## 🛒 CHECKOUT PROCESS

### **1. Validation Steps**
- ✅ **Cart không rỗng** - Phải có ít nhất 1 sản phẩm
- ✅ **Kiểm tra stock** - Mỗi sản phẩm còn đủ hàng không
- ✅ **Kiểm tra giá** - Giá sản phẩm có thay đổi không
- ✅ **Kiểm tra trạng thái** - Sản phẩm còn active không

### **2. Process Flow**
```
1. Lấy tất cả cart items
2. Validate từng sản phẩm (stock, price, status)
3. Tính tổng tiền (subtotal + tax + shipping)
4. Tạo Order với status = PENDING
5. Tạo OrderItems từ CartItems
6. Clear cart sau khi tạo order thành công
```

### **3. Price Locking Logic**
- **OrderItem.price** lưu giá tại thời điểm mua
- **Không bị ảnh hưởng** bởi thay đổi giá sau này
- **Bảo vệ buyer** khỏi tăng giá đột ngột

---

## 🌐 API ENDPOINTS

### **Checkout**
```
POST /api/v1/buyer/orders/checkout
Content-Type: application/json
Authorization: Bearer {token}

Request Body:
{
    "shippingAddress": "123 Đường ABC, Quận 1, TP.HCM",
    "paymentMethod": "COD",
    "notes": "Giao hàng vào buổi chiều"
}

Response:
{
    "success": true,
    "message": "Đặt hàng thành công",
    "data": {
        "orderId": "order-uuid",
        "totalAmount": 15000000,
        "status": "PENDING",
        "createdAt": "2024-01-15T10:30:00Z"
    }
}
```

### **Order Management**
```
# Lịch sử đơn hàng
GET /api/v1/buyer/orders?page=1&size=10&status=ALL
Authorization: Bearer {token}

# Chi tiết đơn hàng
GET /api/v1/buyer/orders/{orderId}
Authorization: Bearer {token}

# Hủy đơn hàng
PUT /api/v1/buyer/orders/{orderId}/cancel
Authorization: Bearer {token}

# Theo dõi đơn hàng
GET /api/v1/buyer/orders/{orderId}/track
Authorization: Bearer {token}
```

---

## 🏪 MULTI-STORE SUPPORT

### **Logic**
- **1 order có thể chứa sản phẩm từ nhiều store**
- **Mỗi OrderItem có store_id riêng**
- **Tính shipping riêng cho từng store** (nếu cần)

### **Ví dụ**
```
Order #12345:
├── OrderItem 1: iPhone 15 (Store A) - 25,000,000 VND
├── OrderItem 2: Laptop (Store B) - 15,000,000 VND
└── Total: 40,000,000 VND
```

---

## 📊 RESPONSE DTOs

### **OrderResponseDTO**
```java
{
    "id": "order-uuid",
    "userId": "user-uuid",
    "totalAmount": 15000000,
    "status": "PENDING",
    "shippingAddress": "123 Đường ABC, Quận 1, TP.HCM",
    "paymentMethod": "COD",
    "notes": "Giao hàng vào buổi chiều",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z",
    "orderItems": [
        {
            "id": "order-item-uuid",
            "productVariant": {
                "id": "product-variant-uuid",
                "name": "iPhone 15 Pro Max 256GB",
                "imageUrl": "https://example.com/iphone15.jpg",
                "price": 25000000
            },
            "store": {
                "id": "store-uuid",
                "name": "Apple Store",
                "logoUrl": "https://example.com/apple-logo.jpg"
            },
            "quantity": 1,
            "price": 25000000,
            "subtotal": 25000000
        }
    ]
}
```

---

## ⚠️ BUSINESS RULES

### **1. Checkout Rules**
- ❌ **Không thể checkout** khi cart rỗng
- ❌ **Không thể checkout** khi sản phẩm hết hàng
- ❌ **Không thể checkout** khi giá thay đổi
- ✅ **Chỉ có thể checkout** tất cả sản phẩm trong cart

### **2. Order Management Rules**
- ✅ **Chỉ được hủy** khi status = PENDING
- ❌ **Không thể hủy** khi đã CONFIRMED hoặc SHIPPING
- ✅ **Chỉ owner** mới được xem/update order của mình
- ✅ **Order history** được lưu vĩnh viễn

### **3. Stock Management**
- ✅ **Kiểm tra stock** trước khi tạo order
- ✅ **Có thể reserve stock** (tùy business logic)
- ✅ **Trừ stock** khi order được CONFIRMED



---

## 📈 FUTURE ENHANCEMENTS

### **1. Payment Integration**
- Tích hợp payment gateway (VNPay, MoMo, etc.)
- Xử lý thanh toán online
- Webhook để cập nhật payment status

### **2. Notification System**
- Email/SMS thông báo khi order status thay đổi
- Push notification cho mobile app
- Real-time status updates

### **3. Advanced Features**
- Order tracking với tracking number
- Estimated delivery time
- Order history analytics
- Return/Refund functionality

---

## 🎯 KẾT LUẬN

**Order Management System** cung cấp:
- ✅ **Complete shopping flow** - Cart → Checkout → Order
- ✅ **Multi-store support** - 1 order nhiều store
- ✅ **Price locking** - Bảo vệ buyer
- ✅ **Status management** - Theo dõi trạng thái
- ✅ **Order history** - Lịch sử đơn hàng
- ✅ **Business rules** - Validation và constraints

Hệ thống đảm bảo tính nhất quán, bảo mật và trải nghiệm người dùng tốt nhất trong quá trình mua hàng.
