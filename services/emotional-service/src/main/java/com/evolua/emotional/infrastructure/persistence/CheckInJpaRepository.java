package com.evolua.emotional.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CheckInJpaRepository
    extends JpaRepository<CheckInEntity, Long>, JpaSpecificationExecutor<CheckInEntity> {}
