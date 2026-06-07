-- Purge existing links (no owner to associate them with).
-- Acceptable for a pre-production system.
DELETE FROM short_links;

-- Users table
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

-- Add ownership FK to short_links
ALTER TABLE short_links
    ADD COLUMN user_id BIGINT NOT NULL REFERENCES users(id);

CREATE INDEX idx_short_links_user_id ON short_links(user_id);
