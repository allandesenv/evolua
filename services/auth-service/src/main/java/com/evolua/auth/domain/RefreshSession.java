package com.evolua.auth.domain; import java.time.Instant; public record RefreshSession(Long id, String userId, String refreshToken, Instant createdAt, boolean revoked) { }
