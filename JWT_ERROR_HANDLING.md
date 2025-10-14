# JWT Error Handling Documentation

## Mô tả
Hệ thống JWT authentication đã được cải tiến để xử lý và trả về các lỗi rõ ràng thay vì lỗi 500 chung chung.

## Các loại lỗi JWT được xử lý

### 1. Token không hợp lệ (Invalid Token)
**Khi nào xảy ra:** Token bị sai format, chữ ký không đúng, hoặc token giả mạo

**Response:**
```json
{
    "success": false,
    "data": null,
    "error": "JWT token không hợp lệ"
}
```
**HTTP Status:** 401 Unauthorized

### 2. Token đã hết hạn (Expired Token)
**Khi nào xảy ra:** Token đã qua thời gian expiration

**Response:**
```json
{
    "success": false,
    "data": null,
    "error": "JWT token đã hết hạn"
}
```
**HTTP Status:** 401 Unauthorized

### 3. Token bị thu hồi (Revoked Token)
**Khi nào xảy ra:** Token đã bị revoke trong database hoặc user bị deactivate

**Response:**
```json
{
    "success": false,
    "data": null,
    "error": "JWT token không hợp lệ"
}
```
**HTTP Status:** 401 Unauthorized

### 4. Token không tồn tại hoặc sai format
**Khi nào xảy ra:** Header Authorization thiếu hoặc không có Bearer prefix

**Response:** Request sẽ được xử lý như user chưa đăng nhập

### 5. Lỗi xử lý JWT khác
**Khi nào xảy ra:** Các lỗi khác trong quá trình parse hoặc validate token

**Response:**
```json
{
    "success": false,
    "data": null,
    "error": "Lỗi khi xử lý JWT token"
}
```
**HTTP Status:** 401 Unauthorized

## Cách test các lỗi

### Test Token không hợp lệ:
```bash
curl -H "Authorization: Bearer invalid_token_here" \
     http://localhost:8080/api/reviews/my-reviews
```

### Test Token hết hạn:
```bash
# Sử dụng token đã hết hạn
curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjk5MDE2NDAwLCJleHAiOjE2OTkwMTY0NjB9.expired_signature" \
     http://localhost:8080/api/reviews/my-reviews
```

### Test Token sai chữ ký:
```bash
curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjk5MDE2NDAwLCJleHAiOjE2OTkwMTY0NjB9.wrong_signature" \
     http://localhost:8080/api/reviews/my-reviews
```

## Luồng xử lý lỗi

### 1. JwtTokenProvider
- Các method `getUsername()`, `validateToken()`, `extractAllClaims()` được wrap trong try-catch
- Ném `JwtAuthenticationException` với message rõ ràng khi có lỗi JWT

### 2. JwtAuthFilter
- Bắt `JwtAuthenticationException` và các exception khác
- Trả về response JSON với status 401 thay vì để exception bubble up
- Sử dụng `handleJwtException()` để format response

### 3. GlobalExceptionHandler
- Handle `JwtAuthenticationException` globally
- Handle các authentication exceptions khác
- Đảm bảo tất cả auth errors đều trả về format JSON nhất quán

## Cải tiến so với trước

### Trước:
- Token sai → 500 Internal Server Error
- Message lỗi không rõ ràng
- Client không biết lý do cụ thể

### Sau:
- Token sai → 401 Unauthorized với message cụ thể
- JSON response nhất quán
- Client có thể xử lý từng loại lỗi khác nhau

## Frontend Error Handling

### JavaScript Example:
```javascript
const makeAuthenticatedRequest = async (url, options = {}) => {
    try {
        const response = await fetch(url, {
            ...options,
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'Content-Type': 'application/json',
                ...options.headers
            }
        });
        
        const data = await response.json();
        
        if (response.status === 401) {
            // Handle authentication errors
            if (data.error.includes('hết hạn')) {
                // Token expired - redirect to login
                localStorage.removeItem('token');
                window.location.href = '/login';
            } else if (data.error.includes('không hợp lệ')) {
                // Invalid token - redirect to login  
                localStorage.removeItem('token');
                window.location.href = '/login';
            }
            throw new Error(data.error);
        }
        
        return data;
    } catch (error) {
        console.error('API Error:', error.message);
        throw error;
    }
};
```

### React Hook Example:
```javascript
const useAuthenticatedFetch = () => {
    const navigate = useNavigate();
    
    const authFetch = useCallback(async (url, options = {}) => {
        try {
            const response = await fetch(url, {
                ...options,
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    'Content-Type': 'application/json',
                    ...options.headers
                }
            });
            
            if (response.status === 401) {
                const data = await response.json();
                
                // Clear token and redirect to login
                localStorage.removeItem('token');
                navigate('/login');
                
                throw new Error(data.error);
            }
            
            return await response.json();
        } catch (error) {
            throw error;
        }
    }, [navigate]);
    
    return authFetch;
};
```

## Security Notes

1. **Không expose secret key** trong error messages
2. **Rate limiting** nên được áp dụng cho authentication endpoints
3. **Logging** các authentication failures để phát hiện tấn công
4. **Token blacklist** để handle token revocation hiệu quả
5. **HTTPS only** để bảo vệ token trong transmission

## Best Practices

1. **Consistent Error Format:** Tất cả lỗi auth đều trả về cùng format
2. **Clear Messages:** Message lỗi rõ ràng nhưng không quá chi tiết về security
3. **Proper HTTP Status:** Sử dụng status codes chuẩn (401, 403)
4. **Client-friendly:** Response format dễ xử lý ở frontend
5. **Logging:** Log security events cho monitoring và audit