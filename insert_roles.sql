-- Insert roles data into the roles table
-- This script will populate the roles table with basic roles

INSERT INTO roles (id, name) VALUES 
(1, 'USER'),
(2, 'ADMIN'), 
(3, 'SHOP'),
(4, 'SELLER')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Verify the data was inserted
SELECT * FROM roles;
