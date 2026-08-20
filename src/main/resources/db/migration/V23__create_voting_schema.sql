-- V23: Voting locations, tables and voter assignments schema
-- Supports polling place lookup by DNI and admin assignment flow

CREATE TABLE dbo.voting_location (
    id             INT IDENTITY(1,1) NOT NULL,
    name           VARCHAR(150)      NOT NULL,
    address        VARCHAR(200)      NOT NULL,
    location_code  CHAR(6)           NOT NULL,
    capacity       INT               NOT NULL,
    is_active      BIT               NOT NULL,
    created_at     DATETIME          NOT NULL,
    updated_at     DATETIME          NULL,
    CONSTRAINT PK_voting_location PRIMARY KEY (id),
    CONSTRAINT FK_vl_location FOREIGN KEY (location_code)
        REFERENCES dbo.locations(location_code)
);

CREATE TABLE dbo.voting_table (
    id                  INT IDENTITY(1,1) NOT NULL,
    table_number        VARCHAR(10)       NOT NULL,
    capacity            INT               NOT NULL,
    voting_location_id  INT               NOT NULL,
    created_at          DATETIME          NOT NULL,
    updated_at          DATETIME          NULL,
    CONSTRAINT PK_voting_table   PRIMARY KEY (id),
    CONSTRAINT UQ_vt_number      UNIQUE (table_number),
    CONSTRAINT FK_vt_location    FOREIGN KEY (voting_location_id)
        REFERENCES dbo.voting_location(id)
);

CREATE TABLE dbo.voter_assignment (
    id               INT  IDENTITY(1,1) NOT NULL,
    voter_id         INT  NOT NULL,
    voting_table_id  INT  NOT NULL,
    assigned_at      DATE NOT NULL,
    created_at       DATETIME NOT NULL,
    CONSTRAINT PK_voter_assignment  PRIMARY KEY (id),
    CONSTRAINT UQ_va_voter          UNIQUE (voter_id),
    CONSTRAINT FK_va_voter          FOREIGN KEY (voter_id)
        REFERENCES dbo.voters(id),
    CONSTRAINT FK_va_table          FOREIGN KEY (voting_table_id)
        REFERENCES dbo.voting_table(id)
);
