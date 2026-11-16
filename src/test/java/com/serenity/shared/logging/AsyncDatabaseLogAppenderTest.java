package com.serenity.shared.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AsyncDatabaseLogAppenderTest {

    @Test
    void databaseUnavailableDoesNotPropagateOrBlockCaller() {
        LoggerContext context = new LoggerContext();
        AsyncDatabaseLogAppender appender = new AsyncDatabaseLogAppender();
        appender.setContext(context);
        appender.setJdbcUrl("jdbc:invalid:database-down");
        appender.setUsername("");
        appender.setPassword("");
        appender.setMaxRetries(1);
        appender.setFlushIntervalMs(1);
        appender.start();

        LoggingEvent event = new LoggingEvent();
        event.setLoggerName("com.serenity.test.BusinessService");
        event.setLevel(Level.ERROR);
        event.setMessage("expected database failure");
        event.setThreadName(Thread.currentThread().getName());
        event.setTimeStamp(System.currentTimeMillis());

        long startedAt = System.nanoTime();
        assertDoesNotThrow(() -> appender.doAppend(event));
        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;

        appender.stop();
        context.stop();
        org.junit.jupiter.api.Assertions.assertTrue(elapsedMillis < 100,
                "enqueue should not wait for database access");
    }

    @Test
    void ignoresDatabaseInfrastructureLogs() {
        LoggerContext context = new LoggerContext();
        AsyncDatabaseLogAppender appender = new AsyncDatabaseLogAppender();
        appender.setContext(context);
        appender.setJdbcUrl("jdbc:invalid:database-down");
        appender.setMaxRetries(0);
        appender.start();

        LoggingEvent event = new LoggingEvent();
        event.setLoggerName("org.hibernate.SQL");
        event.setLevel(Level.ERROR);
        event.setMessage("excluded");
        event.setThreadName("test");
        event.setTimeStamp(System.currentTimeMillis());

        assertDoesNotThrow(() -> appender.doAppend(event));
        appender.stop();
        context.stop();
    }
}
