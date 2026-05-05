ALTER TABLE auth_users
    ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE';

UPDATE auth_users
SET status = 'ACTIVE'
WHERE status IS NULL OR status = '';
