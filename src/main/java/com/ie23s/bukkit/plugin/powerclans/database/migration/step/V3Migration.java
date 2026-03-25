package com.ie23s.bukkit.plugin.powerclans.database.migration.step;

import com.ie23s.bukkit.plugin.powerclans.database.ConnectionProvider;
import com.ie23s.bukkit.plugin.powerclans.database.migration.JavaMigrationStep;
import com.ie23s.bukkit.plugin.powerclans.database.repository.impl.JdbcMemberRepository;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * V3 Java migration step.
 *
 * <p>Back-fills {@code player_uuid} in {@code clan_members} and {@code leader_uuid} in
 * {@code clan_list} for all existing rows using the Bukkit offline-player registry,
 * then drops the now-redundant {@code leader} name column.
 *
 * <p>This is the only permitted use of {@link Bukkit#getOfflinePlayer(String)} in the codebase.
 * After this step runs, all code paths use UUID-based lookups.
 */
public class V3Migration implements JavaMigrationStep {

    @Override
    public void run(ConnectionProvider provider) throws SQLException {
        populateMemberUuids(provider);
        populateLeaderUuids(provider);
        dropLeaderColumn(provider);
    }

    private void populateMemberUuids(ConnectionProvider provider) throws SQLException {
        record Row(String clanUuid, String name) {}
        List<Row> rows = new ArrayList<>();

        try (Connection conn = provider.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT clan_uuid, name FROM clan_members WHERE player_uuid IS NULL")) {
            while (rs.next()) rows.add(new Row(rs.getString("clan_uuid"), rs.getString("name")));
        }

        JdbcMemberRepository repo = new JdbcMemberRepository(provider);
        for (Row row : rows) {
            @SuppressWarnings("deprecation")
            OfflinePlayer op = Bukkit.getOfflinePlayer(row.name());
            repo.updatePlayerUuid(row.clanUuid(),
                    op.getName() != null ? op.getName() : row.name(),
                    op.getUniqueId().toString());
        }
    }

    private void populateLeaderUuids(ConnectionProvider provider) throws SQLException {
        record Row(String clanUuid, String leaderName) {}
        List<Row> rows = new ArrayList<>();

        try (Connection conn = provider.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT uuid, leader FROM clan_list WHERE leader_uuid IS NULL")) {
            while (rs.next()) rows.add(new Row(rs.getString("uuid"), rs.getString("leader")));
        }

        try (Connection conn = provider.getConnection()) {
            for (Row row : rows) {
                @SuppressWarnings("deprecation")
                OfflinePlayer op = Bukkit.getOfflinePlayer(row.leaderName());
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE clan_list SET leader_uuid=? WHERE uuid=?")) {
                    ps.setString(1, op.getUniqueId().toString());
                    ps.setString(2, row.clanUuid());
                    ps.executeUpdate();
                }
            }
        }
    }

    private void dropLeaderColumn(ConnectionProvider provider) throws SQLException {
        try (Connection conn = provider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE clan_list DROP COLUMN leader");
        }
    }
}
