# E-Commerce TechShop - Changelog

## 🔄 Recent Updates (October 2025)

### 📝 Tổng quan thay đổi

Phiên bản này tập trung vào **hệ thống phê duyệt nội dung** và **cải thiện trải nghiệm lỗi** cho nền tảng B2C e-commerce.

---

## ✨ 1. Store Approval System

### **Tính năng mới:**
- ✅ Thêm workflow phê duyệt cửa hàng với 4 trạng thái: `PENDING`, `APPROVED`, `REJECTED`, `DELETED`
- ✅ Shop chỉ được cập nhật thông tin sau khi được admin duyệt
- ✅ Validation upload logo/banner (chỉ khi store APPROVED)
- ✅ Tự động xóa file cũ khi upload media mới

### **Quy trình:**
```
User đăng ký Store → PENDING
    ↓
Admin xét duyệt → APPROVED ✅ (hoạt động bình thường)
               → REJECTED ❌ (không cho phép hoạt động)
               → DELETED 🗑️ (xóa mềm)
```

### **Files thay đổi:**
```
✏️ models/Store.java
   - Thêm enum StoreStatus {PENDING, APPROVED, REJECTED, DELETED}
   - Thêm method isValidStatus() để validate

✏️ services/store/StoreService.java
   + updateStoreLogo() - Upload logo (chỉ khi APPROVED)
   + updateStoreBanner() - Upload banner (chỉ khi APPROVED)
   - Auto delete old files trước khi upload mới

✏️ controllers/b2c/B2CStoreController.java
   - PUT /api/v1/b2c/stores/{id}/logo
   - PUT /api/v1/b2c/stores/{id}/banner
   - Validation: Chỉ cho phép update khi store APPROVED

✏️ controllers/admin/AdminStoreController.java (NEW)
   + GET /api/v1/admin/stores/pending
   + PUT /api/v1/admin/stores/{id}/approve
   + PUT /api/v1/admin/stores/{id}/reject
```

### **Lợi ích:**
- 🛡️ Kiểm soát chất lượng cửa hàng trước khi công khai
- ♻️ Tiết kiệm storage bằng cách xóa file cũ tự động
- 🔒 Bảo mật: Chỉ store được duyệt mới hoạt động được

---

## 📦 2. Product & ProductVariant Approval System

### **Tính năng mới:**
- ✅ Hệ thống duyệt 2 cấp: **Product** → **ProductVariant**
- ✅ Shop tạo sản phẩm → Admin duyệt → Shop tạo biến thể → Admin duyệt lại
- ✅ Shop chỉ được update **giá/tồn kho** của variant đã APPROVED
- ✅ Người dùng chỉ thấy sản phẩm đã được duyệt

### **Quy trình:**
```
Shop tạo Product → PENDING
    ↓
Admin duyệt Product → APPROVED
    ↓
Shop tạo ProductVariant → PENDING
    ↓
Admin duyệt Variant → APPROVED ✅ (hiển thị công khai)
    ↓
Shop update giá/stock → Không cần duyệt lại (linh hoạt)
```

### **Files thay đổi:**
```
✏️ models/Product.java
   - Thêm field: status (PENDING/APPROVED/REJECTED)
   - Thêm field: rejectionReason (lý do từ chối)
   - Thêm enum ProductStatus
   - Thêm method isValidStatus()

✏️ models/ProductVariant.java
   - Thêm field: status (PENDING/APPROVED/REJECTED)
   - Thêm field: rejectionReason
   - Thêm enum VariantStatus

✏️ services/product/ProductService.java
   + createProduct() - Tự động set status = PENDING
   + getPendingProducts() - Lấy danh sách chờ duyệt
   + updateStatus() - Admin duyệt/từ chối
   + rejectProduct(id, reason) - Từ chối với lý do

✏️ services/productVariant/ProductVariantService.java
   + createProductVariant() - Kiểm tra Product đã APPROVED chưa
   + updateProductVariant() - Chỉ update giá/stock nếu APPROVED
   + getVariantsByStatus() - Lấy theo trạng thái
   + updateVariantStatus()
   + rejectVariant(id, reason)

✏️ controllers/admin/AdminProductController.java (NEW)
   + GET /api/v1/admin/products/pending
   + PUT /api/v1/admin/products/{id}/approve
   + PUT /api/v1/admin/products/{id}/reject?reason=...

✏️ controllers/admin/AdminProductVariantController.java (NEW)
   + GET /api/v1/admin/product-variants/pending
   + PUT /api/v1/admin/product-variants/{id}/approve
   + PUT /api/v1/admin/product-variants/{id}/reject?reason=...

✏️ controllers/ProductController.java
   - Chỉ hiển thị Product có status = APPROVED
   
✏️ controllers/ProductVariantController.java
   - Chỉ hiển thị Variant có status = APPROVED
```

### **Lợi ích:**
- ✅ Kiểm soát chặt chẽ chất lượng sản phẩm
- 🔄 Shop linh hoạt điều chỉnh giá theo thị trường (không cần duyệt lại)
- 🛡️ Bảo mật: Sản phẩm chưa duyệt không public
- 🏪 Giống mô hình Shopee, Lazada (realistic)

---

## 🚨 3. Global Exception Handling Enhancement

### **Tính năng mới:**
- ✅ Centralized error handling cho toàn hệ thống
- ✅ Tự động phát hiện lỗi authentication (`currentUser` null)
- ✅ Xử lý lỗi nghiệp vụ (store/product chưa duyệt)
- ✅ Error messages thân thiện với người dùng

### **Cải thiện:**

#### **Trước:**
```json
// Response generic, khó hiểu
{
  "error": "Cannot invoke \"User.getEmail()\" because \"currentUser\" is null"
}
```

#### **Sau:**
```json
// Response rõ ràng, hướng dẫn user
{
  "success": false,
  "message": "Bạn cần đăng nhập để thực hiện chức năng này. Token không hợp lệ hoặc đã hết hạn.",
  "data": null
}
```

### **Files thay đổi:**
```
✏️ controllers/GlobalExceptionHandler.java
   + @ExceptionHandler(NullPointerException.class)
     - Tự động detect khi gọi currentUser.getEmail() mà user null
     - Check stackTrace để xác định method (getEmail, getId, getUsername)
     - Trả về 401 Unauthorized với message rõ ràng
   
   + @ExceptionHandler(IllegalStateException.class)
     - Xử lý lỗi nghiệp vụ:
       * Store chưa được duyệt
       * Product chưa được duyệt
       * Variant chưa được duyệt
     - Trả về 403 Forbidden
   
   + @ExceptionHandler(JwtAuthenticationException.class)
     - Token không hợp lệ / hết hạn
     - Trả về 401 Unauthorized
```

### **Lợi ích:**
- ✅ Không cần thêm `if (currentUser == null)` ở mỗi endpoint
- ✅ Code controller sạch hơn, tập trung vào logic nghiệp vụ
- ✅ Error handling nhất quán trong toàn hệ thống
- ✅ Developer-friendly và User-friendly

---

## 📸 4. File Upload Service Enhancement

### **Cải thiện:**
- ✅ Tự động xóa file cũ khi upload file mới (tránh rác Cloudinary)
- ✅ Validate store/product status trước khi cho phép upload
- ✅ Logging rõ ràng cho debugging

### **Files thay đổi:**
```
✏️ services/store/StoreService.java
   - updateStoreLogo(): Delete old logo trước khi upload
   - updateStoreBanner(): Delete old banner trước khi upload
   - Validate store phải APPROVED

✏️ services/FileUploadService.java
   - Improve error handling
   - Better logging
```

---

## 🔧 Technical Details

### **New Dependencies**
Không có dependency mới, chỉ sử dụng tốt hơn các thư viện có sẵn.

### **Database Changes**
```javascript
// MongoDB Collections Updated

// stores collection
{
  status: "PENDING" | "APPROVED" | "REJECTED" | "DELETED" // NEW field
}

// products collection
{
  status: "PENDING" | "APPROVED" | "REJECTED", // NEW field
  rejectionReason: String // NEW field (optional)
}

// product_variants collection
{
  status: "PENDING" | "APPROVED" | "REJECTED", // NEW field
  rejectionReason: String // NEW field (optional)
}
```

### **Migration Guide**
Nếu bạn có data cũ, chạy script sau để update:

```javascript
// MongoDB shell
db.stores.updateMany(
  { status: { $exists: false } },
  { $set: { status: "APPROVED" } }
);

db.products.updateMany(
  { status: { $exists: false } },
  { $set: { status: "APPROVED" } }
);

db.product_variants.updateMany(
  { status: { $exists: false } },
  { $set: { status: "APPROVED" } }
);
```

---

## 📊 API Changes Summary

### **New Admin Endpoints:**
```http
# Store Management
GET    /api/v1/admin/stores/pending
PUT    /api/v1/admin/stores/{id}/approve
PUT    /api/v1/admin/stores/{id}/reject?reason={text}

# Product Management
GET    /api/v1/admin/products/pending
PUT    /api/v1/admin/products/{id}/approve
PUT    /api/v1/admin/products/{id}/reject?reason={text}

# ProductVariant Management
GET    /api/v1/admin/product-variants/pending
PUT    /api/v1/admin/product-variants/{id}/approve
PUT    /api/v1/admin/product-variants/{id}/reject?reason={text}
```

### **Updated B2C Endpoints:**
```http
# Store logo/banner (chỉ khi APPROVED)
PUT    /api/v1/b2c/stores/{id}/logo
PUT    /api/v1/b2c/stores/{id}/banner

# Product creation (auto set PENDING)
POST   /api/v1/b2c/products/create

# Variant creation (auto set PENDING)
POST   /api/v1/b2c/product-variants/create

# Variant update (chỉ giá/stock, chỉ khi APPROVED)
PUT    /api/v1/b2c/product-variants/update/{id}
```

### **Updated Public Endpoints:**
```http
# Chỉ trả về sản phẩm APPROVED
GET    /api/v1/products
GET    /api/v1/products/{id}
GET    /api/v1/product-variants/{id}
```

---

## ⚠️ Breaking Changes

### **1. Store Model**
- Thêm field `status` (required)
- Existing stores cần được set status = "APPROVED" manually

### **2. Product Model**
- Thêm field `status` (required)
- Existing products cần được set status = "APPROVED"

### **3. ProductVariant Model**
- Thêm field `status` (required)
- Existing variants cần được set status = "APPROVED"

### **4. API Behavior Changes**
- `POST /api/v1/b2c/products/create` → Tạo với status = PENDING (thay vì APPROVED)
- `POST /api/v1/b2c/product-variants/create` → Tạo với status = PENDING
- `GET /api/v1/products` → Chỉ trả về products đã APPROVED
- `PUT /api/v1/b2c/stores/{id}/logo` → Chỉ cho phép khi store APPROVED

---

## 🧪 Testing Checklist

### **Store Approval Flow**
- [ ] User tạo store → status = PENDING
- [ ] Admin approve store → status = APPROVED
- [ ] Shop update store info → Success (khi APPROVED)
- [ ] Shop upload logo/banner → Success (khi APPROVED)
- [ ] Shop upload logo khi PENDING → Error 403

### **Product Approval Flow**
- [ ] Shop tạo product → status = PENDING
- [ ] Admin approve product → status = APPROVED
- [ ] Shop tạo variant (product chưa APPROVED) → Error
- [ ] Shop tạo variant (product đã APPROVED) → Success, variant PENDING
- [ ] Admin approve variant → status = APPROVED
- [ ] Public GET /products → Chỉ thấy APPROVED products
- [ ] Shop update giá variant → Success (khi APPROVED)

### **Exception Handling**
- [ ] Call API không có token → 401 "Bạn cần đăng nhập..."
- [ ] Call API với token hết hạn → 401 "Token không hợp lệ..."
- [ ] Upload logo khi store PENDING → 403 "Cửa hàng chưa được duyệt..."

---

## 📚 Additional Resources

- [CLOUDINARY_MIGRATION.md](CLOUDINARY_MIGRATION.md) - Hướng dẫn setup Cloudinary
- [JWT_ERROR_HANDLING.md](JWT_ERROR_HANDLING.md) - Chi tiết về JWT error handling
- [SWAGGER_SETUP_GUIDE.md](SWAGGER_SETUP_GUIDE.md) - API documentation

---

## 👨‍💻 Developer Notes

### **Code Organization**
- Controllers phân chia theo role: `admin/`, `b2c/`, `buyer/`
- Services tách riêng business logic
- GlobalExceptionHandler xử lý lỗi tập trung

### **Best Practices Applied**
- ✅ Single Responsibility Principle
- ✅ DRY (Don't Repeat Yourself) - GlobalExceptionHandler
- ✅ Security First - Validation ở nhiều layer
- ✅ User Experience - Error messages rõ ràng

---

## 🔜 Future Improvements

- [ ] Add notification system khi store/product được duyệt
- [ ] Add dashboard analytics cho admin
- [ ] Add bulk approval cho products
- [ ] Add comment/feedback khi reject
- [ ] Add versioning cho Product (khi cập nhật lớn)

---

**Last Updated:** October 20, 2025  
**Version:** 2.0  
**Author:** Ngoc Huy
