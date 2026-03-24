package com.ie23s.bukkit.plugin.powerclans.database.repository;

import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.database.BaseDbTest;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDataDto;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.impl.JdbcClanDataRepository;
import com.ie23s.bukkit.plugin.powerclans.database.repository.impl.JdbcClanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JdbcClanDataRepositoryTest extends BaseDbTest {

    private static final String UUID = "550e8400-e29b-41d4-a716-446655440000";

    private JdbcClanDataRepository repo;

    @BeforeEach
    void setUp() throws SQLException {
        new JdbcClanRepository(provider).insert(new ClanDto(0, UUID, "warriors", "steve", 10, 1));
        repo = new JdbcClanDataRepository(provider);
    }

    private Map<ClanDataKey, Object> defaultData() {
        Map<ClanDataKey, Object> data = new EnumMap<>(ClanDataKey.class);
        for (ClanDataKey key : ClanDataKey.values()) data.put(key, key.defaultValue());
        return data;
    }

    @Test
    void findAll_returnsEmptyWhenNoData() throws SQLException {
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void insertAll_insertsOneRowPerKey() throws SQLException {
        repo.insertAll(UUID, defaultData());

        assertEquals(ClanDataKey.values().length, repo.findAll().size());
    }

    @Test
    void insertAll_storesCorrectIdents() throws SQLException {
        repo.insertAll(UUID, defaultData());

        List<ClanDataDto> rows = repo.findAll();
        for (ClanDataKey key : ClanDataKey.values()) {
            assertTrue(rows.stream().anyMatch(r -> r.ident().equals(key.ident())),
                    "Missing row for ident: " + key.ident());
        }
    }

    @Test
    void insertAll_storesDefaultValues() throws SQLException {
        repo.insertAll(UUID, defaultData());

        List<ClanDataDto> rows = repo.findAll();
        for (ClanDataDto row : rows) {
            ClanDataKey key = ClanDataKey.fromIdent(row.ident());
            assertEquals(String.valueOf(key.defaultValue()), row.value(),
                    "Wrong default value for ident: " + row.ident());
        }
    }

    @Test
    void upsert_updatesExistingRow() throws SQLException {
        repo.insertAll(UUID, defaultData());
        repo.upsert(UUID, ClanDataKey.BALANCE.ident(), "1500.0");

        ClanDataDto row = repo.findAll().stream()
                .filter(r -> r.ident().equals(ClanDataKey.BALANCE.ident()))
                .findFirst().orElseThrow();
        assertEquals("1500.0", row.value());
    }

    @Test
    void upsert_insertsRowWhenMissing() throws SQLException {
        repo.upsert(UUID, ClanDataKey.HOME.ident(), "world;0;64;0;0;0");

        ClanDataDto row = repo.findAll().stream()
                .filter(r -> r.ident().equals(ClanDataKey.HOME.ident()))
                .findFirst().orElseThrow();
        assertEquals("world;0;64;0;0;0", row.value());
    }

    @Test
    void upsert_updatesPvpFlag() throws SQLException {
        repo.insertAll(UUID, defaultData());
        repo.upsert(UUID, ClanDataKey.PVP.ident(), "true");

        ClanDataDto row = repo.findAll().stream()
                .filter(r -> r.ident().equals(ClanDataKey.PVP.ident()))
                .findFirst().orElseThrow();
        assertEquals("true", row.value());
    }
}
