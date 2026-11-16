package com.serenity.shared.logging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.logging.database.cleanup", name = "enabled", havingValue = "true")
public class AppLogRetentionScheduler {

    private final JdbcTemplate jdbcTemplate;
    private final int retentionDays;

    public AppLogRetentionScheduler(
            JdbcTemplate jdbcTemplate,
            org.springframework.core.env.Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.retentionDays = Math.max(1, environment.getProperty(
                "app.logging.database.cleanup.retention-days", Integer.class, 30));
    }

    @Scheduled(cron = "0 15 3 * * *")
    public void deleteExpiredLogs() {
        jdbcTemplate.update("DELETE FROM app_logs WHERE created_at < now() - (? * interval '1 day')", retentionDays);
    }
}
