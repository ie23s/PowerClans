package com.ie23s.bukkit.plugin.powerclans.database;

import com.ie23s.bukkit.plugin.powerclans.database.migration.MigrationRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Base class for database tests.
 * Uses a named SQLite in-memory database so multiple connections share the same data.
 * A "keep-alive" connection holds the database open for the duration of each test.
 */
public abstract class BaseDbTest {

    /** Keep-alive connection that prevents the in-memory DB from being destroyed. */
    protected Connection connection;

    /**
     * Provider that opens a fresh connection to the same named in-memory DB on each call.
     * Each connection is independently closeable without destroying the shared data.
     */
    protected ConnectionProvider provider;

    private String dbUrl;

    @BeforeEach
    void setUpDb() throws Exception {
        Class.forName("org.sqlite.JDBC");
        // Unique name per test so parallel tests don't interfere
        dbUrl = "jdbc:sqlite:file:" + UUID.randomUUID() + "?mode=memory&cache=shared";
        connection = DriverManager.getConnection(dbUrl);
        provider = () -> {
            try {
                return DriverManager.getConnection(dbUrl);
            } catch (SQLException e) {
                throw new DatabaseException("Failed to open test connection", e);
            }
        };
        new MigrationRunner(connection, "sqlite").migrate();
    }

    @AfterEach
    void tearDownDb() throws SQLException {
        if (connection != null && !connection.isClosed()) connection.close();
    }
}
