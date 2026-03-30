package com.evolua.auth.config;

import com.evolua.auth.domain.AuthUser;
import com.evolua.auth.domain.AuthUserRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class LocalSeedConfig {
  @Bean
  ApplicationRunner authSeedRunner(AuthUserRepository repository, PasswordEncoder passwordEncoder) {
    return args -> {
      seedUser(
          repository,
          passwordEncoder,
          "clara-rocha",
          "clara@evolua.local",
          List.of("ROLE_ADMIN", "ROLE_PREMIUM", "ROLE_USER"));
      seedUser(
          repository,
          passwordEncoder,
          "leo-respiro",
          "leo@evolua.local",
          List.of("ROLE_USER"));
    };
  }

  private void seedUser(
      AuthUserRepository repository,
      PasswordEncoder passwordEncoder,
      String userId,
      String email,
      List<String> roles) {
    var existingUser = repository.findByUserId(userId).orElse(repository.findByEmail(email).orElse(null));
    if (existingUser != null) {
      if (!existingUser.roles().equals(roles)) {
        repository.save(
            new AuthUser(
                existingUser.id(),
                existingUser.userId(),
                existingUser.email(),
                existingUser.passwordHash(),
                roles,
                existingUser.createdAt()));
      }
      return;
    }

    repository.save(
        new AuthUser(
            null,
            userId,
            email,
            passwordEncoder.encode("123456"),
            roles,
            Instant.now()));
  }
}
