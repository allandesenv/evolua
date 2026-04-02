CREATE TABLE check_ins (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    mood VARCHAR(255) NOT NULL,
    reflection VARCHAR(255) NOT NULL,
    energy_level INTEGER NOT NULL,
    recommended_practice VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO check_ins (user_id, mood, reflection, energy_level, recommended_practice, created_at) VALUES ('seed-user', 'seed-mood', 'seed-reflection', 1, 'seed-recommended_practice', CURRENT_TIMESTAMP);


