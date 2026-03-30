DELETE FROM subscriptions
WHERE user_id = 'seed-user'
  AND plan_code = 'seed-plan_code'
  AND status = 'seed-status'
  AND billing_cycle = 'seed-billing_cycle';

INSERT INTO subscriptions (user_id, plan_code, status, billing_cycle, premium, created_at)
SELECT 'clara-rocha', 'evolua-plus', 'ACTIVE', 'MONTHLY', true, TIMESTAMPTZ '2026-03-20T09:00:00Z'
WHERE NOT EXISTS (
    SELECT 1
    FROM subscriptions
    WHERE user_id = 'clara-rocha'
      AND plan_code = 'evolua-plus'
);

INSERT INTO subscriptions (user_id, plan_code, status, billing_cycle, premium, created_at)
SELECT 'leo-respiro', 'evolua-free', 'ACTIVE', 'MONTHLY', false, TIMESTAMPTZ '2026-03-21T10:05:00Z'
WHERE NOT EXISTS (
    SELECT 1
    FROM subscriptions
    WHERE user_id = 'leo-respiro'
      AND plan_code = 'evolua-free'
);
