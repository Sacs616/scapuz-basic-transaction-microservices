-- V1__create_client_table.sql
-- Initial schema for clients table

CREATE TABLE IF NOT EXISTS clients (
    client_id UUID PRIMARY KEY,
    client_code VARCHAR(20) NOT NULL UNIQUE, -- Added missing column
    identification VARCHAR(15) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    genre VARCHAR(1) NOT NULL,
    birth_date DATE,
    address VARCHAR(500),
    phone VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    CONSTRAINT chk_genre CHECK (genre IN ('M', 'F', 'O')) -- Optional: add genre validation
);

-- Create indexes for better query performance
CREATE INDEX idx_client_code ON clients(client_code);
CREATE INDEX idx_created_at ON clients(created_at DESC);
CREATE INDEX idx_identification ON clients(identification); -- Consider adding this too