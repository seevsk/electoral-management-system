-- Seeds a test Operador Electoral account (role='operator') for local testing.
-- Reuses the same known BCrypt hash as the admin test account (V17).
INSERT INTO dbo.accounts (dni, password_hash, role, is_active, created_at, updated_at)
SELECT
    '00000001',
    '$2a$12$1KAaUsD8qGIWiYhUcTK33.88VYrhONp6paiW0j2nHmHkNVWS2g/4W',
    'operator',
    1,
    GETDATE(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.accounts WHERE dni = '00000001'
);
