# 📋 CHANGELOG - Cập nhật hệ thống quản lý ảnh và store

> **Phiên bản**: v2.0  
> **Ngày cập nhật**: 8 tháng 10, 2025  
> **Branch**: NgocHuy

## 🎯 **Tổng quan các thay đổi**

Phiên bản này tập trung vào việc nâng cấp hệ thống xử lý ảnh cho Product Variants và Store, từ việc chỉ hỗ trợ một ảnh sang hỗ trợ nhiều ảnh với khả năng quản lý tốt hơn.

---

## 🔥 **THAY ĐỔI CHÍNH**

### 1. **Hệ thống quản lý nhiều ảnh cho Product Variants**

#### 🆕 **Model & Database mới:**
- **ProductImage Model**: Quản lý nhiều ảnh cho mỗi Product Variant
- **ProductImageRepository**: Repository để quản lý các thao tác với ảnh
- **Bảng `product_images`**: Lưu trữ metadata của ảnh (path, type, isPrimary)

#### 🔄 **Cập nhật Model hiện có:**
- **ProductVariant**: Thêm quan hệ OneToMany với ProductImage
- **ProductImage**: Chuyển quan hệ từ Product sang ProductVariant

#### 📁 **Files mới/cập nhật:**
```
├── models/
│   ├── ProductImage.java (Cập nhật)
│   └── ProductVariant.java (Thêm quan hệ images)
├── repositories/
│   └── ProductImageRepository.java (MỚI)
├── services/
│   ├── FileUploadService.java (Thêm uploadFiles, deleteFiles)
│   └── productVariant/
│       ├── ProductVariantSerivce.java (Xử lý nhiều ảnh)
│       └── IProductVariantService.java (Thêm getById, updateWithImages)
├── controllers/
│   └── ProductVariantController.java (Endpoints mới)
├── responses/
│   └── ProductVariantResponse.java (Thêm imageUrls, primaryImageUrl)
└── create_product_images_table.sql (Script migration)
```

---

### 2. **Nâng cấp hệ thống Store với upload Logo**

#### 🆕 **Tính năng mới:**
- Upload logo khi tạo store mới
- Update store với logo và banner
- Tự động xóa file cũ khi upload file mới

#### 📁 **Files cập nhật:**
```
├── services/store/
│   ├── StoreService.java (Thêm FileUploadService, xử lý upload)
│   └── IStoreService.java (Thêm updateStoreWithMedia)
└── controllers/b2c/store/
    └── StoreController.java (Endpoint update with media)
```

---

## 🚀 **API ENDPOINTS MỚI**

### **Product Variants:**

#### 1. Lấy Product Variant theo ID
```http
GET /api/product-variants/{id}
```
**Response:**
```json
{
  "status": "success",
  "data": {
    "id": "uuid",
    "name": "Product Name",
    "imageUrls": ["/image/product-variants/img1.jpg", "/image/product-variants/img2.jpg"],
    "primaryImageUrl": "/image/product-variants/img1.jpg",
    "price": 1000000,
    "stock": 20,
    "attributes": {...}
  }
}
```

#### 2. Tạo Product Variant với nhiều ảnh
```http
POST /api/product-variants/create
Content-Type: multipart/form-data

- dto: ProductVariantDTO
- images: List<MultipartFile> (nhiều ảnh)
```

#### 3. Update Product Variant với nhiều ảnh
```http
PUT /api/product-variants/update-with-images/{id}
Content-Type: multipart/form-data

- dto: ProductVariantDTO  
- images: List<MultipartFile>
```

### **Store Management:**

#### 4. Update Store với Logo và Banner
```http
PUT /api/b2c/stores/{storeId}/with-media
Content-Type: multipart/form-data

- storeDTO: StoreDTO
- logo: MultipartFile (optional)
- banner: MultipartFile (optional)
```

---

## 🛠 **CÁCH SỬ DỤNG**

### **1. Migration Database:**
```sql
-- Chạy script tạo bảng product_images
SOURCE create_product_images_table.sql;
```

### **2. Upload nhiều ảnh cho Product Variant:**
```javascript
const formData = new FormData();
formData.append('dto', JSON.stringify(productVariantData));
formData.append('images', file1);
formData.append('images', file2);
formData.append('images', file3);

fetch('/api/product-variants/create', {
    method: 'POST',
    body: formData
});
```

### **3. Upload logo cho Store:**
```javascript
const formData = new FormData();
formData.append('storeDTO', JSON.stringify(storeData));
formData.append('logo', logoFile);

fetch('/api/b2c/stores/create', {
    method: 'POST',
    body: formData
});
```

---

## 📊 **RESPONSE FORMAT MỚI**

### **ProductVariantResponse:**
```json
{
  "id": "variant-uuid",
  "name": "Product Variant Name",
  "imageUrls": [
    "/image/product-variants/uuid1_image1.jpg",
    "/image/product-variants/uuid2_image2.jpg"
  ],
  "primaryImageUrl": "/image/product-variants/uuid1_image1.jpg",
  "price": 25000000,
  "description": "Mô tả sản phẩm",
  "stock": 10,
  "attributes": {
    "CPU": "Intel Core i5",
    "RAM": "16GB",
    "GPU": "RTX 3050"
  }
}
```

### **OrderResponse (Đã sửa):**
```json
{
  "orderItems": [{
    "productImage": "/image/product-variants/primary-image.jpg",  // Lấy ảnh primary
    "productName": "Laptop Gaming",
    "quantity": 1,
    "price": 25000000
  }]
}
```

---

## 🔧 **CÁC TÍNH NĂNG CHI TIẾT**

### **1. Quản lý ảnh thông minh:**
- ✅ **Ảnh chính**: Ảnh đầu tiên tự động được đánh dấu `isPrimary = true`
- ✅ **Fallback**: Nếu không có ảnh primary, lấy ảnh đầu tiên
- ✅ **Null-safe**: Xử lý trường hợp không có ảnh
- ✅ **Auto-cleanup**: Tự động xóa ảnh cũ khi cập nhật

### **2. File Upload nâng cao:**
- ✅ **Batch upload**: Upload nhiều file cùng lúc
- ✅ **Category folder**: Tự động phân loại theo thư mục (product-variants, stores)
- ✅ **File validation**: Kiểm tra định dạng và kích thước
- ✅ **Error handling**: Xử lý lỗi upload gracefully

### **3. Tương thích ngược:**
- ✅ **Legacy API**: Các endpoint cũ vẫn hoạt động bình thường
- ✅ **Migration safe**: Dữ liệu cũ được migrate an toàn
- ✅ **Gradual upgrade**: Có thể nâng cấp từng phần

---

## 📁 **CẤU TRÚC FOLDER LÀM VIỆC**

```
uploads/
├── product-variants/          # Ảnh của Product Variants
│   ├── uuid1_laptop1.jpg     # Ảnh sản phẩm
│   ├── uuid2_laptop2.jpg
│   └── uuid3_laptop3.jpg
├── stores/                    # Logo và banner của Store
│   ├── uuid4_logo.png        # Logo cửa hàng  
│   └── uuid5_banner.jpg      # Banner cửa hàng
└── general/                   # Ảnh khác
    └── ...
```

---

## ⚠️ **LƯU Ý QUAN TRỌNG**

### **1. Database Migration:**
- **SQL**: Có thay đổi trong cấu trúc DB, hãy cập nhật DB mới nhật

### **2. File Storage:**
- **Dung lượng**: Cần đảm bảo đủ không gian lưu trữ
- **Backup**: Thiết lập backup cho thư mục uploads
- **Permissions**: Đảm bảo quyền ghi cho thư mục uploads

### **3. Performance:**
- **Lazy loading**: Ảnh được load lazy để tối ưu performance
- **CDN**: Khuyến nghị sử dụng CDN cho production
- **Image optimization**: Cân nhắc compress ảnh trước khi upload

---

## 🐛 **FIXES & IMPROVEMENTS**

### **Bug Fixes:**
- ✅ **OrderResponse**: Sửa lỗi lấy ảnh primary thay vì ảnh đầu tiên
- ✅ **File cleanup**: Sửa memory leak khi xóa file
- ✅ **Null pointer**: Xử lý trường hợp product variant không có ảnh

### **Performance Improvements:**
- ✅ **Batch processing**: Upload nhiều ảnh hiệu quả hơn
- ✅ **Database indexing**: Thêm index cho bảng product_images
- ✅ **Response optimization**: Giảm dung lượng response JSON

---

## 🔄 **MIGRATION GUIDE**

### **Từ phiên bản cũ lên v2.0:**

1. **Backup dữ liệu:**
```bash
mysqldump -u username -p database_name > backup.sql
```

2. **Chạy migration script:**
```sql
SOURCE create_product_images_table.sql;
```

3. **Update code:**
```bash
git pull origin NgocHuy
mvn clean install
```

4. **Restart application:**
```bash
mvn spring-boot:run
```

5. **Verify migration:**
- Test API endpoints mới
- Kiểm tra upload ảnh
- Verify dữ liệu cũ

---

## 📞 **SUPPORT & CONTACT**

- **Developer**: NgocHuy
- **Repository**: [E-Commerce](https://github.com/ngochuytech/E-Commerce)
- **Branch**: NgocHuy
- **Issues**: Tạo issue trên GitHub repository

---

## 🎉 **CREDITS**

Phiên bản này được phát triển với mục tiêu nâng cao trải nghiệm người dùng và khả năng quản lý sản phẩm của hệ thống E-Commerce. Cảm ơn tất cả những đóng góp và feedback từ team!

---

*Last updated: October 8, 2025*