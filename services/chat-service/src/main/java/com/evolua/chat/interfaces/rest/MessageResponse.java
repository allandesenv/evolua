package com.evolua.chat.interfaces.rest; import java.time.Instant; public record MessageResponse(String id, String userId, String recipientId, String content, Instant createdAt) { }
