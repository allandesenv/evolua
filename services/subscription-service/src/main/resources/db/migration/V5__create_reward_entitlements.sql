CREATE TABLE IF NOT EXISTS reward_entitlements (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    entitlement_type VARCHAR(64) NOT NULL,
    source_reward_session_id BIGINT,
    status VARCHAR(64) NOT NULL,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reward_entitlements_active
    ON reward_entitlements(user_id, entitlement_type, status, expires_at);

CREATE UNIQUE INDEX IF NOT EXISTS uq_reward_entitlement_daily_mentor_pass
    ON reward_entitlements(user_id, entitlement_type, ((starts_at AT TIME ZONE 'UTC')::date))
    WHERE entitlement_type = 'MENTOR_PREMIUM_PASS';
