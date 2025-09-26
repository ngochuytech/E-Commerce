USE e-commerce;
INSERT INTO products (id, store_id, category, brand_id, name, description, price, product_condition, status)
VALUES ('22222222-3333-4444-5555-666666666666', 'a64a7ede-c926-49c7-b38a-9af04eee8b8b', 'Accessory', 13, 'Tai nghe ABC', NULL, 1500000, 'new', 'ACTIVE');
INSERT INTO product_variants (id, product_id, name, image_url, price, description, stock)
VALUES ('bbbbbbbb-cccc-dddd-eeee-ffffffffffff', '22222222-3333-4444-5555-666666666666', 'Tai nghe ABC Black', NULL, 1500000, NULL, 50);
