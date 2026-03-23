package com.ie23s.bukkit.plugin.powerclans.database;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * MySQL {@link ConnectionProvider} backed by a HikariCP connection pool.
 * Configuration is read from {@code config.yml} under the {@code mysql.*} keys.
 */
class MySQL implements ConnectionProvider {

    private final Core core;
    private final HikariDataSource dataSource;

    MySQL(Core core) {
        this.core = core;
        this.dataSource = buildDataSource();
    }

    private HikariDataSource buildDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://"
                + core.getConfig().getString("mysql.host") + ":"
                + core.getConfig().getString("mysql.port") + "/"
                + core.getConfig().getString("mysql.database")
                + "?useUnicode=true&characterEncoding=UTF-8");
        config.setUsername(core.getConfig().getString("mysql.username"));
        config.setPassword(core.getConfig().getString("mysql.password"));
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30_000);
        config.setPoolName("PowerClans-MySQL");
        return new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to acquire MySQL connection", e);
        }
    }

    /** Shuts down the connection pool. Should be called on plugin disable. */
    void disconnect() {
        if (!dataSource.isClosed()) dataSource.close();
    }
}
