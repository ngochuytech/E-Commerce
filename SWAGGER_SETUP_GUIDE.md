# Swagger UI Setup Guide

## ✅ Cài đặt hoàn tất!

Swagger UI đã được cài đặt thành công cho dự án E-Commerce TechShop. Dưới đây là thông tin chi tiết:

---

## 🔗 Truy cập Swagger UI

### URLs để truy cập:

1. **Swagger UI Interface:**
   ```
   http://localhost:8080/swagger-ui.html
   ```

2. **OpenAPI JSON Specification:**
   ```
   http://localhost:8080/api-docs
   ```

3. **OpenAPI YAML Specification:**
   ```
   http://localhost:8080/api-docs.yaml
   ```

---

## 🛠️ Cài đặt đã thực hiện

### 1. Dependencies đã thêm vào `pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

### 2. Cấu hình trong `application.yml`:
```yaml
# Swagger/OpenAPI Configuration
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operationsSorter: method
    tagsSorter: alpha
    tryItOutEnabled: true
    filter: true
  show-actuator: false
  packages-to-scan: com.example.e_commerce_techshop.controllers
```

### 3. Security Configuration cập nhật:
- Đã thêm whitelist cho Swagger endpoints trong `WebSecurityConfig.java`
- Cho phép truy cập không cần authentication tới:
  - `/swagger-ui/**`
  - `/swagger-ui.html`
  - `/api-docs/**`
  - `/v3/api-docs/**`

### 4. Swagger Configuration (`SwaggerConfig.java`):
- Cấu hình thông tin API
- Setup JWT Bearer authentication
- Thêm server URLs

---

## 🎯 Cách sử dụng

### 1. Khởi động ứng dụng:
```bash
.\mvnw.cmd spring-boot:run
```

### 2. Truy cập Swagger UI:
- Mở browser và truy cập: `http://localhost:8080/swagger-ui.html`

### 3. Test APIs với Authentication:
1. **Đăng nhập để lấy JWT token:**
   - Sử dụng endpoint `POST /api/v1/users/login`
   - Copy JWT token từ response

2. **Authorize trong Swagger:**
   - Click nút "Authorize" trên góc phải
   - Nhập JWT token (không cần prefix "Bearer ")
   - Click "Authorize"

3. **Test APIs:**
   - Tất cả APIs sẽ tự động gửi kèm JWT token
   - Green lock icon = API cần authentication
   - Gray lock icon = API public

---

## 📋 Features

### ✅ Đã có:
- [x] Automatic API documentation generation
- [x] Interactive API testing interface
- [x] JWT Bearer authentication support
- [x] Request/Response examples
- [x] Parameter validation
- [x] Multiple response formats
- [x] Try it out functionality
- [x] API filtering and sorting

### 🎨 UI Features:
- **Operations Sorting:** By HTTP method
- **Tags Sorting:** Alphabetical
- **Filter:** Search/filter APIs
- **Try It Out:** Test APIs directly from UI
- **Authentication:** JWT Bearer token support

---

## 🔧 Customization

### Thêm API Documentation:

```java
@RestController
@RequestMapping("${api.prefix}/users")
@Tag(name = "User Management", description = "APIs for user operations")
public class UserController {

    @Operation(
        summary = "Register new user",
        description = "Create a new user account with email and password"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(
        @Valid @RequestBody 
        @Parameter(description = "User registration data") UserDTO userDTO
    ) {
        // Implementation
    }
}
```

### Thêm Security cho specific endpoints:
```java
@Operation(security = @SecurityRequirement(name = "Bearer Authentication"))
@GetMapping("/profile")
public ResponseEntity<?> getProfile() {
    // Protected endpoint
}
```

---

## 🚀 Lợi ích

1. **Documentation tự động:** Không cần viết API docs thủ công
2. **Testing trực tiếp:** Test APIs ngay trong browser
3. **Team collaboration:** Developers dễ hiểu và sử dụng APIs
4. **Frontend integration:** Frontend team có reference rõ ràng
5. **API versioning:** Track changes qua các version
6. **Validation:** Kiểm tra request/response format

---

## 📚 Tài liệu tham khảo

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI 3.0 Specification](https://swagger.io/specification/)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)

---

## 🎉 Next Steps

1. **Khởi động app và test Swagger UI**
2. **Thêm annotations cho controllers quan trọng**
3. **Customize API documentation theo nhu cầu**
4. **Share Swagger URL với team members**

**Happy API Documentation! 🚀**