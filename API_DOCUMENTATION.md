# E-Commerce TechShop API Documentation

## Base URL
```
http://localhost:8088/api/v1
```

## Authentication
- **JWT Token**: Required for protected endpoints
- **Header**: `Authorization: Bearer <token>`
- **Token expiration**: 30 days
- **Refresh token**: Available for token renewal

---

## 1. Authentication & User Management

### 1.1 User Authentication
**Base Path**: `/api/v1/users`

#### POST `/login`
Login with email/password
- **Request Body**:
  ```json
  {
    "email": "string",
    "password": "string"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "data": {
      "message": "Đăng nhập thành công",
      "token": "access_token",
      "refreshToken": "refresh_token",
      "username": "Full Name",
      "id": "user_id",
      "roles": ["USER", "SELLER", "STORE", "SHIPPER", "ADMIN"]
    }
  }
  ```

#### POST `/register`
Register new user account
- **Request Body**:
  ```json
  {
    "email": "string",
    "full_name": "string",
    "password": "string",
    "retype_password": "string",
    "role_id": 1 // Mặc định là 1 (User)
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Đã đăng ký thành công! Cần xác minh email"
  }
  ```

#### GET `/verify`
Verify email with verification code
- **Query Parameters**: `code` (string)
- **Response**:
  ```json
  {
    "success": true,
    "message": "Đã xác minh tài khoản thành công!"
  }
  ```

#### POST `/auth/social/callback`
Login with Google OAuth
- **Request Body**:
  ```json
  {
    "code": "google_auth_code"
  }
  ```

### 1.2 Password Reset
**Base Path**: `/api/v1`

#### POST `/forgot-password`
Request password reset
- **Request Body**:
  ```json
  {
    "email": "user@example.com"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Đã gửi mã xác nhận tói email của bạn"
  }
  ```

#### POST `/reset-password`
Reset password with token
- **Request Body**:
  ```json
  {
    "token": "reset_token",
    "password": "new_password"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Đã đổi mật khẩu thành công"
  }
  ```


---

#### GET `/current`
Lấy user hiện tại (Cần token)
- **Response**:
  ```json
  {
    "success": true,
    "data": {
        "id": "id_user",
        "email": "email",
        "phone": "phone",
        "fullName": "Full name",
        "dateOfBirth": "dateOfBirth",
        "avatar": "avatar_url",
        "address": {
            "id": "id_address",
            "province": "province",
            "district": "district",
            "ward": "ward",
            "homeAddress": "homeAddress",
            "suggestedName": "suggestedName"
        }
    }
  }
  ```


---


## 2. Product Management

### 2.1 Products
**Base Path**: `/api/v1/products`

#### POST `/create`
Create new product (Auth required)
- **Request Body**:
  ```json
  {
    "name": "Product Name",
    "description": "Product Description",
    "category": "category_name",
    "brand_id": "brand_id", // Có id trong bảng Brand
    "store_id": "store_id", // Có id trong bảng store
    "product_condition": "[NEW, LIKENEW]",
    "status": "[ACTIVE, HIDDEN, SOLD]"
  }
  ```

#### GET `/{id}`
Get product by ID
- **Path Parameters**: `id` (string)
- **Response**:
  ```json
  {
    "success": true,
    "data": {
      "id": "product_id",
      "name": "Product Name",
      "description": "Description",
      "category": "Category",
      "brand": "Brand",
      "store": {
            "id": "store_id",
            "name": "store_name",
            "logo": "logo_url"
        }
    }
  }
  ```

#### GET ``
Get all products with filters
- **Query Parameters**:
  - `keyword` (string) - Search keyword
  - `categoryId` (string) - Filter by category
  - `brandId` (string) - Filter by brand
  - `storeId` (string) - Filter by store
  - `page` (int, default: 0) - Page number
  - `limit` (int, default: 10) - Items per page

### 2.2 Product Variants
**Base Path**: `/api/v1/product-variants`

#### POST `/create` (Form Data)
Create product variant with multiple images
- **Content-Type**: `multipart/form-data`
- **Form Fields**:
  - `dto` (JSON): ProductVariantDTO object
    ```json
    {
      "name": "Variant Name",
      "price": 100000,
      "description": "Variant Description",
      "stock": 50,
      "attributes": {"color": "red", "size": "M"},
      "product_id": "product_uuid"
    }
    ```
  - `images` (file[], optional): Multiple image files

#### GET `/{id}`
Get product variant by ID
- **Response**: ProductVariantResponse with imageUrls and primaryImageUrl

#### PUT `/update/{id}` (Form Data)
Update variant with single image
- **Content-Type**: `multipart/form-data`
- **Form Fields**:
  - `dto` (JSON): ProductVariantDTO object
  - `image` (file, optional): Single image file

#### PUT `/update-with-images/{id}` (Form Data)
Update variant with multiple images
- **Content-Type**: `multipart/form-data`
- **Form Fields**:
  - `dto` (JSON): ProductVariantDTO object
  - `images` (file[], optional): Multiple image files

#### DELETE `/delete/{id}`
Delete product variant (soft delete)

#### GET `/product/{productId}`
Get variants by product ID
- **Path Parameters**: `productId` (string)

#### GET `/category/{category}`
Get variants by category
- **Path Parameters**: `category` (string)

#### GET `/category/{category}/brand/{brand}`
Get variants by category and brand
- **Path Parameters**: 
  - `category` (string)
  - `brand` (string)

#### POST `/filter`
Filter products with advanced criteria
- **Request Body**: ProductFilterDTO object

#### GET `/latest`
Get latest product variants with pagination
- **Query Parameters**:
  - `page` (int, default: 0) - Page number (0-based)
  - `size` (int, default: 10) - Items per page
  - `sortBy` (string, default: "createdAt") - Sort field
  - `sortDir` (string, default: "desc") - Sort direction (asc/desc)
- **Response**: `Page<ProductVariantResponse>` object

#### GET `/store/{storeId}`
Get product variants by store with pagination
- **Path Parameters**: `storeId` (string)
- **Query Parameters**:
  - `page` (int, default: 0) - Page number (0-based)
  - `size` (int, default: 10) - Items per page
  - `sortBy` (string, default: "createdAt") - Sort field
  - `sortDir` (string, default: "desc") - Sort direction (asc/desc)
- **Response**: `Page<ProductVariantResponse>` object

---

## 3. Store Management

### 3.1 Store Operations
**Base Path**: `/api/v1/b2c/stores`

#### POST `/create`
Create new store
- **Request Body**:
  ```json
  {
    "name": "Store Name",
    "description": "Store Description",
    "address": "Store Address",
    "phoneNumber": "Phone",
    "email": "store@example.com",
    "ownerId": "owner_user_id"
  }
  ```

#### PUT `/{storeId}/with-media` (Form Data)
Update store with logo/banner
- **Content-Type**: `multipart/form-data`
- **Form Fields**:
  - `name` (string)
  - `description` (string)
  - `address` (string)
  - `phoneNumber` (string)
  - `email` (string)
  - `logo` (file, optional)
  - `banner` (file, optional)

#### GET `/{storeId}`
Get store details

#### GET ``
Get all stores

#### GET `/owner/{ownerId}`
Get stores by owner

#### PUT `/{storeId}/approve`
Approve store (Admin only)

#### PUT `/{storeId}/reject`
Reject store (Admin only)

#### GET `/pending`
Get pending stores (Admin only)

#### GET `/approved`
Get approved stores

#### PUT `/{storeId}/status`
Update store status

#### DELETE `/{storeId}`
Delete store

#### POST `/{storeId}/logo` (Form Data)
Upload store logo
- **Form Field**: `logo` (file)

#### POST `/{storeId}/banner` (Form Data)
Upload store banner
- **Form Field**: `banner` (file)

---

## 4. Shopping Cart & Orders

### 4.1 Cart Management
**Base Path**: `/api/v1/buyer/cart` (Auth required)

#### POST `/add`
Add item to cart
- **Request Body**:
  ```json
  {
    "productVariantId": "variant_id",
    "quantity": 2
  }
  ```

#### GET ``
Get user's cart items

#### PUT `/{cartItemId}`
Update cart item quantity
- **Request Body**:
  ```json
  {
    "quantity": 3
  }
  ```

#### DELETE `/{cartItemId}`
Remove item from cart

#### DELETE `/clear`
Clear entire cart

#### GET `/count`
Get cart items count

### 4.2 Buyer Orders
**Base Path**: `/api/v1/buyer/orders` (Auth required)

#### POST `/checkout`
Create order from cart
- **Request Body**:
  ```json
  {
    "addressId": "address_id",
    "paymentMethod": "COD",
    "note": "Delivery note",
    "promotionId": "promotion_id (optional)"
  }
  ```

#### GET ``
Get user's orders
- **Query Parameters**:
  - `page` (int)
  - `limit` (int)
  - `status` (string)

#### GET `/{orderId}`
Get order details

#### PUT `/{orderId}/cancel`
Cancel order

### 4.3 Store Orders Management
**Base Path**: `/api/v1/b2c/orders` (Auth required)

#### GET `/store/{storeId}`
Get store orders

#### GET `/{orderId}`
Get order details

#### PUT `/{orderId}/status`
Update order status
- **Request Body**:
  ```json
  {
    "status": "CONFIRMED|SHIPPING|DELIVERED|CANCELLED"
  }
  ```

#### GET `/store/{storeId}/status/{status}`
Get orders by status

#### GET `/store/{storeId}/statistics`
Get order statistics

---

## 5. User Address Management

**Base Path**: `/api/v1/buyer/address` (Auth required)

#### GET ``
Get user addresses

#### POST ``
Create new address
- **Request Body**:
  ```json
  {
    "fullName": "Recipient Name",
    "phoneNumber": "Phone",
    "province": "Province",
    "district": "District",
    "ward": "Ward",
    "detailAddress": "Detail Address",
    "isDefault": false
  }
  ```

#### DELETE ``
Delete address
- **Query Parameter**: `addressId` (string)

#### GET `/check`
Check if user has addresses

---

## 6. Reviews & Ratings

**Base Path**: `/api/v1/b2c/reviews`

#### GET `/store/{storeId}`
Get store reviews

#### GET `/product/{productId}`
Get product reviews

#### GET `/variant/{variantId}`
Get variant reviews

#### GET `/{reviewId}`
Get review details

#### PUT `/{reviewId}/respond`
Store owner respond to review

#### GET `/store/{storeId}/statistics`
Get review statistics

#### GET `/store/{storeId}/rating/{rating}`
Get reviews by rating

---

## 7. Promotions & Discounts

**Base Path**: `/api/v1/b2c/promotions`

#### POST `/create`
Create promotion
- **Request Body**:
  ```json
  {
    "name": "Promotion Name",
    "description": "Description",
    "type": "PERCENTAGE|FIXED_AMOUNT",
    "value": 10.0,
    "startDate": "yyyy-MM-dd",
    "endDate": "yyyy-MM-dd",
    "minimumAmount": 100.0,
    "maxUsage": 100,
    "storeId": "store_id"
  }
  ```

#### GET `/active`
Get active promotions

#### GET `/store/{storeId}`
Get store promotions

#### POST `/{promotionId}/validate`
Validate promotion code

#### POST `/{promotionId}/calculate-discount`
Calculate discount amount

---

## 8. Analytics & Reports

**Base Path**: `/api/v1/b2c/analytics` (Auth required)

#### GET `/dashboard/{storeId}`
Get dashboard overview

#### GET `/revenue/{storeId}`
Get revenue analytics

#### GET `/orders/{storeId}`
Get order analytics

#### GET `/products/{storeId}/top`
Get top selling products

#### GET `/customers/{storeId}`
Get customer analytics

#### GET `/reviews/{storeId}`
Get review analytics

---

## 9. Customer Management

**Base Path**: `/api/v1/b2c/customers` (Auth required)

#### GET `/store/{storeId}`
Get store customers

#### GET `/{customerId}`
Get customer details

#### GET `/{customerId}/orders`
Get customer orders

#### GET `/store/{storeId}/top-spenders`
Get top spending customers

---

## Error Response Format

All error responses follow this format:
```json
{
  "success": false,
  "data": null,
  "message": "Error message"
}
```

## HTTP Status Codes

- **200 OK**: Success
- **400 Bad Request**: Validation error or business logic error
- **401 Unauthorized**: Authentication required
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error

## File Upload Constraints

- **Maximum file size**: 30MB
- **Supported image formats**: JPG, JPEG, PNG, GIF
- **Storage path**: `./uploads/images/`
- **Access URL**: `http://localhost:8088/image/{filename}`

## Authentication Requirements

### Public Endpoints:
- User registration/login
- Product browsing
- Store listing
- Password reset

### Buyer Authentication Required:
- Cart operations
- Order management
- Address management
- Profile management

### Store Owner Authentication Required:
- Store management
- Product/variant management
- Order fulfillment
- Analytics access

### Admin Authentication Required:
- Store approval/rejection
- System analytics
- User management

## Pagination

Most list endpoints support pagination:
- **Query Parameters**:
  - `page` (int, default: 0) - Page number (0-based)
  - `limit` (int, default: 10) - Items per page
  - `sort` (string, optional) - Sort field
  - `direction` (string, optional) - Sort direction (ASC/DESC)

## Search & Filtering

Product and store endpoints support:
- **Keyword search**: `keyword` parameter
- **Category filtering**: `categoryId` parameter
- **Brand filtering**: `brandId` parameter
- **Price range**: `minPrice`, `maxPrice` parameters
- **Status filtering**: `status` parameter

## Rate Limiting

- API calls are rate-limited to prevent abuse
- Default limits: 1000 requests per hour per IP
- Authenticated users: 5000 requests per hour

## API Versioning

Current version: `v1`
- All endpoints are prefixed with `/api/v1`
- Future versions will be accessible via `/api/v2`, etc.

---

*Last updated: December 2024*
*Version: 2.0*