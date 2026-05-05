CREATE TABLE user_privacy_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL UNIQUE,
    private_journal BOOLEAN NOT NULL,
    hide_social_check_ins BOOLEAN NOT NULL,
    allow_history_insights BOOLEAN NOT NULL,
    use_emotional_data_for_ai BOOLEAN NOT NULL,
    daily_reminders BOOLEAN NOT NULL,
    content_preferences BOOLEAN NOT NULL,
    ai_tone VARCHAR(32) NOT NULL,
    suggestion_frequency VARCHAR(32) NOT NULL,
    trail_style VARCHAR(32) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
