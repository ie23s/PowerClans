package com.ie23s.bukkit.plugin.powerclans.api;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ClanDataKey} default values and {@link IClanData} typed access on {@link Clan}.
 */
@ExtendWith(MockitoExtension.class)
class ClanDataKeyTest {

    @Mock Core core;

    Clan clan;

    @BeforeEach
    void setUp() {
        clan = new Clan(core, 1, "uuid-1", "warriors",
                java.util.UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), 10, 1);
    }

    // ── Default values ────────────────────────────────────────────────────────

    @Test
    void defaults_areInitialisedOnConstruction() {
        assertEquals("",     clan.getString(ClanDataKey.TAG));
        assertEquals("none", clan.getString(ClanDataKey.HOME));
        assertTrue(           clan.getBoolean(ClanDataKey.PVP));
        assertEquals(0.0,    clan.getDouble(ClanDataKey.BALANCE));
        assertEquals(0,      clan.getInt(ClanDataKey.MOB_KILLS));
        assertEquals(0,      clan.getInt(ClanDataKey.PLAYER_KILLS));
        assertEquals(0,      clan.getInt(ClanDataKey.ONLINE_TIME));
    }

    @Test
    void identNames_matchExpected() {
        assertEquals("tag",          ClanDataKey.TAG.ident());
        assertEquals("home",         ClanDataKey.HOME.ident());
        assertEquals("pvp",          ClanDataKey.PVP.ident());
        assertEquals("balance",      ClanDataKey.BALANCE.ident());
        assertEquals("mob_kills",    ClanDataKey.MOB_KILLS.ident());
        assertEquals("player_kills", ClanDataKey.PLAYER_KILLS.ident());
        assertEquals("online_time",  ClanDataKey.ONLINE_TIME.ident());
    }

    // ── Typed access after set ────────────────────────────────────────────────

    @Test
    void getInt_returnsSetValue() {
        clan.set(ClanDataKey.MOB_KILLS, 42);
        assertEquals(42, clan.getInt(ClanDataKey.MOB_KILLS));
    }

    @Test
    void getDouble_returnsSetValue() {
        clan.set(ClanDataKey.BALANCE, 99.5);
        assertEquals(99.5, clan.getDouble(ClanDataKey.BALANCE));
    }

    @Test
    void getString_returnsSetValue() {
        clan.set(ClanDataKey.TAG, "WAR");
        assertEquals("WAR", clan.getString(ClanDataKey.TAG));
    }

    @Test
    void getBoolean_returnsSetValue() {
        clan.set(ClanDataKey.PVP, false);
        assertFalse(clan.getBoolean(ClanDataKey.PVP));
    }

    // ── Fallback to default ───────────────────────────────────────────────────

    @Test
    void getInt_fallsBackToDefault_whenWrongType() {
        clan.set(ClanDataKey.MOB_KILLS, "not-a-number");
        assertEquals(0, clan.getInt(ClanDataKey.MOB_KILLS));
    }

    @Test
    void getDouble_fallsBackToDefault_whenWrongType() {
        clan.set(ClanDataKey.BALANCE, "not-a-number");
        assertEquals(0.0, clan.getDouble(ClanDataKey.BALANCE));
    }

    @Test
    void getString_fallsBackToDefault_whenWrongType() {
        clan.set(ClanDataKey.TAG, 123);
        assertEquals("", clan.getString(ClanDataKey.TAG));
    }

    @Test
    void getBoolean_fallsBackToDefault_whenWrongType() {
        clan.set(ClanDataKey.PVP, "yes");
        assertTrue(clan.getBoolean(ClanDataKey.PVP));
    }

    // ── getRaw ────────────────────────────────────────────────────────────────

    @Test
    void getRaw_returnsExactStoredObject() {
        clan.set(ClanDataKey.PLAYER_KILLS, 7);
        assertEquals(7, clan.getRaw(ClanDataKey.PLAYER_KILLS));
    }
}
