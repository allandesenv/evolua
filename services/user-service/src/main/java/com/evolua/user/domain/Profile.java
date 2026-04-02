package com.evolua.user.domain; import java.time.Instant; public record Profile(Long id, String userId, String displayName, String bio, Integer journeyLevel, Boolean premium, Instant createdAt) { }
