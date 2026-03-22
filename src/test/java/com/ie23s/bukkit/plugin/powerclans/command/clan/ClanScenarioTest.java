package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.IClanCommand;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.ClanList;
import com.ie23s.bukkit.plugin.powerclans.clan.Member;
import com.ie23s.bukkit.plugin.powerclans.clan.MemberList;
import com.ie23s.bukkit.plugin.powerclans.database.InitDB;
import com.ie23s.bukkit.plugin.powerclans.utils.Request;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * End-to-end scenario tests: real MemberList + Clan objects, mocked DB.
 * Each test walks through a full command flow (execute → request → accept)
 * and verifies the resulting in-memory state and DB call.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class ClanScenarioTest {

    @Mock Core core;
    @Mock InitDB db;
    @Mock FileConfiguration config;

    MemberList memberList;
    ClanList clanList;
    Clan clan;

    @BeforeEach
    void setUp() {
        Request.requests.clear();

        memberList = new MemberList();
        when(core.getMemberList()).thenReturn(memberList);
        when(core.getDb()).thenReturn(db);
        lenient().when(core.lang(anyString())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(core.lang(anyString(), (Object[]) any())).thenAnswer(inv -> inv.getArgument(0));

        clanList = new ClanList(core);
        clan = new Clan(core, "TestClan", "TC", "alice", "none", 10, true, 0, 0, 0, 0, 1);
        clanList.getClans().put("testclan", clan);
        when(core.getClanList()).thenReturn(clanList);
        lenient().when(core.getConfig()).thenReturn(config);
        lenient().when(config.getInt("settings.max_symbols")).thenReturn(20);
        lenient().when(config.getInt("settings.min_symbols")).thenReturn(1);
        lenient().when(config.getString("settings.clan_regex")).thenReturn("[a-zA-Z0-9]+");
        lenient().when(config.getInt("settings.create_cost")).thenReturn(0);
        lenient().when(config.getInt("settings.default_max")).thenReturn(10);

        memberList.addMember(new Member("alice", false, "TestClan"));
        memberList.addMember(new Member("bob", false, "TestClan"));
    }

    /** Creates a mock Player with the given name and all permissions granted. */
    private Player player(String name) {
        Player p = mock(Player.class);
        when(p.getName()).thenReturn(name);
        when(p.hasPermission(anyString())).thenReturn(true);
        return p;
    }

    /**
     * Stubs Bukkit.getOfflinePlayer(any) so broadcast() skips all online checks.
     * Each returned mock has getName() equal to the argument and isOnline() = false.
     */
    @SuppressWarnings("deprecation")
    private void stubAllOffline(MockedStatic<Bukkit> bukkit) {
        bukkit.when(() -> Bukkit.getOfflinePlayer(any(String.class))).thenAnswer(inv -> {
            OfflinePlayer op = mock(OfflinePlayer.class);
            when(op.getName()).thenReturn(inv.getArgument(0));
            return op; // isOnline() = false by default
        });
    }

    // -------------------------------------------------------------------------
    // Scenario: leader invites a player → player accepts → player joins the clan
    // -------------------------------------------------------------------------

    @Test
    void inviteAccept_playerJoinsClan() {
        Player alice  = player("alice");
        Player newguy = player("newguy");

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            stubAllOffline(bukkit);
            bukkit.when(() -> Bukkit.getPlayer("newguy")).thenReturn(newguy);

            // alice sends the invite
            new MemberCommands.Invite(core).execute(alice, new String[]{"invite", "newguy"}, clan, "alice");

            // newguy accepts
            new RequestCommands.Accept(core, Map.of()).execute(newguy, new String[]{"accept"}, null, "newguy");
        }

        assertTrue(memberList.isMember("newguy"), "newguy should be in memberList");
        assertEquals("TestClan", memberList.getMember("newguy").getClan());
        verify(db).createClanMember(argThat(m -> m.getName().equals("newguy") && m.getClan().equals("TestClan")));
    }

    // -------------------------------------------------------------------------
    // Scenario: leader kicks a member → member is removed from the clan
    // -------------------------------------------------------------------------

    @Test
    void kick_removesPlayerFromClan() {
        Player alice = player("alice");
        Player bob   = player("bob");

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            stubAllOffline(bukkit);
            bukkit.when(() -> Bukkit.getPlayer("bob")).thenReturn(bob);

            new MemberCommands.Kick(core).execute(alice, new String[]{"kick", "bob"}, clan, "alice");
        }

        assertFalse(memberList.isMember("bob"), "bob should be removed from memberList");
        verify(db).kick(argThat(m -> m.getName().equals("bob")));
    }

    // -------------------------------------------------------------------------
    // Scenario: member runs /clan leave → accepts → removed from the clan
    // -------------------------------------------------------------------------

    @Test
    void leaveAccept_playerLeavesClan() {
        Player bob = player("bob");
        Map<String, IClanCommand> registry = Map.of("leave", new LifecycleCommands.Leave(core));

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            stubAllOffline(bukkit);

            new LifecycleCommands.Leave(core).execute(bob, new String[]{"leave"}, clan, "bob");
            new RequestCommands.Accept(core, registry).execute(bob, new String[]{"accept"}, clan, "bob");
        }

        assertFalse(memberList.isMember("bob"), "bob should be removed after accepting leave");
        verify(db).kick(argThat(m -> m.getName().equals("bob")));
    }

    // -------------------------------------------------------------------------
    // Scenario: leader promotes a member to moderator
    // -------------------------------------------------------------------------

    @Test
    void addmoder_promotesPlayerToModerator() {
        Player alice = player("alice");

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            stubAllOffline(bukkit);
            // addmoder execute calls getOfflinePlayer(bob).getName() for the broadcast message
            OfflinePlayer offlineBob = mock(OfflinePlayer.class);
            when(offlineBob.getName()).thenReturn("bob");
            bukkit.when(() -> Bukkit.getOfflinePlayer("bob")).thenReturn(offlineBob);

            new ModeratorCommands.AddModer(core).execute(alice, new String[]{"addmoder", "bob"}, clan, "alice");
        }

        assertTrue(clan.hasModer("bob"), "bob should be a moderator after addmoder");
        verify(db).setModer(argThat(m -> m.getName().equals("bob") && m.isModer()));
    }

    // -------------------------------------------------------------------------
    // Scenario: leader transfers leadership → accepts → leadership changes
    // -------------------------------------------------------------------------

    @Test
    void leaderTransferAccept_changesLeader() {
        Player alice = player("alice");
        Map<String, IClanCommand> registry = Map.of("leader", new ModeratorCommands.Leader(core));

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            stubAllOffline(bukkit);
            // Leader.validate() checks hasPlayedBefore() for the target
            OfflinePlayer offlineBob = mock(OfflinePlayer.class);
            when(offlineBob.hasPlayedBefore()).thenReturn(true);
            bukkit.when(() -> Bukkit.getOfflinePlayer("bob")).thenReturn(offlineBob);

            new ModeratorCommands.Leader(core).execute(alice, new String[]{"leader", "bob"}, clan, "alice");
            new RequestCommands.Accept(core, registry).execute(alice, new String[]{"accept"}, clan, "alice");
        }

        assertEquals("bob", clan.getLeader());
        assertFalse(clan.hasLeader("alice"), "alice should no longer be the leader");
        verify(db).setLeader(argThat(m -> m.getName().equals("bob")));
    }

    // -------------------------------------------------------------------------
    // Scenario: player creates a new clan → accepts → clan exists and player is leader
    // -------------------------------------------------------------------------

    @Test
    void createAccept_clanIsCreatedAndPlayerIsLeader() {
        Player charlie = player("charlie");
        Map<String, IClanCommand> registry = Map.of("create", new LifecycleCommands.Create(core));

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            stubAllOffline(bukkit);

            new LifecycleCommands.Create(core).execute(charlie, new String[]{"create", "NewClan"}, null, "charlie");
            new RequestCommands.Accept(core, registry).execute(charlie, new String[]{"accept"}, null, "charlie");
        }

        Clan newClan = clanList.getClan("NewClan");
        assertNotNull(newClan, "NewClan should exist in ClanList");
        assertEquals("charlie", newClan.getLeader());
        assertTrue(memberList.isMember("charlie"), "charlie should be in memberList");
        assertEquals("NewClan", memberList.getMember("charlie").getClan());
        verify(db).createClan(argThat(c -> c.getName().equals("NewClan")));
        verify(db).createClanMember(argThat(m -> m.getName().equals("charlie") && m.getClan().equals("NewClan")));
    }

    // -------------------------------------------------------------------------
    // Scenario: leader disbands the clan → all members removed, clan deleted
    // -------------------------------------------------------------------------

    @Test
    void disbandAccept_removesAllMembersAndClan() {
        Player alice = player("alice");
        Map<String, IClanCommand> registry = Map.of("disband", new LifecycleCommands.Disband(core));

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            stubAllOffline(bukkit);

            new LifecycleCommands.Disband(core).execute(alice, new String[]{"disband"}, clan, "alice");
            new RequestCommands.Accept(core, registry).execute(alice, new String[]{"accept"}, clan, "alice");
        }

        assertFalse(memberList.isMember("alice"), "alice should be removed after disband");
        assertFalse(memberList.isMember("bob"),   "bob should be removed after disband");
        assertNull(clanList.getClan("TestClan"),   "TestClan should no longer exist");
        verify(db, times(2)).kick(any(Member.class));
        verify(db).disband("TestClan");
    }
}
