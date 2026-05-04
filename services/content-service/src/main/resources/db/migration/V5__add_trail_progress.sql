CREATE TABLE IF NOT EXISTS trail_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    trail_id BIGINT NOT NULL REFERENCES trails(id) ON DELETE CASCADE,
    current_step_index INTEGER NOT NULL DEFAULT 0,
    completed_step_indexes TEXT NOT NULL DEFAULT '[]',
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_trail_progress_user_trail UNIQUE (user_id, trail_id)
);

CREATE INDEX IF NOT EXISTS idx_trail_progress_user_id ON trail_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_trail_progress_trail_id ON trail_progress(trail_id);
