package com.serenity.shared.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/** Persists log events on a dedicated worker, keeping database I/O off application threads. */
public class AsyncDatabaseLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static final String INSERT_SQL = "INSERT INTO app_logs "
            + "(created_at, level, logger_name, thread_name, message, stack_trace, "
            + "application_name, hostname, trace_id, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String APPENDER_PACKAGE = "com.serenity.shared.logging.";

    private String jdbcUrl;
    private String username;
    private String password;
    private String applicationName = "serenity";
    private String minimumLevel = "WARN";
    private boolean enabled = true;
    private int batchSize = 100;
    private int flushIntervalMs = 2000;
    private int queueCapacity = 10000;
    private int maxMessageLength = 10000;
    private int maxStackTraceLength = 50000;
    private int maxRetries = 3;
    private int shutdownWaitMs = 5000;

    private ArrayBlockingQueue<LogRecord> queue;
    private volatile boolean accepting;
    private Thread worker;
    private Level threshold;
    private String hostname = "unknown";

    @Override
    public void start() {
        threshold = Level.toLevel(minimumLevel, Level.WARN);
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
            // Host name is optional metadata.
        }
        queue = new ArrayBlockingQueue<>(Math.max(1, queueCapacity));
        accepting = true;
        worker = new Thread(this::processQueue, "database-log-appender");
        worker.setDaemon(true);
        worker.start();
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!enabled || !accepting || event.getLevel().toInt() < threshold.toInt() || isExcluded(event.getLoggerName())) {
            return;
        }
        event.prepareForDeferredProcessing();
        Map<String, String> mdc = event.getMDCPropertyMap();
        String stackTrace = event.getThrowableProxy() instanceof ThrowableProxy throwableProxy
                ? ThrowableProxyUtil.asString(throwableProxy) : null;
        LogRecord record = new LogRecord(
                Instant.ofEpochMilli(event.getTimeStamp()),
                event.getLevel().toString(),
                truncate(event.getLoggerName(), 512),
                truncate(event.getThreadName(), 256),
                truncate(event.getFormattedMessage(), maxMessageLength),
                truncate(stackTrace, maxStackTraceLength),
                truncate(applicationName, 128),
                truncate(hostname, 255),
                truncate(mdc.get("trace_id"), 128),
                truncate(mdc.get("user_id"), 128));
        // Never block a request thread; a full queue drops the newest event silently.
        queue.offer(record);
    }

    private boolean isExcluded(String loggerName) {
        return loggerName == null
                || loggerName.startsWith(APPENDER_PACKAGE)
                || loggerName.startsWith("org.hibernate.")
                || loggerName.startsWith("org.springframework.jdbc.")
                || loggerName.startsWith("org.springframework.orm.jpa.")
                || loggerName.startsWith("java.sql.")
                || loggerName.startsWith("org.postgresql.")
                || loggerName.startsWith("com.zaxxer.hikari.");
    }

    private void processQueue() {
        while (accepting || !queue.isEmpty()) {
            try {
                LogRecord first = queue.poll(Math.max(1, flushIntervalMs), TimeUnit.MILLISECONDS);
                if (first == null) {
                    continue;
                }
                List<LogRecord> batch = new ArrayList<>(Math.max(1, batchSize));
                batch.add(first);
                queue.drainTo(batch, Math.max(0, batchSize - 1));
                persistWithRetries(batch);
            } catch (InterruptedException interrupted) {
                if (accepting) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void persistWithRetries(List<LogRecord> batch) {
        int attempts = Math.max(1, maxRetries + 1);
        for (int attempt = 0; attempt < attempts; attempt++) {
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                 PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
                for (LogRecord record : batch) {
                    statement.setTimestamp(1, Timestamp.from(record.createdAt()));
                    statement.setString(2, record.level());
                    statement.setString(3, record.loggerName());
                    statement.setString(4, record.threadName());
                    statement.setString(5, record.message());
                    statement.setString(6, record.stackTrace());
                    statement.setString(7, record.applicationName());
                    statement.setString(8, record.hostname());
                    statement.setString(9, record.traceId());
                    statement.setString(10, record.userId());
                    statement.addBatch();
                }
                statement.executeBatch();
                return;
            } catch (Exception ignored) {
                // Deliberately silent: logging database failures must not recurse or affect the app.
                if (attempt + 1 < attempts) {
                    try {
                        Thread.sleep(Math.min(1000L, 100L * (attempt + 1)));
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || maxLength < 1 || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    @Override
    public void stop() {
        accepting = false;
        if (worker != null) {
            worker.interrupt();
            try {
                worker.join(Math.max(0, shutdownWaitMs));
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }
        super.stop();
    }

    public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setApplicationName(String applicationName) { this.applicationName = applicationName; }
    public void setMinimumLevel(String minimumLevel) { this.minimumLevel = minimumLevel; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    public void setFlushIntervalMs(int flushIntervalMs) { this.flushIntervalMs = flushIntervalMs; }
    public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }
    public void setMaxMessageLength(int maxMessageLength) { this.maxMessageLength = maxMessageLength; }
    public void setMaxStackTraceLength(int maxStackTraceLength) { this.maxStackTraceLength = maxStackTraceLength; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public void setShutdownWaitMs(int shutdownWaitMs) { this.shutdownWaitMs = shutdownWaitMs; }

    private record LogRecord(Instant createdAt, String level, String loggerName, String threadName,
                             String message, String stackTrace, String applicationName, String hostname,
                             String traceId, String userId) { }
}
