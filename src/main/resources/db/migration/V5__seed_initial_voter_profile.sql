INSERT INTO voters (
    account_id,
    first_surname,
    second_surname,
    full_name,
    gender,
    marital_status,
    birth_date,
    dni_expiry_date,
    location_code,
    status,
    has_voted,
    voted_at,
    created_at,
    updated_at
)
SELECT
    a.id,
    'SALAS',
    'ROJAS',
    'SEBASTIAN JOSE',
    'M',
    'S',
    CAST('2003-05-11' AS DATE),
    CAST('2028-07-08' AS DATE),
    '140101',
    'I',
    0,
    NULL,
    GETDATE(),
    NULL
FROM accounts a
WHERE a.dni = '76023842'
  AND a.role = 'user';
