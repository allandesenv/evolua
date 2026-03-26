package com.evolua.content.domain; import java.time.Instant; public record Trail(Long id, String userId, String title, String description, String category, Boolean premium, Instant createdAt) { }
