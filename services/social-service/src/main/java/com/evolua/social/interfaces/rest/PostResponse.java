package com.evolua.social.interfaces.rest; import java.time.Instant; public record PostResponse(String id, String userId, String content, String community, String visibility, Instant createdAt) { }
