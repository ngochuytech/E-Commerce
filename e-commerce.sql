-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th9 18, 2025 lúc 08:37 AM
-- Phiên bản máy phục vụ: 11.4.5-MariaDB
-- Phiên bản PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `e-commerce`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `addresses`
--

CREATE TABLE `addresses` (
  `id` char(36) NOT NULL,
  `province` varchar(255) NOT NULL,
  `district` varchar(255) NOT NULL,
  `ward` varchar(255) NOT NULL,
  `home_address` varchar(255) NOT NULL,
  `suggested_name` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `approvals`
--

CREATE TABLE `approvals` (
  `id` char(36) NOT NULL,
  `store_id` char(36) NOT NULL,
  `admin_id` char(36) NOT NULL,
  `action` varchar(50) NOT NULL,
  `reason` text DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `attributes`
--

CREATE TABLE `attributes` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `attributes`
--

INSERT INTO `attributes` (`id`, `name`) VALUES
(2, 'CPU'),
(3, 'GPU'),
(4, 'RAM'),
(5, 'RAM_DESCRIPTION'),
(6, 'RAM_SLOT'),
(7, 'STORAGE'),
(8, 'SCREEN_SIZE'),
(9, 'SCREEN_TECHNOLOGY'),
(10, 'BATTERY'),
(11, 'OPERATING_SYSTEM'),
(12, 'RESOLUTION'),
(13, 'PORT');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `brands`
--

CREATE TABLE `brands` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `brands`
--

INSERT INTO `brands` (`id`, `name`) VALUES
(1, 'Macbook'),
(2, 'Dell'),
(3, 'HP'),
(4, 'Asus'),
(5, 'Acer'),
(6, 'Lenovo'),
(7, 'LG'),
(8, 'Kingston'),
(9, 'Samsung'),
(10, 'Lexar'),
(11, 'Adata'),
(12, 'Pny'),
(13, 'JBL'),
(14, 'Sony'),
(15, 'Marshall'),
(16, 'Saramonic'),
(17, 'Boya'),
(18, 'AKG'),
(19, 'Logitech'),
(20, 'Rappo'),
(21, 'Cấu hình sẵn'),
(22, 'All in one'),
(23, 'PC bộ');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `card_items`
--

CREATE TABLE `card_items` (
  `id` char(36) NOT NULL,
  `cart_id` char(36) NOT NULL,
  `product_variant_id` char(36) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `carts`
--

CREATE TABLE `carts` (
  `id` char(36) NOT NULL,
  `user_id` char(36) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `chats`
--

CREATE TABLE `chats` (
  `id` char(36) NOT NULL,
  `buyer_id` char(36) NOT NULL,
  `seller_id` char(36) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `comments`
--

CREATE TABLE `comments` (
  `id` char(36) NOT NULL,
  `product_id` char(36) NOT NULL,
  `user_id` char(36) NOT NULL,
  `content` text NOT NULL,
  `parent_id` char(36) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `disputes`
--

CREATE TABLE `disputes` (
  `id` char(36) NOT NULL,
  `order_id` char(36) NOT NULL,
  `user_id` char(36) NOT NULL,
  `raised_by` varchar(50) NOT NULL,
  `description` text NOT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'OPEN',
  `handled_by` char(36) DEFAULT NULL,
  `resolution` varchar(255) DEFAULT NULL,
  `resolution_details` text DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `messages`
--

CREATE TABLE `messages` (
  `id` char(36) NOT NULL,
  `chat_id` char(36) NOT NULL,
  `sender_id` char(36) NOT NULL,
  `content` text NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `notifications`
--

CREATE TABLE `notifications` (
  `id` char(36) NOT NULL,
  `user_id` char(36) NOT NULL,
  `title` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `orders`
--

CREATE TABLE `orders` (
  `id` char(36) NOT NULL,
  `buyer_id` char(36) NOT NULL,
  `store_id` char(36) NOT NULL,
  `promotion_id` char(36) DEFAULT NULL,
  `total_price` bigint(20) NOT NULL,
  `address_id` char(36) NOT NULL,
  `payment_method` varchar(50) NOT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'PENDING',
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `order_items`
--

CREATE TABLE `order_items` (
  `id` char(36) NOT NULL,
  `order_id` char(36) NOT NULL,
  `product_variant_id` char(36) NOT NULL,
  `quantity` int(11) NOT NULL,
  `price` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `products`
--

CREATE TABLE `products` (
  `id` char(36) NOT NULL,
  `store_id` char(36) DEFAULT NULL,
  `category` varchar(100) NOT NULL,
  `brand_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `product_condition` varchar(50) NOT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `products`
--

INSERT INTO `products` (`id`, `store_id`, `category`, `brand_id`, `name`, `description`, `price`, `product_condition`, `status`, `created_at`, `updated_at`) VALUES
('24b8a699-3496-47e5-919d-5e6d97f9c142', 'a12a5aa6-8fac-11f0-a3a5-60cf84d3b2ef', 'Laptop', 6, 'Laptop LOQ', '', NULL, 'new', 'sold', '2025-09-12 08:24:15', '2025-09-12 13:30:30');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `product_variants`
--

CREATE TABLE `product_variants` (
  `id` char(36) NOT NULL,
  `product_id` char(36) NOT NULL,
  `name` varchar(255) NOT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `price` bigint(20) NOT NULL,
  `description` text DEFAULT NULL,
  `stock` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `product_variants`
--

INSERT INTO `product_variants` (`id`, `product_id`, `name`, `image_url`, `price`, `description`, `stock`) VALUES
('9ae30a4f-c5ed-493a-a759-08322add0233', '24b8a699-3496-47e5-919d-5e6d97f9c142', 'Laptop Lenovo LOQ 15IAX9E 83LK0079VN', '/image/268c81d9-1902-4eeb-aa44-d659f81252f5_Laptop.jpg', 25000000, NULL, 20),
('d3d30964-0571-44b8-a6a1-89a8d228a5fe', '24b8a699-3496-47e5-919d-5e6d97f9c142', 'Laptop Lenovo LOQ 15IAX9E 83LK0079VN', '/image/238fd551-191a-4e2e-8748-3147d4a7923e_Laptop.jpg', 22000000, NULL, 0);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `product_variant_attributes`
--

CREATE TABLE `product_variant_attributes` (
  `product_variant_id` char(36) NOT NULL,
  `attribute_id` int(11) NOT NULL,
  `value` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `product_variant_attributes`
--

INSERT INTO `product_variant_attributes` (`product_variant_id`, `attribute_id`, `value`) VALUES
('d3d30964-0571-44b8-a6a1-89a8d228a5fe', 2, 'Intel Core i5-12450HX, 8C (4P + 4E) / 12T, P-core up to 4.4GHz, E-core up to 3.1GHz, 12MB'),
('d3d30964-0571-44b8-a6a1-89a8d228a5fe', 3, 'NVIDIA GeForce RTX 3050 6GB GDDR6, Boost Clock 1432MHz, TGP 65W, 142 AI TOPS'),
('d3d30964-0571-44b8-a6a1-89a8d228a5fe', 4, '16GB'),
('d3d30964-0571-44b8-a6a1-89a8d228a5fe', 5, 'SO-DIMM DDR5-4800'),
('d3d30964-0571-44b8-a6a1-89a8d228a5fe', 6, '1 khe (1x 16GB, nâng cấp tối đa 32GB DDR5-4800)'),
('d3d30964-0571-44b8-a6a1-89a8d228a5fe', 7, '512GB SSD M.2 2242 PCIe 4.0x4 NVMe (Tối đa hai ổ đĩa, 2x M.2 SSD • M.2 2242 SSD tối đa 1TB mỗi ổ)'),
('d3d30964-0571-44b8-a6a1-89a8d228a5fe', 8, '15.6 inches'),
('d3d30964-0571-44b8-a6a1-89a8d228a5fe', 9, 'Độ sáng 300nits Màn hình chống chói Độ phủ màu 100% sRGB'),
('d3d30964-0571-44b8-a6a1-89a8d228a5fe', 10, '57Wh 35W Slim Tip (3-pin)'),
('d3d30964-0571-44b8-a6a1-89a8d228a5fe', 11, 'Windows 11 Home Single Language, English'),
('d3d30964-0571-44b8-a6a1-89a8d228a5fe', 12, '1920 x 1080 pixels (FullHD)'),
('d3d30964-0571-44b8-a6a1-89a8d228a5fe', 13, '2x USB-A (5Gbps / USB 3.2 Gen 1) - 1x USB-C (5Gbps / USB 3.2 Gen 1), chỉ truyền dữ liệu - 1x HDMI 2.1, lên đến 8K/60Hz -1x giắc cắm kết hợp tai nghe / micrô (3.5mm) - 1x Ethernet (RJ-45) - 1x Đầu đọc thẻ - 1x Đầu nối nguồn');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `promotions`
--

CREATE TABLE `promotions` (
  `id` char(36) NOT NULL,
  `title` varchar(255) NOT NULL,
  `store_id` char(36) NOT NULL,
  `type` varchar(50) NOT NULL,
  `discount_type` varchar(50) NOT NULL,
  `discount_value` bigint(20) DEFAULT NULL,
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  `min_order_value` bigint(20) DEFAULT 0,
  `max_discount_value` bigint(20) DEFAULT 0,
  `status` varchar(50) NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `reviews`
--

CREATE TABLE `reviews` (
  `id` char(36) NOT NULL,
  `order_id` char(36) NOT NULL,
  `product_variant_id` char(36) NOT NULL,
  `user_id` char(36) NOT NULL,
  `rating` int(11) NOT NULL CHECK (`rating` >= 1 and `rating` <= 5),
  `comment` text DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `roles`
--

CREATE TABLE `roles` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `roles`
--

INSERT INTO `roles` (`id`, `name`) VALUES
(1, 'USER'),
(2, 'SELLER'),
(3, 'STORE'),
(4, 'SHIPPER'),
(5, 'ADMIN');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `stores`
--

CREATE TABLE `stores` (
  `id` char(36) NOT NULL,
  `owner_id` char(36) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `address_id` char(36) DEFAULT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `banner_url` varchar(255) DEFAULT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'PENDING',
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `stores`
--

INSERT INTO `stores` (`id`, `owner_id`, `name`, `description`, `address_id`, `logo_url`, `banner_url`, `status`, `created_at`, `updated_at`) VALUES
('a12a5aa6-8fac-11f0-a3a5-60cf84d3b2ef', '29f0356d-7396-4eeb-bd88-bdcdbbac1449', 'Thuận Phát Computer', NULL, NULL, NULL, NULL, 'APPROVED', '2025-09-12 07:46:44', '2025-09-18 04:05:05');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `tokens`
--

CREATE TABLE `tokens` (
  `id` varchar(36) NOT NULL,
  `token` varchar(255) NOT NULL,
  `refresh_token` varchar(255) NOT NULL,
  `token_type` varchar(50) NOT NULL,
  `expiration_date` datetime NOT NULL,
  `refresh_expiration_date` datetime NOT NULL,
  `revoked` tinyint(1) NOT NULL,
  `expired` tinyint(1) NOT NULL,
  `is_mobile` tinyint(1) NOT NULL,
  `user_id` varchar(36) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `tokens`
--

INSERT INTO `tokens` (`id`, `token`, `refresh_token`, `token_type`, `expiration_date`, `refresh_expiration_date`, `revoked`, `expired`, `is_mobile`, `user_id`) VALUES
('859e21b2-c412-4c9f-acaf-229d0eb986b0', 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ29jaHV5bWFpbDI1QGdtYWlsLmNvbSIsImlhdCI6MTc1ODEyNTk5NCwiZXhwIjoxNzU4MTI4NTg2fQ.Pur2b5eA3fo0FxAnKPSXoFXGyDQb6JGKLrc14ku-fzE', 'e001c62f-9802-4221-ad65-b3680412d741', 'Bearer', '2025-10-17 23:19:54', '2025-11-16 23:19:54', 0, 0, 0, 'b9ae67f2-ba80-45b0-a076-e10923c7d51b');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `users`
--

CREATE TABLE `users` (
  `id` char(36) NOT NULL,
  `email` varchar(255) NOT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `google_id` varchar(255) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `address_id` char(36) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `role_id` int(11) NOT NULL DEFAULT 1,
  `is_active` tinyint(1) DEFAULT 1,
  `enable` tinyint(1) DEFAULT NULL,
  `verification_code` varchar(36) DEFAULT NULL,
  `reset_password_token` varchar(36) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `users`
--

INSERT INTO `users` (`id`, `email`, `phone`, `password`, `full_name`, `google_id`, `date_of_birth`, `address_id`, `avatar_url`, `role_id`, `is_active`, `enable`, `verification_code`, `reset_password_token`, `created_at`, `updated_at`) VALUES
('29f0356d-7396-4eeb-bd88-bdcdbbac1449', 'khongcoten3006@gmail.com', NULL, '$2a$10$/6nf6RchfFzVWoE3lLFYK./Sf11qbyMSgbAUXwgU2Y0EAamNqSEYe', 'Nguyen Huy', NULL, NULL, NULL, NULL, 1, 1, 1, NULL, NULL, '2025-09-11 09:50:20', '2025-09-11 09:50:34'),
('b9ae67f2-ba80-45b0-a076-e10923c7d51b', 'ngochuymail25@gmail.com', NULL, '$2a$10$zHS6nFVjmiizlW3XtLyohOG9lrySv1dBY6Pe6Z/3CH5CZvlMC2/Ly', 'Nguyen Huy', NULL, NULL, NULL, NULL, 1, 1, 1, NULL, NULL, '2025-09-17 16:18:53', '2025-09-17 16:19:31'),
('f85412b9-855c-4c81-b970-7082686bd74b', 'huy@gmail.com', NULL, '$2a$10$fmYmLNDMVs/w1ch51PK3b.hzqBSXPDGl8062v/1h.4U9IG/t4Xldm', 'Nguyen Huy', NULL, NULL, NULL, NULL, 1, 1, NULL, NULL, NULL, '2025-09-10 16:07:02', '2025-09-10 16:07:02');

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `addresses`
--
ALTER TABLE `addresses`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `approvals`
--
ALTER TABLE `approvals`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_approvals_store` (`store_id`),
  ADD KEY `fk_approvals_admin` (`admin_id`);

--
-- Chỉ mục cho bảng `attributes`
--
ALTER TABLE `attributes`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `brands`
--
ALTER TABLE `brands`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `card_items`
--
ALTER TABLE `card_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_card_items_cart` (`cart_id`),
  ADD KEY `fk_card_items_product_variant` (`product_variant_id`);

--
-- Chỉ mục cho bảng `carts`
--
ALTER TABLE `carts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_carts_user` (`user_id`);

--
-- Chỉ mục cho bảng `chats`
--
ALTER TABLE `chats`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_chats_user1` (`buyer_id`),
  ADD KEY `fk_chats_user2` (`seller_id`);

--
-- Chỉ mục cho bảng `comments`
--
ALTER TABLE `comments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_comments_product` (`product_id`),
  ADD KEY `fk_comments_user` (`user_id`),
  ADD KEY `fk_comments_parent` (`parent_id`);

--
-- Chỉ mục cho bảng `disputes`
--
ALTER TABLE `disputes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_disputes_order` (`order_id`),
  ADD KEY `fk_disputes_user` (`user_id`),
  ADD KEY `fk_disputes_handler` (`handled_by`);

--
-- Chỉ mục cho bảng `messages`
--
ALTER TABLE `messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_messages_chat` (`chat_id`),
  ADD KEY `fk_messages_user` (`sender_id`);

--
-- Chỉ mục cho bảng `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_notifications_user` (`user_id`);

--
-- Chỉ mục cho bảng `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_orders_promotion` (`promotion_id`),
  ADD KEY `fk_orders_user` (`buyer_id`),
  ADD KEY `fk_orders_store` (`store_id`),
  ADD KEY `fk_orders_address` (`address_id`);

--
-- Chỉ mục cho bảng `order_items`
--
ALTER TABLE `order_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_order_items_order` (`order_id`),
  ADD KEY `fk_order_items_product_variant` (`product_variant_id`);

--
-- Chỉ mục cho bảng `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_products_store` (`store_id`),
  ADD KEY `fk_product_brands` (`brand_id`);

--
-- Chỉ mục cho bảng `product_variants`
--
ALTER TABLE `product_variants`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_product_variants_product` (`product_id`);

--
-- Chỉ mục cho bảng `product_variant_attributes`
--
ALTER TABLE `product_variant_attributes`
  ADD PRIMARY KEY (`product_variant_id`,`attribute_id`),
  ADD KEY `fk_product_attributes` (`attribute_id`);

--
-- Chỉ mục cho bảng `promotions`
--
ALTER TABLE `promotions`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `reviews`
--
ALTER TABLE `reviews`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_reviews_order` (`order_id`),
  ADD KEY `fk_reviews_product` (`product_variant_id`),
  ADD KEY `fk_reviews_user` (`user_id`);

--
-- Chỉ mục cho bảng `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `stores`
--
ALTER TABLE `stores`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_stores_user` (`owner_id`),
  ADD KEY `fk_stores_address` (`address_id`);

--
-- Chỉ mục cho bảng `tokens`
--
ALTER TABLE `tokens`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_tokens_users` (`user_id`);

--
-- Chỉ mục cho bảng `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `phone` (`phone`),
  ADD KEY `fk_users_address` (`address_id`),
  ADD KEY `fk_users_role` (`role_id`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `attributes`
--
ALTER TABLE `attributes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT cho bảng `brands`
--
ALTER TABLE `brands`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `approvals`
--
ALTER TABLE `approvals`
  ADD CONSTRAINT `fk_approvals_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `fk_approvals_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`);

--
-- Các ràng buộc cho bảng `card_items`
--
ALTER TABLE `card_items`
  ADD CONSTRAINT `fk_card_items_cart` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`),
  ADD CONSTRAINT `fk_card_items_product_variant` FOREIGN KEY (`product_variant_id`) REFERENCES `product_variants` (`id`);

--
-- Các ràng buộc cho bảng `carts`
--
ALTER TABLE `carts`
  ADD CONSTRAINT `fk_carts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `chats`
--
ALTER TABLE `chats`
  ADD CONSTRAINT `fk_chats_user1` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `fk_chats_user2` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `comments`
--
ALTER TABLE `comments`
  ADD CONSTRAINT `fk_comments_parent` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`id`),
  ADD CONSTRAINT `fk_comments_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  ADD CONSTRAINT `fk_comments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `disputes`
--
ALTER TABLE `disputes`
  ADD CONSTRAINT `fk_disputes_handler` FOREIGN KEY (`handled_by`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `fk_disputes_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  ADD CONSTRAINT `fk_disputes_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `messages`
--
ALTER TABLE `messages`
  ADD CONSTRAINT `fk_messages_chat` FOREIGN KEY (`chat_id`) REFERENCES `chats` (`id`),
  ADD CONSTRAINT `fk_messages_user` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `fk_orders_address` FOREIGN KEY (`address_id`) REFERENCES `addresses` (`id`),
  ADD CONSTRAINT `fk_orders_promotion` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`id`),
  ADD CONSTRAINT `fk_orders_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`),
  ADD CONSTRAINT `fk_orders_user` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `order_items`
--
ALTER TABLE `order_items`
  ADD CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  ADD CONSTRAINT `fk_order_items_product_variant` FOREIGN KEY (`product_variant_id`) REFERENCES `product_variants` (`id`);

--
-- Các ràng buộc cho bảng `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `fk_product_brands` FOREIGN KEY (`brand_id`) REFERENCES `brands` (`id`),
  ADD CONSTRAINT `fk_products_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`);

--
-- Các ràng buộc cho bảng `product_variants`
--
ALTER TABLE `product_variants`
  ADD CONSTRAINT `fk_product_variants_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);

--
-- Các ràng buộc cho bảng `product_variant_attributes`
--
ALTER TABLE `product_variant_attributes`
  ADD CONSTRAINT `fk_product_attributes` FOREIGN KEY (`attribute_id`) REFERENCES `attributes` (`id`),
  ADD CONSTRAINT `fk_pva_product_variant` FOREIGN KEY (`product_variant_id`) REFERENCES `product_variants` (`id`);

--
-- Các ràng buộc cho bảng `reviews`
--
ALTER TABLE `reviews`
  ADD CONSTRAINT `fk_reviews_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  ADD CONSTRAINT `fk_reviews_product` FOREIGN KEY (`product_variant_id`) REFERENCES `product_variants` (`id`),
  ADD CONSTRAINT `fk_reviews_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `stores`
--
ALTER TABLE `stores`
  ADD CONSTRAINT `fk_stores_address` FOREIGN KEY (`address_id`) REFERENCES `addresses` (`id`),
  ADD CONSTRAINT `fk_stores_user` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `tokens`
--
ALTER TABLE `tokens`
  ADD CONSTRAINT `fk_tokens_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `fk_users_address` FOREIGN KEY (`address_id`) REFERENCES `addresses` (`id`),
  ADD CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
