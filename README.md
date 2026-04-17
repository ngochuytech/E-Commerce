<div align="center">

# 🛒 TechNova - E-Commerce Platform

### *Nền tảng thương mại điện tử công nghệ đa nhà cung cấp*

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-47A248?style=for-the-badge&logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

</div>

---

## 📋 Mục lục

- [Giới thiệu](#-giới-thiệu)
- [Tech Stack](#️-tech-stack)
- [Tính năng chính](#-tính-năng-chính)
- [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
- [Cài đặt & Chạy](#-cài-đặt--chạy)
- [API Documentation](#-api-documentation)

---

## Tài khoản test
URL: 
- pbl-6-eight.vercel.app (USER)
- pbl-6-eight.vercel.app/admin-dashboard (ADMIN)
- pbl-6-eight.vercel.app/store-dashboard (STORE)

TK ADMIN: quang3072004.1@gmail.com TK BUYER: Ndnquang3072004@gmail.com TK CHỦ STORE: quang3072004@gmail.com

MK : 123456789

## 🎯 Giới thiệu

**TechNova** là một nền tảng thương mại điện tử **đa nhà cung cấp (Multi-vendor)** chuyên về sản phẩm công nghệ, được xây dựng nhằm mục đích học tập và demo các công nghệ hiện đại trong phát triển web.

### 🎓 Mục đích dự án
- ✅ Xây dựng hệ thống e-commerce hoàn chỉnh với các tính năng thực tế
- ✅ Áp dụng kiến trúc microservices và design patterns
- ✅ Thực hành DevOps với Docker
- ✅ Demo tích hợp các dịch vụ third-party (Payment, Cloud Storage, Email)

### 🌟 Điểm nổi bật
- 🏪 **Multi-vendor marketplace** - Cho phép nhiều người bán đăng ký và quản lý cửa hàng
- 🔐 **Hệ thống phê duyệt 2 cấp** - Admin duyệt cửa hàng và sản phẩm trước khi công khai
- 💬 **Real-time chat** - WebSocket để hỗ trợ khách hàng trực tiếp
- 💳 **Đa phương thức thanh toán** - VNPay, MoMo
- 📦 **Quản lý vận chuyển** - Tích hợp đơn vị giao hàng và theo dõi đơn
- 📊 **Analytics & Statistics** - Dashboard thống kê cho seller và admin

---

## 🛠️ Tech Stack

### Backend Framework
- **Java 21** - Ngôn ngữ lập trình
- **Spring Boot 3.5.5** - Framework chính
- **Spring Security** - Xác thực & phân quyền
- **Spring Data MongoDB** - ORM cho MongoDB
- **Spring Data Redis** - Cache layer
- **Spring WebSocket** - Real-time communication
- **Spring Mail** - Email service
- **Spring AOP** - Aspect-oriented programming

### Database & Cache
- **MongoDB Atlas** - NoSQL Database (Cloud)
- **Redis** - In-memory cache & session storage

### Security & Authentication
- **JWT (JSON Web Token)** - Stateless authentication
- **BCrypt** - Password hashing
- **Spring Security** - Role-based access control

### Third-party Services
- **Cloudinary** - Cloud storage cho hình ảnh
- **SendGrid** - Email delivery service
- **VNPay API** - Cổng thanh toán Việt Nam
- **MoMo API** - Ví điện tử MoMo

### DevOps & Tools
- **Docker & Docker Compose** - Containerization
- **Maven** - Build automation
- **Swagger/OpenAPI 3** - API documentation
- **Lombok** - Reduce boilerplate code

### Development Tools
- **IntelliJ IDEA / VS Code** - IDE
- **Postman** - API testing
- **MongoDB Compass** - Database GUI
- **Git** - Version control

---

## ✨ Tính năng chính

### 👥 Người dùng (Customer)
- 🔐 Đăng ký / Đăng nhập (Email + Password)
- 👤 Quản lý hồ sơ cá nhân
- 🔑 Quên mật khẩu & Reset password
- 📍 Quản lý địa chỉ giao hàng
- 🛍️ Tìm kiếm & lọc sản phẩm (theo category, brand, price)
- 🛒 Giỏ hàng & Wishlist
- 💳 Đặt hàng & thanh toán (VNPay, MoMo, COD)
- 📦 Theo dõi đơn hàng real-time
- ⭐ Đánh giá & Review sản phẩm
- 💬 Chat trực tiếp với seller
- 🎟️ Sử dụng mã giảm giá (Promotions)
- 💰 Ví điện tử nội bộ (User Wallet)
- 🔄 Yêu cầu hoàn trả / Đổi hàng

### 🏪 Nhà bán hàng (Vendor/B2C)
- 📝 Đăng ký cửa hàng (chờ admin duyệt)
- 🏢 Quản lý thông tin cửa hàng (Logo, Banner, Description)
- 📦 Quản lý sản phẩm (CRUD)
  - Tạo sản phẩm → Chờ admin duyệt
  - Quản lý variants (màu sắc, size, giá)
- 📊 Dashboard thống kê doanh thu
- 📦 Quản lý đơn hàng (Xác nhận, Đóng gói, Giao shipper)
- 💬 Chat với khách hàng
- 🎟️ Tạo & quản lý mã khuyến mãi
- 💰 Quản lý ví tiền & rút tiền
- 🔔 Nhận thông báo đơn hàng mới

### 🚚 Shipper (Delivery Partner)
- 📦 Nhận đơn hàng cần giao
- 📍 Cập nhật trạng thái vận chuyển
- ✅ Xác nhận giao hàng thành công

### 👨‍💼 Admin
- 🏪 Phê duyệt cửa hàng
- 📦 Phê duyệt sản phẩm & variants
- 👥 Quản lý người dùng (Block/Unblock)
- 📊 Xem thống kê tổng quan hệ thống
- 🎟️ Quản lý promotions toàn hệ thống
- 📦 Giám sát tất cả đơn hàng
- 🚫 Xử lý khiếu nại & hoàn trả

---

## 🏗️ Kiến trúc hệ thống

### Tổng quan kiến trúc

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                         |
│                   (Web App / Mobile App)                    │
└──────────────────────────┬──────────────────────────────────┘
                           │ REST API / WebSocket
┌──────────────────────────▼──────────────────────────────────┐
│                    Spring Boot Backend                      │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              Controllers Layer                      │    │
│  │  - AdminController  - B2CController                 │    │
│  │  - BuyerController  - ChatController                │    │
│  │  - ShipperController - PublicController             │    │
│  └──────────────────────┬──────────────────────────────┘    │
│                         │                                   │
│  ┌──────────────────────▼──────────────────────────────┐    │
│  │              Services Layer                         │    │
│  │  - ProductService    - OrderService                 │    │
│  │  - StoreService      - PaymentService               │    │
│  │  - UserService       - NotificationService          │    │
│  │  - ChatService       - FileUploadService            │    │
│  │  - ...                                              │    │
│  └──────────────────────┬──────────────────────────────┘    │
│                         │                                   │
│  ┌──────────────────────▼──────────────────────────────┐    │
│  │           Repositories Layer (MongoDB)              │    │
│  └─────────────────────────────────────────────────────┘    │
└──────────────────────────┬──────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼────────┐ ┌──────▼──────┐ ┌────────▼─────────┐
│  MongoDB Atlas │ │    Redis    │ │  Cloudinary      │
│   (Database)   │ │   (Cache)   │ │ (File Storage)   │
└────────────────┘ └─────────────┘ └──────────────────┘
```

---

## 🚀 Cài đặt & Chạy

### Yêu cầu hệ thống

| Công cụ | Version | Link Download |
|---------|---------|---------------|
| **JDK** | 21 hoặc cao hơn | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) |
| **Maven** | 3.8+ | [Apache Maven](https://maven.apache.org/download.cgi) |
| **Docker** | Latest | [Docker Desktop](https://www.docker.com/products/docker-desktop/) |
| **Git** | Latest | [Git SCM](https://git-scm.com/downloads) |

### Bước 1: Clone repository

```bash
git clone https://github.com/ngochuytech/E-Commerce.git
cd E-Commerce
```

### Bước 2: Cấu hình môi trường

Thay đổi cấu hình biến môi trường trong `application.yml`:

```yaml
# MongoDB Configuration
DB_MONGODB_USERNAME=your_mongodb_username
DB_MONGODB_PASSWORD=your_mongodb_password
DB_MONGODB_NAME=your_mongodb_databasename

# Redis Configuration
REDIS_HOST=your_redis_host
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
REDIS_USERNAME=your_redis_username

# Email Configuration (SendGrid)
SENDGRID_API_KEY=your_sendgrid_api_key
MAIL_FROM_ADDRESS=noreply@TechNova.com

# Cloudinary Configuration
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Payment Gateway
VNPAY_TMNCODE=your_vnpay_code
VNPAY_SECRET_KEY=your_vnpay_secret
VNPAY_RETURN_URI=your_frontend_url
VNPAY_API_URL=http://localhost:8080
MOMO_PARTNER_CODE=your_momo_partnercode
MOMO_ACCESS_KEY=your_momo_accesskey
MOMO_SECRET_KEY=your_secret_key
MOMO_RETURN_URL=your_frontend_url
MOMO_IPN_URL=http://localhost:8080/api/v1/buyer/payments/momo/ipn
```

### Bước 3: Build project

```bash
# Clean & install dependencies
mvn clean install

# Hoặc sử dụng Maven Wrapper (không cần cài Maven)
./mvnw clean install
```

### Bước 4: Chạy trực tiếp (Không dùng Docker)

```bash
# Chạy Spring Boot application
mvn spring-boot:run

# Hoặc
./mvnw spring-boot:run

# Hoặc chạy file JAR
java -jar target/e-commerce-techshop-0.0.1-SNAPSHOT.jar
```

### Chạy với Docker Compose (Khuyến nghị)

```bash
# Build và start tất cả services
docker build -t technova:v1.0 .
```

Ứng dụng sẽ chạy tại: **http://localhost:8080**

---

## 📚 API Documentation

### Swagger UI (Interactive Docs)

Sau khi chạy ứng dụng, truy cập:

🔗 **http://localhost:8080/swagger-ui/index.html**

---

## 📂 Cấu trúc thư mục

```
e-commerce-techshop/
├── src/
│   ├── main/
│   │   ├── java/com/example/e_commerce_techshop/
│   │   │   ├── annotations/          # Custom annotations
│   │   │   ├── aspects/              # AOP aspects
│   │   │   ├── components/           # Spring components
│   │   │   ├── configurations/       # Config classes (Security, WebSocket...)
│   │   │   ├── controllers/
│   │   │   │   ├── admin/           # Admin endpoints
│   │   │   │   ├── b2c/             # Vendor endpoints
│   │   │   │   ├── buyer/           # Customer endpoints
│   │   │   │   ├── chat/            # Chat endpoints
│   │   │   │   └── shipper/         # Shipper endpoints
│   │   │   ├── dtos/                # Data Transfer Objects
│   │   │   ├── exceptions/          # Custom exceptions
│   │   │   ├── filter/              # Security filters (JWT)
│   │   │   ├── models/              # MongoDB entities
│   │   │   ├── repositories/        # Spring Data repositories
│   │   │   ├── responses/           # API response wrappers
│   │   │   ├── services/            # Business logic
│   │   │   └── ECommerceTechshopApplication.java
│   │   └── resources/
│   │       ├── application.yml      # Main config
│   │       ├── static/              # Static files
│   │       └── templates/           # Email templates
│   └── test/                        # Unit tests
├── uploads/                         # Local file storage
├── Dockerfile                       # App containerization
├── pom.xml                          # Maven dependencies
└── README.md                        # Documentation
```

---

<div align="center">

### ⭐ Nếu dự án này hữu ích, hãy cho mình một star nha!

**Made with ❤️ by Nguyen Van Ngoc Huy**

</div>
