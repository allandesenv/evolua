package com.evolua.chat.domain; import java.time.Instant; public record Message(String id, String userId, String recipientId, String content, Instant createdAt) { }
