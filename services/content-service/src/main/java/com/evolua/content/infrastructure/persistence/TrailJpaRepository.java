package com.evolua.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TrailJpaRepository
    extends JpaRepository<TrailEntity, Long>, JpaSpecificationExecutor<TrailEntity> {}
