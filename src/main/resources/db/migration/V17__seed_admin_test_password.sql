-- Updates the admin account with a known BCrypt hash for local testing.
UPDATE accounts
SET password_hash = '$2a$12$1KAaUsD8qGIWiYhUcTK33.88VYrhONp6paiW0j2nHmHkNVWS2g/4W',
    is_active = 1,
    updated_at = GETDATE()
WHERE dni = '10209310'
  AND role = 'admin';
