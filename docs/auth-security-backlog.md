# Auth security backlog

## Rate limit and brute force protection

Status: pending infrastructure decision.

The auth-service currently normalizes failed login responses so the client does
not reveal whether an email exists. Full rate limiting is intentionally not
implemented in this change because there is no shared limiter, gateway policy,
or distributed counter in place yet.

Before production, add and test a rate-limit layer for authentication endpoints,
covering at least:

- repeated failed login attempts by IP and account identifier;
- OAuth start/callback abuse protection;
- friendly `429` responses with no sensitive account details;
- metrics/logging without storing raw passwords or tokens.
