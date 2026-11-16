package com.serenity.shared.logging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "app_logs")
public class AppLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false, length = 16)
    private String level;

    @Column(name = "logger_name", nullable = false, length = 512)
    private String loggerName;

    @Column(name = "thread_name", nullable = false, length = 256)
    private String threadName;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "stack_trace", columnDefinition = "text")
    private String stackTrace;

    @Column(name = "application_name", nullable = false, length = 128)
    private String applicationName;

    @Column(nullable = false, length = 255)
    private String hostname;

    @Column(name = "trace_id", length = 128)
    private String traceId;

    @Column(name = "user_id", length = 128)
    private String userId;

    protected AppLogJpaEntity() {
    }
}
