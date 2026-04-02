CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    plan_code VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    billing_cycle VARCHAR(255) NOT NULL,
    premium BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO subscriptions (user_id, plan_code, status, billing_cycle, premium, created_at) VALUES ('seed-user', 'seed-plan_code', 'seed-status', 'seed-billing_cycle', true, CURRENT_TIMESTAMP);


