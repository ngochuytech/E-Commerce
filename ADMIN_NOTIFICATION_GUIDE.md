## 📢 Admin Notification System - Hướng Dẫn Chi Tiết

## 📋 Tổng Quan

Hệ thống thông báo Admin cung cấp cho các quản trị viên nền tảng những cập nhật quan trọng về các sự kiện cần quản lý như:
- Yêu cầu phê duyệt cửa hàng mới
- Yêu cầu phê duyệt sản phẩm/biến thể
- Yêu cầu rút tiền từ sellers/buyers
---

## 🔧 Cấu Trúc Notification

```java
{
  "_id": "ObjectId",
  "title": "Có 5 đơn hàng mới cần xử lý",
  "message": "Đơn hàng từ ngày 24/11/2025 đã được tạo",
  "type": "ORDER_UPDATE",
  "relatedId": "order_id_123",
  "isAdmin": true,
  "isRead": false,
  "createdAt": "2025-11-24T10:30:00"
}
```

### Các trường chính:
- **title**: Tiêu đề notification
- **message**: Nội dung chi tiết
- **type**: Loại notification (STORE_APPROVAL, PRODUCT_APPROVAL, WITHDRAWAL_REQUEST, SYSTEM)
- **relatedId**: ID của đối tượng liên quan (Store, Product, WithdrawalRequest, etc.)
- **isAdmin**: Luôn là true cho admin notifications
- **isRead**: Trạng thái đã đọc
- **createdAt**: Thời gian tạo

---

## 🌐 API Endpoints

### Base URL
```
http://localhost:8080/api/v1/admin/notifications
```

### 1. Lấy danh sách notification của admin

**Endpoint:**
```
GET /api/v1/admin/notifications
```

**Parameters:**
```
isRead: (optional) true/false - Lọc theo trạng thái đã đọc
page: (optional, default: 0) - Số trang
size: (optional, default: 10) - Số items mỗi trang
```

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/notifications?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "notifications": [
      {
        "id": "notification_id_1",
        "title": "Cửa hàng mới chờ phê duyệt",
        "message": "Cửa hàng 'TechStore' đã đăng ký và chờ phê duyệt",
        "type": "STORE_APPROVAL",
        "relatedId": "store_id_123",
        "isAdmin": true,
        "isRead": false,
        "createdAt": "2025-11-24T10:30:00"
      }
    ],
    "page": 0,
    "size": 10,
    "total": 25,
    "unreadCount": 5
  }
}
```

### 2. Lọc notification theo trạng thái đã đọc

**Endpoint:**
```
GET /api/v1/admin/notifications?isRead=false
```

**cURL Example:**
```bash
# Lấy notification chưa đọc
curl -X GET "http://localhost:8080/api/v1/admin/notifications?isRead=false" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Lấy notification đã đọc
curl -X GET "http://localhost:8080/api/v1/admin/notifications?isRead=true" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Lấy số notification chưa đọc

**Endpoint:**
```
GET /api/v1/admin/notifications/unread-count
```

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/notifications/unread-count" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "unreadCount": 5
  }
}
```

### 4. Đánh dấu 1 notification là đã đọc

**Endpoint:**
```
PUT /api/v1/admin/notifications/{notificationId}/read
```

**cURL Example:**
```bash
curl -X PUT "http://localhost:8080/api/v1/admin/notifications/notification_id_1/read" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

**Response:**
```json
{
  "success": true,
  "data": "Notification marked as read"
}
```

### 5. Đánh dấu tất cả notification là đã đọc

**Endpoint:**
```
PUT /api/v1/admin/notifications/mark-all-read
```

**cURL Example:**
```bash
curl -X PUT "http://localhost:8080/api/v1/admin/notifications/mark-all-read" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

**Response:**
```json
{
  "success": true,
  "data": "All notifications marked as read"
}
```

### 6. Lấy notification theo ID

**Endpoint:**
```
GET /api/v1/admin/notifications/{notificationId}
```

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/notifications/notification_id_1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 7. Xóa notification

**Endpoint:**
```
DELETE /api/v1/admin/notifications/{notificationId}
```

**cURL Example:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/admin/notifications/notification_id_1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "data": "Notification deleted successfully"
}
```

### 8. Lấy notification theo type

**Endpoint:**
```
GET /api/v1/admin/notifications/by-type/{type}
```

**Type Options:**
- `STORE_APPROVAL` - Phê duyệt cửa hàng
- `PRODUCT_APPROVAL` - Phê duyệt sản phẩm/biến thể
- `WITHDRAWAL_REQUEST` - Yêu cầu rút tiền
- `SYSTEM` - Thông báo hệ thống

**cURL Example:**
```bash
# Lấy tất cả notification về phê duyệt cửa hàng
curl -X GET "http://localhost:8080/api/v1/admin/notifications/by-type/STORE_APPROVAL?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Lấy tất cả notification về phê duyệt sản phẩm
curl -X GET "http://localhost:8080/api/v1/admin/notifications/by-type/PRODUCT_APPROVAL" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Lấy tất cả notification về rút tiền
curl -X GET "http://localhost:8080/api/v1/admin/notifications/by-type/WITHDRAWAL_REQUEST" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "notifications": [
      {
        "id": "notification_id_1",
        "title": "Cửa hàng mới chờ phê duyệt",
        "message": "Cửa hàng 'TechStore' đã đăng ký",
        "type": "STORE_APPROVAL",
        "relatedId": "store_id_123",
        "isAdmin": true,
        "isRead": false,
        "createdAt": "2025-11-24T10:30:00"
      }
    ],
    "type": "STORE_APPROVAL",
    "page": 0,
    "size": 20,
    "total": 8
  }
}
```

---

## 💻 Frontend Integration Examples

### React Hook Example

```javascript
import { useEffect, useState } from 'react';

const AdminNotificationPanel = () => {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const token = localStorage.getItem('accessToken');

  // Lấy danh sách notification
  const fetchNotifications = async () => {
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/admin/notifications?page=0&size=20',
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );
      const result = await response.json();
      setNotifications(result.data.notifications);
      setUnreadCount(result.data.unreadCount);
    } catch (error) {
      console.error('Error fetching notifications:', error);
    }
  };

  // Đánh dấu đã đọc
  const markAsRead = async (notificationId) => {
    try {
      await fetch(
        `http://localhost:8080/api/v1/admin/notifications/${notificationId}/read`,
        {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );
      fetchNotifications();
    } catch (error) {
      console.error('Error marking as read:', error);
    }
  };

  // Đánh dấu tất cả đã đọc
  const markAllAsRead = async () => {
    try {
      await fetch(
        'http://localhost:8080/api/v1/admin/notifications/mark-all-read',
        {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );
      fetchNotifications();
    } catch (error) {
      console.error('Error marking all as read:', error);
    }
  };

  // Lấy notification theo type
  const fetchByType = async (type) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/v1/admin/notifications/by-type/${type}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );
      const result = await response.json();
      setNotifications(result.data.notifications);
    } catch (error) {
      console.error('Error fetching notifications by type:', error);
    }
  };

  useEffect(() => {
    fetchNotifications();
    // Polling mỗi 30 giây
    const interval = setInterval(fetchNotifications, 30000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="notification-panel">
      <h2>Admin Notifications ({unreadCount} unread)</h2>
      <button onClick={markAllAsRead}>Mark all as read</button>
      
      <div className="filters">
        <button onClick={() => fetchByType('STORE_APPROVAL')}>Store Approvals</button>
        <button onClick={() => fetchByType('PRODUCT_APPROVAL')}>Product Approvals</button>
        <button onClick={() => fetchByType('WITHDRAWAL_REQUEST')}>Withdrawal Requests</button>
      </div>

      <div className="notifications-list">
        {notifications.map(notification => (
          <div key={notification.id} className={`notification-item ${notification.isRead ? 'read' : 'unread'}`}>
            <h3>{notification.title}</h3>
            <p>{notification.message}</p>
            <small>{new Date(notification.createdAt).toLocaleString()}</small>
            <button onClick={() => markAsRead(notification.id)}>
              {notification.isRead ? 'Marked as read' : 'Mark as read'}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default AdminNotificationPanel;
```

---

## 🚀 Sử Dụng trong Backend

### Tạo notification cho admin từ các service khác

```java
@Service
@RequiredArgsConstructor
public class StoreService {
    private final INotificationService notificationService;

    public Store createStore(StoreRequest request) {
        Store store = new Store();
        // ... Khởi tạo store
        
        // Tạo notification cho admin về store mới
        try {
            notificationService.createAdminNotification(
                "Cửa hàng mới đăng ký: " + store.getName(),
                "Cửa hàng " + store.getName() + " tại địa chỉ " + store.getAddress() + " chờ phê duyệt",
                "STORE_APPROVAL",
                store.getId()
            );
        } catch (Exception e) {
            log.error("Error creating admin notification: {}", e.getMessage());
        }
        
        return storeRepository.save(store);
    }
}

@Service
@RequiredArgsConstructor
public class ProductService {
    private final INotificationService notificationService;

    public Product createProduct(ProductRequest request) {
        Product product = new Product();
        // ... Khởi tạo product
        
        // Tạo notification cho admin
        try {
            notificationService.createAdminNotification(
                "Sản phẩm mới chờ phê duyệt: " + product.getName(),
                "Sản phẩm " + product.getName() + " từ cửa hàng " + store.getName() + " chờ phê duyệt",
                "PRODUCT_APPROVAL",
                product.getId()
            );
        } catch (Exception e) {
            log.error("Error creating admin notification: {}", e.getMessage());
        }
        
        return productRepository.save(product);
    }
}

@Service
@RequiredArgsConstructor
public class WithdrawalService {
    private final INotificationService notificationService;

    public WithdrawalRequest createWithdrawalRequest(WithdrawalRequest request) {
        // ... Khởi tạo withdrawal request
        
        // Tạo notification cho admin
        try {
            notificationService.createAdminNotification(
                "Yêu cầu rút tiền từ " + store.getName(),
                "Store " + store.getName() + " yêu cầu rút " + request.getAmount() + " VNĐ",
                "WITHDRAWAL_REQUEST",
                request.getId()
            );
        } catch (Exception e) {
            log.error("Error creating admin notification: {}", e.getMessage());
        }
        
        return withdrawalRepository.save(request);
    }
}
```

---

## 📊 Notification Types Reference

| Type | Mô tả | Ví dụ |
|------|-------|-------|
| `STORE_APPROVAL` | Yêu cầu phê duyệt cửa hàng | Cửa hàng mới đăng ký |
| `PRODUCT_APPROVAL` | Yêu cầu phê duyệt sản phẩm | Sản phẩm mới, biến thể mới |
| `WITHDRAWAL_REQUEST` | Yêu cầu rút tiền từ seller | Seller yêu cầu rút tiền |
| `SYSTEM` | Thông báo hệ thống | Bảo trì, cập nhật, lỗi |

---

## 🔄 Workflow: Tạo Notification khi có Events

### Event: Store mới được tạo
```
User tạo Store
    ↓
StoreService.createStore()
    ↓
createAdminNotification("Cửa hàng mới...", "STORE_APPROVAL", storeId)
    ↓
Notification lưu vào DB
    ↓
Admin nhất thì thấy notification trong dashboard
    ↓
Admin click vào phê duyệt store
```

### Event: Product được tạo
```
Seller tạo Product
    ↓
ProductService.createProduct()
    ↓
createAdminNotification("Sản phẩm mới...", "PRODUCT_APPROVAL", productId)
    ↓
Admin phê duyệt
```

---

## ⚠️ Important Notes

1. **Admin Notifications không liên kết với User cụ thể** - Chúng được hiển thị cho tất cả admin
2. **Type field rất quan trọng** - Dùng để lọc và phân loại notifications (chỉ có 4 types: STORE_APPROVAL, PRODUCT_APPROVAL, WITHDRAWAL_REQUEST, SYSTEM)
3. **relatedId field** - Chứa ID của đối tượng liên quan, giúp admin có thể truy cập nhanh
4. **Polling vs WebSocket** - Hiện tại dùng polling, có thể upgrade sang WebSocket để real-time

---

## 🎯 Best Practices

1. **Luôn kèm relatedId** - Để admin có thể click vào notification và truy cập trực tiếp
2. **Message phải mô tả rõ ràng** - Admin cần biết chuyện gì xảy ra
3. **Đặt tên title ngắn gọn** - Tiêu đề dễ nhìn trong danh sách
4. **Clean up notifications cũ** - Xóa các notification quá 30 ngày để tiết kiệm space
5. **Batch create notifications** - Nếu có nhiều events, batch insert để tăng performance

---

**Last Updated**: November 25, 2025  
**Version**: 1.0.0
