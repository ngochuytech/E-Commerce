# Review API Documentation

## Mô tả
API này cung cấp các chức năng cơ bản cho hệ thống đánh giá sản phẩm, cho phép user đánh giá sản phẩm sau khi mua hàng.

## Base URL
```
/api/reviews
```

## Authentication
Tất cả các API đều yêu cầu JWT token trong header:
```
Authorization: Bearer <token>
```

## 1. Tạo Review Mới

### Endpoint
```
POST /api/reviews
```

### Request Body
```json
{
    "rating": 5,
    "comment": "Sản phẩm rất tốt, chất lượng cao!",
    "orderId": "order_id_here",
    "productVariantId": "product_variant_id_here"
}
```

### Validation Rules
- `rating`: Required, phải từ 1-5
- `comment`: Required, không được để trống
- `orderId`: Required, order phải thuộc về user hiện tại
- `productVariantId`: Required, product variant phải tồn tại

### Business Rules
- Chỉ có thể review order đã hoàn thành (status: DELIVERED hoặc COMPLETED)
- Mỗi user chỉ có thể review một lần cho mỗi product variant trong một order
- Phải là chủ sở hữu order

### Response
```json
{
    "success": true,
    "data": {
        "id": "review_id",
        "orderId": "order_id",
        "productVariantId": "product_variant_id",
        "userId": "user_id",
        "rating": 5,
        "comment": "Sản phẩm rất tốt, chất lượng cao!",
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-01T00:00:00"
    },
    "error": null
}
```

## 2. Lấy Reviews Theo Product

### Endpoint
```
GET /api/reviews/product/{productId}
```

### Query Parameters
- `page`: Số trang (default: 0)
- `size`: Kích thước trang (default: 10)
- `sortBy`: Trường sắp xếp (default: createdAt)
- `sortDir`: Hướng sắp xếp - asc/desc (default: desc)

### Example
```
GET /api/reviews/product/product_123?page=0&size=10&sortBy=rating&sortDir=desc
```

### Response
```json
{
    "success": true,
    "data": {
        "content": [
            {
                "id": "review_id",
                "rating": 5,
                "comment": "Sản phẩm tuyệt vời!",
                "createdAt": "2024-01-01T00:00:00"
            }
        ],
        "totalElements": 50,
        "totalPages": 5,
        "size": 10,
        "number": 0
    },
    "error": null
}
```

## 3. Lấy Reviews Theo Product Variant

### Endpoint
```
GET /api/reviews/product-variant/{productVariantId}
```

### Response
```json
{
    "success": true,
    "data": [
        {
            "id": "review_id",
            "rating": 4,
            "comment": "Màu sắc đẹp!",
            "createdAt": "2024-01-01T00:00:00"
        }
    ],
    "error": null
}
```

## 4. Lấy Reviews Của User Hiện Tại

### Endpoint
```
GET /api/reviews/my-reviews
```

### Response
```json
{
    "success": true,
    "data": [
        {
            "id": "review_id",
            "orderId": "order_id",
            "productVariantId": "product_variant_id",
            "rating": 5,
            "comment": "Rất hài lòng!",
            "createdAt": "2024-01-01T00:00:00"
        }
    ],
    "error": null
}
```

## 5. Cập Nhật Review

### Endpoint
```
PUT /api/reviews/{reviewId}
```

### Request Body
```json
{
    "rating": 4,
    "comment": "Sản phẩm tốt nhưng giao hàng chậm"
}
```

### Business Rules
- Chỉ có thể cập nhật review của chính mình
- Chỉ có thể cập nhật rating và comment

### Response
```json
{
    "success": true,
    "data": {
        "id": "review_id",
        "rating": 4,
        "comment": "Sản phẩm tốt nhưng giao hàng chậm",
        "updatedAt": "2024-01-01T01:00:00"
    },
    "error": null
}
```

## 6. Xóa Review

### Endpoint
```
DELETE /api/reviews/{reviewId}
```

### Business Rules
- Chỉ có thể xóa review của chính mình

### Response
```json
{
    "success": true,
    "data": "Review deleted successfully",
    "error": null
}
```

## 7. Lấy Thông Tin Chi Tiết Review

### Endpoint
```
GET /api/reviews/{reviewId}
```

### Response
```json
{
    "success": true,
    "data": {
        "id": "review_id",
        "orderId": "order_id",
        "productVariantId": "product_variant_id",
        "userId": "user_id",
        "rating": 5,
        "comment": "Sản phẩm xuất sắc!",
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-01T00:00:00"
    },
    "error": null
}
```

## 8. Lấy Thống Kê Rating Sản Phẩm

### Endpoint
```
GET /api/reviews/product-variant/{productVariantId}/stats
```

### Response
```json
{
    "success": true,
    "data": {
        "totalReviews": 100,
        "averageRating": 4.5,
        "ratingDistribution": {
            "5": 60,
            "4": 25,
            "3": 10,
            "2": 3,
            "1": 2
        }
    },
    "error": null
}
```

## Error Handling

### Common Error Responses

#### 400 Bad Request
```json
{
    "success": false,
    "data": null,
    "error": "You can only review your own orders"
}
```

#### 401 Unauthorized
```json
{
    "success": false,
    "data": null,
    "error": "JWT token is required"
}
```

#### 404 Not Found
```json
{
    "success": false,
    "data": null,
    "error": "Review not found"
}
```

#### 422 Validation Error
```json
{
    "success": false,
    "data": null,
    "error": "Rating must be between 1 and 5"
}
```

## Usage Examples

### Frontend Integration

#### Tạo review sau khi mua hàng
```javascript
const createReview = async (reviewData) => {
    try {
        const response = await fetch('/api/reviews', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(reviewData)
        });
        
        const result = await response.json();
        if (result.success) {
            console.log('Review created:', result.data);
        } else {
            console.error('Error:', result.error);
        }
    } catch (error) {
        console.error('Network error:', error);
    }
};
```

#### Hiển thị reviews cho sản phẩm
```javascript
const loadProductReviews = async (productId, page = 0) => {
    try {
        const response = await fetch(
            `/api/reviews/product/${productId}?page=${page}&size=5&sortBy=createdAt&sortDir=desc`
        );
        
        const result = await response.json();
        if (result.success) {
            displayReviews(result.data.content);
            setupPagination(result.data);
        }
    } catch (error) {
        console.error('Error loading reviews:', error);
    }
};
```

## Notes

1. **Security**: Tất cả API đều có kiểm tra quyền truy cập
2. **Validation**: Input được validate ở cả frontend và backend
3. **Business Logic**: Tuân thủ các quy tắc nghiệp vụ như chỉ review order đã hoàn thành
4. **Performance**: API có hỗ trợ pagination cho hiệu suất tốt
5. **Extensibility**: Dễ dàng mở rộng thêm tính năng như seller response, review images, etc.