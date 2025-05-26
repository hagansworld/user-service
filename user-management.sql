INSERT INTO users (id, username, email, password, enabled, created_at, updated_at)
VALUES (
  gen_random_uuid(),  -- Generates a new UUID
  'admin',             -- Admin username
  'admin@gmail.com', -- Admin email
  'admin12345', -- Admin password (hashed)
  TRUE,                -- Account is enabled
  NOW(),               -- Current timestamp for created_at
  NOW()                -- Current timestamp for updated_at
);

select * from users


-- Assign the ADMIN role to the new user
INSERT INTO user_roles (user_id, role_id)
VALUES (
  (SELECT id FROM users WHERE username = 'admin'),  -- Find the admin user by username
  (SELECT id FROM roles WHERE role = 'ADMIN')      -- Assign the ADMIN role
);

-- query to see users and roles
SELECT u.username, u.email, r.role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id;


-- Insert role 'ADMIN'
INSERT INTO roles (id, role, created_at)
VALUES ( gen_random_uuid(), 'ADMIN', NOW());

-- Insert role 'USER'
INSERT INTO roles (id, role, created_at)
VALUES ( gen_random_uuid(), 'USER', NOW());

-- Insert role 'MANAGER'
INSERT INTO roles (id, role, created_at)
VALUES ( gen_random_uuid(), 'MANAGER', NOW());


