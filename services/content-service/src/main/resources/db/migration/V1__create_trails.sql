CREATE TABLE trails (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    premium BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO trails (user_id, title, description, category, premium, created_at) VALUES ('seed-user', 'seed-title', 'seed-description', 'seed-category', true, CURRENT_TIMESTAMP);


