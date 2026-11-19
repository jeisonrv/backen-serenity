package com.serenity.shared.logging;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppLogJpaRepository extends JpaRepository<AppLogJpaEntity, Long> {
}
