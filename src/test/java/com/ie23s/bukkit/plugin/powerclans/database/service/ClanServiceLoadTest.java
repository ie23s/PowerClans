package com.ie23s.bukkit.plugin.powerclans.database.service;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.ClanList;
import com.ie23s.bukkit.plugin.powerclans.clan.MemberList;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDataDto;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDto;
import com.ie23s.bukkit.plugin.powerclans.database.dto.MemberDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.ClanDataRepository;
import com.ie23s.bukkit.plugin.powerclans.database.repository.ClanRepository;
import com.ie23s.bukkit.plugin.powerclans.database.repository.MemberRepository;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class ClanServiceLoadTest {

    @Mock Core core;
    @Mock ClanRepository clanRepo;
    @Mock ClanDataRepository clanDataRepo;
    @Mock MemberRepository memberRepo;

    ClanList clanList;
    MemberList memberList;

    ClanService clanService;
    ClanDataService clanDataService;
    MemberService memberService;

    private static final String UUID        = "550e8400-e29b-41d4-a716-446655440000";
    private static final String STEVE_UUID  = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    private static final String ALEX_UUID   = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
    private static final String NOTCH_UUID  = "cccccccc-cccc-cccc-cccc-cccccccccccc";
    private static final String LEADER_UUID = "dddddddd-dddd-dddd-dddd-dddddddddddd";

    @BeforeEach
    void setUp() {
        clanList   = new ClanList(core);
        memberList = new MemberList();
        when(core.getClanList()).thenReturn(clanList);
        when(core.getMemberList()).thenReturn(memberList);
        when(core.getClanService()).thenReturn(mock(ClanService.class));
        when(core.getClanDataService()).thenReturn(mock(ClanDataService.class));
        when(core.getMemberService()).thenReturn(mock(MemberService.class));

        clanService     = new ClanService(core, clanRepo);
        clanDataService = new ClanDataService(core, clanDataRepo);
        memberService   = new MemberService(core, memberRepo);
    }

    // ── ClanService.loadAll() ─────────────────────────────────────────────────

    @Test
    void clanService_loadAll_registersClanInList() throws SQLException {
        when(clanRepo.findAll()).thenReturn(List.of(
                new ClanDto(1, UUID, "warriors", LEADER_UUID, 10, 1)
        ));

        clanService.loadAll();

        Clan clan = clanList.getClan("warriors");
        assertNotNull(clan);
        assertEquals(1,          clan.getId());
        assertEquals(UUID,       clan.getUuid());
        assertEquals("warriors", clan.getName());
        assertEquals(java.util.UUID.fromString(LEADER_UUID), clan.getLeaderUuid());
        assertEquals(10,         clan.getMaxPlayers());
        assertEquals(1,          clan.getLevel());
    }

    @Test
    void clanService_loadAll_registersMultipleClans() throws SQLException {
        when(clanRepo.findAll()).thenReturn(List.of(
                new ClanDto(1, UUID,     "warriors", LEADER_UUID, 10, 1),
                new ClanDto(2, "uuid-2", "rangers",  ALEX_UUID,   5,  2)
        ));

        clanService.loadAll();

        assertEquals(2, clanList.number());
        assertNotNull(clanList.getClan("warriors"));
        assertNotNull(clanList.getClan("rangers"));
    }

    @Test
    void clanService_loadAll_emptyRepo_registersNothing() throws SQLException {
        when(clanRepo.findAll()).thenReturn(List.of());

        clanService.loadAll();

        assertEquals(0, clanList.number());
    }

    // ── ClanDataService.loadAll() ─────────────────────────────────────────────

    @Test
    void clanDataService_loadAll_fillsDataMap() throws SQLException {
        Clan clan = new Clan(core, 1, UUID, "warriors", java.util.UUID.fromString(LEADER_UUID), 10, 1);
        clanList.getClans().put("warriors", clan);

        when(clanDataRepo.findAll()).thenReturn(List.of(
                new ClanDataDto(UUID, "tag",          "WAR"),
                new ClanDataDto(UUID, "home",         "world;0.0;64.0;0.0;0.0;0.0"),
                new ClanDataDto(UUID, "pvp",          "false"),
                new ClanDataDto(UUID, "balance",      "500.0"),
                new ClanDataDto(UUID, "mob_kills",    "10"),
                new ClanDataDto(UUID, "player_kills", "3"),
                new ClanDataDto(UUID, "online_time",  "120")
        ));

        clanDataService.loadAll();

        assertEquals("WAR",  clan.getString(ClanDataKey.TAG));
        assertFalse(clan.getBoolean(ClanDataKey.PVP));
        assertEquals(500.0,  clan.getDouble(ClanDataKey.BALANCE));
        assertEquals(10,     clan.getInt(ClanDataKey.MOB_KILLS));
        assertEquals(3,      clan.getInt(ClanDataKey.PLAYER_KILLS));
        assertEquals(120,    clan.getInt(ClanDataKey.ONLINE_TIME));
    }

    @Test
    void clanDataService_loadAll_skipsUnknownUuid() throws SQLException {
        when(clanDataRepo.findAll()).thenReturn(List.of(
                new ClanDataDto("unknown-uuid", "tag", "X")
        ));

        assertDoesNotThrow(() -> clanDataService.loadAll());
        assertEquals(0, clanList.number());
    }

    // ── MemberService.loadAll() ───────────────────────────────────────────────

    @Test
    void memberService_loadAll_addsMembersToList() throws SQLException {
        Clan clan = new Clan(core, 1, UUID, "warriors", java.util.UUID.fromString(LEADER_UUID), 10, 1);
        clanList.getClans().put("warriors", clan);

        when(memberRepo.findAll()).thenReturn(List.of(
                new MemberDto(UUID, "steve", STEVE_UUID, false),
                new MemberDto(UUID, "alex",  ALEX_UUID,  true)
        ));

        memberService.loadAll();

        assertTrue(memberList.isMember("steve"));
        assertTrue(memberList.isMember("alex"));
        assertFalse(memberList.getMember("steve").isModer());
        assertTrue(memberList.getMember("alex").isModer());
    }

    @Test
    void memberService_loadAll_orphanedMember_isNotAddedToList() throws SQLException {
        // clan uuid "unknown-uuid" doesn't exist in clanList
        when(memberRepo.findAll()).thenReturn(List.of(
                new MemberDto("unknown-uuid", "notch", NOTCH_UUID, false)
        ));

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getScheduler).thenReturn(mock(org.bukkit.scheduler.BukkitScheduler.class));
            memberService.loadAll();
        }

        assertFalse(memberList.isMember("notch"), "orphaned member should not be added");
    }

    // ── ClanList.getClanByUuid() ──────────────────────────────────────────────

    @Test
    void getClanByUuid_returnsCorrectClan() {
        Clan clan = new Clan(core, 1, UUID, "warriors", java.util.UUID.fromString(LEADER_UUID), 10, 1);
        clanList.getClans().put("warriors", clan);

        assertSame(clan, clanList.getClanByUuid(UUID));
    }

    @Test
    void getClanByUuid_returnsNull_whenNotFound() {
        assertNull(clanList.getClanByUuid("non-existent-uuid"));
    }
}
