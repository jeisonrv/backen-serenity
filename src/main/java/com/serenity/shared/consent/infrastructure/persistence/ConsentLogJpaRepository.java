package com.serenity.shared.consent.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsentLogJpaRepository extends JpaRepository<ConsentLogJpaEntity, Long> {
}
