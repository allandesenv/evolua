CREATE TABLE IF NOT EXISTS ai_usage_ledger (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    resource VARCHAR(64) NOT NULL,
    usage_date DATE NOT NULL,
    base_used INTEGER NOT NULL DEFAULT 0,
    reward_used INTEGER NOT NULL DEFAULT 0,
    reward_granted INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_ai_usage_user_resource_date
    ON ai_usage_ledger(user_id, resource, usage_date);

CREATE TABLE IF NOT EXISTS ad_reward_sessions (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(64) NOT NULL UNIQUE,
    user_id VARCHAR(64) NOT NULL,
    provider VARCHAR(64) NOT NULL,
    reward_type VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL,
    provider_transaction_id VARCHAR(255),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    granted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ad_reward_user_status
    ON ad_reward_sessions(user_id, reward_type, status);

CREATE UNIQUE INDEX IF NOT EXISTS uq_ad_reward_provider_transaction
    ON ad_reward_sessions(user_id, reward_type, provider_transaction_id)
    WHERE provider_transaction_id IS NOT NULL;
