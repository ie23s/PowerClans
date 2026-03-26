package com.ie23s.bukkit.plugin.powerclans.database.repository;

import com.ie23s.bukkit.plugin.powerclans.database.BaseDbTest;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.impl.JdbcClanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcClanRepositoryTest extends BaseDbTest {

    private JdbcClanRepository repo;

    private static final String STEVE_UUID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

    private static final ClanDto CLAN = new ClanDto(
            0, "550e8400-e29b-41d4-a716-446655440000", "warriors", STEVE_UUID, 10, 1
    );

    @BeforeEach
    void setUp() {
        repo = new JdbcClanRepository(provider);
    }

    @Test
    void findAll_returnsEmptyWhenNoClans() throws SQLException {
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void insert_thenFindAll_returnsClan() throws SQLException {
        repo.insert(CLAN);
        List<ClanDto> result = repo.findAll();

        assertEquals(1, result.size());
        ClanDto found = result.getFirst();
        assertTrue(found.id() > 0);
        assertEquals("550e8400-e29b-41d4-a716-446655440000", found.uuid());
        assertEquals("warriors", found.name());
        assertEquals(STEVE_UUID,  found.leaderUuid());
        assertEquals(10, found.maxPlayers());
        assertEquals(1, found.level());
    }

    private static final String ALEX_UUID = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

    @Test
    void updateLeader_changesLeaderUuid() throws SQLException {
        repo.insert(CLAN);
        repo.updateLeader(CLAN.uuid(), ALEX_UUID);

        assertEquals(ALEX_UUID, repo.findAll().getFirst().leaderUuid());
    }

    @Test
    void updateMaxPlayers_changesMaxPlayers() throws SQLException {
        repo.insert(CLAN);
        repo.updateMaxPlayers(CLAN.uuid(), 20);

        assertEquals(20, repo.findAll().getFirst().maxPlayers());
    }

    @Test
    void updateLevel_changesLevel() throws SQLException {
        repo.insert(CLAN);
        repo.updateLevel(CLAN.uuid(), 5);

        assertEquals(5, repo.findAll().getFirst().level());
    }

    @Test
    void delete_removesClan() throws SQLException {
        repo.insert(CLAN);
        repo.delete(CLAN.uuid());

        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void delete_doesNotAffectOtherClans() throws SQLException {
        repo.insert(CLAN);
        repo.insert(new ClanDto(0, "550e8400-e29b-41d4-a716-446655440001", "rangers", ALEX_UUID, 5, 1));
        repo.delete(CLAN.uuid());

        List<ClanDto> remaining = repo.findAll();
        assertEquals(1, remaining.size());
        assertEquals("rangers", remaining.getFirst().name());
    }
}
