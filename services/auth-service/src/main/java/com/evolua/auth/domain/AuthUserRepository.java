package com.evolua.auth.domain;

import java.util.Optional;

public interface AuthUserRepository {
  AuthUser save(AuthUser user);

  Optional<AuthUser> findByEmail(String email);

  Optional<AuthUser> findByUserId(String userId);

  Optional<AuthUser> findByProviderAndProviderSubject(String provider, String providerSubject);
}
