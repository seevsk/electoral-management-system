INSERT INTO dbo.candidates (
    voter_id,
    party_id,
    election_id,
    list_number,
    photo_url,
    is_active,
    created_at,
    updated_at
)
SELECT
    v.id,
    p.id,
    e.id,
    1,
    NULL,
    1,
    GETDATE(),
    NULL
FROM (
    VALUES
    ('41373494', 1),
    ('43409673', 2),
    ('09177250', 3),
    ('09307547', 5),
    ('06506278', 6),
    ('08263758', 7),
    ('18870364', 8),
    ('40139245', 9),
    ('10266270', 10),
    ('06002034', 11),
    ('06280714', 12),
    ('06466585', 13),
    ('07867789', 15),
    ('16002918', 16),
    ('07246887', 17),
    ('08255194', 18),
    ('41904418', 19),
    ('07260881', 20),
    ('10001088', 21),
    ('17903382', 22),
    ('01211014', 23),
    ('25331980', 24),
    ('10219647', 25),
    ('18141156', 26),
    ('43287528', 27),
    ('06529088', 28),
    ('04411300', 29),
    ('08587486', 30),
    ('41265978', 31),
    ('43632186', 32),
    ('07845838', 33),
    ('40799023', 34),
    ('25681995', 35),
    ('08058852', 36),
    ('40728264', 37),
    ('09871134', 38)
) AS src(dni, party_list_position)
INNER JOIN dbo.accounts a
    ON a.dni = src.dni
    AND a.role = 'user'
INNER JOIN dbo.voters v
    ON v.account_id = a.id
INNER JOIN dbo.parties p
    ON p.list_position = src.party_list_position
INNER JOIN dbo.elections e
    ON e.[name] = 'Elecciones Generales 2026 - Primera Vuelta'
   AND e.[election_type] = 'PRESIDENCIAL'
   AND e.[year] = 2026
LEFT JOIN dbo.candidates c
    ON c.voter_id = v.id
   AND c.election_id = e.id
WHERE c.id IS NULL;
