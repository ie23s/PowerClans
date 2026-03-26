package com.ie23s.bukkit.plugin.powerclans.database.migration;

import com.ie23s.bukkit.plugin.powerclans.database.BaseDbTest;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class MigrationRunnerTest extends BaseDbTest {

    @Test
    void migrate_createsClanListTable() throws SQLException {
        assertTrue(tableExists("clan_list"));
    }

    @Test
    void migrate_createsClanMembersTable() throws SQLException {
        assertTrue(tableExists("clan_members"));
    }

    @Test
    void migrate_createsDbMetaTable() throws SQLException {
        assertTrue(tableExists("db_meta"));
    }

    @Test
    void migrate_createsClanDataTable() throws SQLException {
        assertTrue(tableExists("clan_data"));
    }

    @Test
    void migrate_setsSchemaVersionTo3_afterAllMigrationsRun() throws SQLException {
        try (var ps = connection.prepareStatement("SELECT value FROM db_meta WHERE key='schema_version'");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertEquals("3", rs.getString("value"));
        }
    }

    @Test
    void migrate_addsPlayerUuidColumnToClanMembers() throws SQLException {
        try (var ps = connection.prepareStatement("PRAGMA table_info(clan_members)");
             ResultSet rs = ps.executeQuery()) {
            boolean found = false;
            while (rs.next()) {
                if ("player_uuid".equals(rs.getString("name"))) { found = true; break; }
            }
            assertTrue(found, "player_uuid column should exist in clan_members");
        }
    }

    @Test
    void migrate_addsLeaderUuidColumnToClanList() throws SQLException {
        try (var ps = connection.prepareStatement("PRAGMA table_info(clan_list)");
             ResultSet rs = ps.executeQuery()) {
            boolean found = false;
            while (rs.next()) {
                if ("leader_uuid".equals(rs.getString("name"))) { found = true; break; }
            }
            assertTrue(found, "leader_uuid column should exist in clan_list");
        }
    }

    @Test
    void migrate_isIdempotent() {
        assertDoesNotThrow(() -> new MigrationRunner(provider, "sqlite").migrate());
    }

    private boolean tableExists(String tableName) throws SQLException {
        try (var ps = connection.prepareStatement(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?")) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
