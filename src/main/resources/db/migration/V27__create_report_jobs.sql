IF OBJECT_ID('dbo.report_jobs', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.report_jobs (
        id BIGINT IDENTITY(1,1) NOT NULL,
        report_type VARCHAR(50) NOT NULL,
        status VARCHAR(30) NOT NULL,
        requested_by VARCHAR(100) NULL,
        started_at DATETIME2 NULL,
        finished_at DATETIME2 NULL,
        total_rows INT NOT NULL CONSTRAINT DF_report_jobs_total_rows DEFAULT 0,
        processed_rows INT NOT NULL CONSTRAINT DF_report_jobs_processed_rows DEFAULT 0,
        progress_percentage DECIMAL(5,2) NOT NULL CONSTRAINT DF_report_jobs_progress_percentage DEFAULT 0,
        file_name VARCHAR(255) NULL,
        file_path VARCHAR(500) NULL,
        file_size_bytes BIGINT NULL,
        mime_type VARCHAR(150) NULL,
        error_message VARCHAR(MAX) NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_report_jobs_created_at DEFAULT SYSDATETIME(),
        updated_at DATETIME2 NULL,
        CONSTRAINT PK_report_jobs PRIMARY KEY (id)
    );
END;
