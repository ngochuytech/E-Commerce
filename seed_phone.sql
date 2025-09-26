USE e-commerce;
INSERT INTO products (id, store_id, category, brand_id, name, description, price, product_condition, status)
VALUES ('11111111-2222-3333-4444-555555555555', 'a64a7ede-c926-49c7-b38a-9af04eee8b8b', 'Phone', 9, 'Dien thoai XYZ', NULL, 10000000, 'new', 'ACTIVE');
INSERT INTO product_variants (id, product_id, name, image_url, price, description, stock)
VALUES ('66666666-7777-8888-9999-aaaaaaaaaaaa', '11111111-2222-3333-4444-555555555555', 'Dien thoai XYZ 128GB', NULL, 10000000, NULL, 20);
