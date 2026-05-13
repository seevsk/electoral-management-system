INSERT INTO accounts (
    dni,
    password_hash,
    role,
    is_active,
    created_at,
    updated_at
)
VALUES (
    '10209310',
    '$2a$12$WkCVOpEd4mDXYvS439tdV.jVK/VcBICxCAO7jjzQazIguntMWMdS6',
    'admin',
    1,
    GETDATE(),
    NULL
);
