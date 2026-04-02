package com.evolua.notification.infrastructure.security; import java.util.List; public record AuthenticatedUser(String userId, String email, List<String> roles) { }
