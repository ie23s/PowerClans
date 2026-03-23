package com.ie23s.bukkit.plugin.powerclans.database;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQLite {@link ConnectionProvider} backed by a HikariCP connection pool.
 * Pool size is capped at 1 because SQLite does not support concurrent writes.
 */
public class SQLite implements ConnectionProvider {

    private final Core core;
    private final HikariDataSource dataSource;

    SQLite(Core core, String dbName) {
        this.core = core;
        this.dataSource = buildDataSource(dbName);
    }

    private HikariDataSource buildDataSource(String dbName) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite://" + core.getDataFolder().getAbsolutePath() + "/" + dbName + ".db");
        config.setMaximumPoolSize(1);       // SQLite не поддерживает конкурентную запись
        config.setConnectionTimeout(30_000);
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("PowerClans-SQLite");
        return new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to acquire SQLite connection", e);
        }
    }

    /** Shuts down the connection pool. Should be called on plugin disable. */
    void disconnect() {
        if (!dataSource.isClosed()) dataSource.close();
    }
}
