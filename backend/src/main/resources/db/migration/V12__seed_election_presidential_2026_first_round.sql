INSERT INTO dbo.elections (
    [name],
    [election_type],
    [year],
    [start_date],
    [end_date],
    [status],
    [created_at]
)
SELECT
    'Elecciones Generales 2026 - Primera Vuelta',
    'PRESIDENCIAL',
    2026,
    CAST('2026-06-07 06:00:00' AS DATETIME),
    CAST('2026-06-07 17:00:00' AS DATETIME),
    'P',
    GETDATE()
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.elections e
    WHERE e.[name] = 'Elecciones Generales 2026 - Primera Vuelta'
      AND e.[election_type] = 'PRESIDENCIAL'
      AND e.[year] = 2026
);
