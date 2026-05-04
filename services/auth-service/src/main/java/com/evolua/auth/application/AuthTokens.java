package com.evolua.auth.application;

import com.evolua.auth.domain.AuthUser;

public record AuthTokens(String accessToken, String refreshToken, AuthUser user) {}
