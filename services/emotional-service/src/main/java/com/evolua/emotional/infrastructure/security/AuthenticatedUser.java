package com.evolua.emotional.infrastructure.security; import java.util.List; public record AuthenticatedUser(String userId, String email, List<String> roles) { }
