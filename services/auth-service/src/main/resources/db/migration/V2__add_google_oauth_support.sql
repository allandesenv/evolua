ALTER TABLE auth_users
    ADD COLUMN provider VARCHAR(32) NOT NULL DEFAULT 'LOCAL',
    ADD COLUMN provider_subject VARCHAR(255),
    ADD COLUMN display_name VARCHAR(255),
    ADD COLUMN avatar_url VARCHAR(1024);

CREATE UNIQUE INDEX IF NOT EXISTS uk_auth_users_provider_subject
    ON auth_users (provider, provider_subject);

CREATE TABLE auth_oauth_login_states (
    id BIGSERIAL PRIMARY KEY,
    state VARCHAR(128) NOT NULL UNIQUE,
    frontend_redirect_uri VARCHAR(1024) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE auth_authorization_codes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(128) NOT NULL UNIQUE,
    user_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed BOOLEAN NOT NULL DEFAULT FALSE
);
