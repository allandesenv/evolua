CREATE TABLE feedback_submissions (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL,
    working_well TEXT,
    could_improve TEXT,
    confusing_or_hard TEXT,
    helped_how TEXT,
    feature_suggestion TEXT,
    content_suggestion TEXT,
    visual_suggestion TEXT,
    ai_suggestion TEXT,
    problem_what_happened TEXT,
    problem_where TEXT,
    problem_can_repeat TEXT,
    rating VARCHAR(40),
    rating_comment TEXT,
    screenshot_file_name VARCHAR(255),
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_feedback_submissions_user_id ON feedback_submissions (user_id);
CREATE INDEX idx_feedback_submissions_status ON feedback_submissions (status);
