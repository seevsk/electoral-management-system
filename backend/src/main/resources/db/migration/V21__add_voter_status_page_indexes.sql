-- V21: Performance indexes for the voter status page
-- Supports: filter by status, ORDER BY full_name, DNI search

-- Filter-only index: WHERE v.status = ?
CREATE INDEX IX_voters_status
    ON dbo.voters (status);

-- Composite covering index: WHERE status = ? ORDER BY full_name
-- Hot path for filtered + sorted paginated queries
CREATE INDEX IX_voters_status_fullname
    ON dbo.voters (status, full_name);

-- Sort-only index: ORDER BY full_name when no status filter
CREATE INDEX IX_voters_fullname
    ON dbo.voters (full_name);

-- DNI search index: covers JOIN and equality / prefix scans
CREATE INDEX IX_accounts_dni
    ON dbo.accounts (dni);
