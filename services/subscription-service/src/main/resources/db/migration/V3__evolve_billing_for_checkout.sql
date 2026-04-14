ALTER TABLE subscriptions
    ADD COLUMN IF NOT EXISTS provider VARCHAR(64),
    ADD COLUMN IF NOT EXISTS provider_customer_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS provider_payment_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS provider_subscription_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS current_period_ends_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS canceled_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

UPDATE subscriptions
SET provider = COALESCE(provider, 'MANUAL'),
    updated_at = COALESCE(updated_at, created_at)
WHERE provider IS NULL
   OR updated_at IS NULL;

CREATE TABLE IF NOT EXISTS billing_checkouts (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(64) NOT NULL UNIQUE,
    user_id VARCHAR(64) NOT NULL,
    plan_code VARCHAR(255) NOT NULL,
    billing_cycle VARCHAR(64) NOT NULL,
    provider VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL,
    premium BOOLEAN NOT NULL,
    provider_preference_id VARCHAR(255),
    provider_payment_id VARCHAR(255),
    checkout_url TEXT,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    confirmed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS billing_events (
    id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(64) NOT NULL,
    provider_event_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    checkout_public_id VARCHAR(64),
    payload_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_billing_events_provider_event
    ON billing_events(provider, provider_event_id);
