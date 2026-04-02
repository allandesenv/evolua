package com.evolua.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProfileJpaRepository
    extends JpaRepository<ProfileEntity, Long>, JpaSpecificationExecutor<ProfileEntity> {}
