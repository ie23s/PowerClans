package com.ie23s.bukkit.plugin.powerclans.database.repository.impl;

import com.ie23s.bukkit.plugin.powerclans.database.ConnectionProvider;
import com.ie23s.bukkit.plugin.powerclans.database.dto.MemberDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.MemberRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link MemberRepository} using {@link java.sql.PreparedStatement}.
 * Compatible with both MySQL and SQLite — no dialect-specific SQL is used.
 * Each method acquires a connection from the pool and returns it on completion.
 */
public class JdbcMemberRepository implements MemberRepository {

    private final ConnectionProvider connectionProvider;

    public JdbcMemberRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public List<MemberDto> findAll() throws SQLException {
        List<MemberDto> members = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM clan_members")) {
            while (rs.next()) {
                members.add(new MemberDto(
                        rs.getString("clan"),
                        rs.getString("name"),
                        rs.getBoolean("isModer")
                ));
            }
        }
        return members;
    }

    @Override
    public void insert(MemberDto member) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO clan_members (clan, name, isModer) VALUES (?, ?, 0)")) {
            ps.setString(1, member.clan());
            ps.setString(2, member.name());
            ps.executeUpdate();
        }
    }

    @Override
    public void updateModer(String clanName, String memberName, boolean isModer) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE clan_members SET isModer=? WHERE clan=? AND name=?")) {
            ps.setBoolean(1, isModer);
            ps.setString(2, clanName);
            ps.setString(3, memberName);
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(String clanName, String memberName) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM clan_members WHERE clan=? AND name=?")) {
            ps.setString(1, clanName);
            ps.setString(2, memberName);
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteByClan(String clanName) throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM clan_members WHERE clan=?")) {
            ps.setString(1, clanName);
            ps.executeUpdate();
        }
    }
}
