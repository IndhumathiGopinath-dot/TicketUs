-- =============================================================
-- Ticket System Database Schema (MySQL)
-- Note: JPA (ddl-auto=update) will create/update these tables
-- automatically when the Spring Boot app starts. This file is
-- provided for reference and manual setup if preferred.
-- =============================================================

CREATE DATABASE IF NOT EXISTS ticket_system
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ticket_system;

-- Users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    department VARCHAR(100),
    created_at DATETIME
);

-- Tickets
CREATE TABLE IF NOT EXISTS tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    is_confidential BOOLEAN DEFAULT FALSE,
    request_type VARCHAR(100),
    os_info VARCHAR(100),
    browser_info VARCHAR(100),
    app_version VARCHAR(100),
    severity VARCHAR(20),
    asset_tag VARCHAR(100),
    created_by BIGINT NOT NULL,
    assigned_to BIGINT,
    estimated_resolution_hours INT,
    resolved_at DATETIME,
    satisfaction_rating INT,
    is_escalated BOOLEAN DEFAULT FALSE,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (assigned_to) REFERENCES users(id)
);

-- Timeline
CREATE TABLE IF NOT EXISTS ticket_timeline (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    notes TEXT,
    actor_id BIGINT,
    created_at DATETIME,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (actor_id) REFERENCES users(id)
);

-- Attachments
CREATE TABLE IF NOT EXISTS attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    uploaded_at DATETIME,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE
);

-- Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message VARCHAR(500) NOT NULL,
    ticket_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Knowledge base
CREATE TABLE IF NOT EXISTS knowledge_articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(20),
    keywords VARCHAR(500)
);

-- Related tickets (many-to-many)
CREATE TABLE IF NOT EXISTS ticket_links (
    ticket_id BIGINT NOT NULL,
    related_ticket_id BIGINT NOT NULL,
    PRIMARY KEY (ticket_id, related_ticket_id),
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (related_ticket_id) REFERENCES tickets(id) ON DELETE CASCADE
);
