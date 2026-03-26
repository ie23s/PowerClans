package com.ie23s.bukkit.plugin.powerclans.database.repository.impl;

import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.database.ConnectionProvider;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDataDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.ClanDataRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JDBC implementation of {@link ClanDataRepository} using {@link PreparedStatement}.
 * Compatible with both MySQL and SQLite — no dialect-specific SQL is used.
 * Each method acquires a connection from the pool and returns it on completion.
 *
 * <p>{@link #upsert} uses an UPDATE-then-INSERT pattern so that neither MySQL's
 * {@code ON DUPLICATE KEY UPDATE} nor SQLite's {@code INSERT OR REPLACE} is required.
 */
public class JdbcClanDataRepository implements ClanDataRepository {

    private final ConnectionProvider connectionProvider;

    public JdbcClanDataRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public List<ClanDataDto> findAll() throws SQLException {
        List<ClanDataDto> result = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT clan_uuid, ident, value FROM clan_data")) {
            while (rs.next()) {
                result.add(new ClanDataDto(
                        rs.getString("clan_uuid"),
                        rs.getString("ident"),
                        rs.getString("value")
                ));
            }
        }
        return result;
    }

    @Override
    public void insertAll(String clanUuid, Map<ClanDataKey, Object> data) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO clan_data (clan_uuid, ident, value) VALUES (?, ?, ?)")) {
            ps.setString(1, clanUuid);
            for (Map.Entry<ClanDataKey, Object> entry : data.entrySet()) {
                if (entry.getValue() == null) continue;
                ps.setString(2, entry.getKey().ident());
                ps.setString(3, String.valueOf(entry.getValue()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public void delete(String clanUuid, String ident) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM clan_data WHERE clan_uuid=? AND ident=?")) {
            ps.setString(1, clanUuid);
            ps.setString(2, ident);
            ps.executeUpdate();
        }
    }

    @Override
    public void upsert(String clanUuid, String ident, String value) throws SQLException {
        try (Connection conn = connectionProvider.getConnection()) {
            try (PreparedStatement update = conn.prepareStatement(
                    "UPDATE clan_data SET value=? WHERE clan_uuid=? AND ident=?")) {
                update.setString(1, value);
                update.setString(2, clanUuid);
                update.setString(3, ident);
                if (update.executeUpdate() > 0) return;
            }
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO clan_data (clan_uuid, ident, value) VALUES (?, ?, ?)")) {
                insert.setString(1, clanUuid);
                insert.setString(2, ident);
                insert.setString(3, value);
                insert.executeUpdate();
            }
        }
    }
}
