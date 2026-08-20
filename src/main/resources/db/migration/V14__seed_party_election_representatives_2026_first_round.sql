INSERT INTO dbo.party_election_representatives (
    party_id,
    election_id,
    candidate_id,
    created_at,
    updated_at
)
SELECT
    c.party_id,
    c.election_id,
    c.id,
    GETDATE(),
    NULL
FROM dbo.candidates c
INNER JOIN dbo.elections e
    ON e.id = c.election_id
   AND e.[name] = 'Elecciones Generales 2026 - Primera Vuelta'
   AND e.[election_type] = 'PRESIDENCIAL'
   AND e.[year] = 2026
LEFT JOIN dbo.party_election_representatives per
    ON per.party_id = c.party_id
   AND per.election_id = c.election_id
WHERE per.id IS NULL;
