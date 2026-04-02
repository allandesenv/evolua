CREATE TABLE profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    bio VARCHAR(255) NOT NULL,
    journey_level INTEGER NOT NULL,
    premium BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO profiles (user_id, display_name, bio, journey_level, premium, created_at) VALUES ('seed-user', 'seed-display_name', 'seed-bio', 1, true, CURRENT_TIMESTAMP);


