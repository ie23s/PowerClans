package com.ie23s.bukkit.plugin.powerclans.database.repository.impl;

import com.ie23s.bukkit.plugin.powerclans.database.ConnectionProvider;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDataDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.ClanDataRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link ClanDataRepository} using {@link PreparedStatement}.
 * Compatible with both MySQL and SQLite — no dialect-specific SQL is used.
 * Each method acquires a connection from the pool and returns it on completion.
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
             ResultSet rs = stmt.executeQuery("SELECT * FROM clan_data")) {
            while (rs.next()) {
                result.add(new ClanDataDto(
                        rs.getString("clan_uuid"),
                        rs.getString("tag"),
                        rs.getString("home"),
                        rs.getBoolean("pvp"),
                        rs.getDouble("balance"),
                        rs.getInt("mob_kills"),
                        rs.getInt("player_kills"),
                        rs.getInt("online_time")
                ));
            }
        }
        return result;
    }

    @Override
    public void insert(ClanDataDto data) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO clan_data (clan_uuid, tag, home, pvp, balance, mob_kills, player_kills, online_time) VALUES (?, ?, ?, ?, ?, 0, 0, 0)")) {
            ps.setString(1, data.clanUuid());
            ps.setString(2, data.tag());
            ps.setString(3, data.home());
            ps.setBoolean(4, data.pvp());
            ps.setDouble(5, data.balance());
            ps.executeUpdate();
        }
    }

    @Override
    public void updateBalance(String clanUuid, double balance) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE clan_data SET balance=? WHERE clan_uuid=?")) {
            ps.setDouble(1, balance);
            ps.setString(2, clanUuid);
            ps.executeUpdate();
        }
    }

    @Override
    public void updatePvp(String clanUuid, boolean pvp) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE clan_data SET pvp=? WHERE clan_uuid=?")) {
            ps.setBoolean(1, pvp);
            ps.setString(2, clanUuid);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateHome(String clanUuid, String home) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE clan_data SET home=? WHERE clan_uuid=?")) {
            ps.setString(1, home);
            ps.setString(2, clanUuid);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateMobKills(String clanUuid, int mobKills) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE clan_data SET mob_kills=? WHERE clan_uuid=?")) {
            ps.setInt(1, mobKills);
            ps.setString(2, clanUuid);
            ps.executeUpdate();
        }
    }

    @Override
    public void updatePlayerKills(String clanUuid, int playerKills) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE clan_data SET player_kills=? WHERE clan_uuid=?")) {
            ps.setInt(1, playerKills);
            ps.setString(2, clanUuid);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateOnlineTime(String clanUuid, int onlineTime) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE clan_data SET online_time=? WHERE clan_uuid=?")) {
            ps.setInt(1, onlineTime);
            ps.setString(2, clanUuid);
            ps.executeUpdate();
        }
    }
}
