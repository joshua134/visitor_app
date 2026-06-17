-- src/main/resources/db/migration/V1__initial_db_setup.sql

CREATE TABLE IF NOT EXISTS tbl_role(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_name ON tbl_role(name);

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
    
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES tbl_role(id),
    
    INDEX idx_email(email),
    INDEX idx_enabled(enabled),
    INDEX idx_invitation_token (invitation_token),
    INDEX idx_reset_token(reset_token)
);

-- Active optimization index for user login validation checks
CREATE INDEX idx_user_role_enabled ON tbl_user(role_id, enabled);

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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_first_name (first_name),
    INDEX idx_last_name (last_name),
    INDEX idx_created_at (created_at)
);

CREATE INDEX idx_visitor_national_id ON tbl_visitor(national_id);
CREATE INDEX idx_visitor_email ON tbl_visitor(email);
CREATE INDEX idx_visitor_phone ON tbl_visitor(phone_number);
CREATE INDEX idx_visitor_name_composite ON tbl_visitor(first_name, last_name);


INSERT IGNORE INTO tbl_role (name, description) VALUES 
('ADMIN', 'Full system access - can manage users, visitors, and system settings'),
('MANAGER', 'Can view and manage visitors, but cannot manage users'),
('VIEWER', 'Read-only access to view visitors');