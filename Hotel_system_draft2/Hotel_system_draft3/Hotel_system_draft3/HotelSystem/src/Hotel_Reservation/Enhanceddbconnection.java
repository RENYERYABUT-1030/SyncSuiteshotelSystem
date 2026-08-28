package Hotel_Reservation.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Enhanced Database Connection Manager with HikariCP Connection Pooling
 * 
 * Benefits:
 * - Connection pooling (reuses connections instead of creating new ones)
 * - Better performance (10-100x faster)
 * - Thread-safe
 * - Automatic connection validation
 * - Connection leak detection
 * 
 * Maven Dependencies:
 * <dependency>
 *     <groupId>com.zaxxer</groupId>
 *     <artifactId>HikariCP</artifactId>
 *     <version>5.0.1</version>
 * </dependency>
 * <dependency>
 *     <groupId>mysql</groupId>
 *     <artifactId>mysql-connector-java</artifactId>
 *     <version>8.0.33</version>
 * </dependency>
 */
public class EnhancedDBConnection {
    private static final Logger logger = Logger.getLogger(EnhancedDBConnection.class.getName());
    private static HikariDataSource dataSource;
    private static final Object lock = new Object();

    static {
        initializeConnectionPool();
    }

    /**
     * Initialize HikariCP connection pool with optimal settings
     */
    private static void initializeConnectionPool() {
        synchronized (lock) {
            if (dataSource != null) {
                return; // Already initialized
            }

            try {
                ConfigManager config = ConfigManager.getInstance();
                
                HikariConfig hikariConfig = new HikariConfig();
                hikariConfig.setJdbcUrl(config.getDatabaseUrl());
                hikariConfig.setUsername(config.getDatabaseUsername());
                hikariConfig.setPassword(config.getDatabasePassword());

                // Pool Configuration
                hikariConfig.setMaximumPoolSize(config.getConnectionPoolSize());
                hikariConfig.setMinimumIdle(5);
                hikariConfig.setConnectionTimeout(30000); // 30 seconds
                hikariConfig.setIdleTimeout(600000); // 10 minutes
                hikariConfig.setMaxLifetime(1800000); // 30 minutes

                // Performance & Validation
                hikariConfig.setAutoCommit(true);
                hikariConfig.setConnectionTestQuery("SELECT 1");
                hikariConfig.setLeakDetectionThreshold(60000); // 1 minute leak detection

                // Connection Properties
                hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
                hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
                hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
                hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
                hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
                hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
                hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
                hikariConfig.addDataSourceProperty("maintainTimeStats", "false");

                dataSource = new HikariDataSource(hikariConfig);
                logger.info("Connection pool initialized: " + hikariConfig.getMaximumPoolSize() + " connections");

            } catch (Exception e) {
                logger.severe("Failed to initialize connection pool: " + e.getMessage());
                throw new RuntimeException("Database connection pool initialization failed", e);
            }
        }
    }

    /**
     * Get a database connection from the pool
     * @return Connection from HikariCP pool
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            synchronized (lock) {
                if (dataSource == null) {
                    initializeConnectionPool();
                }
            }
        }

        try {
            Connection conn = dataSource.getConnection();
            logger.fine("Connection obtained from pool. Active connections: " + 
                       dataSource.getHikariPoolMXBean().getActiveConnections());
            return conn;
        } catch (SQLException e) {
            logger.severe("Failed to get connection from pool: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Test database connectivity
     * @return true if connection successful
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.warning("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get pool statistics
     * @return connection pool information
     */
    public static String getPoolStats() {
        if (dataSource == null) {
            return "Connection pool not initialized";
        }

        var mxBean = dataSource.getHikariPoolMXBean();
        return String.format(
            "Pool Stats - Total: %d, Active: %d, Idle: %d, Waiting: %d",
            mxBean.getTotalConnections(),
            mxBean.getActiveConnections(),
            mxBean.getIdleConnections(),
            mxBean.getThreadsAwaitingConnection()
        );
    }

    /**
     * Close the connection pool (should be called on application shutdown)
     */
    public static void closePool() {
        synchronized (lock) {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                logger.info("Connection pool closed");
            }
        }
    }

    /**
     * Get the underlying HikariDataSource
     * @return HikariDataSource instance
     */
    public static HikariDataSource getDataSource() {
        if (dataSource == null) {
            synchronized (lock) {
                if (dataSource == null) {
                    initializeConnectionPool();
                }
            }
        }
        return dataSource;
    }

    /**
     * Add shutdown hook to close pool on application exit
     */
    public static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down database connection pool...");
            closePool();
        }));
    }
}