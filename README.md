
# CÁCH 1: CHẠY LOCAL + DOCKER MYSQL
TAB 1 TERMINAL: docker-compose up -d mysql phpmyadmin
TAB 2 TERMINAL: .\mvnw spring-boot:run


APIs chạy tại: http://localhost:8080
phpMyAdmin: http://localhost:8081

** PHẢI MỞ DOCKER DESKTOP




PLATFORM E-COMMERCE:
├── ADMIN (Role 5)
├── STORE (Role 3) - Chủ cửa hàng
│   ├── Store A
│   └── Store B
├── SELLER (Role 2) - Nhân viên bán hàng độc lập
├── USER (Role 1) - Khách hàng
└── SHIPPER (Role 4) - Người giao hàng

