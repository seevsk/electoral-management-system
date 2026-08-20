IF OBJECT_ID('dbo.stg_voters_import', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.stg_voters_import (
        stg_id      INT            NOT NULL,
        dni         VARCHAR(10)    NOT NULL,
        nombres     NVARCHAR(200)  NOT NULL,
        ap_paterno  NVARCHAR(100)  NOT NULL,
        ap_materno  NVARCHAR(100)  NOT NULL,
        fec_nac     VARCHAR(20)    NOT NULL,
        distrito    NVARCHAR(100)  NOT NULL,
        correo      NVARCHAR(200)  NULL,
        habilitado  VARCHAR(10)    NULL,
        ya_voto     VARCHAR(10)    NULL
    );
END;

IF OBJECT_ID('dbo.voters_import_audit', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.voters_import_audit (
        id                  INT IDENTITY(1,1) NOT NULL,
        run_at              DATETIME NOT NULL DEFAULT GETDATE(),
        total_csv_rows      INT,
        accounts_inserted   INT,
        voters_inserted     INT,
        skipped_no_location INT,
        notes               VARCHAR(500),
        CONSTRAINT PK_voters_import_audit PRIMARY KEY (id)
    );
END;
