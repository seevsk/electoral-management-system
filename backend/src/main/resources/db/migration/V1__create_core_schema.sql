CREATE TABLE locations (
    location_code CHAR(6) NOT NULL,
    department_code NVARCHAR(50) NULL,
    province_code NVARCHAR(50) NULL,
    district_code NVARCHAR(50) NULL,
    department VARCHAR(30) NOT NULL,
    province VARCHAR(30) NOT NULL,
    district VARCHAR(50) NOT NULL,
    CONSTRAINT PK_locations PRIMARY KEY (location_code)
);

CREATE TABLE accounts (
    id INT IDENTITY(1,1) NOT NULL,
    dni CHAR(8) NOT NULL,
    password_hash VARCHAR(255) NULL,
    role VARCHAR(20) NOT NULL,
    is_active BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME NULL,
    CONSTRAINT PK_accounts PRIMARY KEY (id),
    CONSTRAINT UQ_accounts_dni UNIQUE (dni)
);

CREATE TABLE voters (
    id INT IDENTITY(1,1) NOT NULL,
    account_id INT NOT NULL,
    first_surname VARCHAR(60) NOT NULL,
    second_surname VARCHAR(60) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    gender CHAR(1) NOT NULL,
    marital_status CHAR(1) NOT NULL,
    birth_date DATE NOT NULL,
    dni_expiry_date DATE NULL,
    location_code CHAR(6) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'I',
    has_voted BIT NOT NULL DEFAULT 0,
    voted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME NULL,
    CONSTRAINT PK_voters PRIMARY KEY (id),
    CONSTRAINT UQ_voters_account_id UNIQUE (account_id),
    CONSTRAINT FK_voters_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT FK_voters_location FOREIGN KEY (location_code) REFERENCES locations(location_code)
);

CREATE TABLE tokens (
    id INT IDENTITY(1,1) NOT NULL,
    account_id INT NOT NULL,
    token VARCHAR(255) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_tokens PRIMARY KEY (id),
    CONSTRAINT UQ_tokens_token UNIQUE (token),
    CONSTRAINT FK_tokens_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE TABLE parties (
    id INT IDENTITY(1,1) NOT NULL,
    name VARCHAR(150) NOT NULL,
    acronym VARCHAR(20) NOT NULL,
    representative VARCHAR(150) NOT NULL,
    logo_url VARCHAR(500) NULL,
    list_position INT NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME NULL,
    CONSTRAINT PK_parties PRIMARY KEY (id)
);

CREATE TABLE elections (
    id INT IDENTITY(1,1) NOT NULL,
    name VARCHAR(200) NOT NULL,
    election_type VARCHAR(30) NOT NULL,
    year INT NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    status CHAR(1) NOT NULL DEFAULT 'P',
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_elections PRIMARY KEY (id)
);

CREATE TABLE candidates (
    id INT IDENTITY(1,1) NOT NULL,
    voter_id INT NOT NULL,
    party_id INT NOT NULL,
    election_id INT NOT NULL,
    list_number INT NOT NULL,
    photo_url VARCHAR(500) NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME NULL,
    CONSTRAINT PK_candidates PRIMARY KEY (id),
    CONSTRAINT UQ_candidate_election UNIQUE (voter_id, election_id),
    CONSTRAINT UQ_list_number_party_election UNIQUE (list_number, party_id, election_id),
    CONSTRAINT FK_candidates_voter FOREIGN KEY (voter_id) REFERENCES voters(id),
    CONSTRAINT FK_candidates_party FOREIGN KEY (party_id) REFERENCES parties(id),
    CONSTRAINT FK_candidates_election FOREIGN KEY (election_id) REFERENCES elections(id)
);

CREATE TABLE votes (
    id INT IDENTITY(1,1) NOT NULL,
    voter_id INT NOT NULL,
    candidate_id INT NOT NULL,
    election_id INT NOT NULL,
    voted_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_votes PRIMARY KEY (id),
    CONSTRAINT UQ_vote_per_election UNIQUE (voter_id, election_id),
    CONSTRAINT FK_votes_voter FOREIGN KEY (voter_id) REFERENCES voters(id),
    CONSTRAINT FK_votes_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(id),
    CONSTRAINT FK_votes_election FOREIGN KEY (election_id) REFERENCES elections(id)
);
