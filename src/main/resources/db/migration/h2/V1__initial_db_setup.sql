-- ============================================================
-- H2 COMPATIBLE SCHEMA FOR VISITOR MANAGEMENT SYSTEM
-- ============================================================

-- Create roles table
CREATE TABLE IF NOT EXISTS tbl_role(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_name ON tbl_role(name);

-- Create users table
CREATE TABLE IF NOT EXISTS tbl_user(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    invitation_token VARCHAR(255),
    invitation_sent_at TIMESTAMP,
    invitation_accepted_at TIMESTAMP,
    last_login TIMESTAMP,
    failed_attempts INT DEFAULT 0,
    enabled BOOLEAN DEFAULT FALSE,
    locked BOOLEAN DEFAULT FALSE,
    expired BOOLEAN DEFAULT FALSE,
    reset_token VARCHAR(255),          
    reset_token_sent_at TIMESTAMP,     
    reset_token_accepted_at TIMESTAMP, 
    force_password_change BOOLEAN DEFAULT FALSE,
    locked_at TIMESTAMP,
    enabled_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES tbl_role(id)
);

-- Create indexes for user table
CREATE INDEX idx_email ON tbl_user(email);
CREATE INDEX idx_enabled ON tbl_user(enabled);
CREATE INDEX idx_invitation_token ON tbl_user(invitation_token);
CREATE INDEX idx_reset_token ON tbl_user(reset_token);
CREATE INDEX idx_user_role_enabled ON tbl_user(role_id, enabled);

-- Create visitors table
CREATE TABLE IF NOT EXISTS tbl_visitor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    national_id VARCHAR(50) NOT NULL,
    gender VARCHAR(50) NOT NULL,
    phone_number VARCHAR(35) NOT NULL,
    email VARCHAR(200) NOT NULL,
    additional_details TEXT,
    ip_address VARCHAR(255) NOT NULL,
    user_agent VARCHAR(300),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for visitor table
CREATE INDEX idx_first_name ON tbl_visitor(first_name);
CREATE INDEX idx_last_name ON tbl_visitor(last_name);
CREATE INDEX idx_created_at ON tbl_visitor(created_at);
CREATE INDEX idx_visitor_national_id ON tbl_visitor(national_id);
CREATE INDEX idx_visitor_email ON tbl_visitor(email);
CREATE INDEX idx_visitor_phone ON tbl_visitor(phone_number);
CREATE INDEX idx_visitor_name_composite ON tbl_visitor(first_name, last_name);

-- Insert default roles (H2 compatible)
INSERT INTO tbl_role (name, description) 
SELECT 'ADMIN', 'Full system access - can manage users, visitors, and system settings'
WHERE NOT EXISTS (SELECT 1 FROM tbl_role WHERE name = 'ADMIN');

INSERT INTO tbl_role (name, description) 
SELECT 'MANAGER', 'Can view and manage visitors, but cannot manage users'
WHERE NOT EXISTS (SELECT 1 FROM tbl_role WHERE name = 'MANAGER');

INSERT INTO tbl_role (name, description) 
SELECT 'VIEWER', 'Read-only access to view visitors'
WHERE NOT EXISTS (SELECT 1 FROM tbl_role WHERE name = 'VIEWER');