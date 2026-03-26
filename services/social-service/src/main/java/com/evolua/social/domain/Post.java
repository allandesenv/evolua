package com.evolua.social.domain; import java.time.Instant; public record Post(String id, String userId, String content, String community, String visibility, Instant createdAt) { }
