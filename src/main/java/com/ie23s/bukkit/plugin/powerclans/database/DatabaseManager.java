package com.ie23s.bukkit.plugin.powerclans.database;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.database.migration.MigrationRunner;
import com.ie23s.bukkit.plugin.powerclans.database.migration.step.V3Migration;

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

/**
 * Establishes the database connection and runs schema migrations.
 * All business logic has been moved to the service layer.
 */
public class DatabaseManager {

    private final Core core;
    private final ConnectionProvider connectionProvider;
    private final String dialect;

    private DatabaseManager(Core core, ConnectionProvider connectionProvider, String dialect) {
        this.core = core;
        this.connectionProvider = connectionProvider;
        this.dialect = dialect;
        runMigrations();
    }

    /**
     * Creates a {@code DatabaseManager} using the database type from {@code config.yml}.
     * Supported values: {@code mysql}, anything else falls back to SQLite.
     */
    public static DatabaseManager create(Core core) {
        String dbType = Objects.requireNonNull(core.getConfig().getString("database")).toLowerCase();
        ConnectionProvider provider = switch (dbType) {
            case "mysql", "mariadb" -> new MySQL(core);
            default      -> new SQLite(core, "PowerClans");
        };
        return new DatabaseManager(core, provider, dbType);
    }

    public ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    /** Shuts down the underlying connection pool. Should be called on plugin disable. */
    public void disconnect() {
        if (connectionProvider instanceof MySQL mysql)   mysql.disconnect();
        else if (connectionProvider instanceof SQLite sqlite) sqlite.disconnect();
    }

    private void runMigrations() {
        try {
            new MigrationRunner(connectionProvider, dialect)
                    .migrate(Map.of(3, new V3Migration()));
        } catch (SQLException e) {
            core.getUtils().getLogger().error(core.lang("other.mysql_error2"), e);
        }
    }
}
