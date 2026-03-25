package com.ie23s.bukkit.plugin.powerclans.database;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.database.migration.MigrationRunner;
import com.ie23s.bukkit.plugin.powerclans.database.repository.impl.JdbcMemberRepository;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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
            case "mysql" -> new MySQL(core);
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
                    .migrate(Map.of(3, this::populateMemberUuids));
        } catch (SQLException e) {
            core.getUtils().getLogger().error(core.lang("other.mysql_error2"), e);
        }
    }

    /**
     * V3 Java migration step: back-fills the {@code player_uuid} column in {@code clan_members}
     * for all existing rows using the Bukkit offline-player registry.
     *
     * <p>This is the only permitted use of {@link Bukkit#getOfflinePlayer(String)} in the codebase.
     * After this step runs, all code paths use UUID-based or online-player lookups.
     *
     * @param provider connection provider to use for DB access
     * @throws SQLException if any database operation fails
     */
    private void populateMemberUuids(ConnectionProvider provider) throws SQLException {
        record MemberRow(String clanUuid, String name) {}
        List<MemberRow> rows = new ArrayList<>();

        try (Connection conn = provider.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT clan_uuid, name FROM clan_members WHERE player_uuid IS NULL")) {
            while (rs.next()) {
                rows.add(new MemberRow(rs.getString("clan_uuid"), rs.getString("name")));
            }
        }

        JdbcMemberRepository repo = new JdbcMemberRepository(provider);
        for (MemberRow row : rows) {
            @SuppressWarnings("deprecation")
            OfflinePlayer op = Bukkit.getOfflinePlayer(row.name());
            String uuid = op.getUniqueId().toString();
            String canonicalName = op.getName() != null ? op.getName() : row.name();
            repo.updatePlayerUuid(row.clanUuid(), canonicalName, uuid);
        }
    }
}
