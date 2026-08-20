IF OBJECT_ID('dbo.party_election_representatives', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.party_election_representatives (
        id INT IDENTITY(1,1) NOT NULL,
        party_id INT NOT NULL,
        election_id INT NOT NULL,
        candidate_id INT NOT NULL,
        created_at DATETIME NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME NULL,
        CONSTRAINT PK_party_election_representatives PRIMARY KEY (id),
        CONSTRAINT FK_per_party FOREIGN KEY (party_id) REFERENCES dbo.parties(id),
        CONSTRAINT FK_per_election FOREIGN KEY (election_id) REFERENCES dbo.elections(id),
        CONSTRAINT FK_per_candidate FOREIGN KEY (candidate_id) REFERENCES dbo.candidates(id),
        CONSTRAINT UQ_per_party_election UNIQUE (party_id, election_id),
        CONSTRAINT UQ_per_candidate UNIQUE (candidate_id)
    );
END;
