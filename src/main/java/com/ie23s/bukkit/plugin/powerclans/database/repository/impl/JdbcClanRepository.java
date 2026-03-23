package com.ie23s.bukkit.plugin.powerclans.database.repository.impl;

import com.ie23s.bukkit.plugin.powerclans.database.ConnectionProvider;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.ClanRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link ClanRepository} using {@link PreparedStatement}.
 * Compatible with both MySQL and SQLite — no dialect-specific SQL is used.
 * Each method acquires a connection from the pool and returns it on completion.
 */
public class JdbcClanRepository implements ClanRepository {

    private final ConnectionProvider connectionProvider;

    public JdbcClanRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public List<ClanDto> findAll() throws SQLException {
        List<ClanDto> clans = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT uuid, name, leader, max_players, level FROM clan_list")) {
            while (rs.next()) {
                clans.add(new ClanDto(
                        rs.getString("uuid"),
                        rs.getString("name"),
                        rs.getString("leader"),
                        rs.getInt("max_players"),
                        rs.getInt("level")
                ));
            }
        }
        return clans;
    }

    @Override
    public void insert(ClanDto clan) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO clan_list (uuid, name, leader, max_players, level) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, clan.uuid());
            ps.setString(2, clan.name());
            ps.setString(3, clan.leader());
            ps.setInt(4, clan.maxPlayers());
            ps.setInt(5, clan.level());
            ps.executeUpdate();
        }
    }

    @Override
    public void updateLeader(String clanUuid, String leaderName) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE clan_list SET leader=? WHERE uuid=?")) {
            ps.setString(1, leaderName);
            ps.setString(2, clanUuid);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateMaxPlayers(String clanUuid, int maxPlayers) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE clan_list SET max_players=? WHERE uuid=?")) {
            ps.setInt(1, maxPlayers);
            ps.setString(2, clanUuid);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateLevel(String clanUuid, int level) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE clan_list SET level=? WHERE uuid=?")) {
            ps.setInt(1, level);
            ps.setString(2, clanUuid);
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(String clanUuid) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM clan_list WHERE uuid=?")) {
            ps.setString(1, clanUuid);
            ps.executeUpdate();
        }
    }
}
