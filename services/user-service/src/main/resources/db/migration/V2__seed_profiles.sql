DELETE FROM profiles
WHERE user_id = 'seed-user'
  AND display_name = 'seed-display_name'
  AND bio = 'seed-bio';

INSERT INTO profiles (user_id, display_name, bio, journey_level, premium, created_at)
SELECT 'clara-rocha', 'Clara Rocha', 'Construindo constancia com mais calma, sono melhor e pequenos rituais de cuidado.', 3, true, TIMESTAMPTZ '2026-03-20T08:30:00Z'
WHERE NOT EXISTS (
    SELECT 1
    FROM profiles
    WHERE user_id = 'clara-rocha'
);
