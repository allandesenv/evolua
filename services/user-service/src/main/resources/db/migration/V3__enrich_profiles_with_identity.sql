ALTER TABLE profiles
    ALTER COLUMN bio DROP NOT NULL;

ALTER TABLE profiles
    ADD COLUMN birth_date DATE,
    ADD COLUMN gender VARCHAR(16),
    ADD COLUMN custom_gender VARCHAR(64),
    ADD COLUMN avatar_url VARCHAR(1024);

CREATE UNIQUE INDEX IF NOT EXISTS uk_profiles_user_id
    ON profiles (user_id);
