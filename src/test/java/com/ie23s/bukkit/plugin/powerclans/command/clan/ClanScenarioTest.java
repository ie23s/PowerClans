package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.IClanCommand;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.ClanList;
import com.ie23s.bukkit.plugin.powerclans.clan.MemberList;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDto;
import com.ie23s.bukkit.plugin.powerclans.database.dto.MemberDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.impl.JdbcClanDataRepository;
import com.ie23s.bukkit.plugin.powerclans.database.repository.impl.JdbcClanRepository;
import com.ie23s.bukkit.plugin.powerclans.database.repository.impl.JdbcMemberRepository;
import com.ie23s.bukkit.plugin.powerclans.database.service.ClanDataService;
import com.ie23s.bukkit.plugin.powerclans.database.service.ClanService;
import com.ie23s.bukkit.plugin.powerclans.database.service.MemberService;
import com.ie23s.bukkit.plugin.powerclans.database.BaseDbTest;
import com.ie23s.bukkit.plugin.powerclans.utils.Request;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration scenario tests: real in-memory SQLite DB, real repositories and services.
 * Bukkit scheduler is stubbed to run async tasks synchronously so DB writes are immediate.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class ClanScenarioTest extends BaseDbTest {

    private static final String CLAN_UUID        = "550e8400-e29b-41d4-a716-446655440000";
    private static final String ALICE_UUID       = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    private static final String BOB_UUID         = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

    @Mock Core core;
    @Mock FileConfiguration config;

    MemberList memberList;
    ClanList clanList;
    Clan clan;

    JdbcClanRepository clanRepo;
    JdbcClanDataRepository clanDataRepo;
    JdbcMemberRepository memberRepo;

    ClanService clanService;
    ClanDataService clanDataService;
    MemberService memberService;

    @BeforeEach
    void setUp() throws SQLException {
        Request.requests.clear();

        clanRepo     = new JdbcClanRepository(provider);
        clanDataRepo = new JdbcClanDataRepository(provider);
        memberRepo   = new JdbcMemberRepository(provider);

        memberList = new MemberList();
        clanList   = new ClanList(core);

        when(core.getMemberList()).thenReturn(memberList);
        when(core.getClanList()).thenReturn(clanList);
        lenient().when(core.lang(anyString())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(core.lang(anyString(), any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(core.getConfig()).thenReturn(config);
        lenient().when(config.getInt("settings.max_symbols")).thenReturn(20);
        lenient().when(config.getInt("settings.min_symbols")).thenReturn(1);
        lenient().when(config.getString("settings.clan_regex")).thenReturn("[a-zA-Z0-9]+");
        lenient().when(config.getInt("settings.create_cost")).thenReturn(0);
        lenient().when(config.getInt("settings.default_max")).thenReturn(10);

        clanService     = new ClanService(core, clanRepo);
        clanDataService = new ClanDataService(core, clanDataRepo);
        memberService   = new MemberService(core, memberRepo);

        when(core.getClanService()).thenReturn(clanService);
        when(core.getClanDataService()).thenReturn(clanDataService);
        when(core.getMemberService()).thenReturn(memberService);

        // Seed DB: TestClan with alice (leader) and bob
        clanRepo.insert(new ClanDto(0, CLAN_UUID, "TestClan", ALICE_UUID, 10, 1));
        Map<ClanDataKey, Object> clanData = new java.util.EnumMap<>(ClanDataKey.class);
        for (ClanDataKey key : ClanDataKey.values()) clanData.put(key, key.defaultValue());
        clanData.put(ClanDataKey.TAG, "TC");
        clanDataRepo.insertAll(CLAN_UUID, clanData);
        memberRepo.insert(new MemberDto(CLAN_UUID, "alice", ALICE_UUID, false));
        memberRepo.insert(new MemberDto(CLAN_UUID, "bob",   BOB_UUID,   false));

        // Load DB state into memory
        clanService.loadAll();
        clanDataService.loadAll();
        memberService.loadAll();

        clan = clanList.getClan("TestClan");
    }

    /** Creates a mock Player with the given name, a random UUID, and all permissions granted. */
    private Player player(String name) {
        Player p = mock(Player.class);
        when(p.getName()).thenReturn(name);
        when(p.getUniqueId()).thenReturn(UUID.randomUUID());
        when(p.hasPermission(anyString())).thenReturn(true);
        return p;
    }

    /**
     * Stubs the Bukkit scheduler to run async tasks synchronously.
     * broadcast() uses getPlayerExact() which returns null by default → no messages sent in tests.
     */
    private void stubBukkit(MockedStatic<Bukkit> bukkit) {
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
        doAnswer(inv -> { ((Runnable) inv.getArgument(1)).run(); return null; })
                .when(scheduler).runTaskAsynchronously(any(Plugin.class), any(Runnable.class));

        bukkit.when(() -> Bukkit.getPlayerExact(any(String.class))).thenAnswer(inv -> {
            return null; // no online players in tests — broadcast sends to nobody
        });
    }

    // ── invite → accept ───────────────────────────────────────────────────────

    @Test
    void inviteAccept_playerJoinsClan() throws SQLException {
        Player alice  = player("alice");
        Player newguy = player("newguy");

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            stubBukkit(bukkit);
            bukkit.when(() -> Bukkit.getPlayer("newguy")).thenReturn(newguy);

            new MemberCommands.Invite(core).execute(alice, new String[]{"invite", "newguy"}, clan, "alice");
            new RequestCommands.Accept(core, Map.of()).execute(newguy, new String[]{"accept"}, null, "newguy");
        }

        assertTrue(memberList.isMember("newguy"));
        assertEquals("TestClan", memberList.getMember("newguy").getClan());

        List<MemberDto> members = memberRepo.findAll();
        assertTrue(members.stream().anyMatch(m -> m.name().equals("newguy") && m.clanUuid().equals(CLAN_UUID)));
    }

    // ── kick ──────────────────────────────────────────────────────────────────

    @Test
    void kick_removesPlayerFromClan() throws SQLException {
        Player alice = player("alice");
        Player bob   = player("bob");

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            stubBukkit(bukkit);
            bukkit.when(() -> Bukkit.getPlayer("bob")).thenReturn(bob);

            new MemberCommands.Kick(core).execute(alice, new String[]{"kick", "bob"}, clan, "alice");
        }

        assertFalse(memberList.isMember("bob"));

        List<MemberDto> members = memberRepo.findAll();
        assertFalse(members.stream().anyMatch(m -> m.name().equals("bob")));
    }

    // ── leave → accept ────────────────────────────────────────────────────────

    @Test
    void leaveAccept_playerLeavesClan() throws SQLException {
        Player bob = player("bob");
        Map<String, IClanCommand> registry = Map.of("leave", new LifecycleCommands.Leave(core));

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            stubBukkit(bukkit);

            new LifecycleCommands.Leave(core).execute(bob, new String[]{"leave"}, clan, "bob");
            new RequestCommands.Accept(core, registry).execute(bob, new String[]{"accept"}, clan, "bob");
        }

        assertFalse(memberList.isMember("bob"));

        List<MemberDto> members = memberRepo.findAll();
        assertFalse(members.stream().anyMatch(m -> m.name().equals("bob")));
    }

    // ── addmoder ──────────────────────────────────────────────────────────────

    @Test
    void addmoder_promotesPlayerToModerator() throws SQLException {
        Player alice = player("alice");

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            stubBukkit(bukkit);
            new ModeratorCommands.AddModer(core).execute(alice, new String[]{"addmoder", "bob"}, clan, "alice");
        }

        assertTrue(clan.hasModer("bob"));

        List<MemberDto> members = memberRepo.findAll();
        assertTrue(members.stream().anyMatch(m -> m.name().equals("bob") && m.isModer()));
    }

    // ── leader transfer → accept ──────────────────────────────────────────────

    @Test
    void leaderTransferAccept_changesLeader() throws SQLException {
        Player alice = player("alice");
        Map<String, IClanCommand> registry = Map.of("leader", new ModeratorCommands.Leader(core));

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            stubBukkit(bukkit);

            new ModeratorCommands.Leader(core).execute(alice, new String[]{"leader", "bob"}, clan, "alice");
            new RequestCommands.Accept(core, registry).execute(alice, new String[]{"accept"}, clan, "alice");
        }

        assertEquals("bob", clan.getLeaderName());

        List<ClanDto> clans = clanRepo.findAll();
        assertTrue(clans.stream().anyMatch(c -> c.name().equals("TestClan")
                && c.leaderUuid().equals(clan.getLeaderUuid().toString())));
    }

    // ── create → accept ───────────────────────────────────────────────────────

    @Test
    void createAccept_clanIsCreatedAndPlayerIsLeader() throws SQLException {
        Player charlie = player("charlie");
        Map<String, IClanCommand> registry = Map.of("create", new LifecycleCommands.Create(core));

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            stubBukkit(bukkit);

            new LifecycleCommands.Create(core).execute(charlie, new String[]{"create", "NewClan"}, null, "charlie");
            new RequestCommands.Accept(core, registry).execute(charlie, new String[]{"accept"}, null, "charlie");
        }

        Clan newClan = clanList.getClan("NewClan");
        assertNotNull(newClan);
        assertEquals("charlie", newClan.getLeaderName());
        assertTrue(memberList.isMember("charlie"));
        assertEquals("NewClan", memberList.getMember("charlie").getClan());

        // Verify persisted to DB
        List<ClanDto> clansInDb = clanRepo.findAll();
        assertTrue(clansInDb.stream().anyMatch(c -> c.name().equals("NewClan")
                && c.leaderUuid().equals(newClan.getLeaderUuid().toString())));

        String newUuid = newClan.getUuid();
        assertTrue(memberRepo.findAll().stream()
                .anyMatch(m -> m.name().equals("charlie") && m.clanUuid().equals(newUuid)));
    }

    // ── disband → accept ──────────────────────────────────────────────────────

    @Test
    void disbandAccept_removesAllMembersAndClan() throws SQLException {
        Player alice = player("alice");
        Map<String, IClanCommand> registry = Map.of("disband", new LifecycleCommands.Disband(core));

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            stubBukkit(bukkit);

            new LifecycleCommands.Disband(core).execute(alice, new String[]{"disband"}, clan, "alice");
            new RequestCommands.Accept(core, registry).execute(alice, new String[]{"accept"}, clan, "alice");
        }

        assertFalse(memberList.isMember("alice"));
        assertFalse(memberList.isMember("bob"));
        assertNull(clanList.getClan("TestClan"));

        // Verify DB: clan and members deleted
        assertTrue(clanRepo.findAll().isEmpty());
        assertTrue(memberRepo.findAll().stream().noneMatch(m -> m.clanUuid().equals(CLAN_UUID)));
    }
}
