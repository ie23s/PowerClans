package com.ie23s.bukkit.plugin.powerclans.database.migration;

import com.ie23s.bukkit.plugin.powerclans.database.ConnectionProvider;
import com.ie23s.bukkit.plugin.powerclans.database.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * Versioned schema migration runner.
 * Loads SQL scripts from {@code /db/{dialect}/V{n}.sql} on the classpath and applies
 * them in order, tracking the current version in the {@code db_meta} table.
 *
 * <p>Each migration version obtains a fresh connection from the pool so that
 * SQLite JDBC statement-handle state from previous DDL operations does not interfere.
 *
 * <p>To add a pure-SQL migration: create {@code V{n}.sql} for each dialect and add
 * {@code if (version < n) applyVersion(n, javaSteps.get(n));} inside
 * {@link #migrate(Map)}.
 * To add a mixed SQL+Java migration: provide a {@link JavaMigrationStep} implementation
 * in the map passed to {@link #migrate(Map)}.
 */
public class MigrationRunner {

    private static final String VERSION_KEY = "schema_version";

    private final ConnectionProvider provider;
    private final String dialect;

    /**
     * @param provider connection provider (pool or factory)
     * @param dialect  database dialect, e.g. {@code "mysql"} or {@code "sqlite"}.
     *                 Used to locate migration scripts under {@code /db/{dialect}/}.
     */
    public MigrationRunner(ConnectionProvider provider, String dialect) {
        this.provider = provider;
        this.dialect = dialect;
    }

    /**
     * Runs all pending migrations without any Java steps.
     * Equivalent to {@code migrate(Map.of())}.
     */
    public void migrate() throws SQLException {
        migrate(Map.of());
    }

    /**
     * Runs all pending migrations, optionally executing a {@link JavaMigrationStep}
     * after the SQL script for each version that has one.
     *
     * @param javaSteps map from version number to an optional post-SQL Java step
     */
    public void migrate(Map<Integer, JavaMigrationStep> javaSteps) throws SQLException {
        createMetaTable();
        int version = getVersion();
        if (version < 1) applyVersion(1, javaSteps.get(1));
        if (version < 2) applyVersion(2, javaSteps.get(2));
        if (version < 3) applyVersion(3, javaSteps.get(3));
    }

    // ── Migration execution ───────────────────────────────────────────────────

    /**
     * Applies one migration version using a fresh connection, then runs an optional
     * Java step, then records the new version.
     *
     * <p>A fresh connection prevents SQLite JDBC from carrying over stale statement handles
     * from previous DDL operations (e.g. DROP TABLE / RENAME TABLE).
     *
     * @param version   the version to apply
     * @param javaStep  optional post-SQL Java step, or {@code null}
     */
    private void applyVersion(int version, JavaMigrationStep javaStep) throws SQLException {
        try (Connection conn = provider.getConnection()) {
            executeScript(conn, loadScript(version));
        }
        if (javaStep != null) javaStep.run(provider);
        setVersion(version);
    }

    private void executeScript(Connection conn, String sql) throws SQLException {
        for (String statement : sql.split(";")) {
            String trimmed = statement.strip();
            if (!trimmed.isEmpty()) {
                try (Statement stmt = conn.createStatement()) {
                    try {
                        stmt.execute(trimmed);
                    } catch (SQLException e) {
                        // SQLite JDBC internally closes the compiled statement pointer after
                        // DDL execution (DROP TABLE, ALTER TABLE). If SQLite already auto-finalized
                        // the pointer, that internal close throws "finalized". The DDL was executed
                        // successfully — this error is safe to ignore.
                        if (e.getMessage() == null || !e.getMessage().contains("finalized")) {
                            throw e;
                        }
                    }
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
        try (Connection conn = provider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS `db_meta` (
                        `key`   VARCHAR(255) PRIMARY KEY,
                        `value` VARCHAR(255) NOT NULL
                    )""");
        }
    }

    private int getVersion() throws SQLException {
        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT value FROM db_meta WHERE `key`=?")) {
            ps.setString(1, VERSION_KEY);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Integer.parseInt(rs.getString("value")) : 0;
            }
        }
    }

    private void setVersion(int version) throws SQLException {
        try (Connection conn = provider.getConnection();
             PreparedStatement update = conn.prepareStatement(
                     "UPDATE db_meta SET value=? WHERE `key`=?")) {
            update.setString(1, String.valueOf(version));
            update.setString(2, VERSION_KEY);
            if (update.executeUpdate() == 0) {
                try (PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO db_meta (`key`, value) VALUES (?, ?)")) {
                    insert.setString(1, VERSION_KEY);
                    insert.setString(2, String.valueOf(version));
                    insert.executeUpdate();
                }
            }
        }
    }
}
