package com.ie23s.bukkit.plugin.powerclans.database.repository;

import com.ie23s.bukkit.plugin.powerclans.database.BaseDbTest;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDataDto;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.impl.JdbcClanDataRepository;
import com.ie23s.bukkit.plugin.powerclans.database.repository.impl.JdbcClanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class JdbcClanDataRepositoryTest extends BaseDbTest {

    private static final String UUID = "550e8400-e29b-41d4-a716-446655440000";

    private JdbcClanDataRepository repo;

    private static final ClanDataDto DATA = new ClanDataDto(
            UUID, "WAR", "none", false, 0.0, 0, 0, 0
    );

    @BeforeEach
    void setUp() throws SQLException {
        // clan_data has FK on clan_list in MySQL; insert parent row first
        new JdbcClanRepository(provider).insert(new ClanDto(0, UUID, "warriors", "steve", 10, 1));
        repo = new JdbcClanDataRepository(provider);
    }

    @Test
    void findAll_returnsEmptyWhenNoData() throws SQLException {
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void insert_thenFindAll_returnsData() throws SQLException {
        repo.insert(DATA);
        var result = repo.findAll();

        assertEquals(1, result.size());
        ClanDataDto found = result.getFirst();
        assertEquals(UUID, found.clanUuid());
        assertEquals("WAR", found.tag());
        assertEquals("none", found.home());
        assertFalse(found.pvp());
        assertEquals(0.0, found.balance());
    }

    @Test
    void insert_defaultsCountersToZero() throws SQLException {
        repo.insert(DATA);
        ClanDataDto found = repo.findAll().getFirst();

        assertEquals(0, found.mobKills());
        assertEquals(0, found.playerKills());
        assertEquals(0, found.onlineTime());
    }

    @Test
    void updateBalance_changesBalance() throws SQLException {
        repo.insert(DATA);
        repo.updateBalance(UUID, 1500.0);

        assertEquals(1500.0, repo.findAll().getFirst().balance());
    }

    @Test
    void updatePvp_changesPvpFlag() throws SQLException {
        repo.insert(DATA);
        repo.updatePvp(UUID, true);

        assertTrue(repo.findAll().getFirst().pvp());
    }

    @Test
    void updateHome_changesHome() throws SQLException {
        repo.insert(DATA);
        repo.updateHome(UUID, "world;0;64;0;0;0");

        assertEquals("world;0;64;0;0;0", repo.findAll().getFirst().home());
    }

    @Test
    void updateMobKills_changesMobKills() throws SQLException {
        repo.insert(DATA);
        repo.updateMobKills(UUID, 42);

        assertEquals(42, repo.findAll().getFirst().mobKills());
    }

    @Test
    void updatePlayerKills_changesPlayerKills() throws SQLException {
        repo.insert(DATA);
        repo.updatePlayerKills(UUID, 7);

        assertEquals(7, repo.findAll().getFirst().playerKills());
    }

    @Test
    void updateOnlineTime_changesOnlineTime() throws SQLException {
        repo.insert(DATA);
        repo.updateOnlineTime(UUID, 3600);

        assertEquals(3600, repo.findAll().getFirst().onlineTime());
    }
}
