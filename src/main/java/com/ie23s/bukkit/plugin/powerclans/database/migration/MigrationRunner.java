package com.ie23s.bukkit.plugin.powerclans.database.migration;

import com.ie23s.bukkit.plugin.powerclans.database.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Versioned schema migration runner.
 * Loads SQL scripts from {@code /db/{dialect}/V{n}.sql} on the classpath and applies
 * them in order, tracking the current version in the {@code db_meta} table.
 *
 * <p>To add a new migration: create {@code V{n}.sql} for each dialect and add
 * {@code if (version < n) applyVersion(n);} inside {@link #migrate()}.
 */
public class MigrationRunner {

    private static final String VERSION_KEY = "schema_version";

    private final Connection connection;
    private final String dialect;

    /**
     * @param connection active database connection
     * @param dialect    database dialect, e.g. {@code "mysql"} or {@code "sqlite"}.
     *                   Used to locate migration scripts under {@code /db/{dialect}/}.
     */
    public MigrationRunner(Connection connection, String dialect) {
        this.connection = connection;
        this.dialect = dialect;
    }

    public void migrate() throws SQLException {
        createMetaTable();
        int version = getVersion();
        if (version < 1) applyVersion(1);
        if (version < 2) applyVersion(2);
    }

    // ── Migration execution ───────────────────────────────────────────────────

    private void applyVersion(int version) throws SQLException {
        executeScript(loadScript(version));
        setVersion(version);
    }

    private void executeScript(String sql) throws SQLException {
        for (String statement : sql.split(";")) {
            String trimmed = statement.strip();
            if (!trimmed.isEmpty()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }

    private String loadScript(int version) {
        String path = "/db/" + dialect + "/V" + version + ".sql";
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new DatabaseException("Migration script not found: " + path, null);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DatabaseException("Failed to read migration script: " + path, e);
        }
    }

    // ── Meta table ───────────────────────────────────────────────────────────

    private void createMetaTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS `db_meta` (
                        `key`   VARCHAR(255) PRIMARY KEY,
                        `value` VARCHAR(255) NOT NULL
                    )""");
        }
    }

    private int getVersion() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT value FROM db_meta WHERE `key`=?")) {
            ps.setString(1, VERSION_KEY);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Integer.parseInt(rs.getString("value")) : 0;
            }
        }
    }

    private void setVersion(int version) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE db_meta SET value=? WHERE `key`=?")) {
            update.setString(1, String.valueOf(version));
            update.setString(2, VERSION_KEY);
            if (update.executeUpdate() == 0) {
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO db_meta (`key`, value) VALUES (?, ?)")) {
                    insert.setString(1, VERSION_KEY);
                    insert.setString(2, String.valueOf(version));
                    insert.executeUpdate();
                }
            }
        }
    }
}
