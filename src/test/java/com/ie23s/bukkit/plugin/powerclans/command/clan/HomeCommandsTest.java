package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.MemberList;
import com.ie23s.bukkit.plugin.powerclans.utils.LocationSerializer;
import com.ie23s.bukkit.plugin.powerclans.utils.WorldGuardUtils;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class HomeCommandsTest {

    @Mock Core core;
    @Mock Clan clan;
    @Mock Player sender;
    @Mock MemberList memberList;
    @Mock FileConfiguration config;
    @Mock Location location;

    @BeforeEach
    void setUp() {
        lenient().when(core.lang(anyString())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(core.lang(anyString(), any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(sender.hasPermission(anyString())).thenReturn(true);
        lenient().when(sender.getName()).thenReturn("leader");
        lenient().when(core.getMemberList()).thenReturn(memberList);
        lenient().when(core.getConfig()).thenReturn(config);
        lenient().when(clan.getName()).thenReturn("Warriors");
    }

    // -------------------------------------------------------------------------
    // Home.validate
    // -------------------------------------------------------------------------

    @Test
    void home_validate_failsWhenNoPermission() {
        when(sender.hasPermission("PowerClans.home")).thenReturn(false);

        boolean result = new HomeCommands.Home(core).validate(sender, new String[]{"home"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._1");
    }

    @Test
    void home_validate_failsWhenNotInClan() {
        boolean result = new HomeCommands.Home(core).validate(sender, new String[]{"home"}, null, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._9");
    }

    @Test
    void home_validate_failsWhenNoHome() {
        when(clan.getString(ClanDataKey.HOME)).thenReturn(null);

        boolean result = new HomeCommands.Home(core).validate(sender, new String[]{"home"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._28");
    }

    @Test
    void home_validate_passesWhenHomeExists() {
        when(clan.getString(ClanDataKey.HOME)).thenReturn("world;0;64;0;0;0");

        boolean result = new HomeCommands.Home(core).validate(sender, new String[]{"home"}, clan, "leader");

        assertTrue(result);
    }

    // -------------------------------------------------------------------------
    // Home.execute
    // -------------------------------------------------------------------------

    @Test
    void home_execute_startsWarmup() {
        com.ie23s.bukkit.plugin.powerclans.utils.Utils utils =
                mock(com.ie23s.bukkit.plugin.powerclans.utils.Utils.class);
        com.ie23s.bukkit.plugin.powerclans.utils.Warm warm =
                mock(com.ie23s.bukkit.plugin.powerclans.utils.Warm.class);
        when(core.getUtils()).thenReturn(utils);
        when(utils.getWarm()).thenReturn(warm);

        new HomeCommands.Home(core).execute(sender, new String[]{"home"}, clan, "leader");

        verify(warm).addPlayer(sender, clan);
    }

    // -------------------------------------------------------------------------
    // SetHome.validate
    // -------------------------------------------------------------------------

    @Test
    void sethome_validate_failsWhenNoPermission() {
        when(sender.hasPermission("PowerClans.sethome")).thenReturn(false);

        boolean result = new HomeCommands.SetHome(core).validate(sender, new String[]{"sethome"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._1");
    }

    @Test
    void sethome_validate_failsWhenNotInClan() {
        boolean result = new HomeCommands.SetHome(core).validate(sender, new String[]{"sethome"}, null, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._9");
    }

    @Test
    void sethome_validate_failsWhenNotLeader() {
        when(sender.getName()).thenReturn("member");
        when(clan.hasLeader("member")).thenReturn(false);

        boolean result = new HomeCommands.SetHome(core).validate(sender, new String[]{"sethome"}, clan, "member");

        assertFalse(result);
        verify(sender).sendMessage("errors._30");
    }

    @Test
    void sethome_validate_failsWhenWorldGuardDenies() {
        when(clan.hasLeader("leader")).thenReturn(true);
        when(sender.getLocation()).thenReturn(location);

        try (MockedStatic<WorldGuardUtils> wg = mockStatic(WorldGuardUtils.class)) {
            wg.when(() -> WorldGuardUtils.canBuild(any(), any())).thenReturn(false);

            boolean result = new HomeCommands.SetHome(core).validate(sender, new String[]{"sethome"}, clan, "leader");

            assertFalse(result);
            verify(sender).sendMessage("errors._31");
        }
    }

    @Test
    void sethome_validate_failsWhenNotEnoughMembers() {
        when(clan.hasLeader("leader")).thenReturn(true);
        when(sender.getLocation()).thenReturn(location);
        when(config.getInt("settings.home_min")).thenReturn(5);
        when(memberList.getListOfMembers("Warriors")).thenReturn(List.of());

        try (MockedStatic<WorldGuardUtils> wg = mockStatic(WorldGuardUtils.class)) {
            wg.when(() -> WorldGuardUtils.canBuild(any(), any())).thenReturn(true);

            boolean result = new HomeCommands.SetHome(core).validate(sender, new String[]{"sethome"}, clan, "leader");

            assertFalse(result);
            verify(sender).sendMessage("errors._32");
        }
    }

    @Test
    void sethome_validate_passesForLeaderWithEnoughMembers() {
        when(clan.hasLeader("leader")).thenReturn(true);
        when(sender.getLocation()).thenReturn(location);
        when(config.getInt("settings.home_min")).thenReturn(1);
        when(memberList.getListOfMembers("Warriors")).thenReturn(List.of(mock(com.ie23s.bukkit.plugin.powerclans.clan.Member.class)));

        try (MockedStatic<WorldGuardUtils> wg = mockStatic(WorldGuardUtils.class)) {
            wg.when(() -> WorldGuardUtils.canBuild(any(), any())).thenReturn(true);

            boolean result = new HomeCommands.SetHome(core).validate(sender, new String[]{"sethome"}, clan, "leader");

            assertTrue(result);
        }
    }

    // -------------------------------------------------------------------------
    // SetHome.execute
    // -------------------------------------------------------------------------

    @Test
    void sethome_execute_setsHomeAndBroadcasts() {
        org.bukkit.World world = mock(org.bukkit.World.class);
        when(world.getName()).thenReturn("world");
        when(location.getWorld()).thenReturn(world);
        when(sender.getLocation()).thenReturn(location);

        new HomeCommands.SetHome(core).execute(sender, new String[]{"sethome"}, clan, "leader");

        verify(clan).update(ClanDataKey.HOME, LocationSerializer.serialize(location));
        verify(clan).broadcast(anyString());
    }

    // -------------------------------------------------------------------------
    // RemoveHome.validate
    // -------------------------------------------------------------------------

    @Test
    void removehome_validate_failsWhenNoPermission() {
        when(sender.hasPermission("PowerClans.removehome")).thenReturn(false);

        boolean result = new HomeCommands.RemoveHome(core).validate(sender, new String[]{"removehome"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._1");
    }

    @Test
    void removehome_validate_failsWhenNotInClan() {
        boolean result = new HomeCommands.RemoveHome(core).validate(sender, new String[]{"removehome"}, null, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._9");
    }

    @Test
    void removehome_validate_failsWhenNotLeader() {
        when(sender.getName()).thenReturn("member");
        when(clan.hasLeader("member")).thenReturn(false);

        boolean result = new HomeCommands.RemoveHome(core).validate(sender, new String[]{"removehome"}, clan, "member");

        assertFalse(result);
        verify(sender).sendMessage("errors._29");
    }

    @Test
    void removehome_validate_failsWhenNoHome() {
        when(clan.hasLeader("leader")).thenReturn(true);
        when(clan.getString(ClanDataKey.HOME)).thenReturn(null);

        boolean result = new HomeCommands.RemoveHome(core).validate(sender, new String[]{"removehome"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._28");
    }

    @Test
    void removehome_validate_passesWhenLeaderAndHomeExists() {
        when(clan.hasLeader("leader")).thenReturn(true);
        when(clan.getString(ClanDataKey.HOME)).thenReturn("world;0;64;0;0;0");

        boolean result = new HomeCommands.RemoveHome(core).validate(sender, new String[]{"removehome"}, clan, "leader");

        assertTrue(result);
    }

    // -------------------------------------------------------------------------
    // RemoveHome.execute
    // -------------------------------------------------------------------------

    @Test
    void removehome_execute_removesHomeAndBroadcasts() {
        new HomeCommands.RemoveHome(core).execute(sender, new String[]{"removehome"}, clan, "leader");

        verify(clan).delete(ClanDataKey.HOME);
        verify(clan).broadcast(anyString());
    }
}
